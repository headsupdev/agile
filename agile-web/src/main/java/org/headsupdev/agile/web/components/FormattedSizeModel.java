/*
 * HeadsUp Agile
 * Copyright 2009-2013 Heads Up Development Ltd.
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

import com.ibm.icu.text.DecimalFormat;
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
    private static ThreadLocal<DecimalFormat> formatterLocal = new ThreadLocal<DecimalFormat>();

    public FormattedSizeModel( long size )
    {
        this.size = size;
    }

    public String getObject()
    {
        return formatSize( size );
    }

    public static String formatSize( long bytes )
    {
        double size = bytes;
        if ( size < 1024 ) {
            return getFormatter().format( size ) + " Bytes";
        }
        size /= 1024;
        if ( size < 1024 ) {
            return getFormatter().format( size ) + " KiB";
        }
        size /= 1024;
        if ( size < 1024 ) {
            return getFormatter().format( size ) + " MiB";
        }
        size /= 1024;
        if ( size < 1024 ) {
            return getFormatter().format( size ) + " GiB";
        }
        size /= 1024;
        return getFormatter().format( size ) + " TiB";
    }

    protected static DecimalFormat getFormatter()
    {
        DecimalFormat formatter = formatterLocal.get();
        if ( formatter != null )
        {
            return formatter;
        }

        formatter = new DecimalFormat();
        formatter.setMaximumSignificantDigits( 3 );
        formatter.setSignificantDigitsUsed( true );

        formatterLocal.set( formatter );
        return formatter;
    }
}
