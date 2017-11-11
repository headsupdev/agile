/*
 * HeadsUp Agile
 * Copyright 2013 Heads Up Development.
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

package org.headsupdev.agile.app.dashboard.rest;

import org.apache.wicket.PageParameters;
import org.apache.wicket.model.Model;
import org.headsupdev.agile.api.Permission;
import org.headsupdev.agile.api.User;
import org.headsupdev.agile.api.rest.Publish;
import org.headsupdev.agile.app.dashboard.permission.MemberViewPermission;
import org.headsupdev.agile.storage.issues.Issue;
import org.headsupdev.agile.storage.issues.Milestone;
import org.headsupdev.agile.web.HeadsUpSession;
import org.headsupdev.agile.web.MountPoint;
import org.headsupdev.agile.web.model.UserDashboardModel;
import org.headsupdev.agile.web.rest.HeadsUpApi;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * An API that provides a grouped list of all issues assigned to the current user.
 * Grouping is by milestone for issues in a milestone and the other issues in a simple list.
 * <p/>
 * Created: 08/05/2013
 *
 * @author Andrew Williams
 * @since 2.0
 */
@MountPoint( "userdashboard" )
public class UserDashboardApi
        extends HeadsUpApi
{
    public UserDashboardApi( PageParameters params )
    {
        super( params );
    }

    @Override
    public Permission getRequiredPermission()
    {
        return new MemberViewPermission();
    }

    @Override
    public void doGet( PageParameters pageParameters )
    {
        setModel( new Model<UserDashboardSummary>( new UserDashboardSummary( ( (HeadsUpSession) getSession() ).getUser() ) ) );
    }

    protected class UserDashboardSummary
        implements Serializable
    {
        @Publish
        private List<MilestoneWithUserIssues> milestones;

        @Publish
        private List<Issue> otherIssues;

        public UserDashboardSummary( User user )
        {
            milestones = new ArrayList<MilestoneWithUserIssues>();
            UserDashboardModel model = new UserDashboardModel( user );
            for ( Milestone milestone : model.getMilestones() )
            {
                milestones.add( new MilestoneWithUserIssues( milestone, model.getIssuesInMilestone( milestone ) ) );
            }

            otherIssues = model.getIssuesInMilestone( null );
        }
    }
}
