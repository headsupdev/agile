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

import com.timgroup.jgravatar.Gravatar;
import com.timgroup.jgravatar.GravatarDefaultImage;
import com.timgroup.jgravatar.GravatarDownloadException;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.resource.ByteArrayResource;
import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.api.User;
import org.headsupdev.agile.api.logging.Logger;
import org.headsupdev.agile.web.HeadsUpPage;
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
    private final int ICON_EDGE_LENGTH = 100;
    public UserDetailsPanel( String id, final User user, Project project, boolean showFullDetails, HeadsUpPage page )
    {
        super( id );
        final GravatarLinkPanel gravatarLinkPanel = new GravatarLinkPanel( "gravatar", user, ICON_EDGE_LENGTH, page ){
            @Override
            public Object getLink()
            {
                if ( hasGravatar() )
                {
                    return new ExternalLink( "link", new Gravatar().getUrl( user.getEmail() ) );
                }
                return new WebMarkupContainer( "link" );
            }
        };
        add( gravatarLinkPanel );
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

}
