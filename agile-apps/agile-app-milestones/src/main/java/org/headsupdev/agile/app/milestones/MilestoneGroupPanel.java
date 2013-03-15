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
import org.headsupdev.agile.storage.issues.MilestoneGroup;

import java.util.Set;

/**
 * Panel to render the details of a milestone group
 * <p/>
 * Created: 11/12/2012
 *
 * @author Andrew Williams
 * @since 2.0
 */
public class MilestoneGroupPanel
    extends IssueSetPanel
{
    private MilestoneGroup group;

    public MilestoneGroupPanel( String id, MilestoneGroup group )
    {
        super( id );
        this.group = group;

        layout( group.getName(), group.getDescription(), group.getProject(), group.getCreated(), group.getUpdated(),
                group.getStartDate(), group.getDueDate(), group.getCompletedDate() );
    }

    @Override
    protected double getCompleteness()
    {
        return group.getCompleteness();
    }

    @Override
    protected Set<Issue> getReOpenedIssues()
    {
        return group.getReOpenedIssues();
    }

    @Override
    protected Set<Issue> getOpenIssues()
    {
        return group.getOpenIssues();
    }

    @Override
    protected Set<Issue> getIssues()
    {
        return group.getIssues();
    }

    @Override
    protected String getType()
    {
        return "MilestoneGroup";
    }
}
