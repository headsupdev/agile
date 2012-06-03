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

import org.apache.wicket.application.IClassResolver;
import org.osgi.framework.Bundle;

import java.net.URL;
import java.util.Iterator;

/**
 * A naive class resolver that looks at all bundles to resolve an applications internal classes
 * TODO rather than polling all bundles we need to establish which we are using for this app
 *
 * @author Andrew Williams
 * @since 1.0
 */
public class HeadsUpClassResolver
    implements IClassResolver
{
    IClassResolver parent;

    public HeadsUpClassResolver( IClassResolver resolver )
    {
        this.parent = resolver;
    }

    public Class resolveClass( String s )
        throws ClassNotFoundException
    {
        try
        {
            return parent.resolveClass( s );
        }
        catch ( ClassNotFoundException e )
        {
            for ( Bundle bundle : AppTracker.getBundles() )
            {
                try {
                    return bundle.loadClass( s );
                } catch ( ClassNotFoundException e2 ) {
                    // try next bundle
                }
            }
            throw e;
        }
    }

    public Iterator<URL> getResources( String s )
    {
        return parent.getResources( s );
    }
}
