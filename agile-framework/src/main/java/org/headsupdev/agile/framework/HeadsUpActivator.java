/*
 * HeadsUp Agile
 * Copyright 2009-2015 Heads Up Development.
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

import org.headsupdev.agile.api.Application;
import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.framework.webdav.RepositoryServlet;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Properties;
import java.util.TimeZone;
import javax.servlet.Filter;
import javax.servlet.Servlet;

import org.ops4j.pax.web.extender.whiteboard.ErrorPageMapping;
import org.ops4j.pax.web.extender.whiteboard.runtime.DefaultErrorPageMapping;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Extension of the default OSGi bundle activator
 */
public class HeadsUpActivator
    implements BundleActivator
{
    private WebTracker webTracker;
    private AppTracker appTracker;

    /**
     * Called whenever the OSGi framework starts our bundle
     */
    public void start( BundleContext bc )
        throws Exception
    {
        // UTC has been set by our boot script as the system timezone, set up a sensible user default
        String defaultTimeZoneId = System.getProperty( "agile.runtime.timezone" );
        if ( defaultTimeZoneId != null )
        {
            Manager.getLogger( getClass().getName() ).info( "Detected system timezone " + defaultTimeZoneId );
            TimeZone defaultTimeZone = TimeZone.getTimeZone( defaultTimeZoneId );
            Manager.getStorageInstance().getGlobalConfiguration().setDefaultTimeZone( defaultTimeZone );
        }
        Manager.getLogger( getClass().getName() ).info( "Default timezone set to " +
                Manager.getStorageInstance().getGlobalConfiguration().getDefaultTimeZone().getID() );

        // Register our webapp service implementation in the OSGi service registry
        Dictionary props = new Hashtable();
        props.put( "alias", "/" );
        props.put("servlet-name", "Wicket Servlet");
        bc.registerService( Servlet.class.getName(), new HeadsUpServlet(), props );

        props = new Hashtable();
        String[] urls = {"/*"};
        props.put("filter-name", "Wicket Filter");
        props.put("urlPatterns", urls);
        bc.registerService( Filter.class.getName(), new HeadsUpFilter(), props );

        props = new Hashtable();
        props.put( "alias", "/repository/*" );
        bc.registerService( Servlet.class.getName(), new RepositoryServlet(), props );

        props = new Hashtable();
        props.put( "alias", "/favicon.ico" );
        bc.registerService( Servlet.class.getName(), new FaviconServlet(), props );

        props = new Hashtable();
        props.put( "alias", "/robots.txt" );
        bc.registerService( Servlet.class.getName(), new RobotsServlet(), props );

        HomeApplication homeApp = new HomeApplication();
        homeApp.setContext( bc );
        props = new Properties();
        bc.registerService( Application.class.getName(), homeApp, props );

        DefaultErrorPageMapping error = new DefaultErrorPageMapping();
        error.setError( "404" );
        error.setLocation( "/filenotfound" );
        bc.registerService( ErrorPageMapping.class.getName(), error, null );

        System.out.println( "Started version " + Manager.getStorageInstance().getGlobalConfiguration().getBuildVersion() + " at " +
                Manager.getStorageInstance().getGlobalConfiguration().getBaseUrl() );
        webTracker = new WebTracker( bc );
        webTracker.open();
        appTracker = new AppTracker( bc );
        appTracker.open();
    }

    /**
     * Called whenever the OSGi framework stops our bundle
     */
    public void stop( BundleContext bc )
        throws Exception
    {
        appTracker.close();
        webTracker.close();
        System.out.println( "Shutting down" );
    }
}

