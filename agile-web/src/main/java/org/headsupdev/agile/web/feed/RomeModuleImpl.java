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

import com.sun.syndication.feed.module.ModuleImpl;

/**
 * TODO Document me!
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class RomeModuleImpl
    extends ModuleImpl
    implements RomeModule
{
    private String id, type;
    private long time;

    public RomeModuleImpl()
    {
        super( RomeModule.class, RomeModule.URI );
    }

    public void copyFrom( Object obj )
    {
        RomeModule module = (RomeModule) obj;
        setId( module.getId() );
        setType( module.getType() );
        setTime( module.getTime() );
    }

    public Class getInterface()
    {
        return RomeModule.class;
    }

    public String getId()
    {
        return id;
    }

    public void setId( String id )
    {
        this.id = id;
    }

    public String getType()
    {
        return type;
    }

    public void setType( String type )
    {
        this.type = type;
    }

    public void setTime( long time )
    {
        this.time = time;
    }

    public long getTime() {
        return time;
    }
}
