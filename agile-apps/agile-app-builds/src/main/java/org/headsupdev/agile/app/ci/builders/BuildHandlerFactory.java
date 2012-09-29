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
    public static boolean supportsBuilding( Project project )
    {
        return project instanceof MavenTwoProject || project instanceof AntProject ||
                project instanceof EclipseProject || project instanceof XCodeProject ||
                project instanceof CommandLineProject;
    }

    public static BuildHandler getBuildHandler( Project project )
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
        else if ( project instanceof CommandLineProject )
        {
            return new CommandLineBuildHandler();
        }

        return null;
    }
}
