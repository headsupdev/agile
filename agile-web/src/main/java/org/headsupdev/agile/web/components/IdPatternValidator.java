/*
 * HeadsUp Agile
 * Copyright 2014 Heads Up Development.
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

import org.apache.wicket.validation.validator.PatternValidator;

import java.util.regex.Pattern;

/**
 * A simple pattern validator that checks the string can be used for an object id
 *
 * @author Andrew Williams
 * @since 2.1
 */
public class IdPatternValidator
    extends PatternValidator
{
    public static final Pattern ID_PATTERN = Pattern.compile( "[0-9]*" );

    public IdPatternValidator()
    {
        super( ID_PATTERN );
    }

    /**
     * A simple helper to verify a id against thee pattern used by this validator.
     *
     * @param id The id to check for validity
     * @return true if the passed id is valid, false otherwise
     */
    public static boolean isValidId( String id )
    {
        return ID_PATTERN.matcher( id ).matches();
    }
}
