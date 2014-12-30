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

package org.headsupdev.agile.web.components;

import com.timgroup.jgravatar.Gravatar;
import com.timgroup.jgravatar.GravatarDownloadException;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.api.User;
import org.headsupdev.agile.web.ApplicationPageMapper;
import org.headsupdev.agile.web.HeadsUpSession;

/**
 * TODO document me
 *
 * @author Andrew Williams
 * @since 1.0
 */
public class UserDetailsPanel
        extends Panel
{
    private static final int ICON_EDGE_LENGTH = 64;
    private final GravatarLinkPanel gravatarLinkPanel;
    private User user;

    public UserDetailsPanel( String id, final User user, Project project, boolean showFullDetails )
    {
        super( id );
        this.user = user;
        gravatarLinkPanel = new GravatarLinkPanel( "gravatar", user, ICON_EDGE_LENGTH )
        {
            @Override
            public Object getLink()
            {
                if ( showCheckbox() && user.getPreference( "gravatar.show", true ) )
                {
                    return new ExternalLink( "link", new Gravatar().getUrl( user.getEmail() ) );
                }
                PageParameters params = new PageParameters();
                params.add( "username", user.getUsername() );
                params.add( "silent", "true" );
                return new BookmarkablePageLink( "link", ApplicationPageMapper.get().getPageClass( "account" ), params );
            }

            @Override
            public boolean displayHoverText()
            {
                return false;
            }
        };
        add( gravatarLinkPanel );


        AjaxCheckBox checkBox = new AjaxCheckBox( "displayGravatar", new Model<Boolean>()
        {
            @Override
            public void setObject( Boolean object )
            {
                user.setPreference( "gravatar.show", object );
            }

            @Override
            public Boolean getObject()
            {
                return user.getPreference( "gravatar.show", true );
            }
        } )
        {
            @Override
            protected void onUpdate( AjaxRequestTarget ajaxRequestTarget )
            {
                setResponsePage( ApplicationPageMapper.get().getPageClass( "account" ) );
            }
        };
        WebMarkupContainer container = new WebMarkupContainer( "checkboxContainer" );
        container.add( checkBox );
        boolean isCurrentUser = user.equals( ( (HeadsUpSession) getSession() ).getUser() ) && !user.equals( HeadsUpSession.ANONYMOUS_USER );
        add( container.setVisible( isCurrentUser && showCheckbox() ) );

        add( new Label( "username", user.getUsername() ) );
        add( new Label( "firstname", user.getFirstname() ) );
        add( new Label( "lastname", user.getLastname() ) );
        add( new Label( "email", user.getEmail() ).setVisible( showFullDetails ) );
        add( new Label( "telephone", user.getTelephone() ).setVisible( showFullDetails ) );
        add( new Label( "last", new FormattedDateModel( user.getLastLogin(),
                ( (HeadsUpSession) getSession() ).getTimeZone() ) ) );
        add( new Label( "description", new MarkedUpTextModel( user.getDescription(), project ) )
                .setEscapeModelStrings( false ) );
    }

    private boolean showCheckbox()
    {
        byte[] avatarBytes = null;
        try
        {
            avatarBytes = new Gravatar().download( user.getEmail() );
        }
        catch ( GravatarDownloadException e )
        {
        }
        return avatarBytes != null;
    }

}
