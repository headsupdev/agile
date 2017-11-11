/*
 * HeadsUp Agile
 * Copyright 2009-2015 Heads Up Development.
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

package org.headsupdev.agile.web.components;

import org.headsupdev.agile.api.User;
import org.headsupdev.agile.storage.issues.Issue;
import org.headsupdev.agile.storage.issues.Milestone;
import org.headsupdev.agile.storage.issues.MilestoneComparator;
import org.apache.wicket.markup.html.panel.Panel;
import org.headsupdev.agile.web.model.UserDashboardModel;

import java.util.*;

/**
 * TODO: Document me
 * <p/>
 * Created: 28/08/2011
 *
 * @author Andrew Williams
 * @since 1.0
 */
public class AccountSummaryPanel extends Panel

{
    public AccountSummaryPanel( String id )
    {
        super( id );
    }

    public static List<Milestone> getMilestonesForIssuesAssignedTo( User user )
    {
        List<Milestone> milestones = new ArrayList<Milestone>();

        for ( Issue issue : UserDashboardModel.getIssuesAssignedTo( user ) )
        {
            if ( issue.getMilestone() == null )
            {
                continue;
            }

            if ( !milestones.contains( issue.getMilestone() ) )
            {
                milestones.add( issue.getMilestone() );
            }
        }

        Collections.sort( milestones, new MilestoneComparator() );
        return milestones;
    }

    public static boolean userHasOverdueMilestones( User user )
    {
        List<Milestone> milestones = getMilestonesForIssuesAssignedTo( user );
        if ( milestones.size() == 0 )
        {
            return false;
        }

        Date oldestDue = milestones.get( 0 ).getDueDate();
        return oldestDue != null && oldestDue.before( new Date() );
    }

    public static boolean userHasDueSoonMilestones( User user )
    {
        List<Milestone> milestones = getMilestonesForIssuesAssignedTo( user );
        if ( milestones.size() == 0 )
        {
            return false;
        }

        Date oldestDue = milestones.get( 0 ).getDueDate();
        // TODO move this to a central place, duplicated in MilestonesApplication
        return oldestDue != null && oldestDue.before( new Date( System.currentTimeMillis() + ( 1000L * 60 * 60 * 24 * 14 ) ) );
    }
}
