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
import org.headsupdev.agile.storage.HibernateStorage;
import org.headsupdev.agile.storage.ci.TestResult;
import org.headsupdev.agile.storage.ci.TestResultSet;
import org.headsupdev.support.java.ExecUtil;
import org.headsupdev.support.java.FileUtil;
import org.headsupdev.support.java.IOUtil;
import org.headsupdev.agile.api.*;
import org.headsupdev.agile.api.logging.Logger;
import org.headsupdev.agile.app.ci.CIApplication;
import org.headsupdev.agile.storage.ci.Build;
import org.headsupdev.support.java.StringUtil;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The main code for building an xcode project.
 *
 * @author Andrew Williams
 * @since 1.0
 */
public class XCodeBuildHandler
    implements BuildHandler
{
    public static final String ITEM_PREFIX = "▸ ";
    public static final Pattern BUILD_LOG_BUGS_COUNT_PATTERN = Pattern.compile( "scan-build: ([0-9]*) bugs found." );
    public static final Pattern BUILD_LOG_OUTPUT_DIR_PATTERN = Pattern.compile( "scan-build: Run 'scan-view ([^']*)' to examine bug reports." );

    private static Logger log = Manager.getLogger( XCodeBuildHandler.class.getName() );

    private File cachedBuildDirectory;

    @Override
    public boolean isReadyToBuild( Project project, CIBuilder builder )
    {
        return true;
    }

    public void runBuild( Project project, PropertyTree config, PropertyTree appConfig, File dir, File output,
                                    Build build )
    {
        if ( !( project instanceof XCodeProject ) )
        {
            return;
        }

        int result = -1;

        Writer buildOut = null;
        try
        {
            buildOut = new FileWriter( output );
            if ( !prepareProject( project, dir, buildOut ) )
            {
                IOUtil.close( buildOut );

                build.setStatus( Build.BUILD_FAILED );
                build.setEndTime( new Date() );
                return;
            }

            // execute a clean first of all
            ArrayList<String> commands = new ArrayList<String>();
            try
            {
                appendXcodeCommands( project, config, commands, dir, null );
            }
            catch ( FileNotFoundException e )
            {
                buildOut.write( "Could not build as Xcode workspace not found - perhaps you need to install CocoaPods?" );
                IOUtil.close( buildOut );

                build.setStatus( Build.BUILD_FAILED );
                build.setEndTime( new Date() );
                return;
            }
            commands.add( "clean" );
            commands.add( "build" );

            boolean runTests = "true".equals( config.getProperty( CIApplication.CONFIGURATION_XCODE_RUN_TESTS.getKey(),
                    CIApplication.CONFIGURATION_XCODE_RUN_TESTS.getDefault().toString() ) );
            if ( runTests )
            {
                commands.add( "test" );
                appendTestDestinationCommand( config, commands );
            }

            result = ExecUtil.executeLoggingExceptions( commands, dir, buildOut, buildOut );
        }
        catch ( IOException e )
        {
            e.printStackTrace( new PrintWriter( buildOut ) );
            log.error( "Unable to write to build output file - reported in build log", e );
        }
        IOUtil.close( buildOut );

        parseTestResults( output, build );
        tidyOutput( output, dir );

        build.setEndTime( new Date() );
        if ( result != 0 )
        {
            build.setStatus( Build.BUILD_FAILED );
            onBuildFailed( project, config, appConfig, dir, output, build );
        }
        else
        {
            build.setStatus( Build.BUILD_SUCCEEDED );
            onBuildPassed(project, config, appConfig, dir, output, build);
        }

        cleanupBuildOutput( build, dir );
    }

    protected void cleanupBuildOutput( Build build, File checkoutDir )
    {
        try
        {
            File buildDir = getBuildDirectory( build, checkoutDir );
            if ( buildDir != null )
            {
                FileUtil.delete( buildDir );
            }
        }
        catch ( IOException e )
        {
            log.error( "Error cleaning up build output", e );
        }
    }

    private boolean prepareProject( Project project, File dir, Writer buildOut )
    {
        if ( usesCocoaPods( dir ) )
        {
            return prepareCocoaPods( dir, buildOut );
        }

        return true;
    }

    private boolean prepareCocoaPods( File dir, Writer buildOut )
    {
        log.debug( "running pod install to set up workspace" );

        // execute a clean first of all
        ArrayList<String> commands = new ArrayList<String>();
        commands.add( "pod" );
        commands.add( "repo" );
        commands.add( "update" );
        commands.add( "--no-color" );
        int ret = ExecUtil.executeLoggingExceptions( commands, dir, buildOut, buildOut );
        if ( ret != 0 )
        {
            return false;
        }

        commands.clear();
        commands.add( "pod" );
        commands.add( "install" );
        commands.add( "--no-color" );
        ret = ExecUtil.executeLoggingExceptions( commands, dir, buildOut, buildOut );
        return ret == 0;
    }

    public static boolean canFindScanBuild()
    {
        // try and find a binary called scan-build.
        File scanBuild = FileUtil.lookupInPath( "scan-build" );

        return scanBuild != null;
    }

    protected static void appendXcodeCommands( Project project, PropertyTree config, ArrayList<String> commands,
                                               File dir, String overrideConfig )
            throws FileNotFoundException
    {
        boolean buildWorkspace = config.getProperty( CIApplication.CONFIGURATION_XCODE_BUILD_WORKSPACE.getKey(),
                (String) CIApplication.CONFIGURATION_XCODE_SDK.getDefault() ).equals( "true" );

        String sdkName = config.getProperty( CIApplication.CONFIGURATION_XCODE_SDK.getKey(),
                (String) CIApplication.CONFIGURATION_XCODE_SDK.getDefault() );

        commands.add( "xcodebuild" );

        if ( buildWorkspace )
        {
            setupWorkspaceCommand( project, config, commands, dir );
        }
        else
        {
            setupProjectCommand( config, commands, overrideConfig );
        }

        if ( overrideConfig != null )
        {
            commands.add( "-configuration" );
            commands.add( overrideConfig );
        }
        else
        {
            String confName = config.getProperty( CIApplication.CONFIGURATION_XCODE_CONFIG.getKey(),
                    (String) CIApplication.CONFIGURATION_XCODE_CONFIG.getDefault() );

            // build the specified configuration, or default if none specified
            if ( !StringUtil.isEmpty( confName ) )
            {
                commands.add( "-configuration" );
                commands.add( confName );
            }
        }

        // link to the specified sdk if specified
        if ( !StringUtil.isEmpty( sdkName ) )
        {
            commands.add( "-sdk" );
            commands.add( sdkName );
        }

        commands.add( "ONLY_ACTIVE_ARCH=NO" );
    }

    protected void appendTestDestinationCommand( PropertyTree config, ArrayList<String> commands )
    {
        String sdkName = config.getProperty( CIApplication.CONFIGURATION_XCODE_SDK.getKey(),
                (String) CIApplication.CONFIGURATION_XCODE_SDK.getDefault() );
        String simulator = "iphonesimulator";
        int versionIndex = simulator.length();

        if ( !StringUtil.isEmpty( sdkName ) && sdkName.startsWith( simulator ) && sdkName.length() > versionIndex )
        {
            String sdkSimulatorVersion = sdkName.substring( versionIndex );

            if ( sdkSimulatorVersion != null )
            {
                String simulatorDestination = String.format( "'platform=iOS Simulator,name=iPhone Retina (4-inch),OS=%s'", sdkSimulatorVersion );

                commands.add( "-destination" );
                commands.add( simulatorDestination );
            }
        }
    }

    private static String getDefaultWorkspace( Project project, File dir )
    {
        for ( File child : dir.listFiles() )
        {
            if ( !child.isDirectory() )
            {
                continue;
            }

            if ( child.getName().toLowerCase().endsWith( ".xcworkspace" ) )
            {
                return child.getName().substring( 0, child.getName().length() - 12 );
            }
        }

        return null;
    }

    private static void setupWorkspaceCommand( Project project, PropertyTree config, ArrayList<String> commands, File dir )
            throws FileNotFoundException
    {
        String workspace = config.getProperty( CIApplication.CONFIGURATION_XCODE_WORKSPACE.getKey(),
                (String) CIApplication.CONFIGURATION_XCODE_TARGET.getDefault() );
        String scheme = config.getProperty( CIApplication.CONFIGURATION_XCODE_SCHEME.getKey(),
                (String) CIApplication.CONFIGURATION_XCODE_TARGET.getDefault() );

        String defaultWorkspace = getDefaultWorkspace( project, dir );

        // build the specified workspace if specified, otherwise a default
        commands.add( "-workspace" );
        if ( !StringUtil.isEmpty( workspace ) )
        {
            commands.add( workspace + ".xcworkspace" );
            // if we specify a workspace fall back to this rather than the default if no scheme specified
            defaultWorkspace = workspace;
        }
        else
        {
            if ( defaultWorkspace == null )
            {
                throw new FileNotFoundException( "No workspace found for workspace build" );
            }
            commands.add( defaultWorkspace + ".xcworkspace" );
        }

        // build the specified workspace scheme if specified, otherwise a default
        commands.add( "-scheme" );
        if ( !StringUtil.isEmpty( scheme ) )
        {
            commands.add( scheme );
        }
        else
        {
            commands.add( defaultWorkspace );
        }
    }

    private static void setupProjectCommand( PropertyTree config, ArrayList<String> commands, String overrideConfig )
    {
        String targetName = config.getProperty( CIApplication.CONFIGURATION_XCODE_TARGET.getKey(),
                (String) CIApplication.CONFIGURATION_XCODE_TARGET.getDefault() );

        // build the specified target if specified
        if ( !StringUtil.isEmpty( targetName ) )
        {
            commands.add( "-target" );
            commands.add( targetName );
        }
    }

    private static void RenameBuildFromFileToFile( String oldBuildName, String newBuildName, File oldFile, File newFile )
            throws IOException
    {
        BufferedReader oldFileReader = new BufferedReader( new FileReader( oldFile ) );
        FileWriter newFileWriter = new FileWriter( newFile );

        File parentDir = newFile.getParentFile();
        if (parentDir.exists() == false)
        {
            if (parentDir.mkdirs() == false) {
                log.error( "Unable to mkdirs:" + parentDir.getPath() );
            }
        }

        try
        {
            renameBuildFromReaderToWriter( oldBuildName, newBuildName, oldFileReader, newFileWriter );
        }
        finally
        {
            oldFileReader.close();
            newFileWriter.close();
        }
    }

    private static void renameBuildFromReaderToWriter( String oldBuildName, String newBuildName, BufferedReader oldFileReader, FileWriter newFileWriter )
            throws IOException
    {
        String oldLine = null;
        while ( ( oldLine = oldFileReader.readLine() ) != null )
        {
            String newLine = oldLine.replaceAll( oldBuildName, newBuildName );
            newFileWriter.write( newLine );
        }
    }

    protected static void parseTestResults( File output, Build build )
    {
        try
        {
            tryParsingTestResults( output, build );
        }
        catch ( IOException e )
        {
            System.err.println( "Failure in parsing build output for test results: " + " (" + e.getMessage() + ")" );
        }
    }

    protected static void tryParsingTestResults( File dat, Build build )
            throws IOException
    {
        BufferedReader reader = new BufferedReader( new FileReader( dat ) );
        String rootPath = Manager.getStorageInstance().getDataDirectory().getAbsolutePath();
        HibernateStorage storage = (HibernateStorage) Manager.getStorageInstance();

        String line;
        long totalMillis = 0;
        TestResultSet currentSuite = null;
        int setTests = 0, failedTests = 0;
        int totalTests = 0, totalFailed = 0;
        while ( ( line = reader.readLine() ) != null )
        {
            if ( line.length() <= 1 )
            {
                continue;
            }
            if ( ( line.startsWith( "oTest Suite" ) || line.startsWith( "Test Suite" ) ) &&
                    !line.contains( rootPath ) )
            {
                int pos1 = line.indexOf( "\'" );
                int pos2 = line.indexOf( "\'", pos1 + 1 );
                String suiteName = line.substring( pos1 + 1, pos2 );

                if ( line.contains( " started at " ) )
                {

                    currentSuite = new TestResultSet( suiteName, null );
                }
                else if ( line.contains( " finished at " ) )
                {
                    if ( currentSuite == null )
                    {
                        System.err.println( "No Test Suite to close for '" + suiteName + "'" );
                        continue;
                    }

                    currentSuite.setTests( setTests );
                    currentSuite.setFailures( failedTests );
                    currentSuite.setDuration( totalMillis );

                    storage.save( currentSuite );
                    build.getTestResults().add( currentSuite );
                    build.setTests( totalTests );
                    build.setFailures( totalFailed );

                    // reset for next test
                    currentSuite = null;
                    setTests = failedTests = 0;
                    totalMillis = 0;
                }
            }
            else if ( ( line.startsWith( "oTest Case" ) || line.startsWith( "Test Case" ) ) &&
                    !line.endsWith( "started." ) )
            {
                int pos1 = line.indexOf( "\'" );
                int pos2 = line.indexOf( "\'", pos1 + 1 );
                String testName = line.substring( pos1 + 1, pos2 );

                if ( currentSuite == null )
                {
                    System.err.println( "No Test Suite for test '" + testName + "'" );
                    continue;
                }

                pos1 = line.indexOf( "(", pos2 );
                pos2 = line.indexOf( " ", pos1 + 1 );
                String testTime = line.substring( pos1 + 1, pos2 );
                long millis = (long) ( Double.parseDouble( testTime ) * 1000 );
                totalMillis += millis;

                setTests++;
                totalTests++;
                int status;// = TestResult.STATUS_ERROR;
                if ( line.contains( " passed (" ) )
                {
                    status = TestResult.STATUS_PASSED;
                }
                else
                {
                    status = TestResult.STATUS_FAILED;
                    failedTests++;
                    totalFailed++;
                }

                TestResult testResult = new TestResult( testName, status, millis, "", null );
                storage.save( testResult );
                currentSuite.getResults().add( testResult );
            }
            else if ( line.startsWith( "oExecuted" ) || line.startsWith( "Executed" ) )
            {
                // don't think we need this
            }
        }

        IOUtil.close( reader );
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
                if ( !canFindScanBuild() )
                {
                    build.setWarnings( build.getWarnings() + 1 );

                    buildOut.write( "scan-build not found, please read http://clang-analyzer.llvm.org/installation" );
                    return;
                }
                else
                {
                    ArrayList<String> commands = new ArrayList<String>();
                    commands.add( "scan-build" );
                    commands.add( "-o" );
                    File siteRepository = new File( new File( new File( new File( Manager.getStorageInstance().getDataDirectory(), "repository" ), "site" ), project.getId() ), "analyze" );
                    String outputPath = siteRepository.getPath();
                    commands.add( outputPath );
                    log.debug( "Running scan-build in dir:" + dir + " with output path:" + outputPath );

                    appendXcodeCommands( project, config, commands, dir, "Debug" );

                    ExecUtil.execute( commands, dir, buildOut, buildOut );
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

                BufferedReader input = new BufferedReader( new FileReader( output ) );
                try
                {
                    String line = null;
                    while ( ( line = input.readLine() ) != null )
                    {
                        Matcher mBugs = BUILD_LOG_BUGS_COUNT_PATTERN.matcher( line );
                        if ( mBugs.find() )
                        {
                            String bugCount = mBugs.group( 1 );
                            int warnings = build.getWarnings() + Integer.parseInt( bugCount );
                            build.setWarnings( warnings );
                        }
                        Matcher mOutDir = BUILD_LOG_OUTPUT_DIR_PATTERN.matcher( line );
                        if ( mOutDir.find() )
                        {
                            File outputDir = new File( mOutDir.group( 1 ) );
                            File renamePath = new File( new File( new File( new File( new File( Manager.getStorageInstance().getDataDirectory(), "repository" ), "site" ), project.getId() ), "analyze" ), "" + build.getId() );
                            log.debug( "Renaming:" + outputDir.getPath() + " To:" + renamePath.getPath() );
                            boolean success = outputDir.renameTo( renamePath );
                            if ( !success )
                            {
                                log.error( "failed to rename:" + outputDir.getPath() + " To:" + renamePath.getPath() );
                            }

                            //rename build name within index file
                            File indexFile = new File( renamePath, "index.html" );
                            File indexTmpFile = new File( renamePath, "index.tmp.html" );
                            String outputName = dir.getName();
                            try
                            {
                                RenameBuildFromFileToFile( outputName + " -", "build: " + build.getId() + " -", indexFile, indexTmpFile );
                            }
                            catch ( IOException ex )
                            {
                                ex.printStackTrace();
                            }

                            success = indexTmpFile.renameTo( indexFile );
                            if ( !success )
                            {
                                log.error( "failed to rename:" + indexTmpFile.getPath() + "To:" + indexFile.getPath() );
                            }
                        }
                    }
                }
                finally
                {
                    input.close();
                }
            }
            catch ( IOException ex )
            {
                ex.printStackTrace();
            }
        }
    }

    public void onBuildFailed( Project project, PropertyTree config, PropertyTree appConfig, File dir, File output,
                               Build build )
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    protected File getBuildDirectory( Build build, File checkoutDir )
    {
        if ( cachedBuildDirectory != null )
        {
            return cachedBuildDirectory;
        }

        File oldStyle = new File( checkoutDir,  "build" );

        if ( oldStyle.exists() )
        {
            cachedBuildDirectory = oldStyle;
            return oldStyle;
        }

        cachedBuildDirectory = getDerivedDataDirectory( build, checkoutDir );
        return cachedBuildDirectory;
    }

    protected File getDerivedDataDirectory( Build build, File checkoutDir )
    {
        String checkoutName = checkoutDir.getName();
        String buildHashWithDash = checkoutName.substring( checkoutName.indexOf( '-' ) );

        File derivedData = new File( System.getProperty( "user.home" ), "Library/Developer/Xcode/DerivedData" );
        File[] subdirs = derivedData.listFiles();
        if ( subdirs == null || subdirs.length == 0 )
        {
            return null;
        }

        for ( File derivedDataDir : subdirs )
        {
            if ( isDerivedDataDirectoryForBuild( derivedDataDir, buildHashWithDash ) )
            {
                return derivedDataDir;
            }
        }

        return null;
    }

    protected boolean isDerivedDataDirectoryForBuild( File derivedDataDir, String buildHash )
    {
        File info = new File( derivedDataDir, "info.plist" );
        BufferedReader reader = null;
        try
        {
            reader = new BufferedReader( new FileReader( info ) );

            String line = null;
            while ( ( line = reader.readLine() ) != null )
            {
                if ( line.contains( buildHash ) )
                {
                    return true;
                }
            }

            return false;
        }
        catch ( IOException e )
        {
            return false;
        }
        finally
        {
            if ( reader != null )
            {
                IOUtil.close( reader );
            }
        }
    }

    protected boolean usesCocoaPods( File dir )
    {
        return new File( dir, "Podfile" ).exists();
    }

    public static boolean canFindXCPretty()
    {
        // try and find a binary called xcpretty.
        File lint = FileUtil.lookupInPath( "xcpretty" );

        return lint != null;
    }

    protected void tidyOutput( File output, File dir )
    {
        if ( !canFindXCPretty() )
        {
            return;
        }

        String oldName = output.getAbsolutePath();
        String tmpName = output.getAbsolutePath() + "-old";
        File tmpFile = new File( tmpName );
        output.renameTo( tmpFile );

        try
        {
            Writer buildOut = new FileWriter( output );
            if ( usesCocoaPods( dir ) )
            {
                buildOut.write( ITEM_PREFIX + "Updating CocoaPods\n" );
            }
            buildOut.close();

            String[] cmd = { "/bin/sh", "-c",
                "cat \"" + tmpName + "\" | xcpretty >> \"" + output.getAbsolutePath() + "\"" };

            ExecUtil.execute( Arrays.asList( cmd ), dir );
            tmpFile.delete();
        }
        catch ( IOException e )
        {
            output.renameTo( new File( oldName ) );

            Writer buildOut;
            try
            {
                buildOut = new FileWriter( output );
                e.printStackTrace( new PrintWriter( buildOut ) );
                buildOut.close();
            }
            catch ( IOException e1 )
            {
                // ignore
            }
        }
    }
}
