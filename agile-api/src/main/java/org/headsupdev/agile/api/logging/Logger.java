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

package org.headsupdev.agile.api.logging;

import java.io.Serializable;

/**
 * A simple logger interface that advertised loggers will implement
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public interface Logger
    extends Serializable
{
    void debug( String error );
    void info( String error );
    void warn( String error );

    void error( String error );
    void error( String error, Throwable t );

    void fatalError( String fatal );
    void fatalError( String fatal, Throwable t );
}
