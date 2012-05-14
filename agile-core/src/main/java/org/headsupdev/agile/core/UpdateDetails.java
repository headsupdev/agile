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

package org.headsupdev.agile.core;

import java.io.Serializable;

/**
 * A simple class for storing details of available updates.
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class UpdateDetails
    implements Serializable
{
    private String id, title, details, file;
    private long length;
    private boolean beta;

    public UpdateDetails( String id, String title, String details, String file, long length, boolean beta )
    {
        this.id = id;
        this.title = title;
        this.details = details;
        this.file = file;
        this.length = length;
        this.beta = beta;
    }

    public String getId()
    {
        return id;
    }

    public String getTitle()
    {
        return title;
    }

    public String getDetails()
    {
        return details;
    }

    public String getFile()
    {
        return file;
    }

    public long getLength()
    {
        return length;
    }

    public boolean isBeta()
    {
        return beta;
    }
}
