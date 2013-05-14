/*
 * HeadsUp Agile
 * Copyright 2013 Heads Up Development Ltd.
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

package org.headsupdev.agile.web.model;

import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.api.User;
import org.headsupdev.agile.storage.HibernateStorage;
import org.headsupdev.agile.storage.issues.Issue;
import org.headsupdev.agile.storage.issues.Milestone;
import org.headsupdev.agile.storage.issues.MilestoneComparator;
import org.hibernate.Query;
import org.hibernate.Session;

import java.io.Serializable;
import java.util.*;

/**
 * A model representing the main data in a user's dashboard
 * <p/>
 * Created: 08/05/2013
 *
 * @author Andrew Williams
 * @since 2.0
 */
public class UserDashboardModel
    implements Serializable
{
    private Map<Milestone,List<Issue>> userMilestoneIssues;
    private List<Issue> userNoMilestoneIssues;

    private User user;

    public UserDashboardModel( User user )
    {
        this.user = user;

        userMilestoneIssues = new HashMap<Milestone,List<Issue>>();
        userNoMilestoneIssues = new ArrayList<Issue>();

        for ( Issue issue : getIssuesAssignedTo( user ) )
        {
            if ( issue.getMilestone() == null )
            {
                userNoMilestoneIssues.add( issue );
                continue;
            }

            Milestone milestone = issue.getMilestone();
            List<Issue> milestoneIssues = userMilestoneIssues.get( milestone );
            if ( milestoneIssues == null )
            {
                milestoneIssues = new ArrayList<Issue>();
                userMilestoneIssues.put( milestone, milestoneIssues );
            }

            milestoneIssues.add( issue );
        }
    }

    public List<Milestone> getMilestones()
    {
        List<Milestone> milestones = new ArrayList<Milestone>();
        milestones.addAll( userMilestoneIssues.keySet() );
        Collections.sort( milestones, new MilestoneComparator() );

        return milestones;
    }

    public List<Issue> getIssuesInMilestone( Milestone milestone )
    {
        if ( milestone == null )
        {
            return userNoMilestoneIssues;
        }

        return userMilestoneIssues.get( milestone );
    }

    protected List<Issue> getIssuesAssignedTo( User user )
    {
        Session session = ( (HibernateStorage) Manager.getStorageInstance() ).getHibernateSession();

        Query q = session.createQuery( "from Issue i where status < 250 and assignee = :user order by priority, status" );
        q.setEntity( "user", user );
        List<Issue> list = q.list();

        // force loading...
        for ( Issue issue : list )
        {
            if ( issue.getMilestone() != null )
            {
                // force a load
                issue.getMilestone().getIssues().size();
            }
            issue.getAttachments().size();
        }

        return list;
    }
}
