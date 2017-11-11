/*
 * HeadsUp Agile
 * Copyright 2009-2012 Heads Up Development.
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

package org.headsupdev.agile.storage.files;

import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.api.SearchResult;
import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.storage.hibernate.NameProjectBridge;
import org.headsupdev.agile.storage.hibernate.NameProjectId;
import org.headsupdev.agile.api.mime.Mime;

import javax.persistence.*;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.hibernate.search.annotations.*;

/**
 * A simple file entry to hold information about a file within a project
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@Entity
@Table( name = "Files" )
@Indexed( index = "Files" )
public class File
    implements Serializable, SearchResult
{
    @EmbeddedId
    @DocumentId
    @FieldBridge( impl = NameProjectBridge.class )
    @Field
    NameProjectId name;

    @Field
    String revision;

    public File()
    {
    }

    public File( String path, String revision, Project project )
    {
        this.name = new NameProjectId( path, project );
        this.revision = revision;
    }

    public Project getProject()
    {
        return name.getProject();
    }

    public String getName()
    {
        return name.getName();
    }

    public String getRevision()
    {
        return revision;
    }

    public String getIconPath() {
        java.io.File path = Manager.getStorageInstance().getWorkingDirectory( getProject() );
        java.io.File file = new java.io.File( path, getName() );

        Mime mime;
        if ( file.isDirectory() )
        {
            mime = Mime.get( "folder" );
        }
        else
        {
            mime = Mime.get( getName() );
        }

        return "api/mime/" + mime.getIconName();
    }

    public String getLink() {
        String path = getName();
        try
        {
            path = URLEncoder.encode( getName().replace( java.io.File.separatorChar, ':' ), "UTF-8" );
        }
        catch ( UnsupportedEncodingException e )
        {
            // not going to happen
        }

        java.io.File dir = Manager.getStorageInstance().getWorkingDirectory( getProject() );
        java.io.File file = new java.io.File( dir, getName() );
        if ( file.isDirectory() )
        {
            return "/" + getProject().getId() + "/files/path/" + path;
        }
        else
        {
            return "/" + getProject().getId() + "/files/view/path/" + path;
        }
    }

    @Override
    public String getAppId()
    {
        return "files";
    }

    public String toString()
    {
        return getName();
    }

    public boolean equals( Object o )
    {
        return o instanceof File && equals( (File) o );
    }

    public boolean equals( File f )
    {
        return f.getName().equals( getName() ) && f.getProject().equals( getProject() );
    }

    public int hashCode()
    {
        return name.hashCode();
    }
}

