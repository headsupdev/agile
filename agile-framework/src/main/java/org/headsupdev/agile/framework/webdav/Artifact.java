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

package org.headsupdev.agile.framework.webdav;

import org.headsupdev.agile.api.SearchResult;
import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.api.mime.Mime;

import javax.persistence.*;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.io.File;
import java.net.URLEncoder;

import org.hibernate.search.annotations.*;

/**
 * A simple file record for files stored into the repositories
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@Entity
@Table( name = "Artifacts" )
@Indexed( index = "Artifacts" )
public class Artifact
    implements Serializable, SearchResult
{
    @Id
    @DocumentId
    @Field
    String path;

    @Field
    String repository;

    public Artifact()
    {
    }

    public Artifact( String path, String repo )
    {
        // store the repo in the path too, so it is unique
        this.path = new File( repo, path ).getPath();
        this.repository = repo;
    }

    // strip the repo part from the name for rendering
    public String getPath()
    {
        return path.substring( repository.length() + 1 );
    }

    public String getRepository()
    {
        return repository;
    }

    public String getIconPath() {
        java.io.File path = new File( Manager.getStorageInstance().getDataDirectory(), "repository" );
        java.io.File file = new File( path, this.path );

        Mime mime;
        if ( file.isDirectory() )
        {
            mime = Mime.get( "folder" );
        }
        else
        {
            mime = Mime.get( getPath() );
        }

        return "api/mime/" + mime.getIconName();
    }

    public String getLink() {
        String path = getPath();
        try
        {
            path = URLEncoder.encode( getPath().replace( java.io.File.separatorChar, ':' ), "UTF-8" );
        }
        catch ( UnsupportedEncodingException e )
        {
            // not going to happen
        }
        return "/artifacts/" + repository + "/path/" + path;
    }

    @Override
    public String getAppId()
    {
        return "artifacts";
    }

    public String toString()
    {
        return getPath();
    }

    public boolean equals( Object o )
    {
        return o instanceof Artifact && equals( (Artifact) o );
    }

    public boolean equals( Artifact f )
    {
        return f.getPath().equals( getPath() );
    }

    public int hashCode()
    {
        return path.hashCode();
    }
}