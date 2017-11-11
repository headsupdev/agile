/*
 * HeadsUp Agile
 * Copyright 2014 Heads Up Development.
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

import com.github.hipchat.api.UserId;
import com.github.hipchat.api.messages.Message.Color;
import org.headsupdev.agile.api.Event;
import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.api.Notifier;
import org.headsupdev.agile.api.PropertyTree;
import org.headsupdev.agile.api.logging.Logger;
import org.headsupdev.agile.core.notifiers.EmailNotifier;
import org.headsupdev.agile.storage.StoredProject;
import org.headsupdev.support.java.StringUtil;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * Created by Gordon Edwards on 14/07/2014.
 *
 * A system notifier that reports activity to a Hipchat room
 * @since 2.1
 */
public class HipchatNotifier
        implements Notifier
{
    private PropertyTree config = new PropertyTree();
    private CustomHipchat hipChat;
    private String roomId;
    private UserId notifierUser;

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

        if ( roomId != null && hipChat.isValidUser( getEmail() ) )
        {
            Color color = getEventNotifyColor( event );
            hipChat.sendMessageToRoom( roomId, message, notifierUser, color != null, color );
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
                roomId = hipChat.getRoomByName( roomName ).getId();
            }
            else
            {
                Logger log = Manager.getLogger( getClass().getName() );
                if ( log != null )
                {
                    log.error( "Cannot join room. Please make sure the following room exists: " + roomName );
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
        else if ( eventTitle.contains( "FileChangeSetEvent" ) )
        {
            return Color.YELLOW;
        }
        return null;
    }

    private String linkify( String url )
    {
        return "<a href=\"" + url + "\">" + url + "</a> ";
    }
}