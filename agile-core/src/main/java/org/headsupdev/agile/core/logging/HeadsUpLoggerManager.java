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

package org.headsupdev.agile.core.logging;

import org.headsupdev.agile.api.HeadsUpConfiguration;
import org.headsupdev.agile.api.logging.Logger;

/**
 * TODO document me
 *
 * @author Andrew Williams
 * @since 1.0
 */
public class HeadsUpLoggerManager
{
    static ErrorHandler errorHandler;
    private static HeadsUpLoggerManager instance = new HeadsUpLoggerManager();

    public static HeadsUpLoggerManager getInstance()
    {
        return instance;
    }

    private org.headsupdev.agile.api.Storage storage;

    /**
     * Not part of the public API
     * @param config the global configuration to use
     */
    public void setConfiguration( HeadsUpConfiguration config )
    {
        if ( errorHandler == null )
        {
            errorHandler = new ErrorHandler( config );
        }
    }

    public Logger getLoggerForComponent( String component )
    {
        return new HeadsUpLogger();
    }
}
