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

import org.apache.wicket.RestartResponseAtInterceptPageException;

import java.util.TimeZone;

/**
 * Definition of configurable web elements.
 * <p/>
 * Created: 25/05/2012
 *
 * @author Andrew Williams
 * @since 1.0
 */
public abstract class WebManager
{
    private static WebManager instance;

    public static WebManager getInstance()
    {
        return instance;
    }

    public static void setInstance( WebManager instance )
    {
        WebManager.instance = instance;
    }

    public abstract String getHeaderLogo();
    public abstract String getLozengeLogo();

    public abstract String getFooterDescriptionHTML( TimeZone timeZone );

    public abstract String getFooterCopyrightHTML();

    public abstract String getFooterNoteHTML();

    public abstract void checkPermissions( HeadsUpPage page ) throws RestartResponseAtInterceptPageException;
}
