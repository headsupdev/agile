/*
 * HeadsUp Agile
 * Copyright 2012 Heads Up Development Ltd.
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

package org.headsupdev.agile.app.artifacts;

import org.headsupdev.agile.api.Permission;
import org.headsupdev.agile.security.permission.RepositoryReadAppPermission;
import org.headsupdev.agile.web.MountPoint;

/**
 * Repository browser apps browse page
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 2.2
 */
@MountPoint( "apps" )
public class AppsRepository
    extends ReleaseRepository
{
    public Permission getRequiredPermission()
    {
        return new RepositoryReadAppPermission();
    }

    public String getRepositoryName()
    {
        return "Apps";
    }

    public String getRepositoryId()
    {
        return "apps";
    }
}