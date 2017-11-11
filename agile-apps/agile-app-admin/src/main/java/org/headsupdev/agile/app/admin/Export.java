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

package org.headsupdev.agile.app.admin;

import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.api.Permission;
import org.headsupdev.agile.security.permission.AdminPermission;
import org.headsupdev.agile.storage.HibernateUtil;
import org.headsupdev.agile.web.HeadsUpPage;
import org.headsupdev.agile.web.MountPoint;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.basic.Label;
import org.hibernate.jdbc.Work;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Export database page
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 2.0
 */
@MountPoint( "export" )
public class Export
    extends HeadsUpPage
{
    public Permission getRequiredPermission()
    {
        return new AdminPermission();
    }

    public void layout()
    {
        super.layout();
        add( CSSPackageResource.getHeaderContribution( getClass(), "admin.css" ) );

        final String exportScript = Manager.getStorageInstance().getDataDirectory() + "/agile-export.sql";
        add( new Label( "location", exportScript ) );

        new Thread()
        {
            @Override
            public void run() {
                HibernateUtil.getCurrentSession().doWork( new Work()
                {
                    public void execute( Connection connection ) throws SQLException
                    {
                        connection.prepareStatement( "SCRIPT TO '" + exportScript + "'" ).execute();
                    }
                } );
            }
        }.start();
    }

    @Override
    public String getTitle()
    {
        return "Export";
    }
}
