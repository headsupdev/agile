/*
 * HeadsUp Agile
 * Copyright 2015 Heads Up Development.
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

package org.headsupdev.agile.framework;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * render a robots.txt file
 *
 * @author Andrew Williams
 * @since 2.1
 */
public class RobotsServlet
    extends HttpServlet
{
    @Override
    protected void doGet( HttpServletRequest req, HttpServletResponse res )
        throws IOException
    {
        InputStream in = getClass().getClassLoader().getResourceAsStream(
            "/org/headsupdev/agile/web/robots.txt" );
        OutputStream out = res.getOutputStream();

        res.setContentType( "text/plain" );
        try
        {
            int read;
            byte[] buff = new byte[1024];
            while ( ( read = in.read( buff ) ) != -1 )
            {
                out.write( buff, 0, read );
            }
        }
        finally
        {
            if ( in != null )
            {
                in.close();
            }
            if ( out != null )
            {
                out.close();
            }
        }
    }
}
