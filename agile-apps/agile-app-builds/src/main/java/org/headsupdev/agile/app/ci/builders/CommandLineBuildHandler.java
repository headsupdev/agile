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
import org.headsupdev.support.java.IOUtil;
import org.headsupdev.agile.api.CommandLineProject;
import org.headsupdev.agile.api.PropertyTree;
import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.api.logging.Logger;
import org.headsupdev.agile.storage.ci.Build;
import org.headsupdev.agile.app.ci.CIApplication;

import java.io.*;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

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

    @Override
    public boolean isReadyToBuild( Project project, CIBuilder builder )
    {
        return true;
    }

    public void runBuild( Project project, PropertyTree config, PropertyTree appConfig, File dir, File output,
                                               Build build )
    {
        if ( !( project instanceof CommandLineProject ) )
        {
            return;
        }

        int result = -1;
        Writer buildOut = null;
        try
        {
            buildOut = new FileWriter( output );

            String command = config.getProperty( CIApplication.CONFIGURATION_COMMAND_LINE.getKey() );
            if ( command == null )
            {
                command = (String) CIApplication.CONFIGURATION_COMMAND_LINE.getDefault();
            }

            // wrap it in a shell call so we can do things like "make && make install"
            List<String> commands = Arrays.asList( "sh", "-c", command );
            result = ExecUtil.executeLoggingExceptions( commands, dir, buildOut, buildOut );
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
