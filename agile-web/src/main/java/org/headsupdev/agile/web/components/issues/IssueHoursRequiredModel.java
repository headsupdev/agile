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

import org.headsupdev.agile.storage.issues.Duration;
import org.headsupdev.agile.storage.issues.Issue;
import org.apache.wicket.model.Model;

/**
 * A short renderer of hours required for issues.
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class IssueHoursRequiredModel
    extends Model<String>
{
    private Issue issue;
    public IssueHoursRequiredModel( Issue issue )
    {
        this.issue = issue;
    }

    @Override
    public String getObject()
    {
        Duration hours = issue.getTimeRequired();

        if ( hours == null )
        {
            return "";
        }

        return hours.toString();
    }
}
