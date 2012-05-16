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

package org.headsupdev.agile.web.components.issues;

import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.storage.StoredProject;
import org.headsupdev.agile.storage.issues.Duration;
import org.headsupdev.agile.storage.issues.Issue;
import org.apache.wicket.model.Model;

import java.util.Iterator;
import java.util.List;

/**
 * A short renderer of total hour estimates / durations for a list of issues.
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class IssueTotalHoursModel
    extends Model<String>
{
    private Project root;
    private double totalHours;

    public IssueTotalHoursModel( List<? extends Issue> issues, Project root )
    {
        this.root = root;
        setIssues( issues.iterator() );
    }

    public IssueTotalHoursModel( Iterator<? extends Issue> issues, Project root )
    {
        this.root = root;
        setIssues( issues );
    }
    
    @Override
    public String getObject()
    {
        return new Duration( totalHours ).toString();
    }

    public void setIssues( Iterator<? extends Issue> issues )
    {
        calculateHours( issues );
    }

    private void calculateHours( Iterator<? extends Issue> issues )
    {
        final boolean timeBurndown = Boolean.parseBoolean( root.getConfigurationValue(
                StoredProject.CONFIGURATION_TIMETRACKING_BURNDOWN ) );
        totalHours = 0;

        while ( issues.hasNext() )
        {
            Issue issue = issues.next();

            if ( timeBurndown )
            {
                Duration duration = issue.getTimeRequired();
                if ( duration == null )
                {
                    duration = issue.getTimeEstimate();
                }

                if ( duration != null )
                {
                    totalHours += duration.getHours();
                }
            }
            else
            {
                double hoursEstimate = 0;
                if ( issue.getTimeEstimate() != null )
                {
                    issue.getTimeEstimate().getHours();
                }
                if ( issue.getTimeRequired() != null )
                {
                    double remain = hoursEstimate - issue.getTimeRequired().getHours();

                    // remain could be < 0 - ignore this!
                    if ( remain > 0 )
                    {
                        totalHours += remain;
                    }
                }
            }
        }
    }
}
