/*
 * HeadsUp Agile
 * Copyright 2009-2012 Heads Up Development.
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

package org.headsupdev.agile.app.artifacts;

import org.headsupdev.agile.api.*;
import org.headsupdev.agile.web.WebApplication;

import java.util.List;
import java.util.LinkedList;

/**
 * The repository browser application gives styled read access to the artifact repositories
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class RepositoryApplication
    extends WebApplication
{
    List<MenuLink> links;
    List<String> eventTypes;

    public RepositoryApplication()
    {
        links = new LinkedList<MenuLink>();
        links.add( new SimpleMenuLink( "release" ) );
        links.add( new SimpleMenuLink( "snapshot" ) );
        links.add( new SimpleMenuLink( "external" ) );

        links.add( new SimpleMenuLink( "projects" ) );

        eventTypes = new LinkedList<String>();
        eventTypes.add( "uploadartifact" );
        eventTypes.add( "uploadapplication" );
    }

    public String getName()
    {
        return "Artifacts";
    }

    public String getApplicationId()
    {
        return "artifacts";
    }

    public String getDescription()
    {
        return "The " + Manager.getStorageInstance().getGlobalConfiguration().getProductName() + " repository browser application";
    }

    public List<MenuLink> getLinks( Project project )
    {
        return links;
    }

    public List<String> getEventTypes()
    {
        return eventTypes;
    }

    @Override
    public Class<? extends Page>[] getPages()
    {
        return new Class[] { ExternalRepository.class, ListRepositories.class, ReleaseRepository.class,
            SnapshotRepository.class, ProjectsRepository.class, AppsRepository.class };
    }

    @Override
    public Class<? extends Page> getHomePage()
    {
        return ListRepositories.class;
    }
}
