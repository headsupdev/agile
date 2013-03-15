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

import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.api.User;
import org.headsupdev.agile.storage.StoredProject;
import org.headsupdev.agile.storage.issues.*;
import org.headsupdev.agile.storage.resource.DurationWorked;
import org.headsupdev.agile.web.components.StripedListView;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.panel.Panel;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * A tabular layout of the duration worked for a milestone
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class WorkRemainingTable
    extends Panel
{
    public WorkRemainingTable( String id, final Milestone milestone )
    {
        this( id, milestone.getProject(), milestone.getIssues() );
    }

    public WorkRemainingTable( String id, final MilestoneGroup group )
    {
        this( id, group.getProject(), group.getIssues() );
    }

    protected WorkRemainingTable( String id, final Project project, final Set<Issue> issueSet )
    {
        super( id );

        final boolean burndown = Boolean.parseBoolean( project.getConfigurationValue(
                StoredProject.CONFIGURATION_TIMETRACKING_BURNDOWN) );

        List<User> users = new LinkedList<User>();
        for ( Issue issue : issueSet )
        {
            if ( issue.getAssignee() != null && !users.contains( issue.getAssignee() ) )
            {
                users.add( issue.getAssignee() );
            }

            for ( DurationWorked worked : issue.getTimeWorked() )
            {
                if ( worked.getUser() != null && !users.contains( worked.getUser() ) )
                {
                    users.add( worked.getUser() );
                }
            }
        }

        List<Issue> issues = new LinkedList<Issue>( issueSet );
        Collections.sort( issues, new IssueComparator() );
        add( new StripedListView<User>( "person", users )
        {
            @Override
            protected void populateItem( ListItem<User> listItem )
            {
                super.populateItem( listItem );
                final User user = listItem.getModelObject();
                if ( user.isHiddenInTimeTracking() )
                {
                    listItem.setVisible( false );
                    return;
                }

                listItem.add( new Label( "user", user.getFullnameOrUsername() ) );

                int estimate = 0;
                int worked = 0;
                int remaining = 0;
                for ( Issue issue : issueSet )
                {
                    if ( issue.getAssignee() != null && issue.getAssignee().equals( user ) )
                    {
                        if ( issue.getTimeEstimate() != null && issue.getTimeEstimate().getHours() > 0 )
                        {
                            double e = issue.getTimeEstimate().getHours();
                            estimate += e;

                            if ( burndown )
                            {
                                if (issue.getTimeRequired() != null)
                                {
                                    remaining += issue.getTimeRequired().getHours();
                                }
                            }
                            else
                            {
                                if ( issue.getTimeRequired() != null )
                                {
                                    double r = issue.getTimeRequired().getHours();
                                    double delta = e - r;

                                    if ( delta > 0 )
                                    {
                                        remaining += delta;
                                    }
                                }
                            }
                        }
                    }

                    for ( DurationWorked dur : issue.getTimeWorked() )
                    {
                        if ( user.equals( dur.getUser() ) && dur.getWorked() != null )
                        {
                            worked += dur.getWorked().getHours();
                        }
                    }
                }

                listItem.add( new Label( "estimate", new Duration( estimate ).toHoursWithFractionString() ) );
                listItem.add( new Label( "worked", new Duration( worked ).toHoursWithFractionString() ) );
                listItem.add( new Label( "remaining", new Duration( remaining ).toHoursWithFractionString() ) );
            }
        } );
    }
}
