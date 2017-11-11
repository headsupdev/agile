/*
 * HeadsUp Agile
 * Copyright 2009-2014 Heads Up Development.
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

package org.headsupdev.agile.core.notifiers;

import org.headsupdev.agile.api.*;
import org.headsupdev.agile.storage.StoredProject;

import java.util.Collection;
import java.util.HashSet;
import java.util.Arrays;
import java.io.*;
import java.net.URLEncoder;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import org.headsupdev.support.java.Base64;
import org.headsupdev.support.java.IOUtil;
import org.headsupdev.support.java.StringUtil;

/**
 * Twitter notifier
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class TwitterNotifier
    implements Notifier
{
    private PropertyTree config = new PropertyTree();

    public String getId()
    {
        return "twitter";
    }

    public String getDescription()
    {
        return "A Twitter Notifier";
    }

    public void eventAdded( Event event )
    {
        Project project = event.getProject();
        if ( project == null )
        {
            project = StoredProject.getDefault();
        }
        String text = "[" + Manager.getStorageInstance().getGlobalConfiguration().getProductName() + "] " + event.getTitle() + " (" + project.getAlias() +
            ")";

        OutputStreamWriter out = null;
        BufferedReader in = null;
        try
        {
            String data = "status=" + URLEncoder.encode( text, "UTF-8" );
            URL set = new URL( "http://twitter.com/statuses/update.xml" );
            URLConnection conn = set.openConnection();

            // handle authentication
            String auth = "Basic " + new String( Base64.encodeBase64( ( getUsername() + ":" + getPassword() ).getBytes() ) );
            conn.setDoInput( true );
            conn.setDoOutput( true );
            conn.setRequestProperty( "Authorization", auth );
            conn.setAllowUserInteraction( false );

            // write POST data
            out = new OutputStreamWriter( conn.getOutputStream() );
            out.write( data );
            out.flush();

            // Get the response - this causes the request to actually be sent to the server
            in = new BufferedReader( new InputStreamReader( conn.getInputStream() ) );
            while ( in.readLine() != null )
            {
                // ignore
            }
        }
        catch ( IOException e )
        {
            Manager.getLogger( getClass().getName() ).error( "Error sending twitter notification", e );
        }
        finally
        {
            if ( in != null )
            {
                IOUtil.close( in );
            }
            if ( out != null )
            {
                IOUtil.close( out );
            }
        }
    }

    public PropertyTree getConfiguration()
    {
        return config;
    }

    public void setConfiguration( PropertyTree config )
    {
        this.config = config;
    }

    @Override
    public List<String> getConfigurationKeys()
    {
        return Arrays.asList( "username", "password" );
    }

    @Override
    public Collection<String> getIgnoredEvents()
    {
        String eventIds = getConfiguration().getProperty( "ignore-events" );
        if ( StringUtil.isEmpty( eventIds ) )
        {
            return new HashSet<String>();
        }

        return Arrays.asList( eventIds.split( EmailNotifier.IGNORE_EVENTS_JOIN ) );
    }

    public void setIgnoredEvents( Collection<String> eventIds )
    {
        String ignoreList = StringUtil.join( eventIds, EmailNotifier.IGNORE_EVENTS_JOIN );
        getConfiguration().setProperty( EmailNotifier.IGNORE_EVENTS_KEY, ignoreList );
    }

    public void start()
    {
    }

    public void stop()
    {
    }

    public String getUsername()
    {
        return config.getProperty( "username" );
    }

    public String getPassword()
    {
        String password = config.getProperty( "password" );
        if ( password == null )
        {
            return null;
        }

        return new String( Base64.decodeBase64( password.getBytes() ) );
    }
}
