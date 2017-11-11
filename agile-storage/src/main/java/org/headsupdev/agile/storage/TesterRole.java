/*
 * HeadsUp Agile
 * Copyright 2017 Heads Up Development.
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

import javax.persistence.Entity;
import javax.persistence.DiscriminatorValue;

/**
 * The role for users that are just testing.
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 2.2
 */
@Entity
@DiscriminatorValue( "tester" )
public class TesterRole
    extends StoredRole
{
    public TesterRole()
    {
        super( "tester" );

        getPermissions().add( "REPOSITORY-READ-APPS" );
    }

    public String getComment()
    {
        return "The role that marks a user that is only testing";
    }

    @Override
    public boolean isBuiltin()
    {
        return true;
    }
}
