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

public class HeadsUpLogger implements Logger
{
    private String yellow = "";
    private String red = "";
    private String blue = "";
    private String blank = "";

    public HeadsUpLogger() {
        if ( HeadsUpConfiguration.isColorConsole() )
        {
            yellow = "\033[1;33m";
            red = "\033[1;31m";
            blue = "\033[1;34m";
            blank = "\033[0m";
        }
    }
    public void debug( String message )
    {
        if ( HeadsUpConfiguration.isDebug() )
        {
            System.err.println( blank + "[DEBUG] " + message );
        }
    }

    public void info( String message )
    {
        if ( HeadsUpConfiguration.isVerbose() )
        {
            System.err.println( "[" + blue + "INFO " + blank + "] " + message );
        }
    }

    public void warn( String message )
    {
        if ( HeadsUpConfiguration.isVerbose() )
        {
            System.err.println( "[" + yellow + "WARN " + blank + "] " + message );
        }
    }

    public void error( String message )
    {
        System.err.println( "[" + red + "ERROR" + blank + "] " + message );
        HeadsUpLoggerManager.errorHandler.logError( message, null );
    }

    public void error( String message, Throwable t )
    {
        System.err.println( "[" + red + "ERROR" + blank + "] " + message );
        HeadsUpLoggerManager.errorHandler.logError( message, t );
    }

    public void fatalError( String message )
    {
        System.err.println( "[" + red + "FATAL" + blank + "] " + message );
        HeadsUpLoggerManager.errorHandler.logFatalError( message, null );
    }

    public void fatalError( String message, Throwable t )
    {
        System.err.println( "[" + red + "FATAL" + blank + "] " + message );
        HeadsUpLoggerManager.errorHandler.logFatalError( message, t );
    }
}