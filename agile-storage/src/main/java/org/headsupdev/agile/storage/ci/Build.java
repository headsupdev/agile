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

package org.headsupdev.agile.storage.ci;

import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.api.SearchResult;
import org.headsupdev.agile.api.rest.Publish;
import org.headsupdev.agile.storage.hibernate.IdProjectBridge;
import org.headsupdev.agile.storage.hibernate.IdProjectId;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;
import java.util.HashSet;
import java.io.Serializable;

import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.DocumentId;

/**
 * A single build for a project. It has an id and a status as well as other data.
 * It references a set of test results and caches the result totals.
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@Entity
@Table( name = "Builds" )
@Indexed( index = "Builds" )
public class Build
    implements Serializable, SearchResult
{
    public static final int BUILD_QUEUED = 10;
    public static final int BUILD_RUNNING = 20;
    public static final int BUILD_CANCELLED = 30;
    public static final int BUILD_SUCCEEDED = 40;
    public static final int BUILD_FAILED = 50;

    @EmbeddedId
    @DocumentId
    @FieldBridge( impl = IdProjectBridge.class )
    @Field
    @Publish
    private IdProjectId id;

    @Field
    @Publish
    private String revision;

    @Temporal( TemporalType.TIMESTAMP )
    @Publish
    private Date startTime = new Date(), endTime;

    private String configName;

    @Publish
    private int status = 0;

    @OneToMany
    private Set<TestResultSet> testResults = new HashSet<TestResultSet>();

    @Publish
    private Integer tests = 0;
    @Publish
    private Integer failures = 0;
    @Publish
    private Integer errors = 0;
    @Publish
    private Integer warnings = 0;

    Build()
    {
    }

    public Build( Project project, String revision)
    {
        this.id = new IdProjectId( project );
        this.revision = revision;
    }

    public long getId()
    {
        return id.getId();
    }

    public String getRevision()
    {
        return revision;
    }

    public Project getProject()
    {
        return id.getProject();
    }
    
    public int getStatus()
    {
        return status;
    }

    public void setStatus( int status )
    {
        this.status = status;
    }

    public int getTests()
    {
        return tests == null ? 0 : tests;
    }

    public void setTests( int tests )
    {
        this.tests = tests;
    }

    public int getFailures()
    {
        return failures == null ? 0 : failures;
    }

    public void setFailures( int failures )
    {
        this.failures = failures;
    }

    public int getErrors()
    {
        return errors == null ? 0 : errors;
    }

    public void setErrors( int errors )
    {
        this.errors = errors;
    }

    public int getWarnings()
    {
        return warnings == null ? 0 : warnings;
    }

    public void setWarnings( int warnings )
    {
        this.warnings = warnings;
    }

    public Date getStartTime()
    {
        return startTime;
    }

    public Date getEndTime()
    {
        return endTime;
    }

    public void setEndTime( Date endTime )
    {
        this.endTime = endTime;
    }

    public String getConfigName()
    {
        return configName;
    }

    public void setConfigName( String configName )
    {
        this.configName = configName;
    }

    public Set<TestResultSet> getTestResults()
    {
        return testResults;
    }

    public String getIconPath() {
        switch ( status )
        {
            case BUILD_QUEUED:
                return "images/type/ci/queued.png";
            case BUILD_RUNNING:
                return "images/type/ci/running.png";
            case BUILD_CANCELLED:
                return "images/type/ci/failed.png";
            case BUILD_SUCCEEDED:
                return "images/type/ci/passed.png";
            default:
                return "images/type/ci/failed.png";
        }
    }

    public String getLink() {
        return "/" + getProject().getId() + "/builds/view/id/" + getId();
    }

    @Override
    public String getAppId()
    {
        return "builds";
    }

    public String toString()
    {
        String statusName;
        switch ( status )
        {
            case BUILD_QUEUED:
                statusName = "queued";
                break;
            case BUILD_RUNNING:
                statusName = "running";
                break;
            case BUILD_CANCELLED:
                statusName = "cancelled";
                break;
            case BUILD_SUCCEEDED:
                statusName = "succeeded";
                break;
            default:
                statusName = "failed";
        }

        return "Build result " + getId() + " (" + statusName + ")";
    }

    public boolean equals( Object o )
    {
        return o instanceof Build && equals( (Build) o );
    }

    public boolean equals( Build b )
    {
        return b.getId() == getId() && b.getProject().equals( getProject() );
    }

    public int hashCode()
    {
        return id.hashCode();
    }

}
