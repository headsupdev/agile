/*
 * HeadsUp Agile
 * Copyright 2014 Heads Up Development Ltd.
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
package org.headsupdev.agile.web.wicket;

import org.apache.wicket.util.convert.converters.AbstractConverter;
import org.headsupdev.agile.storage.issues.Duration;

import java.util.Locale;

/**
 * Created by Gordon Edwards on 02/07/2014.
 */
public class DurationConverter
        extends AbstractConverter
{
    @Override
    protected Class<?> getTargetType()
    {
        return Duration.class;
    }

    @Override
    public Object convertToObject( String value, Locale locale )
    {
        Duration duration = null;
        try
        {
            duration = Duration.fromString( value );
        }
        catch ( Exception e )
        {
            throw newConversionException( "Invalid", value, locale );
        }
        return duration;
    }
}
