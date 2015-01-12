/*
 * HeadsUp Agile
 * Copyright 2009-2015 Heads Up Development Ltd.
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

import org.apache.wicket.protocol.http.WicketFilter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

/**
 * The main filter for our "webapp" passes control back to RepositoryServlet or FaviconServlet where appropriate
 * and serves everything else as the wicket filter would have but with a custom FilterConfig.
 *
 * Also a place to set up some missing (from jetty?) content type responses...
 *
 * @author Andrew Williams
 * @since 1.0
 */
public class HeadsUpFilter
    extends WicketFilter
{
    @Override
    public void init( FilterConfig filterConfig )
        throws ServletException
    {
        super.init( new ApplicationNameFilterConfig( filterConfig ) );
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
        throws IOException, ServletException
    {
        String path = ( (HttpServletRequest) servletRequest ).getRequestURI();
        int cookiePos = path.indexOf( ";jsessionid" );
        if ( cookiePos > -1 ) {
            path = path.substring( 0, cookiePos );
        }

        if ( path.startsWith( "/repository" ) || path.equals( "/favicon.ico" ) || path.equals( "/robots.txt" ) )
        {
            // bypass the wicket (which we extend) and go straight to the webdav
            filterChain.doFilter(servletRequest, servletResponse);
        }
        else if ( path.startsWith( "/resources/" ) && path.endsWith( ".css" ) )
        {
            // A little hack to compensate for the fact that Jetty does not set the right Content-Type for .css files

            servletResponse.setContentType( "text/css" );
            super.doFilter( servletRequest, servletResponse, filterChain );
        }
        else
        {
            super.doFilter( servletRequest, servletResponse, filterChain );
        }
    }

    @Override
    public FilterConfig getFilterConfig()
    {
        return new ApplicationNameFilterConfig( super.getFilterConfig() );
    }
}

class ApplicationNameFilterConfig
    implements FilterConfig
{
    private FilterConfig parent;

    public ApplicationNameFilterConfig( FilterConfig config )
    {
        this.parent = config;
    }

    public String getInitParameter( String key )
    {
        if ( key.equals( "applicationClassName" ) )
        {
            return "org.headsupdev.agile.framework.HeadsUpWebApplication";
        }

        return parent.getInitParameter( key );
    }

    public Enumeration getInitParameterNames()
    {
        Vector<String> names = new Vector<String>();

        Enumeration nameEnum = parent.getInitParameterNames();
        while ( nameEnum.hasMoreElements() )
        {
            names.add( (String) nameEnum.nextElement() );
        }

        names.add( "applicationClassName" );
        return names.elements();
    }

    public String getFilterName()
    {
        return parent.getFilterName();
    }

    public ServletContext getServletContext()
    {
        return parent.getServletContext();
    }
}
