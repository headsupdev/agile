/*
 * HeadsUp Agile
 * Copyright 2009-2012 Heads Up Development Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.headsupdev.agile.app.ci.builders;

import org.headsupdev.agile.app.ci.CIBuilder;
import org.headsupdev.support.java.ExecUtil;
import org.headsupdev.support.java.FileUtil;
import org.headsupdev.support.java.IOUtil;
import org.headsupdev.support.java.StringUtil;
import org.headsupdev.agile.api.*;
import org.headsupdev.agile.api.logging.Logger;
import org.headsupdev.agile.storage.ci.Build;
import org.headsupdev.agile.app.ci.CIApplication;
import org.headsupdev.agile.storage.ci.TestResultSet;
import org.headsupdev.agile.storage.ci.TestResult;
import org.headsupdev.agile.storage.HibernateStorage;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.shared.invoker.*;
import org.jdom.input.SAXBuilder;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;

/**
 * The main code for building a Maven2 project. Parses JUnit test output also.
 *
 * @author Andrew Williams
 * @since 1.0
 */
public class MavenTwoBuildHandler
        implements BuildHandler
{
    public static final Pattern LINT_URL_SEPERATOR_PATTERN = Pattern.compile( "/" );
    public static final Pattern LINT_LINE_WITH_URL_PATTERN = Pattern.compile( "(.*)<a href=\"file:([^ ']*)build-(\\d*)/([^ ']*)\">([^ ']*)</a>(.*)" );
    public static final Pattern LINT_LINE_WITH_NUMBERED_URL_PATTERN = Pattern.compile( "(.*)<a href=\"file:([^ ']*)build-(\\d*)/([^ ']*)\">([^ ']*)</a>:(\\d*)(.*)" );
    public static final Pattern LINT_ERROR_WARNING_COUNTS_PATTERN = Pattern.compile( "<br/>(\\d*) errors and (\\d*) warnings found:<br/>" );

    private static Logger log = Manager.getLogger( MavenTwoBuildHandler.class.getName() );

    public boolean isReadyToBuild( Project project, CIBuilder builder )
    {
        if ( !( project instanceof MavenTwoProject ) )
        {
            return true;
        }

        if ( project.getParent() != null )
        {
            if ( isBuildWaitingFor( project.getParent(), builder, "parent project", project.getId() ) )
            {
                return false;
            }
        }

        for ( MavenDependency dependency : ( (MavenTwoProject) project ).getDependencies() )
        {
            Project projectForDep = dependency.getProject();

            if ( isBuildWaitingFor( projectForDep, builder, "dependency on", project.getId() ) )
            {
                return false;
            }
        }

        return true;
    }

    protected boolean isBuildWaitingFor( Project project, CIBuilder builder, String type, String name )
    {
        if ( builder.isProjectQueued( project ) )
        {
            log.info( "Deferring build of " + name + " due to " + type + " " + project.getId() );
            return true;
        }

        return false;
    }

    public void runBuild( Project project, PropertyTree config, PropertyTree appConfig, File dir, File logFile,
                          Build build )
    {
        if ( !( project instanceof MavenTwoProject ) )
        {
            return;
        }

        FileOutputHandler buildOut = null;
        Storage storage = Manager.getStorageInstance();

        InvocationResult result = null;
        try
        {
            buildOut = new FileOutputHandler( logFile );

            InvocationRequest request = new DefaultInvocationRequest();
            request.setBaseDirectory( dir );

            String goals = config.getProperty( CIApplication.CONFIGURATION_MAVEN_GOALS.getKey() );
            if ( goals == null )
            {
                goals = (String) CIApplication.CONFIGURATION_MAVEN_GOALS.getDefault();
            }
            request.setGoals( Arrays.asList( goals.split( " " ) ) );

            String profiles = config.getProperty( CIApplication.CONFIGURATION_MAVEN_PROFILES.getKey() );
            if ( profiles == null )
            {
                profiles = (String) CIApplication.CONFIGURATION_MAVEN_PROFILES.getDefault();
            }
            if ( profiles != null && profiles.length() > 0 )
            {
                request.setProfiles( Arrays.asList( profiles.split( " " ) ) );
            }

            request.setPomFile( new File( dir, "pom.xml" ) );
            request.setOutputHandler( buildOut );
            request.setRecursive( false );

            request.setMavenOpts( "-Xmx256m" );

            Invoker invoker = new DefaultInvoker();

            String mavenHome = lookupBuildExecutable( CIApplication.CONFIGURATION_MAVEN_HOME, config, appConfig,
                    build, logFile );
            if ( mavenHome == null )
            {
                return;
            }

            invoker.setMavenHome( new File( mavenHome ) );
            invoker.setWorkingDirectory( dir );
            result = invoker.execute( request );
        }
        catch ( IOException e )
        {
            log.error( "Unable to write to build output file", e );
        }
        catch ( MavenInvocationException e )
        {
            e.printStackTrace( new PrintWriter( buildOut.writer ) );
            log.error( "Error running maven, was reported in build results", e );
        }

        File testReportDir = new File( new File( dir, "target" ), "surefire-reports" );
        parseTests( project, testReportDir, build, storage );

        if ( result == null || result.getExitCode() != 0 )
        {
            build.setStatus( Build.BUILD_FAILED );
            onBuildFailed( project, config, appConfig, dir, logFile, build );
        }
        else
        {
            build.setStatus( Build.BUILD_SUCCEEDED );
            onBuildPassed( project, config, appConfig, dir, logFile, build );
        }

        build.setEndTime( new Date() );
        if ( buildOut != null )
        {
            buildOut.close();
        }
    }

    protected static void parseTests( Project project, File reportDir, Build build, Storage storage )
    {
        File projectDir = CIApplication.getProjectDir( project );
        File testdir = new File( projectDir, "tests-" + build.getId() );
        testdir.mkdirs();

        int tests = 0, failures = 0, errors = 0;
        if ( reportDir != null && reportDir.exists() )
        {
            FilenameFilter reportFilter = new FilenameFilter()
            {
                public boolean accept( File dir, String name )
                {
                    return name.startsWith( "TEST-" ) && name.endsWith( ".xml" );
                }
            };
            SAXBuilder builder = new SAXBuilder();
            for ( File test : reportDir.listFiles( reportFilter ) )
            {
                String testSuiteName = test.getName().substring( 5, test.getName().length() - 4 );
                String suiteOutName = testSuiteName + ".txt";

                File suiteLog = new File( reportDir, suiteOutName );
                File cachedLog = new File( testdir, suiteOutName );
                if ( suiteLog.exists() )
                {
                    suiteLog.renameTo( cachedLog );
                }
                else
                {
                    suiteOutName = "TEST-" + suiteOutName;
                    suiteLog = new File( reportDir, suiteOutName );

                    if ( suiteLog.exists() )
                    {
                        suiteLog.renameTo( cachedLog );
                    }
                }
                TestResultSet set = new TestResultSet( testSuiteName, cachedLog.getAbsolutePath() );

                int setTests = 0, setFailures = 0, setErrors = 0;
                long time = 0;
                try
                {
                    Document doc = builder.build( test ).getDocument();
                    Element root = doc.getRootElement();

                    tests += root.getAttribute( "tests" ).getIntValue();
                    failures += root.getAttribute( "failures" ).getIntValue();
                    errors += root.getAttribute( "errors" ).getIntValue();
                    time = (long) ( root.getAttribute( "time" ).getFloatValue() * 1000 );

                    List<Element> testCases = root.getChildren( "testcase" );
                    for ( Element testCase : testCases )
                    {
                        String testName = testCase.getAttributeValue( "name" );
                        int status = TestResult.STATUS_PASSED;
                        String message = "";
                        String output = "";
                        if ( testCase.getChild( "failure" ) != null )
                        {
                            setFailures++;
                            status = TestResult.STATUS_FAILED;
                            message = testCase.getChild( "failure" ).getAttributeValue( "message" );
                            output = testCase.getChildText( "failure" );
                        }
                        else if ( testCase.getChild( "error" ) != null )
                        {
                            setErrors++;
                            status = TestResult.STATUS_ERROR;
                            message = testCase.getChild( "error" ).getAttributeValue( "type" );
                            output = testCase.getChildText( "error" );
                        }
                        setTests++;
                        long testTime = (long) ( testCase.getAttribute( "time" ).getFloatValue() * 1000 );

                        TestResult testResult = new TestResult( testName, status, testTime, message, output );
                        ( (HibernateStorage) storage ).save( testResult );
                        set.getResults().add( testResult );
                    }
                }
                catch ( JDOMException e )
                {
                    log.error( "Failed to parse surefire report", e );
                }
                catch ( IOException e )
                {
                    log.error( "Failed to load surefire report", e );
                }

                set.setTests( setTests );
                set.setFailures( setFailures );
                set.setErrors( setErrors );
                set.setDuration( time );
                ( (HibernateStorage) storage ).save( set );

                build.getTestResults().add( set );
            }
        }

        build.setTests( tests );
        build.setFailures( failures );
        build.setErrors( errors );
    }


    public static void logError( String error, File output )
    {
        logError( error, null, output );
    }

    public static void logError( String error, Exception e, File output )
    {
        Logger log = Manager.getLogger( MavenTwoBuildHandler.class.getName() );
        log.error( error, e );
        try
        {
            FileUtil.writeToFile( error, output );
        }
        catch ( IOException e2 )
        {
            log.error( "Error whilst writing build output", e2 );
        }
    }

    public static String lookupValueChain( ConfigurationItem item, PropertyTree projectConfig, PropertyTree appConfig )
    {
        String value = projectConfig.getProperty( item.getKey() );
        if ( value == null )
        {
            value = appConfig.getProperty( item.getKey() );
        }
        if ( value == null )
        {
            value = (String) item.getDefault();
        }

        return value;
    }

    public static String lookupBuildExecutable( ConfigurationItem item, PropertyTree projectConfig,
                                                PropertyTree appConfig, Build build, File logFile )
    {
        String exe = lookupValueChain( item, projectConfig, appConfig );
        if ( StringUtil.isEmpty( exe ) || !new File( exe ).exists() )
        {
            build.setEndTime( new Date() );
            build.setStatus( Build.BUILD_FAILED );

            logError( "Invalid or missing " + item.getKey() + " configuration", logFile );
            return null;
        }

        return exe;
    }

    public static boolean canFindLint()
    {
        // try and find a binary called lint.
        File lint = FileUtil.lookupInPath( "lint" );

        return lint != null;
    }

    public void onBuildPassed( Project project, PropertyTree config, PropertyTree appConfig, File dir, File output,
                               Build build )
    {
        boolean analyze = Boolean.parseBoolean( config.getProperty( CIApplication.CONFIGURATION_ANALYZE.getKey(),
                String.valueOf( CIApplication.CONFIGURATION_ANALYZE.getDefault() ) ) );

        // if analyzing is desired
        if ( analyze )
        {
            Writer buildOut = null;
            try
            {
                buildOut = new FileWriter( output, true );
                if ( !canFindLint() )
                {
                    build.setWarnings( build.getWarnings() + 1 );

                    buildOut.write( "lint not found, something wrong with your setup?" );
                    return;
                }
                else
                {
                    RunLint( project, dir, build, buildOut );
                }
            }
            catch ( IOException e )
            {
                e.printStackTrace( new PrintWriter( buildOut ) );
                log.error( "Unable to write to build output file - reported in build log", e );
            }
            finally
            {
                IOUtil.close( buildOut );
            }

            try
            {
                File analyzeDir = new File( new File( new File( new File( new File( Manager.getStorageInstance().getDataDirectory(), "repository" ), "site" ), project.getId() ), "analyze" ), "" + build.getId() );
                log.debug( "analyzeDir:" + analyzeDir.getPath() );

                File dirList[] = analyzeDir.listFiles();
                for ( int index = 0; index < dirList.length; index++ )
                {
                    File htmlFile = dirList[index];
                    File htmlTmpFile = new File( htmlFile.getPath() + ".tmp" );
                    log.debug( "dir index:" + index + ", htmlFile: " + htmlFile );

                    if ( htmlFile.isFile() )
                    {
                        BufferedReader input = new BufferedReader( new FileReader( htmlFile ) );
                        Writer tmpOutput = null;
                        try
                        {
                            tmpOutput = new FileWriter( htmlTmpFile, true );

                            String line = null;
                            while ( ( line = input.readLine() ) != null )
                            {
                                matchErrorWarningCounts( build, line );

                                matchAndProcessUrlsInLine( project, dir, tmpOutput, line );
                            }
                            tmpOutput.close();

                            boolean success = htmlTmpFile.renameTo( htmlFile );
                            if ( !success )
                            {
                                log.error( "failed to rename:" + htmlTmpFile.getPath() + "To:" + htmlFile.getPath() );
                            }
                        }
                        finally
                        {
                            input.close();
                        }
                    }
                }
            }
            catch ( IOException ex )
            {
                ex.printStackTrace();
            }
        }
    }

    private void matchAndProcessUrlsInLine( Project project, File dir, Writer tmpOutput, String line )
            throws IOException
    {
        Matcher mNumberedUrl = LINT_LINE_WITH_NUMBERED_URL_PATTERN.matcher( line );
        Matcher mUrl = LINT_LINE_WITH_URL_PATTERN.matcher( line );
        if ( mNumberedUrl.find() )
        {
            String newLine = updatePathInLineWithLineNumbers( project, line, mNumberedUrl );
            tmpOutput.write( newLine + "\n" );
        }
        else if ( mUrl.find() )
        {
            String newLine = updatePathInLine( project, dir, line, mUrl );
            tmpOutput.write( newLine );
        }
        else
        {
            tmpOutput.write( line + "\n" );
        }
    }

    private void matchErrorWarningCounts( Build build, String line )
    {
        Matcher mBugs = LINT_ERROR_WARNING_COUNTS_PATTERN.matcher( line );
        if ( mBugs.find() )
        {
            updateErrorWarningCounts( build, mBugs );
        }
    }

    private void updateErrorWarningCounts( Build build, Matcher mBugs )
    {
        String bugCountErrors = mBugs.group( 1 );
        String bugCountWarnings = mBugs.group( 2 );

        int warnings = build.getWarnings() + Integer.parseInt( bugCountErrors ) + Integer.parseInt( bugCountWarnings );
        build.setWarnings( warnings );
    }

    private String updatePathInLine( Project project, File dir, String line, Matcher mUrl )
    {
        log.debug( "line: " + line );
        String lineStart = mUrl.group( 1 );
        String linkedPath = mUrl.group( 4 );
        File linkedFile = new File( dir.getPath() + "/" + linkedPath );
        String viewExtra = "";
        if ( linkedFile.isFile() )
        {
            viewExtra = "/view";
        }
        String colonPath = replaceUrlSeperatorsWithColons( linkedPath );
        String filePath = mUrl.group( 5 );
        String lineEnd = mUrl.group( 6 );
        String newLine = lineStart
                + "<a href=\"" + "/" + project.getId()
                + "/files" + viewExtra + "/path/" + colonPath
                + "\">" + filePath + "</a>" + lineEnd;
        log.debug( "newline:" + newLine );
        return newLine;
    }

    private String updatePathInLineWithLineNumbers( Project project, String line, Matcher mNumberedUrl )
    {
        log.debug( "line: " + line );
        String lineStart = mNumberedUrl.group( 1 );
        String colonPath = replaceUrlSeperatorsWithColons( mNumberedUrl.group( 4 ) );
        String lineNumber = mNumberedUrl.group( 6 );
        String filePath = mNumberedUrl.group( 5 );
        String lineEnd = mNumberedUrl.group( 7 );
        String newLine = lineStart
                + "<a href=\"" + "/" + project.getId()
                + "/files/view/path/" + colonPath
                + "#" + lineNumber + "\">"
                + filePath + "</a>"
                + ":" + lineNumber
                + lineEnd;
        log.debug( "newline: " + newLine );
        return newLine;
    }

    private void RunLint( Project project, File dir, Build build, Writer buildOut )
    {
        log.debug( "Running Lint" );

        ArrayList<String> commands = new ArrayList<String>();
        commands.add( "lint" );
        commands.add( dir.getPath() );
        commands.add( "--html" );
        File outputDir = new File( new File( new File( new File( new File( Manager.getStorageInstance().getDataDirectory(), "repository" ), "site" ), project.getId() ), "analyze" ), "" + build.getId() );
        String outputPath = outputDir.getPath();
        commands.add( outputPath );

        if ( !outputDir.exists() )
        {
            if ( !outputDir.mkdirs() )
            {
                log.error( "Unable to mkdirs:" + outputDir.getPath() );
            }
        }

        log.debug( "Running Lint:" + dir.getPath() + " withOutput:" + outputPath );
        ExecUtil.executeLoggingExceptions( commands, dir, buildOut, buildOut );
    }

    private String replaceUrlSeperatorsWithColons( String colonedPath )
    {
        Matcher mUrlSeperators = LINT_URL_SEPERATOR_PATTERN.matcher( colonedPath );
        if ( mUrlSeperators.find() )
        {
            colonedPath = mUrlSeperators.replaceAll( ":" );
        }
        return colonedPath;
    }

    public void onBuildFailed( Project project, PropertyTree config, PropertyTree appConfig, File dir, File output,
                               Build build )
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
