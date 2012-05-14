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

import javax.persistence.*;
import java.util.Date;
import java.io.Serializable;

import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.*;
import org.headsupdev.agile.api.User;

/**
 * Simple class used for comments on issues.
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@Entity
@Table( name = "Comments" )
public class Comment
    implements Serializable
{
    @Id
    @GeneratedValue
    private long id;

    @OneToOne( targetEntity = StoredUser.class )
    @IndexedEmbedded( targetElement = StoredUser.class )
    private User user;

    @Type( type = "text" )
    @Field(index = Index.TOKENIZED)
    private String comment;

    @Temporal( TemporalType.TIMESTAMP )
    private Date created;

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

    public String getComment()
    {
        return comment;
    }

    public void setComment( String comment )
    {
        this.comment = comment;
    }

    public Date getCreated()
    {
        return created;
    }

    public void setCreated( Date created )
    {
        this.created = created;
    }

    public boolean equals( Object o )
    {
        return o instanceof Comment && equals( (Comment) o );
    }

    public boolean equals( Comment c )
    {
        return c.getId() == id;
    }

    public int hashCode()
    {
        return ( (Long) id ).hashCode();
    }
}
