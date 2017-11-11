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

package org.headsupdev.agile.core;

import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.core.logging.HeadsUpLoggerManager;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * The activator that starts the core services
 *
 * @author Andrew Williams
 * @since 1.0
 */
public class CoreActivator
    implements BundleActivator
{
    private AppTracker tracker;
    private ServTracker services;

    /**
     * Called whenever the OSGi framework starts our bundle
     */
    public void start( BundleContext bc )
        throws Exception
    {
        HeadsUpLoggerManager.getInstance().setConfiguration( Manager.getStorageInstance().getGlobalConfiguration() );

        DefaultManager manager = new DefaultManager();
        manager.load();
        Manager.setInstance( manager );

        tracker = new AppTracker( bc );
        tracker.open();
        
        services = new ServTracker( bc );
        services.open();
    }

    /**
     * Called whenever the OSGi framework stops our bundle
     */
    public void stop( BundleContext bc )
        throws Exception
    {
        services.close();
        tracker.close();

        ( (DefaultManager) Manager.getInstance() ).unload();
    }
}
