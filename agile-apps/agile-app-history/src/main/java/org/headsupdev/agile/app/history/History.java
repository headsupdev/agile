/*
 * HeadsUp Agile
 * Copyright 2009-2013 Heads Up Development Ltd.
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

import org.headsupdev.agile.api.Permission;
import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.api.Event;
import org.headsupdev.agile.app.history.permission.HistoryViewPermission;
import org.headsupdev.agile.security.permission.ProjectListPermission;
import org.headsupdev.agile.web.components.history.HistoryPanel;
import org.headsupdev.agile.web.components.FilterBorder;
import org.headsupdev.agile.web.components.StripedListView;
import org.headsupdev.agile.web.*;
import org.headsupdev.agile.storage.StoredProject;

import java.util.*;

import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.ajax.AjaxRequestTarget;

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

    public Permission getRequiredPermission() {
        return new HistoryViewPermission();
    }

    public void layout()
    {
        super.layout();
        types = new LinkedList<String>();
        List<String> apps = ApplicationPageMapper.get().getApplicationIds();

        Iterator<String> iter = apps.iterator();
        while ( iter.hasNext() )
        {
            String appId = iter.next();
            List<String> typesInApp = ApplicationPageMapper.get().getApplication( appId ).getEventTypes();

            if ( typesInApp == null || typesInApp.size() == 0 )
            {
                iter.remove();
            }

            types.addAll( typesInApp );
        }
        loadFilters();

        FilterBorder filter = new FilterBorder( "filter" );
        add( filter );

        final Form filterForm = new Form( "filterform" )
        {
            @Override
            protected void onSubmit() {
                super.onSubmit();

                saveFilters();
            }
        };
        filter.add( filterForm.setOutputMarkupId( true ) );
        Button cancelButton = new Button( "cancelbutton" );
        filterForm.add( cancelButton );
        cancelButton.add( new AttributeModifier( "onclick", true, new Model<String>() {
            public String getObject() {
                return "filterbuttonAnimator.reverse();";
            }
        } ) );

        Button applyButton = new Button( "applybutton" );
        filterForm.add( applyButton );
        applyButton.add( new AttributeModifier( "onclick", true, new Model<String>() {
            public String getObject() {
                return "filterbuttonAnimator.reverse();";
            }
        } ) );

        CheckGroup typeGroup = new CheckGroup( "types", new PropertyModel( this, "types" ) );
        filterForm.add( typeGroup );

        final Map<String,Boolean> appsVisible = new HashMap<String,Boolean>();
        for ( String app : apps )
        {
            appsVisible.put( app, true );
        }
        typeGroup.add( new StripedListView<String>( "applist", apps )
        {
            protected void populateItem( final ListItem<String> listItem )
            {
                super.populateItem( listItem );
                final String appId = listItem.getModelObject();

                listItem.add( new Label( "app-label", appId ) );
                listItem.add( new AjaxCheckBox( "app-check", new Model<Boolean>()
                {
                    public Boolean getObject()
                    {
                        return appsVisible.get( appId );
                    }

                    public void setObject( Boolean b )
                    {
                        appsVisible.put( appId, b );
                    }
                })
                {
                    protected void onUpdate( AjaxRequestTarget target )
                    {
                        if ( appsVisible.get( appId ) )
                        {
                            types.addAll( ApplicationPageMapper.get().getApplication( appId ).getEventTypes() );
                            target.addComponent( filterForm );
                        }
                        else
                        {
                            types.removeAll( ApplicationPageMapper.get().getApplication( appId ).getEventTypes() );
                            target.addComponent( filterForm );
                        }
                    }
                } );

                listItem.add( new ListView<String>( "type" , ApplicationPageMapper.get().getApplication( appId ).getEventTypes() )
                {
                    protected void populateItem( ListItem<String> typeListItem )
                    {
                        final String typeId = typeListItem.getModelObject();

                        typeListItem.add( new Label( "name", typeId ) );
                        typeListItem.add( new Check<String>( "type-check", typeListItem.getModel() ));
                    }
                } );
            }
        } );

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
    }

    private void loadFilters()
    {
        String typeStr = getSession().getUser().getPreference( "filter.history.types", (String) null );

        if ( typeStr != null )
        {
            types.clear();
            types.addAll( Arrays.asList( typeStr.split( "," ) ) );
        }
    }

    private void saveFilters()
    {
        StringBuilder typeStr = new StringBuilder();
        boolean first = true;
        for ( String type : types )
        {
            if ( !first )
            {
                typeStr.append( "," );
            }
            else
            {
                first = false;
            }

            typeStr.append( type );
        }

        getSession().getUser().setPreference( "filter.history.types", typeStr.toString() );
    }
}
