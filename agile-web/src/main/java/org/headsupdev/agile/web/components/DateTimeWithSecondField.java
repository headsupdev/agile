/*
 * HeadsUp Agile
 * Copyright 2013 Heads Up Development Ltd.
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

import org.apache.wicket.extensions.yui.calendar.DateTimeField;
import org.apache.wicket.model.IModel;
import org.headsupdev.agile.web.HeadsUpSession;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * TODO Document me!
 *
 * @author Andrew Williams
 * @since 2.0
 */
public class DateTimeWithSecondField extends DateTimeWithTimeZoneField
{
    public DateTimeWithSecondField( String id )
    {
        super( id );
    }

    public DateTimeWithSecondField( String id, IModel<Date> model )
    {
        super( id, model );
    }

    @Override
    protected void convertInput()
    {
        super.convertInput();

        Calendar cal = Calendar.getInstance();
        cal.setTime( getConvertedInput() );

        Calendar current = Calendar.getInstance();
        current.setTime( new Date() );

        cal.set( Calendar.SECOND, current.get( Calendar.SECOND ) );
        cal.set( Calendar.MILLISECOND, current.get( Calendar.MILLISECOND ) );

        setConvertedInput( cal.getTime() );
    }
}
