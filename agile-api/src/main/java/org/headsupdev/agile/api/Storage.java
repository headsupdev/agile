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

package org.headsupdev.agile.api;

import java.util.List;
import java.util.Date;
import java.util.Map;
import java.io.File;
import java.io.IOException;

/**
 * Storage interface for storage code to implement. Loading only 1 at runtime will automatically hook into the load
 * and save calls where needed.
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public interface Storage
{
    File getDataDirectory();
    File getApplicationDataDirectory( Application application );

    File getWorkingDirectory( Project project );
    void copyWorkingDirectory( Project project, File dest )
        throws IOException;

    Project getProject( String id);
    List<Project> getProjects();
    List<Project> getProjects( boolean withDisabled );
    List<Project> getRootProjects();
    List<Project> getRootProjects( boolean withDisabled );

    List<Project> getActiveRootProjects();
    List<Project> getRecentRootProjects( User user );

    void addProject( Project project );

    List<Event> getEvents( Date start, Date stop );
    List<Event> getEvents( Application app, Date start, Date stop );

    List<Event> getEventsForProject( Project project, Date start, Date stop );
    List<Event> getEventsForProject( Project project, Application app, Date start, Date stop );

    List<Event> getEventsForProjectTree( Project project, Date start, Date stop );
    List<Event> getEventsForProjectTree( Project project, Application app, Date start, Date stop );

    List<Event> getEventsForUser( User user, Date start, Date stop );

    void addEvent( Event event );

    HeadsUpConfiguration getGlobalConfiguration();

    String getConfigurationItem( String name );
    void setConfigurationItem( String name, String value );
    void removeConfigurationItem( String name );

    Map<String, String> getConfigurationItems( String prefix );
    void setConfigurationItems( Map<String, String> items );
}
