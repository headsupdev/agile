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

package org.headsupdev.agile.security;

import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.api.User;
import org.headsupdev.support.java.Base64;

import java.util.*;

/**
 * A login manager class that handles remembering users on the system
 * <p/>
 * Created: 02/02/2013
 *
 * @author Andrew Williams
 * @since 2.0
 */
public abstract class RememberedLoginManager
{
    private static final String REMEMBER_STORE_KEY = "remembermes";

    private static Map<String, Set<String>> rememberMap = null;

    public User getRememberedUser( String context )
    {
        checkRemembers();

        int pos = context.indexOf( ':' );
        if ( pos != -1 ) {
            String username = context.substring( 0, pos );
            String key = context.substring( pos + 1 );

            Set<String> remembers = rememberMap.get( username );
            if ( remembers != null && remembers.contains( key ) ) {
                return Manager.getSecurityInstance().getUserByUsername( username );
            }
        }

        return null;
    }

    public void rememberUser( User user, String random )
    {
        checkRemembers();
        Set<String> remembers = rememberMap.get(user.getUsername());
        if ( remembers == null )
        {
            remembers = new HashSet<String>();
        }
        remembers.add( random );
        rememberMap.put( user.getUsername(), remembers );
        saveRemembers();
    }

    public void forgetUser( User user )
    {
        checkRemembers();
        rememberMap.remove( user.getUsername() );
        saveRemembers();
    }

    private void checkRemembers()
    {
        if ( rememberMap != null )
        {
            return;
        }

        rememberMap = new HashMap<String,Set<String>>();
        String data = Manager.getStorageInstance().getGlobalConfiguration().getProperty( REMEMBER_STORE_KEY );
        if ( data == null ) {
            return;
        }
        data = new String( Base64.decodeBase64( data.getBytes() ) );

        String[] entries = data.split( ";" );
        for ( String entry : entries )
        {
            String[] parts = entry.split( ":" );
            if ( parts.length < 2 )
            {
                continue;
            }

            rememberMap.put( parts[0], setFromString( parts[1] ) );
        }
    }

    private void saveRemembers()
    {
        if ( rememberMap == null )
        {
            return;
        }

        StringBuilder buffer = new StringBuilder();
        for ( String user : rememberMap.keySet() )
        {
            buffer.append( user );
            buffer.append( ":" );
            buffer.append( stringFromSet( rememberMap.get( user ) ) );
            buffer.append( ";" );
        }
        String data = buffer.toString();
        if ( buffer.length() > 0 )
        {
            data = data.substring( 0, buffer.length() - 1 );
        }

        data = new String( Base64.encodeBase64( data.getBytes() ) );
        Manager.getStorageInstance().getGlobalConfiguration().setProperty( REMEMBER_STORE_KEY, data );
    }

    private String stringFromSet( Set<String> set )
    {
        StringBuilder builder = new StringBuilder();

        if ( set == null || set.size() == 0 )
        {
            return "";
        }

        for ( String string : set )
        {
            builder.append( string );
            builder.append( "," );
        }

        String ret = builder.toString();
        return ret.substring( 0, ret.length() - 1 );
    }

    private Set<String> setFromString( String input )
    {
        Set<String> ret = new HashSet<String>();
        if ( input == null || input.length() == 0 )
        {
            return ret;
        }

        String[] split = input.split( "," );
        ret.addAll( Arrays.asList( split ) );
        return ret;
    }
}
