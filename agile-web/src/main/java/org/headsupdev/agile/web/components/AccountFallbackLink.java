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

package org.headsupdev.agile.web.components;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.api.User;
import org.headsupdev.agile.web.RenderUtil;

/**
 * A very handy panel that shows a user link if the user is found or a label otherwise.
 *
 * @author Andrew Williams
 * @since 2.0
 */
public class AccountFallbackLink
    extends Panel
{
    public AccountFallbackLink( String id, String userString )
    {
        this( id, userString, Manager.getSecurityInstance().getUserByUsernameEmailOrFullname( userString ) );
    }

    public AccountFallbackLink( String id, User user )
    {
        this( id, user == null ? null : user.getFullnameOrUsername(), user );
    }

    protected AccountFallbackLink( String id, String userString, User user )
    {
        super( id );
        if ( userString == null )
        {
            setVisible( false );
            return;
        }

        Label authorLabel = new Label( "author-label", userString );
        Link authorLink;
        if ( user != null )
        {
            PageParameters params = new PageParameters();
            params.add( "username", user.getUsername() );
            authorLink = new BookmarkablePageLink( "author-link", RenderUtil.getPageClass("account"), params );

            authorLabel = new Label( "author-label", user.getFullnameOrUsername() );
            authorLink.add( authorLabel );
            add( authorLink );
            add( new WebMarkupContainer( "author-label" ).setVisible( false ) );
        }
        else
        {
            add( new WebMarkupContainer( "author-link" ).setVisible( false ) );
            add( authorLabel );
        }
    }
}
