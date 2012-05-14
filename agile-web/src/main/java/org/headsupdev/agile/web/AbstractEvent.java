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

import org.headsupdev.agile.storage.StoredEvent;
import org.headsupdev.agile.api.MenuLink;
import org.headsupdev.agile.api.User;
import org.headsupdev.agile.api.Application;
import org.headsupdev.agile.api.Manager;

import java.util.Date;
import java.util.List;
import java.util.LinkedList;
import java.io.IOException;
import java.io.Writer;
import java.io.StringWriter;

/**
 * TODO enter description
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class AbstractEvent
    extends StoredEvent
{
    private transient List<MenuLink> links = new LinkedList<MenuLink>();

    public AbstractEvent()
    {
    }

    public AbstractEvent( String title, Date time )
    {
        super( title, null, time );
    }

    public AbstractEvent( String title, String summary, Date time )
    {
        super( title, summary, time );
    }

    public void setUser( User user )
    {
        setUsername( user.getUsername() );
    }

    public void setApplication( Application app )
    {
        setApplicationId( app.getApplicationId() );
    }

    public String getBody()
    {
        if ( getSummary() == null ) {
            return "<p></p>";
        }

        return "<p>" + getSummary() + "</p>";
    }

    public String getBodyHeader()
    {
        final Writer out = new StringWriter();

        for ( CssReference ref : getBodyCssReferences() )
        {
            String url = Manager.getStorageInstance().getGlobalConfiguration().getFullUrl(
                "/resources/" + ref.getScope().getName() + "/" + ref.getName() );

            try
            {
                out.write( "<link rel=\"stylesheet\" type=\"text/css\" href=\"" + url + "\" />" );
            }
            catch ( IOException e )
            {
                // not gonna happen, just a string writer
            }
        }

        return out.toString();
    }

    public List<CssReference> getBodyCssReferences()
    {
        return new LinkedList<CssReference>();
    }

    public static CssReference referenceForCss( Class scope, String name )
    {
        return new CssReference( scope, name );
    }

    public void addLink( MenuLink link )
    {
        links.add( link );
    }

    public void addLinks( List<MenuLink> links )
    {
        this.links.addAll( links );
    }

    public List<MenuLink> getLinks()
    {
        return links;
    }

    public static class CssReference
    {
        private Class scope;
        private String name;

        public CssReference( Class scope, String name )
        {
            this.scope = scope;
            this.name = name;
        }

        public Class getScope()
        {
            return scope;
        }

        public String getName()
        {
            return name;
        }
    }
}