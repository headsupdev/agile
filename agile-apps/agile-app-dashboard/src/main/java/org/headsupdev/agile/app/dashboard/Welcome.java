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

package org.headsupdev.agile.app.dashboard;

import org.apache.wicket.PageParameters;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.image.Image;
import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.api.Permission;
import org.headsupdev.agile.security.permission.ProjectListPermission;
import org.headsupdev.agile.web.HeadsUpPage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The HeadsUp welcome page - will display the dashboard most likely.
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class Welcome extends HeadsUpPage
{
    public Permission getRequiredPermission()
    {
        return new ProjectListPermission();
    }

    public void layout()
    {
        super.layout();
        add( CSSPackageResource.getHeaderContribution( getClass(), "welcome.css" ) );

        List<Project> recent = getStorage().getRecentRootProjects( getSession().getUser() );
        Collections.sort( recent );
        addProjectGraphListView( "recentprojects", recent );

        List<Project> active = new ArrayList<Project>( getStorage().getActiveRootProjects() );
        active.removeAll( recent );
        Collections.sort( active );
        addProjectGraphListView( "activeprojects", active );

        List<Project> inactive = new ArrayList<Project>( getStorage().getRootProjects() );
        inactive.removeAll( recent );
        inactive.removeAll( active );
        Collections.sort( inactive );
        addProjectGraphListView( "inactiveprojects", inactive );
    }

    @Override
    public String getTitle()
    {
        return "Welcome to " + getStorage().getGlobalConfiguration().getProductName();
    }

    protected void addProjectGraphListView( String id, List<Project> projects )
    {
        add( new ListView<Project>( id, projects ) {
            protected void populateItem( ListItem<Project> listItem )
            {
                Project project = listItem.getModelObject();

                PageParameters params = new PageParameters();
                params.add( "project", project.getId() );
                Link projectLink = new BookmarkablePageLink( "project-link", Show.class, params );

                DashboardApplication app = (DashboardApplication) getHeadsUpApplication();
                if ( app.getConfigurationValue( DashboardApplication.DEFAULT_TIMEFRAME ).equals( ActivityGraph.TIME_YEAR ) )
                {
                    params.add( "time", ActivityGraph.TIME_YEAR );
                }
                else
                {
                    params.add( "time", ActivityGraph.TIME_MONTH );
                }
                params.add( "tree", "true" );
                params.add( "silent", "true" );
                projectLink.add( new Image( "activity", new ResourceReference( "activity.png" ), params ) );
                projectLink.add( new Label( "name", project.getAlias() ) );
                listItem.add( projectLink );
            }
        } );
    }
}