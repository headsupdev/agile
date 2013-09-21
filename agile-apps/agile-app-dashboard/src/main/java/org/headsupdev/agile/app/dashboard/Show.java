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

import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.PageParameters;
import org.headsupdev.agile.api.*;
import org.headsupdev.agile.app.dashboard.permission.ProjectViewPermission;
import org.headsupdev.agile.web.components.ProjectDetailsPanel;
import org.headsupdev.agile.web.components.MavenTwoProjectDetailsPanel;
import org.headsupdev.agile.web.MountPoint;
import org.headsupdev.agile.web.HeadsUpPage;
import org.headsupdev.agile.storage.StoredProject;
import org.headsupdev.agile.web.components.XCodeProjectDetailsPanel;

import java.util.LinkedList;

/**
 * Show the full details of a project and summarise it's activity too
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@MountPoint( "show" )
public class Show extends HeadsUpPage
{
    private String name;

    public Permission getRequiredPermission()
    {
        return new ProjectViewPermission();
    }

    public void layout()
    {
        super.layout();
        add( CSSPackageResource.getHeaderContribution( getClass(), "welcome.css" ) );

        Project project = getProject();
        if ( project.equals( StoredProject.getDefault() ) ) {
            setResponsePage( Welcome.class );
            return;
        }

        name = project.getAlias();
        add( new ProjectDetailsPanel( "projectdetails", project ) );

        PageParameters params = new PageParameters();
        params.add( "project", project.getId() );
        params.add( "time", ActivityGraph.TIME_MONTH );
        params.add( "tree", "false" );
        params.add( "silent", "true" );
        add( new Image( "activity-month", new ResourceReference( "activity.png" ), params ) );

        params = new PageParameters();
        params.add( "project", project.getId() );
        params.add( "time", ActivityGraph.TIME_YEAR );
        params.add( "tree", "false" );
        params.add( "silent", "true" );
        add( new Image( "activity-year", new ResourceReference( "activity.png" ), params ) );

        if ( project instanceof MavenTwoProject)
        {
            add( new MavenTwoProjectDetailsPanel( "m2", (MavenTwoProject) project, false ) );
        }
        else
        {
            add( new WebMarkupContainer( "m2" ).setVisible( false ) );
        }

        if ( project instanceof XCodeProject )
        {
            add( new XCodeProjectDetailsPanel( "xcode", (XCodeProject) project ) );
        }
        else
        {
            add( new WebMarkupContainer( "xcode" ).setVisible( false ) );
        }

        add( new ListView<Project>( "projectlist", new LinkedList<Project>( project.getChildProjects() ) ) {
            protected void populateItem( ListItem<Project> listItem )
            {
                Project project = listItem.getModelObject();

                PageParameters params = new PageParameters();
                params.add( "project", project.getId() );
                Link projectLink = new BookmarkablePageLink( "project-link", Show.class, params );
                params.add( "time", ActivityGraph.TIME_MONTH );
                params.add( "tree", "true" );
                params.add( "silent", "true" );
                projectLink.add( new Image( "activity", new ResourceReference( "activity.png" ), params ) );
                projectLink.add( new Label( "name", project.getAlias() ) );
                listItem.add( projectLink );
            }
        });
    }

    @Override
    public String getPageTitle()
    {
        return name + PAGE_TITLE_SEPARATOR + super.getPageTitle();
    }
}
