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

package org.headsupdev.agile.web;

import org.headsupdev.agile.api.HeadsUpConfiguration;
import org.apache.wicket.markup.html.image.resource.DynamicImageResource;
import org.apache.wicket.util.time.Time;
import org.apache.wicket.util.value.ValueMap;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * TODO Document me!
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public abstract class CachedImageResource
        extends DynamicImageResource //PlexusImageResource
        implements Serializable
{
    private transient Set<CachedResource> resources;

    @Override
    protected ResourceState getResourceState()
    {
        CachedResource res = getCachedResource();
        if ( res == null )
        {
            // never modified
            setLastModifiedTime( Time.milliseconds( 0 ) );
        }
        else
        {
            setLastModifiedTime( Time.milliseconds( res.getCached().getTime() ) );
        }

        return super.getResourceState();
    }

    private synchronized CachedResource getCachedResource()
    {
        Set<CachedResource> remove = new HashSet<CachedResource>();
        if ( resources == null )
        {
            resources = new HashSet<CachedResource>();
        }

        try
        {
            for ( CachedResource resource : resources )
            {
                if ( hasResourceExpired( resource ) )
                {
                    remove.add( resource );
                    continue;
                }

                ValueMap params = resource.getParams();
                boolean somenull = ( params == null || getParameters() == null );
                boolean matches = ( params == null && getParameters() == null );

                if ( !matches && !somenull )
                {
                    if ( params.size() == getParameters().size() )
                    {
                        matches = true;
                        for ( Object key : params.keySet() )
                        {
                            if ( !params.get( key ).equals( getParameters().get( key ) ) )
                            {
                                matches = false;
                                break;
                            }
                        }
                    }
                    else
                    {
                        matches = false;
                    }
                }

                if ( matches )
                {
                    return resource;
                }
            }
        }
        finally
        {
            resources.removeAll( remove );
        }

        return null;
    }

    synchronized protected byte[] getImageData()
    {
        CachedResource cached = getCachedResource();
        if ( cached != null )
        {
            return cached.getData();
        }

        BufferedImage img = new BufferedImage( getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB );
        Graphics g = img.createGraphics();

        renderImage( g );

        byte[] data = toImageData( img );
        resources.add( new CachedResource( getParameters(), new Date(), data ) );

        return data;
    }

    public boolean hasResourceExpired( CachedResource resource )
    {
        long expires = resource.getCached().getTime() + getExpireTimeout();
        return System.currentTimeMillis() > expires;
    }

    public long getExpireTimeout()
    {
        if ( HeadsUpConfiguration.isDebug() )
        {
            return 1;
        }
        return 1000 * 60;
    }

    /**
     * Get the width of this image resource, used by the default implementation of getImageData().
     *
     * @return the width of the image to render and pass back to the client
     */
    protected abstract int getWidth();

    /**
     * Get the height of this image resource, used by the default implementation of getImageData().
     *
     * @return the height of the image to render and pass back to the client
     */
    protected abstract int getHeight();

    /**
     * An optional method that is used by the default implementation to draw the dynamic image resource.
     *
     * @param g the graphics object used to paint to the image resource
     */
    protected void renderImage( Graphics g )
    {
    }
}

class CachedResource
{
    private ValueMap params;
    private Date cached;
    private byte[] data;

    CachedResource( ValueMap params, Date cached, byte[] data )
    {
        this.params = params;
        this.cached = cached;
        this.data = data;
    }

    public ValueMap getParams()
    {
        return params;
    }

    public Date getCached()
    {
        return cached;
    }

    public byte[] getData()
    {
        return data;
    }
}