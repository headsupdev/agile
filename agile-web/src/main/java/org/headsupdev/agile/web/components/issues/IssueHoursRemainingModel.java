/*
 * HeadsUp Agile
 * Copyright 2009-2012 Heads Up Development.
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

package org.headsupdev.agile.web.components.issues;

import org.headsupdev.agile.storage.StoredProject;
import org.headsupdev.agile.storage.issues.Duration;
import org.headsupdev.agile.storage.issues.Issue;
import org.apache.wicket.model.Model;

/**
 * A short renderer of hours remaining for issues.
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class IssueHoursRemainingModel
    extends Model<String>
{
    private Issue issue;

    public IssueHoursRemainingModel( Issue issue )
    {
        this.issue = issue;
    }

    @Override
    public String getObject()
    {
        final boolean timeBurndown = Boolean.parseBoolean( issue.getProject().getConfigurationValue(
                StoredProject.CONFIGURATION_TIMETRACKING_BURNDOWN ) );

        double hoursRemaining = 0;
        if ( timeBurndown )
        {
            Duration duration = issue.getTimeRequired();
            if ( duration == null )
            {
                duration = issue.getTimeEstimate();
            }

            if ( duration == null )
            {
                hoursRemaining = 0;
            }
            else
            {
                hoursRemaining = duration.getHours();
            }
        }
        else
        {
            double hoursEstimate = 0;
            if ( issue.getTimeEstimate() != null )
            {
                hoursEstimate = issue.getTimeEstimate().getHours();
            }
            if ( issue.getTimeRequired() != null )
            {
                hoursRemaining = hoursEstimate - issue.getTimeRequired().getHours();
                if ( hoursRemaining < 0 )
                {
                    hoursRemaining = 0;
                }
            }
        }

        Duration remaining = new Duration( hoursRemaining );
        return remaining.toString();
    }
}
