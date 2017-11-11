/*
 * HeadsUp Agile
 * Copyright 2009-2014 Heads Up Development.
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

import org.headsupdev.agile.app.history.permission.HistoryViewPermission;
import org.headsupdev.agile.web.HeadsUpPage;
import org.headsupdev.agile.web.ApplicationPageMapper;
import org.headsupdev.agile.web.MountPoint;
import org.headsupdev.agile.api.*;
import org.headsupdev.agile.storage.StoredProject;
import org.apache.wicket.markup.html.basic.Label;
import org.headsupdev.agile.web.components.IdPatternValidator;

/**
 * Show the event details - used when a history item is clicked or a permalink from rss etc.
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@MountPoint( "event" )
public class ShowEvent
    extends HeadsUpPage
{
    private Application application = getHeadsUpApplication();

    public void layout()
    {
        if ( !IdPatternValidator.isValidId( getPageParameters().getString( "id" ) ) )
        {
            userError( "Invalid event id" );
            return;
        }

        long id = getPageParameters().getLong( "id" );
        Event event = HistoryApplication.getEvent( id );

        if ( event == null )
        {
            notFoundError();
            return;
        }

        String appId = event.getApplicationId();
        if ( appId == null || appId.equals( "admin" ) )
        {
            appId = "home";
        }
        Application app = ApplicationPageMapper.get().getApplication( appId );
        if ( app != null )
        {
            application = app;
        }
        Project project = event.getProject();
        if ( project == null )
        {
            project = StoredProject.getDefault();
        }
        getPageParameters().remove( "project" );
        getPageParameters().add( "project", project.getId() );

        super.layout();

        add( new Label( "header", event.getBodyHeader() ).setEscapeModelStrings( false ) );

        String body = event.getBody();
        if ( body == null )
        {
            body = "";
        }
        add( new Label( "body", body ).setEscapeModelStrings( false ) );

        addLinks( application.getLinks( project ) );
        for ( MenuLink link : event.getLinks() )
        {
            addLink( link );
        }
    }

    public Permission getRequiredPermission()
    {
        return new HistoryViewPermission();
    }

    /**
     * here we override the default behaviour of returning our own application
     * @return return the application that the event belongs to so we have the right links and buttong highlighted
     */
    public Application getHeadsUpApplication()
    {
        return application;
    }
}
