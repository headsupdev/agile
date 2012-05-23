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

package org.headsupdev.agile.web;

import org.headsupdev.agile.api.util.HashUtil;
import org.headsupdev.support.java.Base64;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.protocol.http.servlet.AbortWithWebErrorCodeException;
import org.apache.wicket.Session;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.headsupdev.agile.api.*;

/**
 * Some utilities for general web work such as authentication and authorization.
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class WebUtil
{
    public static void authenticate( WebRequest request, WebResponse response, Permission permission,
            Project project )
    {
        authenticate( request, response, permission, project, false );
    }

    public static void authenticate( HttpServletRequest request, HttpServletResponse response, Permission permission,
            Project project )
    {
        authenticate( request, response, permission, project, false );
    }

    public static void authenticate( WebRequest request, WebResponse response, Permission permission,
            Project project, boolean silent )
    {
        authenticate( request.getHttpServletRequest(), response.getHttpServletResponse(), permission, project,
                silent );
    }

    public static void authenticate( HttpServletRequest request, HttpServletResponse response, Permission permission,
            Project project, boolean silent )
    {
        boolean fail = true;

        Role anon = Manager.getSecurityInstance().getRoleById( "anonymous" );
        if ( !anon.getPermissions().contains( permission.getId() ) )
        {
            User user = null;

            String header = request.getHeader( "Authorization" );
            String message = "You must provide a username and password to access this feed.";
            if ( ( header != null ) && header.startsWith( "Basic " ) )
            {
                String base64Token = header.substring( 6 );
                String token = new String( Base64.decodeBase64( base64Token.getBytes() ) );

                String username = "";
                String password = "";
                int delim = token.indexOf( ':' );

                if ( delim != ( -1 ) )
                {
                    username = token.substring( 0, delim );
                    password = token.substring( delim + 1 );
                }

                String encodedPass = HashUtil.getMD5Hex( password );

                user = Manager.getSecurityInstance().getUserByUsername( username );
                if ( user != null )
                {
                    if ( !user.getPassword().equals( encodedPass ) )
                    {
                        user = null;
                        message = "Invalid username or password";
                    }
                    else if ( !user.canLogin() )
                    {
                        user = null;
                        message = "Account is not currently active";
                    }
                }
                else
                {
                    message = "Invalid username or password";
                }
            } else {
                
                Session session = Session.get();
                if ( session != null )
                {
                    user = ((HeadsUpSession) session).getUser();

                    if ( user.equals( HeadsUpSession.ANONYMOUS_USER ) )
                    {
                        user = null;
                    }
                }
            }

            if ( user != null )
            {
                fail = !Manager.getSecurityInstance().userHasPermission( user, permission, project );
            }

            if ( fail )
            {
                if ( !silent )
                {
                    response.addHeader( "WWW-Authenticate", "Basic realm=\"" + Manager.getStorageInstance().getGlobalConfiguration().getProductName() +
                        "\"" );
                }
                throw new AbortWithWebErrorCodeException( 401, message );
            }
        }
    }
}
