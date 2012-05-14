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

package org.headsupdev.agile.app.dashboard;

import org.headsupdev.agile.api.User;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ResourceReference;
import org.headsupdev.agile.api.Permission;
import org.headsupdev.agile.app.dashboard.permission.MemberListPermission;
import org.headsupdev.agile.web.MountPoint;
import org.headsupdev.agile.web.HeadsUpPage;
import org.headsupdev.agile.web.HeadsUpSession;

import java.util.List;
import java.util.Collections;

/**
 * A simple members page, listing the current members.
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@MountPoint( "accounts" )
public class Accounts
    extends HeadsUpPage
{
    public Permission getRequiredPermission()
    {
        return new MemberListPermission();
    }

    public void layout()
    {
        super.layout();
        add( CSSPackageResource.getHeaderContribution( getClass(), "welcome.css" ) );

        List<org.headsupdev.agile.api.User> users = getSecurityManager().getUsers();
        Collections.sort( users );

        add( new ListView<User>( "users", users )
        {
            protected void populateItem( ListItem<User> listItem )
            {
                User user = listItem.getModelObject();
                if ( !user.canLogin() )
                {
                    listItem.setVisible( false );
                    return;
                }

                if ( user.equals( HeadsUpSession.ANONYMOUS_USER ) ) {
                    listItem.setVisible( false );
                    return;
                }

                listItem.add( new Label( "fullname", user.getFullname() ) );
                PageParameters params = new PageParameters();
                params.add( "username", user.getUsername() );
                params.add( "silent", "true" );
                BookmarkablePageLink link = new BookmarkablePageLink( "user-link", getPageClass( "account" ), params );
                link.add( new Image( "activity", new ResourceReference( "member.png" ), params ) );
                listItem.add( link );

                link = new BookmarkablePageLink( "user-link2", getPageClass( "account" ), params );
                link.add( new Label( "user-label", user.getUsername() ) );
                listItem.add( link );
            }
        } );
    }

    @Override
    public String getTitle()
    {
        return "Accounts";
    }
}
