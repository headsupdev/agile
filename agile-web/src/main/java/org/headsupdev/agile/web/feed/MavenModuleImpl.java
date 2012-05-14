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
public class MavenModuleImpl
    extends ModuleImpl
    implements MavenModule
{
    private String groupId, artifactId, version, packaging;

    public MavenModuleImpl()
    {
        super( MavenModule.class, MavenModule.URI );
    }

    public void copyFrom( Object obj )
    {
        MavenModule module = (MavenModule) obj;
        setGroupId( module.getGroupId() );
        setArtifactId( module.getArtifactId() );
        setVersion( module.getVersion() );
        setPackaging( module.getPackaging() );
    }

    public Class getInterface()
    {
        return MavenModule.class;
    }

    public String getGroupId()
    {
        return groupId;
    }

    public void setGroupId( String groupId )
    {
        this.groupId = groupId;
    }

    public String getArtifactId()
    {
        return artifactId;
    }

    public void setArtifactId( String artifactId )
    {
        this.artifactId = artifactId;
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion( String version )
    {
        this.version = version;
    }

    public String getPackaging()
    {
        return packaging;
    }

    public void setPackaging( String packaging )
    {
        this.packaging = packaging;
    }
}
