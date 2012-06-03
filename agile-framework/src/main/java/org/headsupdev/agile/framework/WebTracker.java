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

import org.osgi.framework.*;
import org.osgi.util.tracker.ServiceTracker;

/**
 * A tracker to keep track of providers of the web service
 *
 * @author Andrew Williams
 * @since 2.0
 */
public class WebTracker
    extends ServiceTracker
{
    public WebTracker(BundleContext bundleContext)
    {
        super( bundleContext, createFilter( bundleContext ), null );
    }

    private static Filter createFilter( final BundleContext bundleContext )
    {
        final String filter = "(objectClass=org.headsupdev.agile.web.WebManager)";
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
        AppTracker.getBundles().add( serviceReference.getBundle() );

        return super.addingService( serviceReference );
    }

    @Override
    public void modifiedService( ServiceReference serviceReference, Object o )
    {
        AppTracker.getBundles().remove( serviceReference.getBundle() );
        AppTracker.getBundles().add( serviceReference.getBundle() );

        super.modifiedService( serviceReference, o );
    }

    @Override
    public void removedService( ServiceReference serviceReference, Object o )
    {
        AppTracker.getBundles().remove( serviceReference.getBundle() );

        super.removedService( serviceReference, o );
    }
}
