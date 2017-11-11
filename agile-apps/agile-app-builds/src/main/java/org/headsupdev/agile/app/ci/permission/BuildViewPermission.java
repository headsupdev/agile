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

package org.headsupdev.agile.app.ci.permission;

import org.headsupdev.agile.api.Permission;

import java.util.List;
import java.util.LinkedList;

/**
 * Permission that allows users to view a build
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class BuildViewPermission
    implements Permission
{
    private String id = "BUILD-VIEW";
    private String description = "View build permission";

    private transient List<String> defaultRoles = new LinkedList<String>();

    public BuildViewPermission()
    {
        defaultRoles.add( "anonymous" );
        defaultRoles.add( "member" );
        defaultRoles.add( "administrator" );
    }

    public String getId()
    {
        return id;
    }

    public String getDescription()
    {
        return description;
    }

    public boolean equals( Object permission )
    {
        return permission instanceof Permission && equals( (Permission) permission );
    }

    public boolean equals( Permission permission )
    {
        return id.equals( permission.getId() );
    }

    public List<String> getDefaultRoles()
    {
        return defaultRoles;
    }
}