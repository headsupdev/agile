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
import java.util.Date;

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
    final static String PLUGIN_FILE_NAME = "org.headsupdev.agile.build.eclipse_1.0.0.jar";

    public void runBuild( Project project, PropertyTree config, PropertyTree appConfig, File dir, File output,
                          Build build )
    {
        if ( !( project instanceof EclipseProject) )
        {
            return;
        }

        Logger log = Manager.getLogger( EclipseBuildHandler.class.getName() );

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
        Process process = null;
        StreamGobbler serr = null, sout = null;
        try
        {
            buildOut = new FileWriter( output );

            File eclipseExe = new File( eclipseHome, "eclipse" );
            if ( !eclipseExe.exists() )
            {
                eclipseExe = new File( eclipseHome, "eclipse.exe" );
            }
            String[] commands = new String[]
            {
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
            };
            process = Runtime.getRuntime().exec( commands, null, new File( eclipseHome ) );

            serr = new StreamGobbler( new InputStreamReader( process.getErrorStream() ), buildOut );
            sout = new StreamGobbler( new InputStreamReader( process.getInputStream() ), buildOut );
            serr.start();
            sout.start();

            result = process.waitFor();
        }
        catch ( InterruptedException e )
        {
            // TODO use this hook when we cancel the process
        }
        catch ( IOException e )
        {
            e.printStackTrace( new PrintWriter( buildOut ) );
            log.error( "Error running eclipse build", e );
        }
        finally
        {
            if ( process != null )
            {
                // defensively try to close the gobblers
                if ( serr != null && sout != null )
                {
                    // check that our gobblers are finished...
                    while ( !serr.isComplete() || !sout.isComplete() )
                    {
                        log.debug( "waiting 1s to close gobbler" );
                        try
                        {
                            Thread.sleep( 1000 );
                        }
                        catch ( InterruptedException e )
                        {
                            // we were just trying to tidy up...
                        }
                    }
                }

                IOUtil.close( process.getOutputStream() );
                IOUtil.close( process.getErrorStream() );
                IOUtil.close( process.getInputStream() );
                process.destroy();
            }

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
            onBuildPassed(project, config, appConfig, dir, output, build);
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
}
