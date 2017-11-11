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

package org.headsupdev.agile.framework;

import org.headsupdev.agile.api.Permission;
import org.headsupdev.agile.core.DefaultManager;
import org.headsupdev.agile.core.UpdateDetails;
import org.headsupdev.agile.security.permission.AdminPermission;
import org.headsupdev.agile.web.HeadsUpPage;
import org.headsupdev.agile.web.MountPoint;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.link.Link;

/**
 * The HeadsUp software updates page.
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@MountPoint( "updates" )
public class Updates
    extends HeadsUpPage
{
    public Permission getRequiredPermission()
    {
        return null;
    }

    public void layout()
    {
        super.layout();
        add( CSSPackageResource.getHeaderContribution( getClass(), "updates.css" ) );

        WebMarkupContainer updating = new UpdatingPanel( "updating", HomeApplication.getHeadsUpRuntime() );
        add( updating );

        WebMarkupContainer login = new WebMarkupContainer( "login" );
        add( login.setVisible( ( (DefaultManager) getManager() ).getAvailableUpdates().size() > 0 &&
            !getSecurityManager().userHasPermission( getSession().getUser(), new AdminPermission(), null ) ) );

        add( new ListView<UpdateDetails>( "update", ( (DefaultManager) getManager() ).getAvailableUpdates() )
        {
            protected void populateItem( ListItem<UpdateDetails> listItem )
            {
                UpdateDetails update = listItem.getModelObject();

                listItem.add( new Label( "title", update.getTitle() ) );
                listItem.add( new Label( "details", update.getDetails() ).setEscapeModelStrings( false ) );
            }
        } );
        add( new WebMarkupContainer( "noupdates" ).setVisible( !getManager().isUpdateAvailable() ) );

        add( new Link( "checknow" )
        {
            public void onClick()
            {
                ( (DefaultManager) getManager() ).checkForUpdates();
            }
        }.setVisible( getSecurityManager().userHasPermission( getSession().getUser(), new AdminPermission(), null ) ) );
    }

    @Override
    public String getTitle()
    {
        return "Software Updates";
    }
}