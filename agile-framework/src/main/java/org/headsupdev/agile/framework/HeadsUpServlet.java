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

package org.headsupdev.agile.framework;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;

/**
 * TODO document me
 *
 * @author Andrew Williams
 * @since 1.0
 */
public class HeadsUpServlet
    extends HttpServlet
{
    @Override
    protected void doGet( HttpServletRequest req, HttpServletResponse res )
        throws IOException
    {
        // any request not caught by the filter is a 404 - redirect appropriately...
        String uri = req.getRequestURI();
        if ( uri == null || uri.equals( "/filenotfound" ) )
        {
            res.sendRedirect( "/filenotfound" );
        }
        else
        {
            uri = URLEncoder.encode( uri, "UTF-8" );
            res.sendRedirect( "/filenotfound/uri/" + uri + "/" );
        }
    }
}
