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
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * The activator that starts the storage services, registering it with the manager
 *
 * @author Andrew Williams
 * @since 1.0
 */
public class StorageActivator
    implements BundleActivator
{
    private ApplicationTracker tracker;

    private String[] propertyKeys = new String[] { "headsup.db.url", "headsup.db.dialect", "headsup.db.driver",
            "headsup.db.username", "headsup.db.password", "org.osgi.service.http.port" };
    /**
     * Called whenever the OSGi framework starts our bundle
     */
    public void start( BundleContext bc )
        throws Exception
    {
        for ( String key : propertyKeys )
        {
            HibernateUtil.properties.put( key, bc.getProperty( key ) );
        }

        Manager.setStorageInstance( new HibernateStorage() );

        tracker = new ApplicationTracker( bc );
        tracker.open();
    }

    /**
     * Called whenever the OSGi framework stops our bundle
     */
    public void stop( BundleContext bc )
        throws Exception
    {
        tracker.close();

        if ( "org.h2.Driver".equals( bc.getProperty( "headsup.db.driver" ) ) )
        {
            try
            {
                ( (HibernateStorage) Manager.getStorageInstance() ).getHibernateSession()
                    .createSQLQuery( "SHUTDOWN" ).executeUpdate();
            }
            catch ( Exception e )
            {
                Manager.getLogger( "StorageActivator" ).info( "Unable to shut down DB - " + e.getMessage() );
            }
        }
        HibernateUtil.shutdown();
    }
}
