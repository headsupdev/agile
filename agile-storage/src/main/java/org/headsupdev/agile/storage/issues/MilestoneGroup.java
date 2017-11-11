/*
 * HeadsUp Agile
 * Copyright 2009-2014 Heads Up Development.
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
 * A simple grouping of milestones used to report on a selection of milestones
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 2.0
 */
@Entity
@Table(name = "MilestoneGroups")
@Indexed(index = "MilestoneGroups")
public class MilestoneGroup
        implements Serializable, SearchResult
{
    @EmbeddedId
    @DocumentId
    @FieldBridge(impl = NameProjectBridge.class)
    @Field
    NameProjectId name;

    @Type(type = "text")
    @Field(index = Index.TOKENIZED)
    String description;

    @Temporal(TemporalType.TIMESTAMP)
    Date created, updated, due, completed;

    @OneToMany(mappedBy = "group")
    private Set<Milestone> milestones = new HashSet<Milestone>();

    @OneToMany
    @IndexedEmbedded
    private Set<Comment> comments = new HashSet<Comment>();

    public MilestoneGroup()
    {
    }

    public MilestoneGroup( String name, Project project )
    {
        this.name = new NameProjectId( name, project );
    }

    public void addMilestone( Milestone milestone )
    {
        getMilestones().add( milestone );
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
        if ( milestones.size() == 0 )
        {
            return null;
        }

        Date earliest = null;
        for ( Milestone milestone : milestones )
        {
            if ( earliest == null || ( milestone.getStartDate() != null && earliest.after( milestone.getStartDate() ) ) )
            {
                earliest = milestone.getStartDate();
            }
        }

        return earliest;
    }

    public Date getDueDate()
    {
        return due;
    }

    public void updateDueDate()
    {
        due = null;
        for ( Milestone milestone : getMilestones() )
        {
            if ( milestone.getDueDate() == null )
            {
                continue;
            }

            if ( getDueDate() == null || milestone.getDueDate().after( getDueDate() ) )
            {
                due = milestone.getDueDate();
            }
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
        Date startDate = getStartDate();
        Date dueDate = getDueDate();
        if ( startDate != null && dueDate != null )
        {
            return startDate.before( dueDate );
        }
        return false;
    }

    public Set<Milestone> getMilestones()
    {
        return milestones;
    }

    public Set<Issue> getIssues()
    {
        Set<Issue> issues = new HashSet<Issue>();

        for ( Milestone milestone : getMilestones() )
        {
            issues.addAll( milestone.getIssues() );
        }

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
        return "/" + getProject().getId() + "/milestones/viewgroup/id/" + getName();
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
        return o instanceof MilestoneGroup && equals( (MilestoneGroup) o );
    }

    public boolean equals( MilestoneGroup m )
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
        if ( milestones.size() == 0 )
        {
            return 0.0;
        }

        return ( (HibernateStorage) Manager.getStorageInstance() ).getResourceManager().
                getMilestoneGroupCompleteness( this );
    }
}