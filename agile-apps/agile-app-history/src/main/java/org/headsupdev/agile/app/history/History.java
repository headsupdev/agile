/*
 * HeadsUp Agile
 * Copyright 2009-2013 Heads Up Development.
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

import org.headsupdev.agile.api.*;
import org.headsupdev.agile.app.history.permission.HistoryViewPermission;
import org.headsupdev.agile.security.permission.ProjectListPermission;
import org.headsupdev.agile.web.components.filters.ApplicationFilterPanel;
import org.headsupdev.agile.web.components.history.HistoryPanel;
import org.headsupdev.agile.web.*;
import org.headsupdev.agile.storage.StoredProject;

import java.util.*;

import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.PageParameters;

/**
 * History home page
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class History
    extends HeadsUpPage
{
    public static final int ROWS_PER_PAGE = 50;

    private List<String> types;
    private long before;

    private ApplicationFilterPanel filter;

    public Permission getRequiredPermission() {
        return new HistoryViewPermission();
    }

    public void layout()
    {
        super.layout();
        types = new LinkedList<String>();

        add( filter = new ApplicationFilterPanel( "filter", "history", getFilterButton() )
        {
            @Override
            public void onFilterUpdated()
            {
                resetTypes();
            }
        });
        resetTypes();

        final Project project = getProject();
        final boolean allProject = getProject().equals( StoredProject.getDefault() );
        if ( allProject )
        {
            requirePermission( new ProjectListPermission() );
        }

        try
        {
            before = getPageParameters().getLong( "before" );
        }
        catch ( Exception e ) // NumberFormatException or a wicket wrapped NumberFormatException
        {
            before = Long.MAX_VALUE;
        }
        add( new HistoryPanel( "history", new AbstractReadOnlyModel<List<? extends Event>>()
        {
            public List<? extends Event> getObject()
            {
                if ( allProject )
                {
                    return ( (HistoryApplication) getHeadsUpApplication() ).getEvents( before, types, ROWS_PER_PAGE );
                }
                else
                {
                    return ( (HistoryApplication) getHeadsUpApplication() ).getEventsForProject( project, before, types, ROWS_PER_PAGE );
                }
            }
        }, allProject ) );

        boolean more = false;
        List<Event> events;
        if ( allProject )
        {
            events = ( (HistoryApplication) getHeadsUpApplication() ).getEvents( before, types, ROWS_PER_PAGE );
        }
        else
        {
            events = ( (HistoryApplication) getHeadsUpApplication() ).getEventsForProject( project, before, types, ROWS_PER_PAGE );
        }
        long oldest = Long.MAX_VALUE;
        if ( events != null && events.size() > 0 )
        {
            more = events.size() == 50; // TODO we need a better way if figuring if there are more events to see
            oldest = events.get( events.size() - 1 ).getTime().getTime();
        }

        PageParameters params = getProjectPageParameters();
        params.put( "before", oldest );
        if ( more ) {
            addLink( new BookmarkableMenuLink( getClass(), params, "\u25c0 earlier" ) );
        }

        // link to the other view for event listings
        addLink( new SimpleMenuLink( "grouped" ) );
    }

    private void resetTypes()
    {
        types.clear();
        Set<String> allApps = filter.getApplications().keySet();
        for ( String appId : allApps )
        {
            if ( filter.getApplications().get( appId ) )
            {
                if ( appId.equals( "system" ) )
                {
                    types.addAll( ApplicationPageMapper.get().getApplication( "home" ).getEventTypes() );
                }
                else
                {
                    Application app = ApplicationPageMapper.get().getApplication( appId );
                    if ( app != null )
                    {
                        types.addAll( app.getEventTypes() );
                    }
                }
            }
        }
    }

    @Override
    public boolean hasFilter()
    {
        return true;
    }
}
