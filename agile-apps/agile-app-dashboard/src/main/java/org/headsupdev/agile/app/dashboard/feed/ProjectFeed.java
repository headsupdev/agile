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

package org.headsupdev.agile.app.dashboard.feed;

import org.headsupdev.agile.api.*;
import org.headsupdev.agile.security.permission.ProjectListPermission;
import org.headsupdev.agile.web.MountPoint;
import org.headsupdev.agile.web.feed.*;

/**
 * A simple feed page that reports the loaded projects
 * This has been replaced by the new JSON API
 *
 * @deprecated
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@MountPoint( "project-feed.xml" )
public class ProjectFeed
   extends AbstractFeed
{
    private Storage storage = Manager.getStorageInstance();

    public Permission getRequiredPermission() {
        return new ProjectListPermission();
    }
}
