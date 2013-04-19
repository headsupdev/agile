/*
 * HeadsUp Agile
 * Copyright 2013 Heads Up Development Ltd.
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

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.headsupdev.agile.api.Application;
import org.headsupdev.agile.api.Event;
import org.headsupdev.agile.api.Permission;
import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.app.history.permission.HistoryViewPermission;
import org.headsupdev.agile.security.permission.ProjectListPermission;
import org.headsupdev.agile.storage.StoredProject;
import org.headsupdev.agile.web.*;
import org.headsupdev.agile.web.components.filters.ApplicationFilterPanel;
import org.headsupdev.agile.web.components.history.HistoryPanel;

import java.util.*;

@MountPoint( "grouped" )
public class GroupedActivity
        extends HeadsUpPage
{
    public static final int ROWS_IN_GROUP = 10;

    private List<String> appIds;
    private Map<String, List<String>> types;
    private long before;

    private ApplicationFilterPanel filter;

    public Permission getRequiredPermission() {
        return new HistoryViewPermission();
    }

    public void layout()
    {
        super.layout();

        final Project project = getProject();
        final boolean allProject = getProject().equals( StoredProject.getDefault() );
        if ( allProject )
        {
            requirePermission( new ProjectListPermission() );
        }

        types = new HashMap<String, List<String>>();
        appIds = new ArrayList<String>();
        add( filter = new ApplicationFilterPanel( "filter", "history-grouped" )
        {
            @Override
            public void onFilterUpdated()
            {
                resetFilter();
            }
        });
        resetFilter();

        try
        {
            before = getPageParameters().getLong( "before" );
        }
        catch ( Exception e ) // NumberFormatException or a wicket wrapped NumberFormatException
        {
            before = Long.MAX_VALUE;
        }

        add( new ListView<String>( "group", appIds )
        {
            protected void populateItem( final ListItem<String> listItem )
            {
                final String appId = listItem.getModelObject();
                listItem.add( new Label( "app-label", appId ) );

                final List<String> appTypes = types.get( appId );
                listItem.add(new HistoryPanel( "history", new AbstractReadOnlyModel<List<? extends Event>>() {
                    public List<? extends Event> getObject() {
                        if (allProject) {
                            return ((HistoryApplication) getHeadsUpApplication()).getEvents( before, appTypes, ROWS_IN_GROUP );
                        } else {
                            return ((HistoryApplication) getHeadsUpApplication()).getEventsForProject( project, before, appTypes, ROWS_IN_GROUP );
                        }
                    }
                }, allProject ) );
            }
        } );

        // link to the main view for event listings
        addLink( new BookmarkableMenuLink( History.class, null, "list" ) );
    }


    private void resetFilter()
    {
        appIds.clear();
        for ( String appId : filter.getApplications().keySet() )
        {
            if ( filter.getApplications().get( appId ) )
            {
                appIds.add( appId );
            }
        }

        Collections.sort( appIds, new ApplicationIdComparator() );
        if ( appIds.get( 0 ).equals( "system" ) )
        {
            String homeId = appIds.remove( 0 );
            appIds.add( appIds.size(), homeId );
        }

        types.clear();
        for ( String appId : appIds )
        {
            if ( filter.getApplications().get( appId ) )
            {
                if ( appId.equals( "system" ) )
                {
                    types.put( appId, ApplicationPageMapper.get().getApplication( "home" ).getEventTypes() );
                }
                else
                {
                    Application app = ApplicationPageMapper.get().getApplication( appId );
                    if ( app != null )
                    {
                        types.put( appId, app.getEventTypes() );
                    }
                }
            }
        }
    }
}