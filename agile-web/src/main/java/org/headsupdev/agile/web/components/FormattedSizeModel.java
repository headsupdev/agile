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

package org.headsupdev.agile.web.components;

import org.apache.wicket.model.Model;

/**
 * TODO Document me!
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class FormattedSizeModel
    extends Model<String>
{
    private long size;

    public FormattedSizeModel( long size )
    {
        this.size = size;
    }

    public String getObject()
    {
        return formatSize( size );
    }

    public static String formatSize( long size )
    {
        if ( size < 1024 ) {
            return String.valueOf( size ) + " bytes";
        }
        size /= 1024;
        if ( size < 1024 ) {
            return String.valueOf( size ) + " kB";
        }
        size /= 1024;
        if ( size < 1024 ) {
            return String.valueOf( size ) + " MB";
        }
        size /= 1024;
        if ( size < 1024 ) {
            return String.valueOf( size ) + " GB";
        }
        size /= 1024;
        return String.valueOf( size ) + " TB";
    }
}
