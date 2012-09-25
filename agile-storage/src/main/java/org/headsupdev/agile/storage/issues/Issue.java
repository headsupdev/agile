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

package org.headsupdev.agile.storage.issues;

import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.api.SearchResult;
import org.headsupdev.agile.api.User;
import org.headsupdev.agile.api.service.ChangeSet;
import org.headsupdev.agile.storage.Attachment;
import org.headsupdev.agile.storage.Comment;
import org.headsupdev.agile.storage.ScmChangeSet;
import org.headsupdev.agile.storage.StoredUser;
import org.headsupdev.agile.storage.hibernate.IdProjectBridge;
import org.headsupdev.agile.storage.hibernate.IdProjectId;
import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * A basic Issue class for holding the main data about issues.
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@Entity
@Table(name = "Issues")
@Indexed(index = "Issues")
public class Issue
        implements Serializable, SearchResult
{
    public static final int TYPE_BUG = 10;
    public static final int TYPE_FEATURE = 20;
    public static final int TYPE_ENHANCEMENT = 30;
    public static final int TYPE_TASK = 40;
    public static final int TYPE_SPEC = 50;
    public static final int TYPE_ENQUIRY = 60;

    public static final int PRIORITY_BLOCKER = 110;
    public static final int PRIORITY_CRITICAL = 120;
    public static final int PRIORITY_MAJOR = 130;
    public static final int PRIORITY_MINOR = 140;
    public static final int PRIORITY_TRIVIAL = 150;

    public static final int STATUS_NEW = 210;
    public static final int STATUS_FEEDBACK = 220;
    public static final int STATUS_ASSIGNED = 230;
    public static final int STATUS_REOPENED = 240;
    public static final int STATUS_INPROGRESS = 245;
    public static final int STATUS_RESOLVED = 250;
    public static final int STATUS_CLOSED = 260;

    public static final int RESOLUTION_FIXED = 310;
    public static final int RESOLUTION_INVALID = 320;
    public static final int RESOLUTION_CANNOT_REPRODUCE = 325;
    public static final int RESOLUTION_WONTFIX = 330;
    public static final int RESOLUTION_DUPLICATE = 340;

    public static final Integer ORDER_NO_ORDER = Integer.MAX_VALUE;

    @EmbeddedId
    @DocumentId
    @FieldBridge(impl = IdProjectBridge.class)
    @Field
    private IdProjectId id;

    @Type(type = "text")
    @Field(index = Index.TOKENIZED)
    private String summary, body, testNotes;

    @ManyToOne(targetEntity = StoredUser.class)
    private User reporter;

    @ManyToOne(targetEntity = StoredUser.class)
    private User assignee;

    @Temporal(TemporalType.TIMESTAMP)
    private Date created, updated;

    private int type, priority = PRIORITY_MAJOR, status, resolution;
    private String version, environment;

    private Integer reopened;
    private Integer rank = ORDER_NO_ORDER;

    private Boolean includeInInitialEstimates;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "time", column = @Column(name = "hoursEstimate")),
            @AttributeOverride(name = "timeUnit", column = @Column(name = "hoursEstimateUnit"))
    })
    private Duration timeEstimate;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "time", column = @Column(name = "hoursRequired")),
            @AttributeOverride(name = "timeUnit", column = @Column(name = "hoursRequiredUnit"))
    })
    private Duration timeRequired;

    // TODO mappedBy = "issue" (once move complete)
    @OneToMany
    private Set<DurationWorked> timeWorked = new HashSet<DurationWorked>();

    @OneToMany
    @IndexedEmbedded
    private Set<Comment> comments = new HashSet<Comment>();

    @OneToMany
    @IndexedEmbedded
    private Set<Attachment> attachments = new HashSet<Attachment>();

    @OneToMany(mappedBy = "owner")
    private Set<IssueRelationship> relationships = new HashSet<IssueRelationship>();

    @OneToMany(mappedBy = "related")
    private Set<IssueRelationship> reverseRelationships = new HashSet<IssueRelationship>();

    @ManyToOne
    private Milestone milestone;

    @ManyToMany(targetEntity = ScmChangeSet.class)
    private Set<ChangeSet> changeSets = new HashSet<ChangeSet>();

    @ManyToMany(targetEntity = StoredUser.class)
    @JoinTable(name = "ISSUES_WATCHERS")
    private Set<User> watchers = new HashSet<User>();

    public Issue()
    {
    }

    public Issue( Project project )
    {
        this.id = new IdProjectId( project );
    }

    public long getId()
    {
        return id.getId();
    }

    public IdProjectId getInternalId()
    {
        return id;
    }

    public String getSummary()
    {
        return summary;
    }

    public void setSummary( String summary )
    {
        this.summary = summary;
    }

    public String getBody()
    {
        return body;
    }

    public void setBody( String body )
    {
        this.body = body;
    }

    public String getTestNotes()
    {
        return testNotes;
    }

    public void setTestNotes( String testNotes )
    {
        this.testNotes = testNotes;
    }

    public User getReporter()
    {
        return reporter;
    }

    public void setReporter( User reporter )
    {
        this.reporter = reporter;
    }

    public User getAssignee()
    {
        return assignee;
    }

    public void setAssignee( User assignee )
    {
        if ( this.assignee == null && ( this.status == STATUS_NEW || this.status == STATUS_FEEDBACK ) )
        {
            this.status = STATUS_ASSIGNED;
        }

        this.assignee = assignee;
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

    public int getType()
    {
        return type;
    }

    public void setType( int type )
    {
        this.type = type;
    }

    public int getPriority()
    {
        return priority;
    }

    public void setPriority( int priority )
    {
        this.priority = priority;
    }

    public int getStatus()
    {
        return status;
    }

    public void setStatus( int status )
    {
        this.status = status;
    }

    public int getResolution()
    {
        return resolution;
    }

    public void setResolution( int resolution )
    {
        this.resolution = resolution;
    }

    public int getReopened()
    {
        if ( reopened == null )
        {
            return 0;
        }

        return reopened;
    }

    public void setReopened( int reopened )
    {
        this.reopened = reopened;
    }

    public Integer getOrder()
    {
        if ( rank == null || rank.equals( ORDER_NO_ORDER ) )
        {
            return null;
        }

        return rank;
    }

    public void setOrder( Integer rank )
    {
        if ( rank == null || rank == 0 )
        {
            this.rank = ORDER_NO_ORDER;
            return;
        }

        this.rank = rank;
    }

    public Project getProject()
    {
        return id.getProject();
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion( String version )
    {
        this.version = version;
    }

    public String getEnvironment()
    {
        return environment;
    }

    public void setEnvironment( String environment )
    {
        this.environment = environment;
    }

    public Duration getTimeEstimate()
    {
        return timeEstimate;
    }

    public void setTimeEstimate( Duration time )
    {
        this.timeEstimate = time;
    }

    public Duration getTimeRequired()
    {
        return timeRequired;
    }

    public void setTimeRequired( Duration time )
    {
        this.timeRequired = time;
    }

    public Set<DurationWorked> getTimeWorked()
    {
        return timeWorked;
    }

    public void setTimeWorked( Set<DurationWorked> timeWorked )
    {
        this.timeWorked = timeWorked;
    }

    public Set<IssueRelationship> getRelationships()
    {
        return relationships;
    }

    public Set<IssueRelationship> getReverseRelationships()
    {
        return reverseRelationships;
    }

    public Set<Comment> getComments()
    {
        return comments;
    }

    public Set<Attachment> getAttachments()
    {
        return attachments;
    }

    public Milestone getMilestone()
    {
        return milestone;
    }

    public void setMilestone( Milestone milestone )
    {
        this.milestone = milestone;
    }

    public Set<ChangeSet> getChangeSets()
    {
        return changeSets;
    }

    public Set<User> getWatchers()
    {
        return watchers;
    }

    public String getIconPath()
    {
        return null;
    }

    public String getLink()
    {
        return "/" + getProject().getId() + "/issues/view/id/" + getId();
    }

    public Boolean getIncludeInInitialEstimates()
    {
        if ( includeInInitialEstimates == null )
        {
            return Boolean.FALSE;
        }
        return includeInInitialEstimates;
    }

    public void setIncludeInInitialEstimates( Boolean includeInInitialEstimates )
    {
        this.includeInInitialEstimates = includeInInitialEstimates;
    }

    public String toString()
    {
        return getSummary();
    }

    public boolean equals( Object o )
    {
        return o instanceof Issue && equals( (Issue) o );
    }

    public boolean equals( Issue i )
    {
        return i.getId() == getId() && i.getProject().equals( getProject() );
    }

    public int hashCode()
    {
        // TODO WTF? - if we view a milestone with issues, then another screen then hit back we get NPE!!!
        if ( id == null )
        {
            return 0;
        }
        return id.hashCode();
    }
}
