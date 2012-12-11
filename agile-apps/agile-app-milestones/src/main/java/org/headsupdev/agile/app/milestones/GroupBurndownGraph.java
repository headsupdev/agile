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

import org.headsupdev.agile.storage.DurationWorkedUtil;
import org.headsupdev.agile.storage.issues.Duration;
import org.headsupdev.agile.storage.issues.Issue;
import org.headsupdev.agile.storage.issues.MilestoneGroup;
import org.headsupdev.agile.web.MountPoint;

import java.util.Date;
import java.util.Set;

/**
 * A graph of the milestone group burndown over time or time spent on a project to date.
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 2.0
 */
@MountPoint("groupburndown.png")
public class GroupBurndownGraph
        extends BurndownGraph
{
    private MilestoneGroup getMilestoneGroup()
    {
        String groupId = getParameters().getString( "id" );
        if ( groupId == null || groupId.length() == 0 )
        {
            return null;
        }

        return MilestonesApplication.getMilestoneGroup( groupId, getProject() );
    }

    protected Set<Issue> getIssues()
    {
        return getMilestoneGroup().getIssues();
    }

    protected double getTotalHoursForDay( Date day )
    {
        double total = 0;
        if ( getMilestoneGroup() == null )
        {
            return total;
        }

        for ( Issue issue : getIssues() )
        {
            total += DurationWorkedUtil.totalWorkedForDay( issue, day ).getHours();
        }

        return total;
    }

    protected java.util.List<Date> getDates()
    {
        return DurationWorkedUtil.getMilestoneGroupDates( getMilestoneGroup(), true );
    }

    protected Duration[] getEffort()
    {
        return DurationWorkedUtil.getMilestoneGroupEffortRequired( getMilestoneGroup() );
    }
}
