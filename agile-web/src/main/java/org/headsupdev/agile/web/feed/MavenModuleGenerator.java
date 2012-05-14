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
import org.jdom.Namespace;
import org.jdom.Element;

import java.util.Set;
import java.util.HashSet;
import java.util.Collections;

/**
 * TODO Document me!
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class MavenModuleGenerator
    implements ModuleGenerator
{
    private static final Namespace NAMESPACE = Namespace.getNamespace( "maven", MavenModule.URI );
    private static final Set NAMESPACES;

    static
    {
        Set<Namespace> namespaces = new HashSet<Namespace>();
        namespaces.add( NAMESPACE );
        NAMESPACES = Collections.unmodifiableSet( namespaces );
    }

    public String getNamespaceUri()
    {
        return MavenModule.URI;
    }

    public Set getNamespaces()
    {
        return NAMESPACES;
    }

    public void generate( Module module, Element element )
    {
        MavenModule mavenModule = (MavenModule) module;
        if ( mavenModule.getGroupId() != null )
        {
            Element romeElement = new Element( "groupId", NAMESPACE );
            romeElement.setText( mavenModule.getGroupId() );
            element.addContent( romeElement );
        }
        if ( mavenModule.getArtifactId() != null )
        {
            Element romeElement = new Element( "artifactId", NAMESPACE );
            romeElement.setText( mavenModule.getArtifactId() );
            element.addContent( romeElement );
        }
        if ( mavenModule.getVersion() != null )
        {
            Element romeElement = new Element( "version", NAMESPACE );
            romeElement.setText( mavenModule.getVersion() );
            element.addContent( romeElement );
        }
        if ( mavenModule.getPackaging() != null )
        {
            Element romeElement = new Element( "packaging", NAMESPACE );
            romeElement.setText( mavenModule.getPackaging() );
            element.addContent( romeElement );
        }
    }
}
