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

package org.headsupdev.agile.core.notifiers;

//import com.github.hipchat.api.HipChat;
//import com.github.hipchat.api.Room;

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
import org.headsupdev.support.java.Base64;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Gordon Edwards on 14/07/2014.
 */
public class HipchatNotifier
        implements Notifier
{
    private PropertyTree config = new PropertyTree();
    private HipChat hipChat;
    private Room room;
    private UserId headsUp;

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
                Manager.getStorageInstance().getGlobalConfiguration().getFullUrl( "/activity/event/id/" +
                        event.getId() );
        getEventNotifyColor( event );
        room.sendMessage( message, headsUp, true, getEventNotifyColor( event ) );
    }

    @Override
    public PropertyTree getConfiguration()
    {
        return config;
    }

    @Override
    public void setConfiguration( PropertyTree config )
    {
        this.config = config;
    }

    @Override
    public List<String> getConfigurationKeys()
    {
        return Arrays.asList( "api key", "room name", "username", "password" );
    }

    @Override
    public void start()
    {
        connect();
    }

    private void connect()
    {
        hipChat = new HipChat( getApiKey() );
        if ( isValidHipchatUser() )
        {
            long time = System.currentTimeMillis();
            headsUp = User.create( "HeadsUp", "HeadsUp Notifications" );
            room = Room.create( getRoomName(), hipChat, getRoomName(), "HeadsUp Notifications", time, time, "notifier",
                    false, false, "", Arrays.asList( headsUp ), "" );
        }
    }

    private boolean isValidHipchatUser()
    {
        for ( User user : hipChat.listUsers() )
        {
            if ( user.getName().equals( getUsername() ) && user.getPassword().equals( getPassword() ) )
            {
                return true;
            }
        }
        return false;
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

    public Color getEventNotifyColor(Event event)
    {
        if ( event.getTitle().toLowerCase().contains( "build" ))
        {
            if ( event.getTitle().toLowerCase().contains( "failed" ) )
            {
                return Color.RED;
            }
            if ( event.getTitle().toLowerCase().contains( "succeeded" ) )
            {
                return Color.GREEN;
            }
        }
        return Color.YELLOW;
    }
}
