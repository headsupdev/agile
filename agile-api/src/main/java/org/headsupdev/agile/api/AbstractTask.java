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

package org.headsupdev.agile.api;

import java.util.Date;

/**
 * A simple abstract implementation of Task that concrete implementations can make use of.
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public abstract class AbstractTask implements Task
{
    private String title, description;
    private Project project;
    private Date startTime;

    public AbstractTask( String title, String description )
    {
        this( title, description, null );
    }

    public AbstractTask( String title, String description, Project project )
    {
        this.title = title;
        this.description = description;
        this.project = project;
        this.startTime = new Date();
    }

    public String getTitle()
    {
        return title;
    }

    public String getDescription()
    {
        return description;
    }

    public Project getProject()
    {
        return project;
    }

    public Date getStartTime()
    {
        return startTime;
    }
}