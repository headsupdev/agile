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
import org.headsupdev.agile.api.service.ScmService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

/**
 * A tracker to keep track of agile services
 *
 * @author Andrew Williams
 * @since 1.0
 */
public class ServTracker
    extends ServiceTracker
{
    private BundleContext bc;

    public ServTracker(BundleContext bundleContext)
    {
        super( bundleContext, createFilter( bundleContext ), null );
        this.bc = bundleContext;
    }

    private static Filter createFilter( final BundleContext bundleContext )
    {
        final String filter = "(objectClass=org.headsupdev.agile.api.service.ScmService)";
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
        ScmService scm = (ScmService) bc.getService( serviceReference );
        ( (DefaultManager) Manager.getInstance() ).setScmService( scm );

        return super.addingService( serviceReference );
    }

    @Override
    public void modifiedService( ServiceReference serviceReference, Object o )
    {
        ScmService scm = (ScmService) bc.getService( serviceReference );
        ( (DefaultManager) Manager.getInstance() ).setScmService( scm );

        super.modifiedService( serviceReference, o );
    }

    @Override
    public void removedService( ServiceReference serviceReference, Object o )
    {
        ( (DefaultManager) Manager.getInstance() ).setScmService( null );

        super.removedService( serviceReference, o );
    }
}
