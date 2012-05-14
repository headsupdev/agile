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

package org.headsupdev.agile.web;

import org.headsupdev.agile.api.Permission;

/**
 * TODO Document me!
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public abstract class ErrorPage
    extends HeadsUpPage
{
    // we cannot make an exception seriaizable, if we re-load from cache then the exception will just be empty
    // render some excuse...
    private transient Exception error;

    public Exception getError()
    {
        return error;
    }

    public void setError( Exception error )
    {
        this.error = error;
    }

    public Permission getRequiredPermission()
    {
        return null;
    }

    @Override
    public boolean isErrorPage()
    {
        return true;
    }
}
