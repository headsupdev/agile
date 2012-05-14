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

package org.headsupdev.agile.web.feed;

import com.sun.syndication.io.ModuleGenerator;
import com.sun.syndication.feed.module.Module;

import java.util.Set;
import java.util.Collections;
import java.util.HashSet;

import org.jdom.Element;
import org.jdom.Namespace;

/**
 * TODO Document me!
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class RomeModuleGenerator
    implements ModuleGenerator
{
    private static final Namespace NAMESPACE = Namespace.getNamespace( "hud", RomeModule.URI );
    private static final Set NAMESPACES;

    static
    {
        Set<Namespace> namespaces = new HashSet<Namespace>();
        namespaces.add( NAMESPACE );
        NAMESPACES = Collections.unmodifiableSet( namespaces );
    }

    public String getNamespaceUri()
    {
        return RomeModule.URI;
    }

    public Set getNamespaces()
    {
        return NAMESPACES;
    }

    public void generate( Module module, Element element )
    {
        RomeModule romeModule = (RomeModule) module;
        if ( romeModule.getId() != null )
        {
            Element romeElement = new Element( "id", NAMESPACE );
            romeElement.setText( romeModule.getId() );
            element.addContent( romeElement );
        }
        if ( romeModule.getType() != null )
        {
            Element romeElement = new Element( "type", NAMESPACE );
            romeElement.setText( romeModule.getType() );
            element.addContent( romeElement );
        }
        if ( romeModule.getTime() != 0 )
        {
            Element romeElement = new Element( "time", NAMESPACE );
            romeElement.setText( String.valueOf( romeModule.getTime() ) );
            element.addContent( romeElement );
        }
    }
}
