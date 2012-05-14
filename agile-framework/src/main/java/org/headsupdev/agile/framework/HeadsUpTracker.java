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

package org.headsupdev.agile.framework;

import org.osgi.util.tracker.ServiceTracker;
import org.osgi.framework.*;

import java.util.Set;
import java.util.HashSet;

import org.headsupdev.agile.api.Application;
import org.headsupdev.agile.api.Manager;

/**
 * A tracker to keep track of application services
 *
 * @author Andrew Williams
 * @since 1.0
 */
public class HeadsUpTracker
    extends ServiceTracker
{
    private static Set<Bundle> bundles = new HashSet<Bundle>();
    private BundleContext bc;

    public HeadsUpTracker( BundleContext bundleContext )
    {
        super( bundleContext, createFilter( bundleContext ), null );
        this.bc = bundleContext;

        bundles.add( bc.getBundle() );
    }

    private static Filter createFilter( final BundleContext bundleContext )
    {
        final String filter = "(objectClass=org.headsupdev.agile.api.Application)";
        try
        {
            return bundleContext.createFilter( filter );
        }
        catch( InvalidSyntaxException e )
        {
            throw new IllegalArgumentException( "Unexpected InvalidSyntaxException: " + e.getMessage() );
        }
    }

    @Override
    public Object addingService( ServiceReference serviceReference )
    {
        Application app = (Application) bc.getService( serviceReference );
        Manager.getLogger( getClass().getName() ).info( "Loading " + app.getApplicationId() );
        bundles.add( serviceReference.getBundle() );

        HeadsUpWebApplication.addApplication( app );
        return super.addingService( serviceReference );
    }

    @Override
    public void modifiedService( ServiceReference serviceReference, Object o )
    {
        Manager.getLogger( getClass().getName() ).info( "Reloading " + ( (Application) o ).getApplicationId() );
        HeadsUpWebApplication.removeApplication( (Application) o );
        HeadsUpWebApplication.addApplication( (Application) o );
        super.modifiedService( serviceReference, o );
    }

    @Override
    public void removedService( ServiceReference serviceReference, Object o )
    {
        Manager.getLogger( getClass().getName() ).info( "Unloading " + ( (Application) o ).getApplicationId() );
        bundles.remove( serviceReference.getBundle() );

        HeadsUpWebApplication.removeApplication( (Application) o );
        super.removedService( serviceReference, o );
    }

    public static Set<Bundle> getBundles()
    {
        return bundles;
    }
}
