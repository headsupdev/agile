/*
 * HeadsUp Agile
 * Copyright 2013 Heads Up Development Ltd.
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

package org.headsupdev.agile.app.dashboard.rest;

import org.apache.wicket.PageParameters;
import org.apache.wicket.model.Model;
import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.api.Permission;
import org.headsupdev.agile.app.dashboard.permission.MemberListPermission;
import org.headsupdev.agile.web.MountPoint;
import org.headsupdev.agile.web.rest.HeadsUpApi;

import java.util.ArrayList;

/**
 * This project API lists all active accounts in the system.
 * <p/>
 * Created: 10/02/2013
 *
 * @author Andrew Williams
 * @since 2.0
 */
@MountPoint("accounts")
public class AccountApi
        extends HeadsUpApi
{
    public AccountApi( PageParameters params )
    {
        super( params );
    }

    @Override
    public Permission getRequiredPermission()
    {
        return new MemberListPermission();
    }

    @Override
    public void doGet( PageParameters params )
    {
        setModel( new Model( new ArrayList( Manager.getSecurityInstance().getRealUsers() ) ) );
    }
}
