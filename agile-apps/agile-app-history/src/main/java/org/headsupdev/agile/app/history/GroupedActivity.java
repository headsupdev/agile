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

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.Model;
import org.headsupdev.agile.api.Event;
import org.headsupdev.agile.api.Permission;
import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.app.history.permission.HistoryViewPermission;
import org.headsupdev.agile.security.permission.ProjectListPermission;
import org.headsupdev.agile.storage.StoredProject;
import org.headsupdev.agile.web.ApplicationIdComparator;
import org.headsupdev.agile.web.ApplicationPageMapper;
import org.headsupdev.agile.web.HeadsUpPage;
import org.headsupdev.agile.web.MountPoint;
import org.headsupdev.agile.web.components.history.HistoryPanel;

import java.util.*;

@MountPoint( "grouped" )
public class GroupedActivity
        extends HeadsUpPage
{
    public static final int ROWS_IN_GROUP = 10;

    private Map<String, List<String>> types;
    private long before;

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
        List<String> appIds = ApplicationPageMapper.get().getApplicationIds();
        Collections.sort( appIds, new ApplicationIdComparator() );
        String homeId = appIds.remove( 0 );
        appIds.add( appIds.size(), homeId );

        Iterator<String> iter = appIds.iterator();
        while ( iter.hasNext() )
        {
            String appId = iter.next();
            List<String> typesInApp = ApplicationPageMapper.get().getApplication( appId ).getEventTypes();

            if ( typesInApp == null || typesInApp.size() == 0 )
            {
                iter.remove();
            }

            types.put( appId, typesInApp );
        }

        add( new WebMarkupContainer( "feedlink" ).add( new AttributeModifier( "href", true, new Model<String>()
        {
            public String getObject()
            {
                return getStorage().getGlobalConfiguration().getFullUrl( "/" + getProject().getId() + "/activity/feed.xml" );
            }
        } ) ) );


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

                String label = appId;
                if ( appId.equals( "home" ) )
                {
                    label = "system";
                }
                listItem.add( new Label( "app-label", label ) );

                final List<String> appTypes = types.get( appId );
                listItem.add(new HistoryPanel("history", new AbstractReadOnlyModel<List<? extends Event>>() {
                    public List<? extends Event> getObject() {
                        if (allProject) {
                            return ((HistoryApplication) getHeadsUpApplication()).getEvents(before, appTypes, ROWS_IN_GROUP);
                        } else {
                            return ((HistoryApplication) getHeadsUpApplication()).getEventsForProject(project, before, appTypes, ROWS_IN_GROUP);
                        }
                    }
                }, allProject));
            }
        } );
    }
}