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

import com.sun.syndication.io.ModuleParser;
import com.sun.syndication.feed.module.Module;
import org.jdom.Element;
import org.jdom.Namespace;

/**
 * TODO Document me!
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class RomeModuleParser
    implements ModuleParser
{
    public String getNamespaceUri()
    {
        return RomeModule.URI;
    }

    public Module parse( Element element ) {
        Namespace namespace = Namespace.getNamespace( RomeModule.URI );
        RomeModule romeModule = new RomeModuleImpl();
        if ( element.getNamespace().equals( namespace ) )
        {
            if ( element.getName().equals( "id" ) )
            {
                romeModule.setId( element.getTextTrim() );
            }
            if ( element.getName().equals( "type" ) )
            {
                romeModule.setType( element.getTextTrim() );
            }
            if ( element.getName().equals( "time" ) )
            {
                romeModule.setTime( Long.parseLong( element.getTextTrim() ) );
            }
        }

        return romeModule;
    }
}
