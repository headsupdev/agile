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

package org.headsupdev.agile.app.ci.builders;

import org.apache.maven.shared.invoker.InvocationOutputHandler;

import java.io.Writer;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.headsupdev.agile.api.Manager;

/**
 * A consumer to send the maven output to a file.
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class FileOutputHandler
    implements InvocationOutputHandler
{
    Writer writer;

    public FileOutputHandler( File out )
        throws IOException
    {
        writer = new FileWriter( out );
    }

    public void consumeLine( String line )
    {
        try {
            writer.write( line );
            writer.write( '\n' );
        } catch (IOException e) {
            Manager.getLogger( getClass().getName() ).error( "Error consuming build output", e );
        }
    }

    public Writer getWriter()
    {
        return writer;
    }

    public void close()
    {
        try
        {
            writer.close();
        }
        catch ( IOException e )
        {
            //ignore
        }
    }
}
