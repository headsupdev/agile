/*
 * HeadsUp Agile
 * Copyright 2012-2013 Heads Up Development Ltd.
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

/**
 * A factory for BuildHandlers available to manage building of various types of project
 * <p/>
 * Created: 29/09/2012
 *
 * @author Andrew Williams
 * @since 2.2
 */
public class BuildHandlerFactory
{
    public boolean supportsBuilding( Project project )
    {
        return project instanceof MavenTwoProject || project instanceof AntProject ||
                project instanceof EclipseProject || project instanceof XCodeProject ||
                project instanceof GradleProject || project instanceof CommandLineProject;
    }

    public BuildHandler getBuildHandler( Project project )
    {
        if ( project instanceof MavenTwoProject)
        {
            return new MavenTwoBuildHandler();
        }
        else if ( project instanceof AntProject)
        {
            return new AntBuildHandler();
        }
        else if ( project instanceof EclipseProject)
        {
            return new EclipseBuildHandler();
        }
        else if ( project instanceof XCodeProject)
        {
            return new XCodeBuildHandler();
        }
        else if ( project instanceof GradleProject)
        {
            return new GradleBuildHandler();
        }
        else if ( project instanceof CommandLineProject )
        {
            return new CommandLineBuildHandler();
        }

        return null;
    }
}
