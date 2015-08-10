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

import org.headsupdev.agile.api.*;
import org.headsupdev.agile.app.ci.CIBuilder;
import org.headsupdev.support.java.ExecUtil;
import org.headsupdev.support.java.IOUtil;
import org.headsupdev.agile.api.logging.Logger;
import org.headsupdev.agile.app.ci.CIApplication;
import org.headsupdev.agile.storage.ci.Build;
import org.headsupdev.support.java.StringUtil;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * The code used to build an ant based project - also parses JUnit test results if applicable.
 *
 * @author Andrew Williams
 * @since 1.0
 */
public class AntBuildHandler
    extends MavenTwoBuildHandler
{
    private static Logger log = Manager.getLogger( AntBuildHandler.class.getName() );

    @Override
    public boolean isReadyToBuild( Project project, CIBuilder builder )
    {
        return true;
    }

    public void runBuild( Project project, PropertyTree config, PropertyTree appConfig, File dir, File output,
                          Build build )
    {
        int result = -1;
        Writer buildOut = null;
        try
        {
            buildOut = new FileWriter( output, true );

            String antHome = MavenTwoBuildHandler.lookupBuildExecutable( CIApplication.CONFIGURATION_ANT_HOME,
                    config, appConfig, build, output );
            if ( antHome == null )
            {
                return;
            }

            String tasksProperty = config.getProperty( CIApplication.CONFIGURATION_ANT_TASKS.getKey() );
            if ( StringUtil.isEmpty( tasksProperty ) )
            {
                tasksProperty = (String) CIApplication.CONFIGURATION_ANT_TASKS.getDefault();
            }
            List<String> commands = new ArrayList<String>();
            if ( !StringUtil.isEmpty( tasksProperty ) )
            {
                commands.addAll( Arrays.asList( tasksProperty.split( " " ) ) );
            }

            File antExe = new File( antHome, "ant" );
            if ( !antExe.exists() )
            {
                antExe = new File( antHome, "ant.bat" );
            }
            commands.add( 0, antExe.getAbsolutePath() );
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

        parseTests( project, findJUnitReports( dir ), build, Manager.getStorageInstance() );

        build.setEndTime( new Date() );
        if ( result != 0 )
        {
            build.setStatus( Build.BUILD_FAILED );
            onBuildFailed(project, config, appConfig, dir, output, build);
        }
        else
        {
            build.setStatus( Build.BUILD_SUCCEEDED );
            onBuildPassed(project, config, appConfig, dir, output, build);
        }
    }

    protected static File findJUnitReports( File inDir )
    {
        return new File( new File( inDir, "target" ), "surefire-reports" );
    }

    @Override
    public void onBuildFailed( Project project, PropertyTree config, PropertyTree appConfig, File dir, File output,
                               Build build )
    {
        // don't execute the inherited maven callbacks
    }

    @Override
    public void onBuildPassed( Project project, PropertyTree config, PropertyTree appConfig, File dir, File output,
                               Build build )
    {
        // don't execute the inherited maven callbacks
    }
}
