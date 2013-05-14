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

package org.headsupdev.agile.app.dashboard.rest;

import org.headsupdev.agile.api.rest.Publish;
import org.headsupdev.agile.storage.hibernate.NameProjectId;
import org.headsupdev.agile.storage.issues.Issue;
import org.headsupdev.agile.storage.issues.Milestone;

import java.util.*;

/**
 * A class representing a milestone with just issues assigned to the user included.
 * <p/>
 * Created: 14/05/2013
 *
 * @author Andrew Williams
 * @since 2.0
 */
public class MilestoneWithUserIssues
{
    @Publish
    NameProjectId name;

    @Publish
    String description;

    @Publish
    Date created, updated, start, due, completed;

    @Publish
    private List<Issue> userIssues = new ArrayList<Issue>();

    public MilestoneWithUserIssues( Milestone milestone, List<Issue> userIssues )
    {
        this.name = milestone.getInternalId();
        this.description = milestone.getDescription();

        this.created = milestone.getCreated();
        this.updated = milestone.getUpdated();
        this.start = milestone.getStartDate();
        this.due = milestone.getDueDate();
        this.completed = milestone.getCompletedDate();

        this.userIssues = userIssues;
    }
}
