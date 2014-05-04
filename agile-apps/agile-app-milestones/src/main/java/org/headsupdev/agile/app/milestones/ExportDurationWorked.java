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

package org.headsupdev.agile.app.milestones;

import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.storage.dao.MilestoneGroupsDAO;
import org.headsupdev.agile.storage.dao.MilestonesDAO;
import org.headsupdev.agile.app.milestones.entityproviders.MilestoneProvider;
import org.headsupdev.agile.app.milestones.permission.MilestoneViewPermission;
import org.headsupdev.agile.storage.HibernateStorage;
import org.headsupdev.agile.storage.StoredProject;
import org.headsupdev.agile.storage.issues.*;
import org.headsupdev.agile.storage.resource.DurationWorked;
import org.headsupdev.agile.web.HeadsUpSession;
import org.headsupdev.agile.web.MountPoint;
import org.headsupdev.agile.web.WebUtil;
import org.headsupdev.agile.web.components.issues.IssueUtils;
import org.headsupdev.agile.web.wicket.SortableEntityProvider;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.markup.html.WebResource;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.util.resource.AbstractStringResourceStream;
import org.apache.wicket.util.resource.IResourceStream;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Export a CSV of the estimates and time worked on milestones
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@MountPoint( "export-worked.csv" )
public class ExportDurationWorked
    extends WebResource
{
    private MilestonesDAO dao = new MilestonesDAO();
    private MilestoneGroupsDAO groupDao = new MilestoneGroupsDAO();

    @Override
    protected void setHeaders( WebResponse response )
    {
        super.setHeaders(response);    //To change body of overridden methods use File | Settings | File Templates.

        WebUtil.authenticate( (WebRequest) RequestCycle.get().getRequest(), response, new MilestoneViewPermission(),
                getProject() );
    }

    @Override
    public IResourceStream getResourceStream()
    {
        return new AbstractStringResourceStream( "text/csv" )
        {
            @Override
            protected String getString()
            {
                return getBody();
            }
        };
    }

    protected Project getProject()
    {
        String projectId = getParameters().getString( "project" );
        if ( projectId == null || projectId.length() == 0 )
        {
            return null;
        }

        return Manager.getStorageInstance().getProject( projectId );
    }

    protected String getBody()
    {
        StringBuffer ret = new StringBuffer();

        String milestoneId = getParameters().getString( "id" );
        if ( milestoneId != null && milestoneId.length() > 0 )
        {
            Milestone milestone = dao.find(milestoneId, getProject());

            if ( milestone != null )
            {
                exportMilestone( milestone, ret );

                return ret.toString();
            }
            // here we could throw some error I guess...
        }

        String groupId = getParameters().getString( "groupId" );
        if ( groupId != null && groupId.length() > 0 )
        {
            MilestoneGroup group = groupDao.find(groupId, getProject());

            if ( group != null )
            {
                for ( Milestone milestone : group.getMilestones() )
                {
                    exportMilestone( milestone, ret );
                }

                return ret.toString();
            }
            // here we could throw some error I guess...
        }

        MilestoneFilterPanel filter = new MilestoneFilterPanel( "dummy", HeadsUpSession.ANONYMOUS_USER );
        filter.setFilters( MilestonesApplication.QUERY_DUE_ALL, true, true );
        SortableEntityProvider<Milestone> provider = new MilestoneProvider( getProject(), filter );

        // fall back to listing all milestones
        Iterator<Milestone> milestones = provider.iterator( 0, provider.size() );
        while ( milestones.hasNext() )
        {
            exportMilestone( milestones.next(), ret );
        }

        return ret.toString();
    }

    protected void exportMilestone( Milestone milestone, StringBuffer ret )
    {
        final boolean burndown = Boolean.parseBoolean( milestone.getProject().getConfigurationValue(
                StoredProject.CONFIGURATION_TIMETRACKING_BURNDOWN ) );

        ret.append( "\"" );
        ret.append( milestone.getName() );
        ret.append( "\"," );
        if ( milestone.getDueDate() == null )
        {
            ret.append( "\"no due date\"" );
        }
        else
        {
            ret.append( milestone.getDueDate() );
        }
        ret.append( "\n\n" );

        List<Date> dates = ( (HibernateStorage) Manager.getStorageInstance() ).getResourceManager().
                getMilestoneDates( milestone, true );
        if ( burndown )
        {
            ret.append( "\"Estimated Hours\"\n" );
        }
        else
        {
            ret.append( "\"Elapsed Hours\"\n" );
        }
        ret.append( "Id,Task,Status,Assignee," );
        if ( burndown )
        {
            ret.append( "Estimate" );
        }
        else
        {
            ret.append( "Initial" );
        }

        boolean first = true;
        for ( Date date : dates )
        {
            if ( first )
            {
                first = false;
                continue;
            }

            ret.append( "," );
            ret.append( date );
        }
        ret.append( "\n" );

        for ( Issue issue : milestone.getIssues() )
        {
            ret.append( "\"" );
            ret.append( issue.getId() );
            ret.append( "\",\"" );
            ret.append( issue.getSummary().replace( "\"","\"\"" ) );
            ret.append( "\"," );
            ret.append( IssueUtils.getStatusName( issue.getStatus() ) );

            ret.append( "," );
            if ( issue.getAssignee() != null )
            {
                ret.append( issue.getAssignee().getUsername() );
            }

            Duration estimate = null;
            // for non-burndown we start at 0 anyway
            if ( burndown )
            {
                estimate = issue.getTimeEstimate();

                if ( dates.size() > 0 )
                {
                    Date firstDay = dates.get( 0 );
                    for ( DurationWorked worked : issue.getTimeWorked() )
                    {
                        if ( estimate == null || ( worked.getDay().before( firstDay ) && worked.getUpdatedRequired() != null &&
                                worked.getUpdatedRequired().getHours() < estimate.getHours() ) ) {
                            estimate = worked.getUpdatedRequired();
                        }
                    }
                }
            }
            if ( estimate == null )
            {
                estimate = new Duration( 0 );

                if ( dates.size() > 0 )
                {
                    Date firstDay = dates.get( 0 );
                    for ( DurationWorked worked : issue.getTimeWorked() )
                    {
                        if ( worked.getDay().before( firstDay ) && worked.getUpdatedRequired() != null &&
                                worked.getUpdatedRequired().getHours() > estimate.getHours() ) {
                            estimate = worked.getUpdatedRequired();
                        }
                    }
                }
            }

            for ( Date date : dates )
            {
                Duration est = ( (HibernateStorage) Manager.getStorageInstance() ).getResourceManager().
                        lastEstimateForDay( issue, date );

                if ( est != null )
                {
                    estimate = est;
                }

                ret.append( "," );
                ret.append( estimate.getHours() );
            }

            ret.append( "\n" );
        }

        ret.append( "\n\n" );

        if ( dates.size() > 0 )
        {
            dates.remove( 0 );
        }
        ret.append( "Hours Logged\nId,Task,Status,Assignee,Worked" );
        for ( Date date : dates )
        {
            ret.append( "," );
            ret.append( date );
        }
        ret.append( "\n" );

        for ( Issue issue : milestone.getIssues() )
        {
            ret.append( issue.getId() );
            ret.append( "," );
            ret.append( "\"" );
            ret.append( issue.getSummary() );
            ret.append( "\"," );
            ret.append( IssueUtils.getStatusName( issue.getStatus() ) );

            StringBuilder hourLog = new StringBuilder();
            double totalHours = 0;
            for ( Date date : dates )
            {
                hourLog.append( "," );
                Duration total = ( (HibernateStorage) Manager.getStorageInstance() ).getResourceManager().
                        totalWorkedForDay( issue, date );
                if ( total != null )
                {
                    hourLog.append( total.getHours() );
                    totalHours += total.getHours();
                }
                else
                {
                    hourLog.append( "0" );
                }
            }

            ret.append( "," );
            if ( issue.getAssignee() != null )
            {
                ret.append( issue.getAssignee().getUsername() );
            }
            ret.append( "," );
            ret.append( totalHours );

            ret.append( hourLog.toString() );

            ret.append( "\n" );
        }

        ret.append( "\n\n" );
    }
}
