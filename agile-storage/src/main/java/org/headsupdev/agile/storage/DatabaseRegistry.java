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

package org.headsupdev.agile.storage;

import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.api.logging.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;

/**
 * A simple registry to keep all the information about databases we support.
 * If you add more here you must remember to add the driver jar to support it!
 *
 * @author Andrew Williams
 * @since 1.0
 */
public class DatabaseRegistry {
    private static List<String> types = new LinkedList<String>();
    private static Map<String,String> drivers = new HashMap<String,String>();
    private static Map<String,String> dialects = new HashMap<String,String>();
    private static Map<String,String> urls = new HashMap<String,String>();

    static
    {
        addDatabase( "h2", "org.h2.Driver", "org.hibernate.dialect.H2Dialect",
            "file:${user.home}/.headsupagile/data;DB_CLOSE_ON_EXIT=FALSE" );
        addDatabase( "mysql", "com.mysql.jdbc.Driver", "org.hibernate.dialect.MySQLDialect",
            "//localhost/headsupagile?autoReconnect=true" );
    }

    private static void addDatabase( String type, String driver, String dialect, String url )
    {
        types.add( type );
        drivers.put( type, driver );
        dialects.put( type, dialect );
        urls.put( type, url );
    }

    public static List<String> getTypes()
    {
        return types;
    }

    public static String getDriver( String type )
    {
        return drivers.get( type );
    }

    public static String getDialect( String type )
    {
        return dialects.get( type );
    }

    public static String getDefaultUrl( String type )
    {
        return urls.get( type );
    }

    public static String getTypeForUrl( String url )
    {
        String[] splits = url.split( ":" );
        if ( splits.length < 2 )
        {
            return "unknown";
        }

        return splits[1];
    }

    public static boolean canConnect( String url, String username, String password )
    {
        Logger log = Manager.getLogger( DatabaseRegistry.class.getName() );

        String type = getTypeForUrl( url );
        try
        {
            Class.forName( getDriver( type ) );
            Connection conn = DriverManager.getConnection( url, username, password );

            return conn != null;
        }
        catch( Exception e )
        {
            log.error( "Failed SQL test", e );
        }

        return false;
    }
}
