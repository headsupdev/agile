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

        resp.addHeader( "WWW-Authenticate", "Basic realm=\"HeadsUp Webdav\"" );
        resp.sendError( HttpServletResponse.SC_UNAUTHORIZED, message );

        return false;
    }
}
