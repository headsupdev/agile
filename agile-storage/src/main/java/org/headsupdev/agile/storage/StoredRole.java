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

package org.headsupdev.agile.storage;

import org.headsupdev.agile.api.Role;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * The basic implementation of a role class
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@Entity
@Table( name = "Roles" )
@Inheritance( strategy = InheritanceType.SINGLE_TABLE )
@DiscriminatorColumn( name = "type", discriminatorType = DiscriminatorType.STRING )
@DiscriminatorValue( "default" )
public class StoredRole
    implements Role
{
    @Id
    private String id;

    private String comment;

    @org.hibernate.annotations.CollectionOfElements
    @JoinTable( name = "RolePermissions", joinColumns = @JoinColumn( name = "Role_id" ) )
    private Set<String> permissions = new HashSet<String>();

    StoredRole()
    {
    }

    public StoredRole( String id )
    {
        this.id = id;
    }

    public String getId()
    {
        return id;
    }

    public String getComment()
    {
        return comment;
    }

    public void setComment( String comment )
    {
        this.comment = comment;
    }

    public Set<String> getPermissions()
    {
        return permissions;
    }

    public boolean isBuiltin()
    {
        return false;
    }

    public boolean equals( Object role )
    {
        return role instanceof Role && equals( (Role) role );
    }

    public boolean equals( Role role )
    {
        return id.equals( role.getId() );
    }

    public int hashCode()
    {
        return id.hashCode();
    }

    public String toString()
    {
        return "Role " + id;
    }
}