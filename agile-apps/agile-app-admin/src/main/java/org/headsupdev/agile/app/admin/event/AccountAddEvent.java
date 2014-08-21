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

package org.headsupdev.agile.app.admin.event;

import org.headsupdev.agile.app.admin.AdminApplication;
import org.headsupdev.agile.web.HeadsUpPage;
import org.headsupdev.agile.web.components.UserDetailsPanel;
import org.headsupdev.agile.web.RenderUtil;
import org.headsupdev.agile.web.AbstractEvent;
import org.headsupdev.agile.api.User;
import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.api.Manager;

import javax.persistence.Entity;
import javax.persistence.DiscriminatorValue;
import java.util.Date;

import org.apache.wicket.markup.html.panel.Panel;

/**
 * TODO enter description
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@Entity
@DiscriminatorValue( "accountadd" )
public class AccountAddEvent
    extends AbstractEvent
{
    AccountAddEvent()
    {
    }

    public AccountAddEvent( User user )
    {
        super( "Account " + user.getUsername() + " was added",
               "An account was created for \"" + user.getFullname() + "\" (" + user.getUsername() + ")", new Date() );

        setApplicationId( AdminApplication.ID );
        setObjectId( user.getUsername() );
    }

    private String renderUser( final User user, final Project project )
    {
        return new RenderUtil()
        {
            public Panel getPanel()
            {
                return new UserDetailsPanel( RenderUtil.PANEL_ID, user, project, false);
            }
        }.getRenderedContent();
    }

    public String getBodyHeader()
    {
        return "<link rel=\"stylesheet\" type=\"text/css\" href=\"/resources/org.headsupdev.agile.app.dashboard.Account/account.css\" />";
    }

    public String getBody()
    {
        User user = Manager.getSecurityInstance().getUserByUsername( getObjectId() );
        if ( user == null )
        {
            return "<p>User " + getObjectId() + " could not be found</p>";
        }

        return renderUser( user, getProject() );
    }
}
