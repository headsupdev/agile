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

package org.headsupdev.agile.core.notifiers;

import org.headsupdev.agile.api.*;
import org.headsupdev.agile.storage.StoredProject;

import org.headsupdev.irc.*;
import org.headsupdev.irc.impl.DefaultIRCServiceManager;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An IRC notifier
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class IRCNotifier
    implements Notifier
{
    private static IRCServiceManager manager;

    // provide a static source for the non-serializable irc connections
    private static final Map<IRCNotifier, IRCConnection> connections = new HashMap<IRCNotifier, IRCConnection>();

    static
    {
        manager = IRCServiceManager.getInstance();
        ( (DefaultIRCServiceManager) manager ).addListener( new AbstractIRCListener()
        {
            public void onDisconnected( IRCConnection connection )
            {
                IRCNotifier notifier = null;
                System.out.println( "disconnected " + connection );
                for ( IRCNotifier n : connections.keySet() )
                {
                    if ( connections.get( n ).equals( connection ) )
                    {
                        notifier = n;
                        break;
                    }
                }

                if ( notifier == null )
                {
                    return;
                }

                System.out.println( "notifier reconnecting " + notifier );
                notifier.connected = false;
                notifier.connect();
            }
        } );
    }

    private boolean connected = false;

    private PropertyTree config = new PropertyTree();

    public String getId()
    {
        return "irc";
    }

    public String getDescription()
    {
        return "An IRC notifier";
    }

    public void start()
    {
        connect();
    }

    public void connect()
    {
        if ( connected )
        {
            return;
        }
        manager.setServiceName( Manager.getStorageInstance().getGlobalConfiguration().getProductName() + " IRC Bot");
        manager.setDescription( Manager.getStorageInstance().getGlobalConfiguration().getProductUrl() );

        try
        {
            IRCConnection conn = manager.connect( getHost(), getNick(), getPassword(), getUsername(), getName() );
            connections.put( this, conn );
            conn.join( getChannel() );

            connected = true;
        }
        catch ( IOException e )
        {
            Manager.getLogger( getClass().getName() ).error( "Error connecting to IRC server " + getHost(), e );
        }
    }

    public void stop()
    {
        disconnect();
    }

    public void disconnect()
    {
        try
        {
            if ( connected )
            {
                connected = false;

                IRCConnection conn = connections.get( this );
                if ( conn != null )
                {
                    conn.disconnect( "Notifier going offline" );
                    connections.remove( this );
                }
            }
        }
        catch ( IOException e )
        {
            Manager.getLogger( getClass().getName() ).error( "Error disconnecting from IRC server", e );
        }
    }

    public void eventAdded( Event event )
    {
        connect();

        String projectName = StoredProject.getDefault().getAlias();
        if ( event.getProject() != null )
        {
            projectName = event.getProject().getAlias();
        }

        IRCConnection conn = connections.get( this );
        if ( conn != null )
        {
            conn.sendMessage( getChannel(), event.getTitle() + " (" + projectName + ") - " +
                    Manager.getStorageInstance().getGlobalConfiguration().getFullUrl( "/activity/event/id/" +
                            event.getId() ) );
        }
    }

    public PropertyTree getConfiguration()
    {
        return config;
    }

    public void setConfiguration( PropertyTree config )
    {
        boolean reconnect = connected;
        disconnect();

        if ( config != null )
        {
            this.config = config;

            if ( reconnect )
            {
                connect();
            }
        }
    }

    public List<String> getConfigurationKeys() {
        return Arrays.asList( "host", "username", "password", "nick", "name", "channel" );
    }

    public String getHost()
    {
        return config.getProperty( "host" );
    }

    public String getUsername()
    {
        return config.getProperty( "username" );
    }

    public String getPassword()
    {
        return config.getProperty( "password" );
    }

    public String getNick()
    {
        return config.getProperty( "nick" );
    }

    public String getName()
    {
        return config.getProperty( "name" );
    }

    public String getChannel()
    {
        return config.getProperty( "channel" );
    }
}
