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

package org.headsupdev.agile.app.admin.configuration;

import org.headsupdev.agile.app.admin.AdminApplication;
import org.headsupdev.agile.web.components.OnePressSubmitButton;
import org.headsupdev.agile.web.components.ProjectTreeDropDownChoice;
import org.headsupdev.agile.web.ApplicationPageMapper;
import org.headsupdev.agile.web.MountPoint;
import org.headsupdev.agile.api.*;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.extensions.markup.html.tabs.TabbedPanel;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.PageParameters;

import java.util.List;
import java.util.ArrayList;

/**
 * A panel for editing the configuration options for each of our installed applications.
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@MountPoint( "configuration" )
public class ApplicationsConfiguration
    extends ConfigurationPage
{
    public void layout()
    {
        super.layout();
        add( CSSPackageResource.getHeaderContribution( AdminApplication.class, "admin.css" ) );

        String tabName = getPageParameters().getString( "app" );
        int tabId = 0;
        int i = 0;
        List<ITab> tabs = new ArrayList<ITab>();
        for ( final Application app : ApplicationPageMapper.get().getApplications() )
        {
            if ( app.getApplicationId().equals( "admin" ) )
            {
                continue;
            }

            // TODO set the app from the parameter string...
            tabs.add( new AbstractTab( new PropertyModel<String>( app, "name" ) )
            {
                public Panel getPanel( String panelId )
                {
                    return new ApplicationTab( panelId, app );
                }
            });

            if ( app.getApplicationId().equals( tabName ) )
            {
                tabId = i;
            }
            i++;
        }
        TabbedPanel panel = new TabbedPanel( "tabs", tabs );
        panel.setSelectedTab( tabId );
        add( panel );
    }

    class ApplicationTab
        extends Panel
    {
        ApplicationTab( String id, final Application app )
        {
            super( id );

            final PropertyTree tree = Manager.getStorageInstance().getGlobalConfiguration().getApplicationConfiguration( app );
            boolean hasConfig = app.getConfigurationItems() != null && app.getConfigurationItems().size() > 0;

            Form config = new Form( "config" )
            {
                @Override
                protected void onSubmit()
                {
                    app.onConfigurationChanged();
                }
            };
            add( config.setVisible( hasConfig ) );
            config.add( new ConfigurationPanel( "item", app.getConfigurationItems(), tree, app, null ) );
            config.add( new OnePressSubmitButton( "submitConfig" ) );
            add( new WebMarkupContainer( "noconfig" ).setVisible( !hasConfig ) );

            final PropertyTree projectTree = Manager.getStorageInstance().getGlobalConfiguration().
                    getApplicationConfigurationForProject( app, getProject() );
            List<ConfigurationItem> items = app.getProjectConfigurationItems( getProject() );
            boolean hasProjectConfig = items != null && items.size() > 0;

            Form projectConfig = new Form( "projectconfig" )
            {
                @Override
                protected void onSubmit()
                {
                    app.onProjectConfigurationChanged( getProject() );
                }
            };
            projectConfig.add( new ChangeProjectDropDownChoice( "project", app ) );
            projectConfig.add( new OnePressSubmitButton( "submitProjConfig" ) );
            add( projectConfig.setVisible( hasProjectConfig ) );
            projectConfig.add( new ConfigurationPanel( "item", items, projectTree, app, getProject() ) );
            WebMarkupContainer noProjectConfig = new WebMarkupContainer( "noprojectconfig" );
            noProjectConfig.add( new ChangeProjectDropDownChoice( "project", app ) );
            add( noProjectConfig.setVisible( !hasProjectConfig ) );
        }
    }

    class ChangeProjectDropDownChoice
        extends ProjectTreeDropDownChoice
    {
        public ChangeProjectDropDownChoice( String id, final Application app )
        {
            super( id, new Model<Project>()
            {
                @Override
                public Project getObject()
                {
                    return getProject();
                }

                @Override
                public void setObject( Project p )
                {
                    super.setObject( p );

                    PageParameters params = new PageParameters();
                    params.add( "project", p.getId() );
                    params.remove( "app" );
                    params.add( "app", app.getApplicationId() );
                    ApplicationsConfiguration.this.setResponsePage( ApplicationsConfiguration.class, params );
                }
            } );
        }

        @Override
        protected boolean wantOnSelectionChangedNotifications()
        {
            return true;
        }
    }
}
