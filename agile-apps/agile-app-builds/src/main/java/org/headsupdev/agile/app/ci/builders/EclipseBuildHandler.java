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

import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.app.ci.CIBuilder;
import org.headsupdev.support.java.ExecUtil;
import org.headsupdev.support.java.FileUtil;
import org.headsupdev.support.java.IOUtil;
import org.headsupdev.agile.api.EclipseProject;
import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.api.PropertyTree;
import org.headsupdev.agile.api.logging.Logger;
import org.headsupdev.agile.app.ci.CIApplication;
import org.headsupdev.agile.storage.ci.Build;

import java.io.*;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * The code to build a project using eclipse.
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class EclipseBuildHandler
    implements BuildHandler
{
    final static String PLUGIN_FILE_NAME = "org.headsupdev.agile.build.eclipse_2.0.0.jar";

    private static Logger log = Manager.getLogger( EclipseBuildHandler.class.getName() );

    @Override
    public boolean isReadyToBuild( Project project, CIBuilder builder )
    {
        return true;
    }

    public void runBuild( Project project, PropertyTree config, PropertyTree appConfig, File dir, File output,
                          Build build )
    {
        if ( !( project instanceof EclipseProject) )
        {
            return;
        }

        if ( isAndroidProject(project) )
        {
            convertAndroidBuildToAnt( project, dir, output );
            runAndroidAntBuild( project, config, appConfig, "debug", dir, output, build );
            return;
        }

        URL plugin = EclipseBuildHandler.class.getResource( "eclipse/" + PLUGIN_FILE_NAME );
        if ( plugin == null )
        {
            build.setEndTime( new Date() );
            build.setStatus( Build.BUILD_FAILED );

            MavenTwoBuildHandler.logError( "Could not locate eclipse build plugin", output );
            return;
        }

        String eclipseHome = MavenTwoBuildHandler.lookupBuildExecutable( CIApplication.CONFIGURATION_ECLIPSE_HOME,
                config, appConfig, build, output );
        if ( eclipseHome == null )
        {
            return;
        }
        File pluginFile = new File( new File( eclipseHome, "plugins" ), PLUGIN_FILE_NAME );

        try
        {
            FileUtil.downloadToFile( plugin, pluginFile );
        }
        catch ( IOException e )
        {
            build.setEndTime( new Date() );
            build.setStatus( Build.BUILD_FAILED );

            MavenTwoBuildHandler.logError( "Unable to install plugin", e, output );
            return;
        }

        File tmpWorkspace;
        try
        {
            tmpWorkspace = org.headsupdev.agile.api.util.FileUtil.createTempDir( "workspace", "" );
            tmpWorkspace.deleteOnExit();
        }
        catch ( IOException e )
        {
            build.setEndTime( new Date() );
            build.setStatus( Build.BUILD_FAILED );

            MavenTwoBuildHandler.logError( "Unable to create temporary workspace", e, output );
            pluginFile.delete();
            return;
        }

        int result = -1;
        Writer buildOut = null;
        try
        {
            buildOut = new FileWriter( output );

            File eclipseExe = new File( eclipseHome, "eclipse" );
            if ( !eclipseExe.exists() )
            {
                eclipseExe = new File( eclipseHome, "eclipse.exe" );
            }
            List<String> commands = Arrays.asList(
                eclipseExe.getAbsolutePath(),
                "-application",
                "org.headsupdev.agile.build.eclipse.run",
                "-data",
                tmpWorkspace.getAbsolutePath(),
                "-project",
                dir.getAbsolutePath(),
                "-nosplash",
                "-logVerbose",
                "-logDebug"
            );

            result = ExecUtil.executeLoggingExceptions( commands, new File( eclipseHome ), buildOut, buildOut );
        }
        catch ( IOException e )
        {
            e.printStackTrace( new PrintWriter( buildOut ) );
            log.error( "Error running eclipse build", e );
        }
        finally
        {
            IOUtil.close( buildOut );

            pluginFile.delete();
            try
            {
                FileUtil.delete( tmpWorkspace );
            }
            catch ( IOException e )
            {
                // ignore - will be deleted when jvm shuts down
            }
        }

        build.setEndTime( new Date() );
        if ( result != 0 )
        {
            build.setStatus( Build.BUILD_FAILED );
            onBuildFailed( project, config, appConfig, dir, output, build );
        }
        else
        {
            build.setStatus( Build.BUILD_SUCCEEDED );
            onBuildPassed( project, config, appConfig, dir, output, build );
        }
    }

    public void onBuildPassed( Project project, PropertyTree config, PropertyTree appConfig, File dir, File output,
                               Build build )
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void onBuildFailed( Project project, PropertyTree config, PropertyTree appConfig, File dir, File output,
                               Build build )
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    protected boolean isAndroidProject( Project project )
    {
        if ( !( project instanceof EclipseProject) )
        {
            return false;
        }

        EclipseProject eclipse = ( (EclipseProject) project );
        log.debug( "checking project for android " + eclipse.getNature() );

        String nature = "";
        if ( eclipse.getNature() != null )
        {
            nature = eclipse.getNature().substring( eclipse.getNature().lastIndexOf( '.' ) + 1 );
        }
        return "AndroidNature".equals( nature );
    }

    protected boolean convertAndroidBuildToAnt( Project project, File buildDir, File output )
    {
        log.debug( "Converting eclipse project to ant in " + buildDir.getAbsolutePath() );
        List<String> commands = Arrays.asList( "android", "update", "project", "-p", buildDir.getAbsolutePath() );

        int result = -1;
        Writer buildOut = null;
        try
        {
            buildOut = new FileWriter( output );

            result = ExecUtil.executeLoggingExceptions( commands, buildOut, buildOut );
            convertAndroidBuildLibraries( buildDir, buildOut );
        }
        catch ( IOException e )
        {
            e.printStackTrace( new PrintWriter( buildOut ) );
            log.error( "Error converting eclipse android build to ant", e );
        }
        finally
        {
            IOUtil.close( buildOut );
        }

        return result == 0;
    }

    protected void convertAndroidBuildLibraries( File buildDir, Writer buildOut )
    {
        try
        {
            BufferedReader reader = new BufferedReader( new FileReader( new File( buildDir, "project.properties" ) ) );

            String line;
            while ( ( line = reader.readLine() ) != null )
            {
                if ( line.trim().startsWith( "android.library.reference." ) )
                {
                    String path = line.substring( line.indexOf( "=" ) + 1 ).trim();
                    convertAndroidBuildDirectoryToLibrary( buildDir, path, buildOut );
                }
            }
        }
        catch ( IOException e )
        {
            try
            {
                buildOut.write( "Error reading project.properties: " + e.getMessage() );
            }
            catch ( IOException e1 )
            {
                // ignore this one
            }
        }
    }

    protected void convertAndroidBuildDirectoryToLibrary( File buildDir, String dirName, Writer buildOut )
    {
        log.debug( "Converting eclipse subdirectory to library for " + dirName );

        File libDir = new File( buildDir, dirName );
        List<String> commands = Arrays.asList( "android", "update", "lib-project", "--path", libDir.getAbsolutePath() );

        ExecUtil.executeLoggingExceptions( commands, buildOut, buildOut );
    }

    protected void runAndroidAntBuild( Project project, final PropertyTree eclipseConfig, PropertyTree appConfig, String target,
                                       File buildDir, File output, Build build )
    {
        log.debug( "Running ant build for Eclipse Android project" );

        appConfig.setProperty( CIApplication.CONFIGURATION_ANT_TASKS.getKey(), target );

        final EclipseBuildHandler eclipseHandler = this;
        new AntBuildHandler()
        {
            @Override
            public void onBuildFailed( Project project, PropertyTree config, PropertyTree appConfig, File dir, File output, Build build )
            {
                eclipseHandler.onBuildFailed( project, eclipseConfig, appConfig, dir, output, build );
            }

            @Override
            public void onBuildPassed( Project project, PropertyTree config, PropertyTree appConfig, File dir, File output, Build build )
            {
                eclipseHandler.onBuildPassed( project, eclipseConfig, appConfig, dir, output, build );
            }
        }.runBuild( project, appConfig, appConfig, buildDir, output, build );
    }
}
