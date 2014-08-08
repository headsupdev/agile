/*
 * HeadsUp Agile
 * Copyright 2009-2014 Heads Up Development Ltd.
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

package org.headsupdev.agile.app.admin;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.api.Permission;
import org.headsupdev.agile.api.Role;
import org.headsupdev.agile.security.DefaultSecurityManager;
import org.headsupdev.agile.security.permission.AdminPermission;
import org.headsupdev.agile.storage.StoredRole;
import org.headsupdev.agile.web.HeadsUpPage;
import org.headsupdev.agile.web.MountPoint;
import org.headsupdev.agile.web.components.OnePressButton;

/**
 * TODO document me
 *
 * @author Andrew Williams
 * @since 1.0
 */
@MountPoint("add-role")
public class AddRole
        extends HeadsUpPage
{
    private String roleid, comment;

    public Permission getRequiredPermission()
    {
        return new AdminPermission();
    }

    @Override
    public void layout()
    {
        super.layout();

        Form addRoleForm = new Form( "addrole", new CompoundPropertyModel( this ) )
        {
            @Override
            protected void onSubmit()
            {
                Role role = Manager.getSecurityInstance().getRoleById( roleid );

                if ( role != null )
                {
                    info( "A role with that id aready exists, please choose another" );
                    return;
                }

                role = new StoredRole( roleid );
                ( (StoredRole) role ).setComment( comment );

                ( (DefaultSecurityManager) Manager.getSecurityInstance() ).addRole( role );
                setResponsePage( Permissions.class );
            }
        };
        addRoleForm.add( new TextField( "roleid" ) );
        addRoleForm.add( new TextField( "comment" ) );
        addRoleForm.add( new OnePressButton( "submitRole" ) );

        add( addRoleForm );
    }

    @Override
    public String getTitle()
    {
        return "Add Role";
    }
}