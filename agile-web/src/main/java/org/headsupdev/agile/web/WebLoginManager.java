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

import org.apache.wicket.Session;
import org.headsupdev.agile.api.User;
import org.headsupdev.agile.security.RememberedLoginManager;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Random;

/**
 * A login manager with many web helper methods. Extends the in memory remember login manager by the use of cookies.
 * <p/>
 * Created: 02/02/2013
 *
 * @author Andrew Williams
 * @since 2.0
 */
public class WebLoginManager
    extends RememberedLoginManager
{
    public static final String REMEMBER_COOKIE_NAME = "agile-login";

    private static WebLoginManager instance;

    public static WebLoginManager getInstance()
    {
        if ( instance == null )
        {
            instance = new WebLoginManager();
        }

        return instance;
    }

    public User getLoggedInUser( HttpServletRequest request )
    {
        User wicketUser = getWicketUser();
        if ( wicketUser != null && !wicketUser.equals( HeadsUpSession.ANONYMOUS_USER ) )
        {
            return wicketUser;
        }

        User cookieUser = getRememberedUser( request );
        if ( cookieUser != null )
        {
            setWicketUser( cookieUser );
            return cookieUser;
        }

        return null;
    }

    private Cookie getCookie( Cookie[] cookies )
    {
        for ( Cookie cookie : cookies )
        {
            if ( cookie.getName().equals( REMEMBER_COOKIE_NAME ) )
            {
                return cookie;
            }
        }

        return null;
    }

    public User getRememberedUser( HttpServletRequest request )
    {
        Cookie cookie = getCookie( request.getCookies() );
        if ( cookie == null )
        {
            return null;
        }

        return getRememberedUser( cookie.getValue() );
    }

    public void logUserIn( User user, boolean remember, HttpServletResponse response )
    {
        setWicketUser( user );
        if ( remember )
        {
            String random = getRandomValue();
            setRememberCookie( user.getUsername(), random, response );
            rememberUser( user, random );
        }
        else
        {
            removeRememberCookie( response );
            forgetUser( user );
        }
    }

    protected String getRandomValue()
    {
        return String.valueOf( new Random( System.currentTimeMillis() ).nextInt() );
    }

    public void logUserOut( User user, HttpServletResponse response )
    {
        if ( user != null )
        {
            removeRememberCookie( response );
            forgetUser( user );
        }

        setWicketUser( null );
    }

    protected User getWicketUser()
    {
        try
        {
            return ( (HeadsUpSession) Session.get() ).getUser();
        }
        catch ( java.lang.IllegalStateException e )
        {
            return null;
        }
    }

    protected void setWicketUser( User user )
    {
        try
        {
            ( (HeadsUpSession) Session.get() ).setUser(user);
        }
        catch ( java.lang.IllegalStateException e )
        {
            // nothing to do
        }
    }

    public void setRememberCookie( String username, String random, HttpServletResponse response )
    {
        Cookie cookie = createCookie( username, random, 60 * 60 * 24 * 32 );
        response.addCookie( cookie );
    }

    public void removeRememberCookie( HttpServletResponse response )
    {
        Cookie cookie = createCookie( "", "", 0 );
        response.addCookie( cookie );
    }

    protected Cookie createCookie( String username, String random, int expires )
    {
        Cookie cookie = new Cookie( REMEMBER_COOKIE_NAME, username + ":" + random );
        cookie.setMaxAge( expires );

        return cookie;
    }
}
