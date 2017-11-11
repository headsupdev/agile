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

package org.headsupdev.agile.web.components;

import org.apache.wicket.model.Model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * A formatter for dates that uses a standard format or one specified in the constructor
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class FormattedDateModel
    extends Model<String>
{
    public static final Date NO_DATE = new Date( 0 );

    private DateFormat format;
    private Date date;

    public FormattedDateModel( Date date, TimeZone timeZone )
    {
        this( date, timeZone, "yyyy-MM-dd HH:mm" );
    }

    public FormattedDateModel( Date date, TimeZone timeZone, String formatStr )
    {
        this.date = date;
        format = new SimpleDateFormat( formatStr );
        format.setTimeZone( timeZone );
    }

    public String getObject()
    {
        if ( date == null || date.equals( NO_DATE ) )
        {
            return "";
        }

        return formatDate( date );
    }

    public synchronized String formatDate( Date date )
    {
        return format.format( date );
    }
}
