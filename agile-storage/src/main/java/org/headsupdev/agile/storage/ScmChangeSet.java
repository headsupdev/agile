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

import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.api.SearchResult;
import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.api.service.Change;
import org.headsupdev.agile.api.service.ChangeSet;
import org.headsupdev.agile.storage.hibernate.NameProjectBridge;
import org.headsupdev.agile.storage.hibernate.NameProjectId;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;
import java.util.HashSet;
import java.io.Serializable;

import org.headsupdev.agile.storage.issues.Issue;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.Proxy;
import org.hibernate.search.annotations.*;

/**
 * TODO add a description
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@Entity
@Table( name = "ChangeSets" )
@Inheritance( strategy = InheritanceType.SINGLE_TABLE )
@DiscriminatorColumn( name = "type", discriminatorType = DiscriminatorType.STRING )
@DiscriminatorValue( "scm" )
@Indexed( index = "ChangeSets" )
@Proxy( lazy = false )
public class ScmChangeSet
    implements ChangeSet, Serializable, SearchResult
{
    @EmbeddedId
    @DocumentId
    @FieldBridge( impl = NameProjectBridge.class )
    NameProjectId id;

    @Field
    private String author;

    @Type( type = "text" )
    @Field(index = Index.TOKENIZED)
    private String comment;

    @Temporal( TemporalType.TIMESTAMP )
    private Date date;

    private String previous_name;
    private String next_name;

    @OneToMany( mappedBy = "set", fetch = FetchType.LAZY, targetEntity = ScmChange.class )
    @IndexedEmbedded
    private Set<Change> changes = new HashSet<Change>();

    @ManyToMany( fetch = FetchType.LAZY )
    private Set<Issue> issues = new HashSet<Issue>();

    public ScmChangeSet()
    {
    }

    public ScmChangeSet(String author, String comment, Date date, Project project)
    {
        this.author = author;
        this.comment = comment;
        this.date = date;

        this.id = new NameProjectId( author + ":" + date.getTime(), project );
    }

    public String getId()
    {
        return id.getName();
    }

    public Project getProject()
    {
        return id.getProject();
    }

    public String getAuthor()
    {
        return author;
    }

    public String getComment()
    {
        return comment;
    }

    public Date getDate()
    {
        return date;
    }

    public Set<Change> getChanges()
    {
        return changes;
    }

    public ChangeSet getPrevious()
    {
        if ( previous_name == null )
        {
            return null;
        }

        return Manager.getInstance().getScmService().getChangeSet( getProject(), previous_name );
    }

    public void setPrevious( ChangeSet previous )
    {
        if ( previous == null )
        {
            this.previous_name = null;
        }
        else
        {
            this.previous_name = previous.getId();
        }
    }

    public ChangeSet getNext()
    {
        if ( next_name == null )
        {
            return null;
        }

        return Manager.getInstance().getScmService().getChangeSet( getProject(), next_name );
    }

    public void setNext( ChangeSet next )
    {
        if ( next == null )
        {
            this.next_name = null;
        }
        else
        {
            this.next_name = next.getId();
        }
    }

    public Set<Issue> getIssues()
    {
        return issues;
    }

    public void setIssues( Set<Issue> issues )
    {
        this.issues = issues;
    }

    public String getIconPath() {
        return null;
    }

    public String getLink() {
        return "/" + getProject().getId() + "/files/change/id/" + getId();
    }

    @Override
    public String getAppId()
    {
        return "files";
    }

    public String toString()
    {
        return "ScmChangeSet by " + author + " \"" + comment + "\"";
    }

    public boolean equals( Object o )
    {
        return o instanceof ScmChangeSet && equals( (ScmChangeSet) o );

    }

    public boolean equals( ScmChangeSet set )
    {
        return set != null && id.equals( set.id );
    }

    public int hashCode() {
        return id.hashCode();
    }
}
