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

package org.headsupdev.agile.web.components;

import org.headsupdev.agile.api.*;
import org.headsupdev.agile.storage.issues.IssueHelper;
import org.headsupdev.agile.web.HeadsUpPage;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.html.PackageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

/**
 * Displaying details for the various types of project
 *
 * @author Andrew Williams
 * @since 1.0
 */
public class ProjectDetailsPanel
        extends Panel
{
    public ProjectDetailsPanel( String id, final Project project )
    {
        super( id );
        HeadsUpConfiguration config = Manager.getStorageInstance().getGlobalConfiguration();
        add( new Label( "title", project.getAlias() ) );

        if ( project.getAlias().equals( project.getName() ) )
        {
            add( new Label( "name", project.getAlias() ) );
        }
        else
        {
            add( new Label( "name", project.getName() + " (alias " + project.getAlias() + ")" ) );
        }
        add( new Label( "type", project.getTypeName() ) );

        addIssueBreakdown( this, project );

        if ( project instanceof MavenTwoProject )
        {
            add( new WebMarkupContainer( "type-icon" ).setVisible( false ) );

            add( new Label( "artifactId", ( (MavenTwoProject) project ).getArtifactId() ) );
            add( new Label( "groupId", ( (MavenTwoProject) project ).getGroupId() ) );
            add( new Label( "version", ( (MavenTwoProject) project ).getVersion() ) );
            add( new Label( "packaging", ( (MavenTwoProject) project ).getPackaging() ) );

            String image = "images/packaging/" + ( (MavenTwoProject) project ).getPackaging() + ".png";
            if ( !PackageResource.exists( HeadsUpPage.class, image, null, null ) )
            {
                image = "images/packaging/other.png";
            }
            add( new Image( "packaging-icon", new ResourceReference( HeadsUpPage.class, image ) ) );

            add( new WebMarkupContainer( "module" ).setVisible( false ) );
            add( new WebMarkupContainer( "org" ).setVisible( false ) );
            add( new WebMarkupContainer( "revision" ).setVisible( false ) );
            add( new WebMarkupContainer( "xcode-version" ).setVisible( false ) );
            add( new WebMarkupContainer( "xcode-platform" ).setVisible( false ) );
            add( new WebMarkupContainer( "xcode-bundleid" ).setVisible( false ) );
        }
        else if ( project instanceof AntProject )
        {
            add( new Image( "type-icon", new ResourceReference( HeadsUpPage.class, "images/packaging/ant.png" ) ) );
            add( new Label( "module", ( (AntProject) project ).getModule() ) );
            add( new Label( "org", ( (AntProject) project ).getOrganisation() ) );
            add( new Label( "revision", ( (AntProject) project ).getVersion() ) );

            add( new WebMarkupContainer( "artifactId" ).setVisible( false ) );
            add( new WebMarkupContainer( "groupId" ).setVisible( false ) );
            add( new WebMarkupContainer( "version" ).setVisible( false ) );
            add( new WebMarkupContainer( "packaging" ).setVisible( false ) );
            add( new WebMarkupContainer( "packaging-icon" ).setVisible( false ) );
            add( new WebMarkupContainer( "xcode-version" ).setVisible( false ) );
            add( new WebMarkupContainer( "xcode-platform" ).setVisible( false ) );
            add( new WebMarkupContainer( "xcode-bundleid" ).setVisible( false ) );
        }
        else if ( project instanceof GradleProject )
        {
            add( new Image( "type-icon", new ResourceReference( HeadsUpPage.class, "images/packaging/gradle.png" ) ) );
            add( new Label( "groupId", ( (GradleProject) project ).getGroup() ) );
            add( new Label( "version", ( (GradleProject) project ).getVersion() ) );
//            add( new Label( "description", ( (GradleProject) project ).getDescription() ) );

            add( new WebMarkupContainer( "artifactId" ).setVisible( false ) );
            add( new WebMarkupContainer( "packaging" ).setVisible( false ) );
            add( new WebMarkupContainer( "packaging-icon" ).setVisible( false ) );
            add( new WebMarkupContainer( "xcode-version" ).setVisible( false ) );
            add( new WebMarkupContainer( "xcode-platform" ).setVisible( false ) );
            add( new WebMarkupContainer( "xcode-bundleid" ).setVisible( false ) );

            add( new WebMarkupContainer( "module" ).setVisible( false ) );
            add( new WebMarkupContainer( "org" ).setVisible( false ) );
            add( new WebMarkupContainer( "revision" ).setVisible( false ) );
        }
        else if ( project instanceof EclipseProject )
        {
            add( new Image( "type-icon", new ResourceReference( HeadsUpPage.class, "images/packaging/eclipse.png" ) ) );

            add( new WebMarkupContainer( "artifactId" ).setVisible( false ) );
            add( new WebMarkupContainer( "groupId" ).setVisible( false ) );
            add( new WebMarkupContainer( "version" ).setVisible( false ) );
            add( new WebMarkupContainer( "packaging" ).setVisible( false ) );
            add( new WebMarkupContainer( "packaging-icon" ).setVisible( false ) );
            add( new WebMarkupContainer( "xcode-version" ).setVisible( false ) );
            add( new WebMarkupContainer( "xcode-platform" ).setVisible( false ) );
            add( new WebMarkupContainer( "xcode-bundleid" ).setVisible( false ) );

            add( new WebMarkupContainer( "module" ).setVisible( false ) );
            add( new WebMarkupContainer( "org" ).setVisible( false ) );
            add( new WebMarkupContainer( "revision" ).setVisible( false ) );
        }
        else
        {
            if ( project instanceof XCodeProject )
            {
                add( new Image( "type-icon", new ResourceReference( HeadsUpPage.class, "images/packaging/xcode.png" ) ) );
                add( new Label( "xcode-version", ( (XCodeProject) project ).getVersion() ) );
                add( new Label( "xcode-platform", ( (XCodeProject) project ).getPlatform() ) );
                add( new Label( "xcode-bundleid", ( (XCodeProject) project ).getBundleId() ) );
            }
            else
            {
                add( new Image( "type-icon", new ResourceReference( HeadsUpPage.class, "images/packaging/cmdline.png" ) ) );
                add( new WebMarkupContainer( "xcode-version" ).setVisible( false ) );
                add( new WebMarkupContainer( "xcode-platform" ).setVisible( false ) );
                add( new WebMarkupContainer( "xcode-bundleid" ).setVisible( false ) );
            }
            add( new WebMarkupContainer( "artifactId" ).setVisible( false ) );
            add( new WebMarkupContainer( "groupId" ).setVisible( false ) );
            add( new WebMarkupContainer( "version" ).setVisible( false ) );
            add( new WebMarkupContainer( "packaging" ).setVisible( false ) );
            add( new WebMarkupContainer( "packaging-icon" ).setVisible( false ) );

            add( new WebMarkupContainer( "module" ).setVisible( false ) );
            add( new WebMarkupContainer( "org" ).setVisible( false ) );
            add( new WebMarkupContainer( "revision" ).setVisible( false ) );
        }

        WebMarkupContainer urlrow = new WebMarkupContainer( "urlrow" );
        urlrow.add( new AttributeModifier( "class", true, new Model<String>()
        {
            @Override
            public String getObject()
            {
//                if ( project instanceof XCodeProject )
//                {
//                    return "odd";
//                }
                return "even";
            }
        } ) );
        urlrow.add( new Label( "url", config.getFullUrl( project.getId() ) ) );
        add( urlrow );
        WebMarkupContainer scmrow = new WebMarkupContainer( "scmrow" );
        scmrow.add( new AttributeModifier( "class", true, new Model<String>()
        {
            @Override
            public String getObject()
            {
//                if ( project instanceof XCodeProject )
//                {
//                    return "even";
//                }
                return "odd";
            }
        } ) );
        scmrow.add( new Label( "scm", project.getScm() ) );
        add( scmrow );
    }

    private void addIssueBreakdown( ProjectDetailsPanel projectDetailsPanel, Project project )
    {
        int issueCountForProject = IssueHelper.getIssueCountForProject( project );
        int issueOpenCountForProject = IssueHelper.getIssueOpenCountForProject( project );
        int issueReOpenedCountForProject = IssueHelper.getIssueReOpenedCountForProject( project );

        projectDetailsPanel.add( new Label( "issuesTotal",
                issueReOpenedCountForProject == 0
                        ? String.valueOf( issueCountForProject )
                        : String.format( "%d (%d reopened) ", issueCountForProject, issueReOpenedCountForProject ) ) );
        projectDetailsPanel.add( new Label( "issuesOpen", String.valueOf( issueOpenCountForProject ) ) );
    }
}
