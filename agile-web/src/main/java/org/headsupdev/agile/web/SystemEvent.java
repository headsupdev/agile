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

import javax.persistence.Entity;
import javax.persistence.DiscriminatorValue;
import java.util.Date;

/**
 * A simple event for system actions - they have a title and summary but nothing else,
 * they occur at the time they are stored.
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@Entity
@DiscriminatorValue( "system" )
public class SystemEvent
    extends AbstractEvent
{
    private static String SPLIT = "\n";
    public SystemEvent()
    {
    }

    public SystemEvent( String title, String summary )
    {
        this( title, summary, null );
    }

    public SystemEvent( String title, String summary, String body )
    {
        super( title, body == null ? summary : summary + SPLIT + body, new Date() );
    }

    public String getSummary()
    {
        String summary = super.getSummary();

        int pos = summary.indexOf( SPLIT );

        if ( pos > -1 )
        {
            return summary.substring( 0, pos );
        }

        return summary;
    }

    public String getBody()
    {
        String summary = super.getSummary();

        int pos = summary.indexOf( SPLIT );

        if ( pos > -1 )
        {
            return summary.substring( pos + SPLIT.length() );
        }

        return super.getBody();
    }
}