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
import org.headsupdev.support.java.IOUtil;
import org.headsupdev.agile.api.CommandLineProject;
import org.headsupdev.agile.api.PropertyTree;
import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.api.logging.Logger;
import org.headsupdev.agile.storage.ci.Build;
import org.headsupdev.agile.app.ci.CIApplication;

import java.io.*;
import java.util.Date;

/**
 * The main code for building a command line project.
 *
 * @author Andrew Williams
 * @since 1.0
 */
public class CommandLineBuildHandler
    implements BuildHandler
{
    private static Logger log = Manager.getLogger( CommandLineBuildHandler.class.getName() );

    public void runBuild( Project project, PropertyTree config, PropertyTree appConfig, File dir, File output,
                                               Build build )
    {
        if ( !( project instanceof CommandLineProject ) )
        {
            return;
        }

        int result = -1;
        Writer buildOut = null;
        Process process = null;
        StreamGobbler serr = null, sout = null;
        try
        {
            buildOut = new FileWriter( output );

            String command = config.getProperty( CIApplication.CONFIGURATION_COMMAND_LINE.getKey() );
            if ( command == null )
            {
                command = (String) CIApplication.CONFIGURATION_COMMAND_LINE.getDefault();
            }

            // wrap it in a shell call so we can do things like "make && make install"
            String[] commands = new String[3];
            commands[0] = "sh";
            commands[1] = "-c";
            commands[2] = command;
            process = Runtime.getRuntime().exec( commands, null, dir );

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
            log.error( "Unable to write to build output file - reported in build log", e );
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
}
