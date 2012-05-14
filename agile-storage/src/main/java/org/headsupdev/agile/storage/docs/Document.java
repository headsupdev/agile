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

package org.headsupdev.agile.storage.docs;

import org.headsupdev.agile.api.*;
import org.headsupdev.agile.storage.hibernate.NameProjectBridge;
import org.headsupdev.agile.storage.hibernate.NameProjectId;
import org.headsupdev.agile.storage.StoredUser;
import org.headsupdev.agile.storage.Comment;
import org.headsupdev.agile.storage.Attachment;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;
import java.util.HashSet;
import java.io.Serializable;

import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.*;

/**
 * This class represents a single document on the system and provides helper methods for editing and rendering.
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@Entity
@Table( name = "Documents" )
@Indexed( index = "Documents" )
public class Document
    implements Serializable, SearchResult, Comparable<Document>
{
    @EmbeddedId
    @DocumentId
    @FieldBridge( impl = NameProjectBridge.class )
    @Field
    NameProjectId name;

    @Type( type = "text" )
    @Field( index = Index.TOKENIZED )
    private String content;
    private String format = "apt";

    @ManyToOne( targetEntity = StoredUser.class )
    private User creator;

    @Temporal( TemporalType.TIMESTAMP )
    private Date created, updated;

    @OneToMany
    @IndexedEmbedded
    private Set<Comment> comments = new HashSet<Comment>();

    @OneToMany
    @IndexedEmbedded
    private Set<Attachment> attachments = new HashSet<Attachment>();

    @ManyToMany( targetEntity = StoredUser.class )
    @JoinTable(name = "DOCUMENTS_WATCHERS" )
    private Set<User> watchers = new HashSet<User>();

    Document()
    {
    }

    public Document( String name, Project project )
    {
        this.name = new NameProjectId( name, project );
    }

    public String getName() {
        return name.getName();
    }

    public Project getProject() {
        return name.getProject();
    }

    public String getContent() {
        return content;
    }

    public void setContent( String content )
    {
        this.content = content;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat( String format )
    {
        this.format = format;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator( User creator )
    {
        this.creator = creator;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    public Set<Comment> getComments()
    {
        return comments;
    }

    public Set<Attachment> getAttachments()
    {
        return attachments;
    }

    public Set<User> getWatchers()
    {
        return watchers;
    }

    public String getIconPath() {
        return null;
    }

    public String getLink() {
        return "/" + getProject().getId() + "/docs/page/" + getName();
    }

    public String toString()
    {
        return getName();
    }

    public boolean equals( Object o )
    {
        return o instanceof Document && equals( (Document) o );
    }

    public boolean equals( Document d )
    {
        return d.getName().equals( getName() ) && d.getProject().equals( getProject() );
    }

    public int hashCode()
    {
        return name.hashCode();
    }

    public int compareTo( Document document )
    {
        return getName().compareToIgnoreCase( document.getName() );
    }
}