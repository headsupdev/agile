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
import org.headsupdev.agile.api.Manager;

import java.io.RandomAccessFile;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * A simple error log to write unhandled exceptions out for later inspection.
 *
 * @author Andrew Williams
 * @since 1.0
 */
public class ErrorHandler {
    private java.io.File log;

    public ErrorHandler( HeadsUpConfiguration config )
    {
        log = new java.io.File( config.getDataDir(), "error.xml" );
    }

    public void logError( String message, Throwable t )
    {
        log( "error", message, t );
    }

    public void logFatalError( String message, Throwable t )
    {
        log( "fatal", message, t );
    }

    public synchronized void log( String level, String message, Throwable t )
    {
        if ( !Manager.getStorageInstance().getGlobalConfiguration().getLogErrors() )
        {
            return;
        }

        try
        {
            boolean exists = log.exists();
            RandomAccessFile writer = new RandomAccessFile( log, "rw" );

            if ( exists )
            {
                writer.seek( log.length() - 9 );
            }
            else
            {
                writer.writeBytes( "<errors>\n" );
            }

            writer.writeBytes( "  <error>\n" );
            writer.writeBytes( "    <level>" );
            writer.writeBytes( level );
            writer.writeBytes( "</level>\n" );
            writer.writeBytes( "    <message><![CDATA[" );
            writer.writeBytes( message );
            writer.writeBytes( "]]></message>\n" );
            writer.writeBytes( "    <time>" );
            writer.writeBytes( String.valueOf( System.currentTimeMillis() ) );
            writer.writeBytes( "</time>\n" );
            writer.writeBytes( "    <version>" );
            writer.writeBytes( Manager.getStorageInstance().getGlobalConfiguration().getBuildVersion() );
            writer.writeBytes( "</version>\n" );

            if ( t == null )
            {
                writer.writeBytes( "    <stack></stack>\n" );
            }
            else
            {
                writer.writeBytes( "    <stack><![CDATA[" );

                StringWriter sw = new StringWriter();
                PrintWriter out = new PrintWriter( sw );
                t.printStackTrace( out );
                byte[] bytes = sw.toString().getBytes();
                writer.write( bytes );

                writer.writeBytes( "]]></stack>\n" );
            }
            writer.writeBytes( "  </error>\n" );

            writer.writeBytes( "</errors>" );
            writer.close();
        }
        catch ( IOException e2 )
        {
            System.err.print( "Unable to log exception: " );
            if ( t == null )
            {
                System.err.println( "(no stack trace)" );
            }
            else
            {
                t.printStackTrace();
            }
            System.err.print( "Cause: " );
            e2.printStackTrace();
        }
    }
}
