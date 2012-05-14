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

package org.headsupdev.agile.app.history;

import org.headsupdev.agile.api.Event;
import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.api.Permission;
import org.headsupdev.agile.app.history.permission.HistoryViewPermission;
import org.headsupdev.agile.web.HeadsUpPage;
import org.headsupdev.agile.web.MountPoint;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.model.Model;

/**
 * Show the content of the event (with style information) used for embedding in external applications etc.
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@MountPoint( "eventbody" )
public class ShowEventBody
    extends HeadsUpPage
{
    public void layout()
    {
        long id = getPageParameters().getLong( "id" );
        Event event = HistoryApplication.getEvent( id );

        if ( event == null )
        {
            add( new WebMarkupContainer( "header" ).setVisible( false ) );
            add( new Label( "body", "The event " + id + " was not found" ) );
            return;
        }

        WebMarkupContainer base = new WebMarkupContainer( "base" );
        base.add( new AttributeModifier( "href", new Model<String>()
        {
            public String getObject()
            {
                return Manager.getStorageInstance().getGlobalConfiguration().getBaseUrl();
            }
        } ) );
        add( base );

        add( CSSPackageResource.getHeaderContribution( HeadsUpPage.class, "embed.css" ) );
        add( new Label( "header", event.getBodyHeader() ).setEscapeModelStrings( false ) );

        String body = event.getBody();
        if ( body == null ) {
            body = "";
        }
        add( new Label( "body", body ).setEscapeModelStrings( false ) );
    }

    public Permission getRequiredPermission() {
        return new HistoryViewPermission();
    }
}