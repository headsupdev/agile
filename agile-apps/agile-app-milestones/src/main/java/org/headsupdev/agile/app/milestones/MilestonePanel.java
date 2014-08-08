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

import org.headsupdev.agile.storage.issues.Issue;
import org.headsupdev.agile.storage.issues.Milestone;

import java.util.Set;

/**
 * Panel to render the details of a milestone.
 *
 * @author Andrew Williams
 * @since 1.0
 */
public class MilestonePanel
        extends IssueSetPanel
{
    private Milestone milestone;

    public MilestonePanel( String id, Milestone milestone )
    {
        super( id );
        this.milestone = milestone;

        layout( milestone.getName(), milestone.getDescription(), milestone.getProject(), milestone.getCreated(), milestone.getUpdated(),
                milestone.getStartDate(), milestone.getDueDate(), milestone.getCompletedDate(), milestone.getGroup() );
    }

    public Milestone getMilestone()
    {
        return milestone;
    }

    @Override
    protected double getCompleteness()
    {
        return milestone.getCompleteness();
    }

    @Override
    protected Set<Issue> getReOpenedIssues()
    {
        return milestone.getReOpenedIssues();
    }

    @Override
    protected Set<Issue> getOpenIssues()
    {
        return milestone.getOpenIssues();
    }

    @Override
    protected Set<Issue> getIssues()
    {
        return milestone.getIssues();
    }

    @Override
    protected String getType()
    {
        return "Milestone";
    }
}
