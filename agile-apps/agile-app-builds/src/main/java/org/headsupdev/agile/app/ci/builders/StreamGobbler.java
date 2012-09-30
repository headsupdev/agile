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

import java.io.*;

/**
 * A simple gobbler thread that eats a streams output dumping it to another - used for Process reading
 *
 * @author Andrew Williams
 * @since 1.0
 */
public class StreamGobbler extends Thread
{
    private Reader in;
    private Writer out;

    private boolean complete = false;

    public StreamGobbler( Reader in, Writer out )
    {
        this.in = in;
        this.out = out;
    }

    public void run()
    {
        try
        {
            BufferedReader reader = new BufferedReader( in );
            String line;
            while ( ( line = reader.readLine() ) != null )
            {
                out.write( line );
                out.write( '\n' );
            }
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }

        complete = true;
    }

    public boolean isComplete()
    {
        return complete;
    }
}
