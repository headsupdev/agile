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

package org.headsupdev.agile.app.files;

import org.headsupdev.agile.api.LinkProvider;
import org.headsupdev.agile.api.Project;

/**
 * Docs link format for a file's history in the scm app
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class ChangeLogLinkProvider extends LinkProvider
{
    @Override
    public String getId()
    {
        return "changelog";
    }

    public String getPageName()
    {
        return "files/history";
    }

    public String getParamName()
    {
        return "path";
    }

    @Override
    public boolean isLinkBroken( String params, Project project )
    {
        return !BrowseApplication.getFileExists( project, params );
    }
}
