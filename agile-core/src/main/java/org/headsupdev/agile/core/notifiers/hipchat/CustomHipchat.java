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

import com.github.hipchat.api.*;
import com.github.hipchat.api.messages.Message;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by Gordon Edwards on 24/07/2014.
 *
 * Adding methods to the Hipchat API
 * @since 2.1
 */
public class CustomHipchat
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


    public boolean sendMessageToRoom( String roomId, String message, UserId from, boolean notify, Message.Color color )
    {
        String query = String.format( HipChatConstants.ROOMS_MESSAGE_QUERY_FORMAT, HipChatConstants.JSON_FORMAT, getAuthToken() );

        StringBuilder params = new StringBuilder();

        if ( message == null || from == null )
        {
            throw new IllegalArgumentException( "Cant send message with null message or null user" );
        }
        else
        {
            params.append( "room_id=" );
            params.append( roomId );
            params.append( "&from=" );
            try
            {
                params.append( URLEncoder.encode( from.getName(), "UTF-8" ) );
                params.append( "&message=" );
                params.append( URLEncoder.encode( message, "UTF-8" ) );
            }
            catch ( UnsupportedEncodingException e )
            {
                throw new RuntimeException( e );
            }

        }

        if ( notify )
        {
            params.append( "&notify=1" );
        }

        if ( color != null )
        {
            params.append( "&color=" );
            params.append( color.name().toLowerCase() );
        }
        else
        {
            params.append( "&color=gray" );
        }

        final String paramsToSend = params.toString();

        OutputStream output = null;
        InputStream input = null;

        HttpURLConnection connection = null;
        boolean result = false;

        try
        {
            URL requestUrl = new URL( HipChatConstants.API_BASE + HipChatConstants.ROOMS_MESSAGE + query );
            connection = (HttpURLConnection) requestUrl.openConnection();
            connection.setDoOutput( true );

            connection.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded" );
            connection.setRequestProperty( "Content-Length", Integer.toString( paramsToSend.getBytes().length ) );
            connection.setRequestProperty( "Content-Language", "en-US" );

            output = new BufferedOutputStream( connection.getOutputStream() );
            IOUtils.write( paramsToSend, output );
            IOUtils.closeQuietly( output );

            input = connection.getInputStream();
            result = UtilParser.parseMessageResult( input );

        }
        catch ( MalformedURLException e )
        {
            e.printStackTrace();
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
        finally
        {
            IOUtils.closeQuietly( output );
            if ( connection != null )
            {
                connection.disconnect();
            }
        }
        return result;
    }
}
