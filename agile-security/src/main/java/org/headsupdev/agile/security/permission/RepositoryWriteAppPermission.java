/*
 * HeadsUp Agile
 * Copyright 2012 Heads Up Development.
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

package org.headsupdev.agile.security.permission;

import org.headsupdev.agile.api.Permission;

import java.util.LinkedList;
import java.util.List;

/**
 * TODO enter description
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 2.2
 */
public class RepositoryWriteAppPermission
    implements Permission
{
    private String id = "REPOSITORY-WRITE-APPS";
    private String description = "Write apps repository permission";

    private transient List<String> defaultRoles = new LinkedList<String>();

    public RepositoryWriteAppPermission()
    {
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
