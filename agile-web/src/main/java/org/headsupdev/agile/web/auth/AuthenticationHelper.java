/*
 * HeadsUp Agile
 * Copyright 2013 Heads Up Development Ltd.
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

package org.headsupdev.agile.web.auth;

import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.api.User;
import org.headsupdev.agile.api.util.HashUtil;
import org.headsupdev.support.java.Base64;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * A helper class to handle HTTP authentication.
 * <p/>
 * Created: 10/02/2013
 *
 * @author Andrew Williams
 * @since 2.0
 */
public class AuthenticationHelper
{
    public static boolean requestAuthentication( HttpServletRequest req, HttpServletResponse resp )
            throws IOException
    {
        String header = req.getHeader( "Authorization" );
        String message = "You must provide a username and password to access this resource.";
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

            User user = Manager.getSecurityInstance().getUserByUsername( username );
            if ( user != null )
            {
                if ( !user.getPassword().equals( encodedPass ) )
                {
                    message = "Invalid username or password";
                }
                else if ( !user.canLogin() )
                {
                    message = "Account is not currently active";
                }
                else
                {
                    WebLoginManager.getInstance().logUserIn( user, false, req, resp );
                    return true;
                }
            }
            else
            {
                message = "Invalid username or password";
            }
        }

        String productName = Manager.getStorageInstance().getGlobalConfiguration().getProductName();
        resp.addHeader( "WWW-Authenticate", "Basic realm=\"" + productName + "\"" );
        resp.sendError( HttpServletResponse.SC_UNAUTHORIZED, message );

        return false;
    }
}
