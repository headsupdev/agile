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

package org.headsupdev.agile.web.components.filters;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.headsupdev.agile.web.ApplicationIdComparator;
import org.headsupdev.agile.web.ApplicationPageMapper;
import org.headsupdev.agile.web.HeadsUpSession;
import org.headsupdev.agile.web.components.FilterBorder;

import java.util.*;

/**
 * A filter panel that allows pages to select which applications should provide content for the current view.
 * <p/>
 * Created: 19/04/2013
 *
 * @author Andrew Williams
 * @since 2.0
 */
public abstract class ApplicationFilterPanel
        extends Panel
{
    private String filterId;
    private List<String> allApps;
    private WebMarkupContainer filterButton;
    final Map<String, Boolean> appsVisible = new HashMap<String, Boolean>();

    public ApplicationFilterPanel( String id, String filterId, WebMarkupContainer filterButton )
    {
        super( id );
        this.filterId = filterId;

        setupFilter();
        layoutFilter( filterButton );
    }

    public Map<String, Boolean> getApplications()
    {
        return appsVisible;
    }

    protected void setupFilter()
    {
        allApps = ApplicationPageMapper.get().getApplicationIds();
        Iterator<String> iter = allApps.iterator();
        while ( iter.hasNext() )
        {
            String appId = iter.next();
            List<String> typesInApp = ApplicationPageMapper.get().getApplication( appId ).getEventTypes();

            if ( typesInApp == null || typesInApp.size() == 0 )
            {
                iter.remove();
            }
        }
        allApps.remove( "home" );
        Collections.sort( allApps, new ApplicationIdComparator() );
        allApps.add( "system" );

        loadFilters();
    }

    protected void layoutFilter( WebMarkupContainer filterButton )
    {

        FilterBorder filter = new FilterBorder( "filter", filterButton );
        add( filter );

        final Form filterForm = new Form( "filterform" )
        {
            @Override
            protected void onSubmit()
            {
                super.onSubmit();

                saveFilters();
            }
        };
        filter.add( filterForm.setOutputMarkupId( true ) );
        Button cancelButton = new Button( "cancelbutton" );
        filterForm.add( cancelButton );
        cancelButton.add( new AttributeModifier( "onclick", true, new Model<String>()
        {
            public String getObject()
            {
                return "filterbuttonAnimator.reverse();";
            }
        } ) );

        Button applyButton = new Button( "applybutton" );
        filterForm.add( applyButton );
        applyButton.add( new AttributeModifier( "onclick", true, new Model<String>()
        {
            public String getObject()
            {
                return "filterbuttonAnimator.reverse();";
            }
        } ) );

        final ListView<String> appListView = new ListView<String>( "applist", allApps )
        {
            protected void populateItem( final ListItem<String> listItem )
            {
                final String appId = listItem.getModelObject();

                listItem.add( new Label( "app-label", appId ) );
                listItem.add( new CheckBox( "app-check", new Model<Boolean>()
                {
                    public Boolean getObject()
                    {
                        return appsVisible.get( appId );
                    }

                    public void setObject( Boolean b )
                    {
                        appsVisible.put( appId, b );
                        onFilterUpdated();
                    }
                } ) );
            }
        };
        filterForm.add( appListView.setOutputMarkupId( true ) );
    }

    public abstract void onFilterUpdated();

    private void loadFilters()
    {
        String typeStr = ( (HeadsUpSession) getSession() ).getUser().getPreference( "filter." + filterId + ".apps", (String) null );
        if ( typeStr == null )
        {
            for ( String appId : allApps )
            {
                appsVisible.put( appId, true );
            }
            return;
        }

        for ( String appId : allApps )
        {
            appsVisible.put( appId, false );
        }
        for ( String appId : Arrays.asList( typeStr.split( "," ) ) )
        {
            appsVisible.put( appId, true );
        }
    }

    private void saveFilters()
    {
        StringBuilder typeStr = new StringBuilder();
        boolean first = true;
        for ( String appId : allApps )
        {
            if ( !appsVisible.get( appId ) )
            {
                continue;
            }

            if ( !first )
            {
                typeStr.append( "," );
            }
            else
            {
                first = false;
            }

            typeStr.append( appId );
        }

        ( (HeadsUpSession) getSession() ).getUser().setPreference( "filter." + filterId + ".apps", typeStr.toString() );
    }
}
