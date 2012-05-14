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

package org.headsupdev.agile.security.permission;

import org.headsupdev.agile.api.Permission;

import java.util.List;
import java.util.LinkedList;

/**
 * Permission required to add a user to the system - if this is granted to anonymous then users can register themselves
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class AccountCreatePermission
    implements Permission
{
    private String id = "ACCOUNT-CREATE";
    private String description = "Account create permission - required to create a new account, allows anonymous users to register";

    private transient List<String> defaultRoles = new LinkedList<String>();

    public AccountCreatePermission()
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
