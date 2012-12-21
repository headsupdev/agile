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

import org.headsupdev.agile.api.LinkProvider;
import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.storage.issues.MilestoneGroup;

/**
 * Docs link format for a milestone group
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 2.0
 */
public class MilestoneGroupLinkProvider
        extends LinkProvider
{
    @Override
    public String getId()
    {
        return "milestonegroup";
    }

    public String getPageName()
    {
        return "milestones/viewgroup";
    }

    public String getParamName()
    {
        return "id";
    }

    @Override
    public boolean isLinkBroken( String params, Project project )
    {
        MilestoneGroup group = MilestonesApplication.getMilestoneGroup( params, project );
        return group == null;
    }
}