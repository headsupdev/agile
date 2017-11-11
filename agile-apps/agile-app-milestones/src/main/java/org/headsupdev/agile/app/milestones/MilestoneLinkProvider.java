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

package org.headsupdev.agile.app.milestones;

import org.headsupdev.agile.api.LinkProvider;
import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.storage.dao.MilestonesDAO;
import org.headsupdev.agile.storage.issues.Milestone;

/**
 * Docs link format for a milestone
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class MilestoneLinkProvider extends LinkProvider
{
    private MilestonesDAO dao = new MilestonesDAO();

    @Override
    public String getId()
    {
        return "milestone";
    }

    public String getPageName()
    {
        return "milestones/view";
    }

    public String getParamName()
    {
        return "id";
    }

    @Override
    public boolean isLinkBroken( String params, Project project )
    {
        Milestone milestone = dao.find(params, project);
        return milestone == null;
    }
}