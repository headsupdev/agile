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

package org.headsupdev.agile.storage.resource;

import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.api.User;
import org.headsupdev.agile.storage.Comment;
import org.headsupdev.agile.storage.HibernateStorage;
import org.headsupdev.agile.storage.StoredUser;
import org.headsupdev.agile.storage.issues.Duration;
import org.headsupdev.agile.storage.issues.Issue;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.search.annotations.IndexedEmbedded;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Representing a unit of work done - this may include an updated estimate for burndown.
 * A day is associated with this so we can reconstruct the timeline (past work added).
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@Entity
@Table( name = "DurationWorked" )
public class DurationWorked
    implements Serializable
{
    @Id
    @GeneratedValue
    private long id;

    @OneToOne( targetEntity = StoredUser.class )
    @IndexedEmbedded( targetElement = StoredUser.class )
    private User user;

    @Embedded
    @AttributeOverrides( {
            @AttributeOverride( name = "time", column=@Column( name = "timeWorked" ) ),
            @AttributeOverride( name = "timeUnit", column=@Column( name = "timeWorkedUnit" ) )
    } )
    private Duration worked;

    // optional, for burndown
    @Embedded
    @AttributeOverrides( {
            @AttributeOverride( name = "time", column=@Column( name = "updatedRequired" ) ),
            @AttributeOverride( name = "timeUnit", column=@Column( name = "updatedRequiredUnit" ) )
    } )
    private Duration updatedRequired;

    @OneToOne
    @IndexedEmbedded
    private Comment comment;

    private Date day;

    @ManyToOne( fetch = FetchType.LAZY )
    private Issue issue;

    // TODO see if we can always require old duration required for later comparison
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

    public Duration getWorked()
    {
        return worked;
    }

    public void setWorked( Duration worked )
    {
        this.worked = worked;
    }

    public Duration getUpdatedRequired()
    {
        return updatedRequired;
    }

    public void setUpdatedRequired( Duration updatedRequired )
    {
        this.updatedRequired = updatedRequired;
    }

    public Date getDay()
    {
        return day;
    }

    public void setDay( Date day )
    {
        this.day = day;
    }

    public Issue getIssue()
    {
        if ( issue == null )
        {
            for ( Issue issue : getAllIssues() )
            {
                if ( issue.getTimeWorked() == null || issue.getTimeWorked().size() == 0 )
                {
                    continue;
                }

                if ( issue.getTimeWorked().contains( this ) )
                {
                    this.issue = issue;
                }
            }
        }
        return issue;
    }

    public void setIssue( Issue issue )
    {
        this.issue = issue;
    }

    private static List<Issue> getAllIssues()
    {
        List<Issue> allIssues = new ArrayList<Issue>();
        Session session = ( (HibernateStorage) Manager.getStorageInstance() ).getHibernateSession();

        Criteria c = session.createCriteria( Issue.class );

        return c.list();
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

    public String toString()
    {
        return "worked " + worked + " on " + day;
    }

    public boolean equals( Object o )
    {
        return o instanceof DurationWorked && equals( (DurationWorked) o );
    }

    public boolean equals( DurationWorked d )
    {
        return d.getId() == id;
    }

    public int hashCode()
    {
        return ( (Long) id ).hashCode();
    }

}
