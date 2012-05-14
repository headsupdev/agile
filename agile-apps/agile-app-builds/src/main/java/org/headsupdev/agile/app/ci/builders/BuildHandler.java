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
import org.headsupdev.agile.storage.ci.Build;

import java.io.*;

/**
 * The build manager, marshalling requests to builders.
 *
 * @author Andrew Williams
 * @since 1.0
 */
public class BuildHandler
{
    public static boolean supportsBuilding( Project project )
    {
        return project instanceof MavenTwoProject || project instanceof AntProject ||
            project instanceof EclipseProject || project instanceof XCodeProject ||
            project instanceof CommandLineProject;
    }

    public static void runBuild( Project project, PropertyTree config, PropertyTree appConfig, File dir, File output,
                                 Build build, long buildId )
    {
        if ( project instanceof MavenTwoProject )
        {
            MavenTwoBuildHandler.runBuild( (MavenTwoProject) project, config, appConfig, dir, output, build, buildId );
        }
        else if ( project instanceof AntProject )
        {
            AntBuildHandler.runBuild( (AntProject) project, config, appConfig, dir, output, build, buildId );
        }
        else if ( project instanceof EclipseProject )
        {
            EclipseBuildHandler.runBuild( (EclipseProject) project, config, appConfig, dir, output, build, buildId );
        }
        else if ( project instanceof XCodeProject )
        {
            XCodeBuildHandler.runBuild( (XCodeProject) project, config, dir, output, build, buildId );
        }
        else if ( project instanceof CommandLineProject )
        {
            CommandLineBuildHandler.runBuild( (CommandLineProject) project, config, dir, output, build, buildId );
        }
    }
}
