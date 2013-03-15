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

import org.headsupdev.agile.storage.issues.Duration;
import org.headsupdev.agile.storage.resource.DurationWorked;
import org.headsupdev.agile.storage.issues.Issue;
import org.apache.wicket.model.Model;

/**
 * A short renderer of hours worked for issues.
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 2.0
 */
public class IssueHoursWorkedModel
    extends Model<String>
{
    private Issue issue;

    public IssueHoursWorkedModel( Issue issue )
    {
        this.issue = issue;
    }

    @Override
    public String getObject()
    {
        double hours = 0;

        for ( DurationWorked worked : issue.getTimeWorked() )
        {
            if ( worked.getWorked() == null )
            {
                continue;
            }

            hours += worked.getWorked().getHours();
        }

        return new Duration( hours ).toString();
    }
}
