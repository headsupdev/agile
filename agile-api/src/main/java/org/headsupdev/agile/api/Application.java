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

package org.headsupdev.agile.api;

import org.headsupdev.agile.api.rest.Api;
import org.headsupdev.irc.IRCCommand;

import java.util.List;
import java.util.Date;

/**
 * The description for an application in HeadsUp, provide configuration items and return page lists etc.
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public interface Application
{
    String getName();

    String getApplicationId();

    Class<? extends Page>[] getPages();
    Class<? extends Page> getHomePage();
    Class<? extends Api>[] getApis();
    Class[] getResources();

    Permission[] getPermissions();
    LinkProvider[] getLinkProviders();
    IRCCommand[] getIRCCommands();

    String getDescription();

    List<MenuLink> getLinks( Project project );

    List<String> getEventTypes();

    List<Event> getEvents( Date start, Date stop );

    void addEvent( Event event );

    void addEvent( Event event, boolean notify );

    List<Event> getEventsForProject( Project project, Date start, Date stop );

    List<Event> getEventsForProjectTree( Project project, Date start, Date stop );

    Class[] getPersistantClasses();

    PropertyTree getConfiguration();
    String getConfigurationValue( ConfigurationItem item );
    
    List<ConfigurationItem> getConfigurationItems();

    List<ConfigurationItem> getProjectConfigurationItems();
    List<ConfigurationItem> getProjectConfigurationItems( Project project );

    void onConfigurationChanged();
    void onProjectConfigurationChanged( Project project );
}
