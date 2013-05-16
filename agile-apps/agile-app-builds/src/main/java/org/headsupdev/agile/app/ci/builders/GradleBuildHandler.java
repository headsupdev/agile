/*
 * HeadsUp Agile
 * Copyright 2013 Heads Up Development Ltd.
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

import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.api.PropertyTree;
import org.headsupdev.agile.api.logging.Logger;
import org.headsupdev.agile.app.ci.CIApplication;
import org.headsupdev.agile.app.ci.CIBuilder;
import org.headsupdev.agile.storage.ci.Build;
import org.headsupdev.support.java.ExecUtil;
import org.headsupdev.support.java.IOUtil;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * The code used to build a gradle based project - also parses JUnit test results if applicable.
 *
 * @author Andrew Williams
 * @since 2.0
 */
public class GradleBuildHandler
    extends MavenTwoBuildHandler
{
    private static Logger log = Manager.getLogger( GradleBuildHandler.class.getName() );

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

            String gradleHome = MavenTwoBuildHandler.lookupBuildExecutable( CIApplication.CONFIGURATION_GRADLE_HOME,
                    config, appConfig, build, output );
            if ( gradleHome == null )
            {
                return;
            }

            String tasksProperty = config.getProperty( CIApplication.CONFIGURATION_GRADLE_TASKS.getKey() );
            if ( tasksProperty == null )
            {
                tasksProperty = (String) CIApplication.CONFIGURATION_GRADLE_TASKS.getDefault();
            }
            List<String> commands = new ArrayList<String>( Arrays.asList( tasksProperty.split( " " ) ) );

            File gradleExe = new File( gradleHome, "gradle" );
            if ( !gradleExe.exists() )
            {
                gradleExe = new File( gradleHome, "gradle.bat" );
            }
            commands.add( 0, gradleExe.getAbsolutePath() );
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
        return new File( inDir, "reports" );
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
