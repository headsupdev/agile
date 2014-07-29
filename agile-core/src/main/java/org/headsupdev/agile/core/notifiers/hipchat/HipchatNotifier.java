/*
 * HeadsUp Agile
 * Copyright 2014 Heads Up Development Ltd.
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

package org.headsupdev.agile.core.notifiers.hipchat;

import com.github.hipchat.api.HipChat;
import com.github.hipchat.api.Room;
import com.github.hipchat.api.User;
import com.github.hipchat.api.UserId;
import com.github.hipchat.api.messages.Message.Color;
import org.headsupdev.agile.api.Event;
import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.api.Notifier;
import org.headsupdev.agile.api.PropertyTree;
import org.headsupdev.agile.storage.StoredProject;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Gordon Edwards on 14/07/2014.
 */
public class HipchatNotifier
        implements Notifier
{
    private PropertyTree config = new PropertyTree();
    private CustomHipchat hipChat;
    private Room room;
    private UserId notifierUser;
    private Color WHITE;

    @Override
    public String getId()
    {
        return "hipchat";
    }

    @Override
    public String getDescription()
    {
        return "A Hipchat Notifier";
    }

    @Override
    public void eventAdded( Event event )
    {
        String projectName = StoredProject.getDefault().getAlias();
        if ( event.getProject() != null )
        {
            projectName = event.getProject().getAlias();
        }
        String message = event.getTitle() + " (" + projectName + ") - " +
                linkify( Manager.getStorageInstance().getGlobalConfiguration().getFullUrl( "/activity/event/id/" +
                        event.getId() ) );

        if ( room != null && hipChat.isValidUser( getEmail() ) )
        {
            Color color = getEventNotifyColor( event );
            CustomHipchatRoom.sendMessage( message, hipChat, room.getId(), notifierUser, color != null, color );
        }
    }

    @Override
    public PropertyTree getConfiguration()
    {
        return config;
    }

    @Override
    public void setConfiguration( PropertyTree config )
    {
        if ( config != null )
        {
            this.config = config;
            connect();
        }
    }

    @Override
    public List<String> getConfigurationKeys()
    {
        return Arrays.asList( "api key", "room name", "email", "notifier username" );
    }

    @Override
    public void start()
    {
        connect();
    }

    private void connect()
    {
        notifierUser = UserId.create( "Notifier", getNotifierUsername() );
        hipChat = new CustomHipchat( getApiKey() );
        if ( hipChat.isValidUser( getEmail() ) )
        {
            String roomName = getRoomName();
            if ( hipChat.getRoomByName( roomName ) != null )
            {
                room = hipChat.getRoomByName( roomName );
            }
            else
            {
                try
                {
                    room = hipChat.createRoom( "HeadsUpNotifications", notifierUser.getId(), false, "", false );
                }
                catch ( IOException e )
                {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void stop()
    {

    }

    public String getApiKey()
    {
        return config.getProperty( "api key" );
    }

    public String getRoomName()
    {
        return config.getProperty( "room name" );
    }

    public String getEmail()
    {
        return config.getProperty( "email" );
    }

    public String getNotifierUsername()
    {
        return config.getProperty( "notifier username" );
    }

    public Color getEventNotifyColor( Event event )
    {
        String eventTitle = event.getClass().getSimpleName();
        if ( eventTitle.equals( "BuildFailedEvent" ) )
        {
            return Color.RED;
        }
        else if ( eventTitle.contains( "BuildSucceededEvent" ) )
        {
            return Color.GREEN;
        }
        else if (eventTitle.contains( "FileChangeSetEvent" ))
        {
            return Color.YELLOW;
        }
        return null;
    }

    private String linkify( String url )
    {
        return "<a href=\"" + url + "\">"+ url + "</a> ";
    }


    private class CustomHipchat
            extends HipChat
            implements Serializable
    {
        public CustomHipchat( String authToken )
        {
            super( authToken );
        }

        public boolean isValidUser( String email )
        {
            for ( User user : listUsers() )
            {
                if ( user.getEmail().contains( email ) )
                {
                    return true;
                }
            }
            return false;
        }

        public String idOfAdmin()
        {
            for ( User user : listUsers() )
            {
                if ( user.isGroupAdmin() )
                {
                    return user.getId();
                }
            }
            return null;
        }

        public Room getRoomByName( String name )
        {

            for ( Room room : listRooms() )
            {
                if ( room.getName().equals( name ) )
                {
                    return room;
                }
            }
            return null;
        }
    }
}