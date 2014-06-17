/*
 * HeadsUp Agile
 * Copyright 2009-2014 Heads Up Development Ltd.
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

package org.headsupdev.agile.storage.issues;

import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.api.SearchResult;
import org.headsupdev.agile.api.rest.Publish;
import org.headsupdev.agile.storage.Comment;
import org.headsupdev.agile.storage.HibernateStorage;
import org.headsupdev.agile.storage.hibernate.NameProjectBridge;
import org.headsupdev.agile.storage.hibernate.NameProjectId;
import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * A simple milestone to gather info on target releases.
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@Entity
@Table(name = "Milestones")
@Indexed(index = "Milestones")
public class Milestone
        implements Serializable, SearchResult
{
    @EmbeddedId
    @DocumentId
    @FieldBridge(impl = NameProjectBridge.class)
    @Field
    @Publish
    NameProjectId name;

    @Type(type = "text")
    @Field(index = Index.TOKENIZED)
    @Publish
    String description;

    @Temporal(TemporalType.TIMESTAMP)
    @Publish
    Date created, updated, start, due, completed;

    @OneToMany(mappedBy = "milestone")
    @Publish
    private Set<Issue> issues = new HashSet<Issue>();

    @ManyToOne
    private MilestoneGroup group;

    @OneToMany
    @IndexedEmbedded
    private Set<Comment> comments = new HashSet<Comment>();

    public Milestone()
    {
    }

    public Milestone( String name, Project project )
    {
        this.name = new NameProjectId( name, project );
    }

    public Project getProject()
    {
        return name.getProject();
    }

    public String getName()
    {
        return name.getName();
    }

    public NameProjectId getInternalId()
    {
        return name;
    }

    /**
     * This should not be used once the object has been persisted!....
     *
     * @param name The new name to set
     */
    public void setName( String name )
    {
        this.name.setName( name );
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription( String description )
    {
        this.description = description;
    }

    public Date getCreated()
    {
        return created;
    }

    public void setCreated( Date created )
    {
        this.created = created;
    }

    public Date getUpdated()
    {
        return updated;
    }

    public void setUpdated( Date updated )
    {
        this.updated = updated;
    }

    public Date getStartDate()
    {
        return start;
    }

    public void setStartDate( Date start )
    {
        this.start = start;
    }

    public Date getDueDate()
    {
        return due;
    }

    public void setDueDate( Date due )
    {
        this.due = due;
        if ( group != null )
        {
            group.updateDueDate();
        }
    }

    public Date getCompletedDate()
    {
        return completed;
    }

    public void setCompletedDate( Date completed )
    {
        this.completed = completed;
    }

    public boolean isCompleted()
    {
        return completed != null;
    }

    public boolean hasValidTimePeriod()
    {
        if ( getStartDate() != null && getDueDate() != null )
        {
            return getStartDate().before( getDueDate() );
        }
        return false;

    }

    public Set<Issue> getIssues()
    {
        return issues;
    }

    public Set<Issue> getOpenIssues()
    {
        Set<Issue> ret = new HashSet<Issue>();

        for ( Issue issue : getIssues() )
        {
            if ( issue.getStatus() < Issue.STATUS_RESOLVED )
            {
                ret.add( issue );
            }
        }

        return ret;
    }

    public Set<Issue> getReOpenedIssues()
    {
        Set<Issue> ret = new HashSet<Issue>();
        for ( Issue issue : getIssues() )
        {
            if ( issue.getReopened() >= 1 )
            {
                ret.add( issue );
            }
        }
        return ret;
    }

    public MilestoneGroup getGroup()
    {
        return group;
    }

    public void setGroup( MilestoneGroup group )
    {
        this.group = group;
    }

    public Set<Comment> getComments()
    {
        return comments;
    }

    public String getIconPath()
    {
        return null;
    }

    public String getLink()
    {
        return "/" + getProject().getId() + "/milestones/view/id/" + getName();
    }

    @Override
    public String getAppId()
    {
        return "milestones";
    }

    public String toString()
    {
        return getName();
    }

    public boolean equals( Object o )
    {
        return o instanceof Milestone && equals( (Milestone) o );
    }

    public boolean equals( Milestone m )
    {
        return m.getName().equals( getName() ) && m.getProject().equals( getProject() );
    }

    public int hashCode()
    {
        // TODO wtf?
        if ( name == null )
        {
            return 0;
        }

        return name.hashCode();
    }

    public double getCompleteness()
    {
        return ( (HibernateStorage) Manager.getStorageInstance() ).getResourceManager().
                getMilestoneCompleteness( this );
    }
}