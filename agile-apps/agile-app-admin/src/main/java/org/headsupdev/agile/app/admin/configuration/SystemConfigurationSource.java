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

package org.headsupdev.agile.app.admin.configuration;

import org.headsupdev.agile.api.ConfigurationItem;
import org.headsupdev.agile.api.Manager;
import org.headsupdev.support.java.StringUtil;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

/**
 * Source of system configuration options, allow overriding to add more options when required.
 * <p/>
 * Created: 03/01/2013
 *
 * @author Andrew Williams
 * @since 2.0
 */
public class SystemConfigurationSource
    implements Serializable
{
    private static SystemConfigurationSource instance;

    public static SystemConfigurationSource getInstance()
    {
        if ( instance == null )
        {
            instance = new SystemConfigurationSource();
        }

        return instance;
    }

    private List<ConfigurationItem> config = new LinkedList<ConfigurationItem>();
    private List<ConfigurationItem> restartConfig = new LinkedList<ConfigurationItem>();

    public SystemConfigurationSource()
    {
        config.add( new SystemConfigurationItem( "baseUrl", "Public URL for this system",
                "Use this field to set the published URL for this system - used for notifications and feeds" )
        {
            public boolean test( String value )
            {
                try
                {
                    URL url = new URL( value + "project-feed.xml" );
                    // todo can we test the project-feed.xml - might be password protected
                    return true;
                }
                catch ( MalformedURLException e )
                {
                    return false;
                }
            }
        });
        config.add( new SystemConfigurationItem( "timezone.id", "UTC",  "Default Timezone",
                "The name of the default timezone - this will be used for anonymous users and as a default for all accounts" )
        {
            @Override
            public boolean test( String value )
            {
                try
                {
                    return TimeZone.getTimeZone( value ).getID().equals( value );
                }
                catch ( Exception e ) // catching errors that occur when looking up null or invalid timezone
                {
                    return false;
                }
            }
        });

        config.add( new SystemConfigurationItem( "log.errors", true, "Keep a log of any errors encountered",
                "Switching off this parameter can be useful if you encounter lots of problems with an external component " +
                        "and wish to save database space" ) );
        config.add( new SystemConfigurationItem( "userList.useNames", true, "Show real names in user lists",
                "Check this if you prefer real names to usernames in user lists" ) );

        config.add( new SystemConfigurationItem( "smtp.host", "Hostname for outgoing email",
                "This should be set to the host used to send notification emails" )
        {
            public boolean test( String value )
            {
                if ( StringUtil.isEmpty( value ) )
                {
                    return false;
                }

                try
                {
                    InetAddress address = InetAddress.getByName( value );
                    return address != null && address.isReachable( 10000 );
                }
                catch ( IOException e )
                {
                    return false;
                }
            }
        });
        config.add( new SystemConfigurationItem( "smtp.from", "Senders email address for outgoing email",
                "This should be set to the email address to send notification emails" )
        {
            public boolean test( String value )
            {
                return !StringUtil.isEmpty( value );
            }
        });
        config.add( new SystemConfigurationItem( "smtp.username", "Username for outgoing email account",
                "This should be set to the username used to send notification emails" ) );
        config.add( new SystemConfigurationItem( "smtp.password", "Password for outgoing email account",
                "This should be set to the password used to send notification emails" ) );


        restartConfig.add( new SystemConfigurationItem( "dataDir", "Data Directory",
                "This is the location that " + Manager.getStorageInstance().getGlobalConfiguration().getProductName() +
                        " stores files and runs it's builds. " )
        {
            public boolean test( String value )
            {
                File dir = new File( value );
                return dir.isDirectory() && dir.canWrite();
            }
        } );
        restartConfig.add( new SystemConfigurationItem( "org.osgi.service.http.port", 8069, "Server Port",
                "The port that the system should run on - used either in the URL or for mapping through a web server" )
        {
            public boolean test( String value )
            {
                try
                {
                    int port = Integer.parseInt( value );
                    return port > 0 && port <= 65535;
                }
                catch ( NumberFormatException e )
                {
                    return false;
                }
            }
        } );
        restartConfig.add( new SystemConfigurationItem( "heasup.db.url", "Database URL",
                "The URL of the SQL \"agile\" database " ) );
        restartConfig.add( new SystemConfigurationItem( "headsup.db.username", "Database Username",
                "The username for the SQL \"agile\" database ") );
        restartConfig.add( new SystemConfigurationItem( "headsup.db.password", "Database Password",
                "The password for the SQL \"agile\" database " ) );
    }

    public List<ConfigurationItem> getConfigurationItems()
    {
        return config;
    }

    public List<ConfigurationItem> getConfigurationItemsRequiringRestart()
    {
        return restartConfig;
    }
}
