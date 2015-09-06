/*
 * HeadsUp Agile
 * Copyright 2009-2015 Heads Up Development Ltd.
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

package org.headsupdev.agile.web.wicket;

import org.apache.wicket.protocol.http.request.WebRequestCodingStrategy;
import org.apache.wicket.request.target.coding.IRequestTargetUrlCodingStrategy;
import org.apache.wicket.util.string.AppendingStringBuffer;
import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.storage.StoredProject;

import java.util.HashSet;
import java.util.Set;

/**
 * TODO document me
 *
 * @author Andrew Williams
 * @since 1.0
 */
public class HeadsUpRequestCodingStrategy
    extends WebRequestCodingStrategy
{
    public static final String DEFAULT_PAGE = "show";

    private static Set<String> projectIds;

    public IRequestTargetUrlCodingStrategy urlCodingStrategyForPath( String path )
    {

        int pos = path.indexOf( "/" );
        if ( pos > 0 )
        {
            // if there is, or could be, a project component
            String projectId = path.substring( 0, pos );
            if ( getProjectIds().contains( projectId ) )
            {
                // project matched, strip beginning of path
                String newPath = path.substring( pos + 1);

                if ( newPath.length() == 0 )
                {
                    // remaining was empty, return the default page
                    return super.urlCodingStrategyForPath( DEFAULT_PAGE );
                }

                return super.urlCodingStrategyForPath( newPath );
            }

            // project not found for string, fall through
        }
        else
        {
            // match when we have simply specified the project name
            if ( getProjectIds().contains( path ) )
            {
                return super.urlCodingStrategyForPath( DEFAULT_PAGE );
            }
        }

        // what if nothing matched?
        return super.urlCodingStrategyForPath( path );
    }

    public static Set<String> getProjectIds()
    {
        if ( projectIds == null )
        {
            projectIds = new HashSet<String>();
            updateProjectIds();
        }

        return projectIds;
    }

    public static void updateProjectIds()
    {
        projectIds.clear();

        // setup id map
        projectIds.add( Project.ALL_PROJECT_ID );

        for ( Project p : Manager.getStorageInstance().getProjects( true ) )
        {
            projectIds.add( p.getId() );
        }
    }

    public static void addProject( Project project )
    {
        // if the set is not initialised we will catch it later
        if ( projectIds != null )
        {
            projectIds.add( project.getId() );

            if ( project.getChildProjects() != null )
            {
                for ( Project child : project.getChildProjects() )
                {
                    addProject( child );
                }
            }
        }
    }

    static ProjectUrl decodePath( String path )
    {
        Project found = StoredProject.getDefault();
        String foundPath = path;

        String projectId = path;
        String newPath = DEFAULT_PAGE;
        int pos = path.indexOf( "/" );
        if ( pos > 0 )
        {
            projectId = path.substring( 0, pos );
            newPath = path.substring( pos + 1);

            if ( newPath.length() == 0 )
            {
                newPath = DEFAULT_PAGE;
            }
        }

        if ( projectId.equals( Project.ALL_PROJECT_ID ) )
        {
            found = StoredProject.getDefault();
            foundPath = newPath;
        }
        else
        {
            if ( getProjectIds().contains( projectId ) )
            {
                found = Manager.getStorageInstance().getProject( projectId );
                foundPath = newPath;
            }
        }

        return new ProjectUrl( found, foundPath );
    }

    static void encodePath( AppendingStringBuffer url, Object project )
    {
        if ( project instanceof Project )
        {
            url.append( ( (Project) project ).getId() );
        }
        else
        {
            if ( project == null || project.toString().length() == 0 )
            {
                url.append( Project.ALL_PROJECT_ID );
            }
            else
            {
                url.append( project.toString() );
            }
        }
    }
}

class ProjectUrl
{
    private Project project;
    private String url;

    ProjectUrl( Project project, String url )
    {
        this.project = project;
        this.url = url;
    }

    public Project getProject()
    {
        return project;
    }

    public String getUrl()
    {
        return url;
    }
}
