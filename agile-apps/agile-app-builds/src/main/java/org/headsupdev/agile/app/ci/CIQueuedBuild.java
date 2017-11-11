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

package org.headsupdev.agile.app.ci;

import org.headsupdev.agile.api.PropertyTree;
import org.headsupdev.agile.api.Project;

/**
 * TODO document me
 *
 * @author Andrew Williams
 * @since 1.0
 */
public class CIQueuedBuild
{
    private Project project;
    private String id;
    private PropertyTree config;
    private boolean notify, deferred;

    public CIQueuedBuild( Project project, String id, PropertyTree config, boolean notify )
    {
        this.project = project;
        this.id = id;
        this.config = config;
        this.notify = notify;
    }

    public Project getProject()
    {
        return project;
    }

    public String getId()
    {
        return id;
    }

    public String getConfigName()
    {
        if ( !config.getPropertyNames().contains( CIApplication.CONFIGURATION_BUILD_NAME.getKey() ) )
        {
            return null;
        }

        return config.getProperty( CIApplication.CONFIGURATION_BUILD_NAME.getKey(),
                (String) CIApplication.CONFIGURATION_BUILD_NAME.getDefault() );
    }

    public PropertyTree getConfig()
    {
        return config;
    }

    public boolean getNotify()
    {
        return notify;
    }

    public boolean isDeferred()
    {
        return deferred;
    }

    public void setDeferred( boolean deferred )
    {
        this.deferred = deferred;
    }

    public boolean equals( Object o )
    {
        return o instanceof CIQueuedBuild && equals( (CIQueuedBuild) o );
    }

    public boolean equals( CIQueuedBuild b )
    {
        return b.getProject().equals( project ) && b.getId().equals( id );
    }

    public int hashCode()
    {
        return ( getProject().getId() + id ).hashCode(); 
    }
}
