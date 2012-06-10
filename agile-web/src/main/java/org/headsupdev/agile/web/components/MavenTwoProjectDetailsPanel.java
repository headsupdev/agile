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

package org.headsupdev.agile.web.components;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.PackageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.PageParameters;
import org.headsupdev.agile.api.*;
import org.headsupdev.agile.web.HeadsUpPage;
import org.headsupdev.agile.web.RenderUtil;

import java.io.File;

/**
 * A panel to display the maven 2 specific project information. Link to dependencies and developers if they
 * can be found in the system.
 *
 * @author Andrew Williams
 * @since 1.0
 */
public class MavenTwoProjectDetailsPanel
    extends Panel
{
    public MavenTwoProjectDetailsPanel( String id, final MavenTwoProject project, boolean listModules )
    {
        super( id );

        add( new StripedListView<MavenDependency>( "dependencies", project.getDependencies() )
        {
            protected void populateItem( ListItem<MavenDependency> listItem )
            {
                super.populateItem( listItem );
                MavenDependency dependency = listItem.getModelObject();

                String path = dependency.getGroupId().replace( ".", File.separator );
                path = path + File.separator + dependency.getArtifactId();
                path = path + File.separator + dependency.getVersion();

                String foundRepo = null;
                File repos = new File( Manager.getStorageInstance().getDataDirectory(), "repository" );
                for ( String repo : new String[] {"release", "external", "snapshot"} )
                {
                    File artifactDir = new File( new File( repos, repo ), path );

                    if ( artifactDir.exists() ) {
                        foundRepo = repo;
                    }
                }

                if ( foundRepo == null )
                {
                    listItem.add( new Label( "dependency-artifact", dependency.getArtifactId() ) );
                    listItem.add( new WebMarkupContainer( "artifact-link" ).setVisible( false ) );
                    listItem.add( new Label( "dependency-group", dependency.getGroupId() ) );
                    listItem.add( new WebMarkupContainer( "group-link" ).setVisible( false ) );
                    listItem.add( new Label( "dependency-version", dependency.getVersion() ) );
                    listItem.add( new WebMarkupContainer( "version-link" ).setVisible( false ) );
                }
                else
                {
                    String repoPath = dependency.getGroupId().replace( ".", ":" );
                    Class repoClass = RenderUtil.getPageClass( "artifacts/" + foundRepo );
                    PageParameters params = new PageParameters();
                    params.add( "project", project.getId() );
                    params.add( "path", repoPath + ":" + dependency.getArtifactId() );

                    listItem.add( new WebMarkupContainer( "dependency-artifact" ).setVisible( false ) );
                    Link link = new BookmarkablePageLink( "artifact-link", repoClass, params );
                    link.add( new Label( "artifact-label", dependency.getArtifactId() ) );
                    listItem.add( link );

                    params.remove( "path" );
                    params.add( "path", repoPath );
                    listItem.add( new WebMarkupContainer( "dependency-group" ).setVisible( false ) );
                    link = new BookmarkablePageLink( "group-link", repoClass, params );
                    link.add( new Label( "group-label", dependency.getGroupId() ) );
                    listItem.add( link );

                    params.remove( "path" );
                    params.add( "path", repoPath + ":" + dependency.getArtifactId() + ":" + dependency.getVersion() );
                    listItem.add( new WebMarkupContainer( "dependency-version" ).setVisible( false ) );
                    link = new BookmarkablePageLink( "version-link", repoClass, params );
                    link.add( new Label( "version-label", dependency.getVersion() ) );
                    listItem.add( link );
                }

                String image = "images/packaging/" + dependency.getType() + ".png";
                if ( !PackageResource.exists( HeadsUpPage.class, image, null, null ) ) {
                    image = "images/packaging/other.png";
                }
                listItem.add( new Image( "type-icon", new ResourceReference( HeadsUpPage.class, image ) ) );
                listItem.add( new Label( "dependency-type", dependency.getType() ) );
            }
        } );

        add( new StripedListView<String>( "developers", project.getDevelopers() )
        {
            protected void populateItem( ListItem<String> listItem )
            {
                super.populateItem( listItem );
                String developer = listItem.getModelObject();

                User user = Manager.getSecurityInstance().getUserByUsername( developer );
                if ( user == null )
                {
                    listItem.add( new Label( "developer", developer ) );
                    listItem.add( new WebMarkupContainer( "developer-link" ).setVisible( false ) );
                }
                else
                {
                    listItem.add( new WebMarkupContainer( "developer" ).setVisible( false ) );
                    PageParameters params = new PageParameters();
                    params.add( "username", user.getUsername() );
                    Link link = new BookmarkablePageLink( "developer-link", RenderUtil.getPageClass( "account" ), params );
                    link.add( new Label( "developer-label", developer ) );
                    listItem.add( link );
                }
            }
        } );

        add( new StripedListView<String>( "modules", project.getModules() )
        {
            protected void populateItem( ListItem<String> listItem )
            {
                super.populateItem( listItem );
                String module = listItem.getModelObject();

                String scm = project.getScm();
                if ( scm.endsWith( File.separator ) )
                {
                    scm = scm + module + File.separator;
                }
                else
                {
                    scm = scm + File.separator + module;
                }

                Project child = null;
                for ( Project childProject : project.getChildProjects() )
                {
                    if ( childProject.getScm().equals( scm ) )
                    {
                        child = childProject;
                    }
                }

                if ( child == null )
                {
                    listItem.add( new Label( "name", module ) );
                    listItem.add( new WebMarkupContainer( "module-link" ).setVisible( false ) );
                }
                else
                {
                    listItem.add( new WebMarkupContainer( "name" ).setVisible( false ) );
                    PageParameters params = new PageParameters();
                    params.add( "project", child.getId() );
                    Link link = new BookmarkablePageLink( "module-link", RenderUtil.getPageClass( "show" ), params );
                    link.add( new Label( "module-label", module ) );
                    listItem.add( link );
                }
            }
        }.setVisible( listModules ) );
    }
}
