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

import java.util.Date;

/**
 * A model for formatting the duration between start and end in a user friendly way.
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class FormattedDurationModel
    extends Model<String>
{
    private long duration;
    private boolean unended;

    public FormattedDurationModel( Date start, Date end )
    {
        if ( end == null )
        {
            unended = true;
        }
        else
        {
            if ( start == null )
            {
                start = new Date();
            }

            unended = false;
            duration = end.getTime() - start.getTime();
        }
    }

    public FormattedDurationModel( long millis )
    {
        unended = false;
        duration = millis;
    }

    public String getObject()
    {
        return parseDuration( duration, unended );
    }

    public static String parseDuration( Date start, Date end )
    {
        if ( end == null )
        {
            return parseDuration( 0, true );
        }
        else
        {
            if ( start == null )
            {
                start = new Date();
            }

            return parseDuration( end.getTime() - start.getTime(), false );
        }
    }

    public static String parseDuration( long diff, boolean blank )
    {
        if ( blank )
        {
            return "";
        }

        if ( diff < 1000 )
        {
            return String.valueOf( diff ) + " ms";
        }
        diff /= 1000;
        if ( diff < 60 )
        {
            return String.valueOf( diff ) + " second" + plural( diff );
        }
        diff /= 60;
        if ( diff < 60 )
        {
            return String.valueOf( diff ) + " minute" + plural( diff );
        }
        diff /= 60;
        if ( diff < 24 )
        {
            return String.valueOf( diff ) + " hour" + plural( diff );
        }
        diff /= 24;
        if ( diff < 7 )
        {
            return String.valueOf( diff ) + " day" + plural( diff );
        }
        if ( diff < 28 )
        {
            long weeks = diff / 7;
            return String.valueOf( weeks ) + " week" + plural( weeks );
        }

        // nasty hacks for a month approximation
        if ( diff < 30 )
        {
            diff = 30;
        }
        diff /= 30;
        if ( diff < 12 )
        {
            return String.valueOf( diff ) + " month" + plural( diff );
        }
        diff /= 12;
        return String.valueOf( diff ) + " year" + plural( diff );
    }

    private static String plural( long test ) {
        if ( test == 1 )
        {
            return "";
        }

        return "s";
    }
}
