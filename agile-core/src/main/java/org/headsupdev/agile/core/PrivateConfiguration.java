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

package org.headsupdev.agile.core;

import org.headsupdev.agile.api.HeadsUpConfiguration;

import java.util.Set;

import org.headsupdev.agile.api.PropertyTree;
import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.api.Manager;

/**
 * A class for managing configuration items that are not part of the public api.
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class PrivateConfiguration {
    public static final int STEP_NEW = 0;
    public static final int STEP_DATABASE = 1;
    public static final int STEP_POPULATE = 2;
    public static final int STEP_UPDATES = 3;
    public static final int STEP_ADMIN = 4;

    // This can up raised to add new steps - the setup app will see the new steps that need to be added and run them
    public static final int STEP_FINISHED = 5;

    private static final String KEY_PERMISSIONS = "permissions";
    private static final String KEY_NOTIFIERS = "notifiers";

    private static final String KEY_INSTALLED = "installed";
    private static final String KEY_SETUP_STEP = "setupStep";

    private static final String KEY_UPDATES_CHECK = "updates.check";
    private static final String KEY_UPDATES_BETA_CHECK = "updates.beta.check";

    public static final String UPDATE_FEED_URL = Manager.getStorageInstance().getGlobalConfiguration().getProductUrl() + "releases/feed.xml";
    public static final String BETA_UPDATE_FEED_URL = Manager.getStorageInstance().getGlobalConfiguration().getProductUrl() + "releases/beta/feed.xml";

    static
    {
        PropertyTree notifiers = getSubTree( KEY_NOTIFIERS );
        if ( notifiers == null )
        {
            addSubTree( KEY_NOTIFIERS, new PropertyTree() );
        }
        PropertyTree permissions = getSubTree( KEY_PERMISSIONS );
        if ( permissions == null )
        {
            addSubTree( KEY_PERMISSIONS, new PropertyTree() );
        }
    }

    public static boolean isInstalled()
    {
        String ret = getProperty( KEY_INSTALLED );

        try
        {
            return Boolean.parseBoolean( ret );
        }
        catch ( Exception e )
        {
            return false;
        }
    }

    public static void setInstalled( boolean installed )
    {
        setProperty( KEY_INSTALLED, String.valueOf( installed ) );
    }

    public static Set<String> getConfiguredPermissionIds()
    {
        return getSubTree( KEY_PERMISSIONS ).getPropertyNames();
    }

    public static void addConfiguredPermissionId( String permission )
    {
        setProperty( KEY_PERMISSIONS + "." + permission, "true" );
    }

    public static Set<String> getNotifierList( Project project )
    {
        return getSubTree( KEY_NOTIFIERS ).getSubTree( project.getId() ).getSubTreeIds();
    }

    public static PropertyTree getNotifierConfiguration( String id, Project project )
    {
        return getSubTree( KEY_NOTIFIERS ).getSubTree( project.getId() ).getSubTree( id );
    }

    public static void setNotifierConfiguration( String id, PropertyTree config, Project project )
    {
        PropertyTree current = getSubTree( KEY_NOTIFIERS ).getSubTree( project.getId() ).getSubTree( id );

        for ( String name : config.getPropertyNames() )
        {
            current.setProperty( name, config.getProperty( name ) );
        }
    }

    public static void addNotifierConfiguration( String id, PropertyTree config, Project project )
    {
        getSubTree( KEY_NOTIFIERS ).getSubTree( project.getId() ).addSubTree( id, config );
    }

    public static void removeNotifierConfiguration( String id, Project project )
    {
        getSubTree( KEY_NOTIFIERS ).getSubTree( project.getId() ).removeSubTree( id );
    }

    public static int getSetupStep()
    {
        String prop = getProperty( KEY_SETUP_STEP );
        if ( prop == null || prop.length() == 0 )
        {
            return 0;
        }

        try
        {
            return Integer.parseInt( prop );
        }
        catch ( NumberFormatException e )
        {
            return 0;
        }
    }

    public static void setSetupStep( int step )
    {
        setProperty( KEY_SETUP_STEP, String.valueOf( step ) );
    }

    public static boolean getUpdatesEnabled()
    {
        return Boolean.parseBoolean( getProperty( KEY_UPDATES_CHECK, "true" ) );
    }

    public static void setUpdatesEnabled( boolean enabled )
    {
        setProperty( KEY_UPDATES_CHECK, String.valueOf( enabled ) );
    }

    public static boolean getBetaUpdatesEnabled()
    {
        return Boolean.parseBoolean( getProperty( KEY_UPDATES_BETA_CHECK, "true" ) );
    }

    public static void setBetaUpdatesEnabled( boolean enabled )
    {
        setProperty( KEY_UPDATES_BETA_CHECK, String.valueOf( enabled ) );
    }

    public static String getProperty( String key )
    {
        return Manager.getStorageInstance().getGlobalConfiguration().getProperty( key );
    }

    public static String getProperty( String key, String deflt )
    {
        return Manager.getStorageInstance().getGlobalConfiguration().getProperty( key, deflt );
    }

    public static void setProperty( String key, String value )
    {
        Manager.getStorageInstance().getGlobalConfiguration().setProperty( key, value );
    }

    private static PropertyTree getSubTree( String prefix )
    {
        return Manager.getStorageInstance().getGlobalConfiguration().getSubTree( prefix );
    }

    private static void addSubTree( String prefix, PropertyTree tree )
    {
        Manager.getStorageInstance().getGlobalConfiguration().addSubTree( prefix, tree );
    }
}
