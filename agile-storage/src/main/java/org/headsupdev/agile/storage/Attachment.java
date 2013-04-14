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

package org.headsupdev.agile.storage;

import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.IndexedEmbedded;

import javax.persistence.*;
import java.util.Date;
import java.io.File;
import java.io.Serializable;

import org.headsupdev.agile.api.User;
import org.headsupdev.agile.api.Storage;

/**
 * Simple class used for attachments on issues and documents.
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@Entity
@Table( name = "Attachments" )
public class Attachment
    implements Serializable
{
    @Id
    @GeneratedValue
    private long id;

    @OneToOne( targetEntity = StoredUser.class )
    @IndexedEmbedded( targetElement = StoredUser.class )
    private User user;

    @Temporal( TemporalType.TIMESTAMP )
    private Date created;

    @Field
    private String filename;

    @OneToOne
    @IndexedEmbedded
    private Comment comment;

    public long getId()
    {
        return id;
    }

    public User getUser()
    {
        return user;
    }

    public void setUser( User user )
    {
        this.user = user;
    }

    public String getFilename()
    {
        return filename;
    }

    public void setFilename( String filename )
    {
        this.filename = filename;
    }

    public Date getCreated()
    {
        return created;
    }

    public void setCreated( Date created )
    {
        this.created = created;
    }

    public Comment getComment()
    {
        return comment;
    }

    public void setComment( Comment comment )
    {
        if ( comment != null && comment.getComment() != null )
        {
            this.comment = comment;
        }
    }

    public boolean equals( Object o )
    {
        return o instanceof Attachment && equals( (Attachment) o );
    }

    public boolean equals( Attachment a )
    {
        return a.getId() == id;
    }

    public int hashCode()
    {
        return ( (Long) id ).hashCode();
    }

    public File getFile( Storage storage )
    {
        File attachDir = new File( storage.getDataDirectory(), "attachments" );
        File fileDir = new File( attachDir, String.valueOf( id ) );

        return new File( fileDir, filename );
    }
}
