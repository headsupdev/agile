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

package org.headsupdev.agile.web;

import java.util.List;
import java.util.Date;
import java.util.LinkedList;
import java.io.Serializable;

import org.headsupdev.agile.api.*;
import org.headsupdev.agile.api.rest.Api;
import org.headsupdev.agile.storage.StoredProject;
import org.headsupdev.irc.IRCCommand;

/**
 * An abstract base for applications, manages the events list and configuration objects and their storage / loading.
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public abstract class AbstractApplication
    implements Application, Serializable
{
    private List<String> noEventTypes = new LinkedList<String>();
    private List<ConfigurationItem> items = new LinkedList<ConfigurationItem>();
    private List<ConfigurationItem> projectItems = new LinkedList<ConfigurationItem>();

    public List<String> getEventTypes()
    {
        return noEventTypes;
    }

    public List<Event> getEvents( Date start, Date stop )
    {
        return Manager.getStorageInstance().getEvents( this, start, stop );
    }

    public List<Event> getEventsForProject( Project project, Date start, Date stop )
    {
        return Manager.getStorageInstance().getEventsForProject( project, this, start, stop );
    }

    public List<Event> getEventsForProjectTree( Project project, Date start, Date stop )
    {
        return Manager.getStorageInstance().getEventsForProjectTree( project, this, start, stop );
    }

    public void addEvent( Event event )
    {
        addEvent( event, true );
    }

    public void addEvent( Event event, boolean notify )
    {
        Manager.getStorageInstance().addEvent( event );

        if ( notify )
        {
            Manager.getInstance().fireEventAdded( event );
        }
    }

    public void addConfigurationItem( ConfigurationItem item )
    {
        items.add( item );
    }

    public List<ConfigurationItem> getConfigurationItems()
    {
        return items;
    }

    public PropertyTree getConfiguration()
    {
        return Manager.getStorageInstance().getGlobalConfiguration().getApplicationConfiguration( this );
    }

    public String getConfigurationValue( ConfigurationItem item )
    {
        String ret = getConfiguration().getProperty( item.getKey() );
        if ( ret == null )
        {
            return String.valueOf( item.getDefault() );
        }

        return ret;
    }

    public PropertyTree getProjectConfiguration( Project project )
    {
        return Manager.getStorageInstance().getGlobalConfiguration().getApplicationConfigurationForProject( this,
            project );
    }

    public String getProjectConfigurationValue( ConfigurationItem item, Project project )
    {
        while ( project != null )
        {
            String ret = getProjectConfiguration( project ).getProperty( item.getKey() );
            if ( ret != null ) {
                return ret;
            }

            project = project.getParent();
        }

        String ret = getProjectConfiguration( StoredProject.getDefault() ).getProperty( item.getKey() );
        if ( ret != null )
        {
            return ret;
        }

        return String.valueOf( item.getDefault() );
    }

    public void addProjectConfigurationItem( ConfigurationItem item )
    {
        projectItems.add( item );
    }

    public List<ConfigurationItem> getProjectConfigurationItems()
    {
        return projectItems;
    }

    public List<ConfigurationItem> getProjectConfigurationItems( Project project )
    {
        return getProjectConfigurationItems();
    }

    public void onConfigurationChanged()
    {
    }

    public void onProjectConfigurationChanged( Project project )
    {
    }

    public Class[] getPersistantClasses()
    {
        return new Class[0];
    }

    public Class<? extends Page>[] getPages()
    {
        return (Class<? extends Page>[]) new Class[0];
    }

    public Class<? extends Page> getHomePage()
    {
        return null;
    }

    public Class<? extends Api>[] getApis()
    {
        return (Class<? extends Api>[]) new Class[0];
    }

    public Class[] getResources()
    {
        return new Class[0];
    }

    public Permission[] getPermissions()
    {
        return new Permission[0];
    }

    public LinkProvider[] getLinkProviders()
    {
        return new LinkProvider[0];
    }

    public IRCCommand[] getIRCCommands()
    {
        return new IRCCommand[0];
    }

    public int hashCode()
    {
        return getApplicationId().hashCode();
    }

    public boolean equals( Object application )
    {
        return application instanceof Application && equals( (Application) application );
    }

    public boolean equals( Application application )
    {
        return getApplicationId().equals( application.getApplicationId() );
    }
}
