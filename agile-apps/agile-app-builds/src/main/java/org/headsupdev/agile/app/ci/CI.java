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

package org.headsupdev.agile.app.ci;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.list.ListView;
import org.headsupdev.agile.api.PropertyTree;
import org.headsupdev.agile.app.ci.event.UploadApplicationEvent;
import org.headsupdev.agile.app.ci.permission.BuildForcePermission;
import org.headsupdev.agile.app.ci.permission.BuildListPermission;
import org.headsupdev.agile.web.HeadsUpPage;
import org.headsupdev.agile.web.HeadsUpSession;
import org.headsupdev.agile.web.DynamicMenuLink;
import org.headsupdev.agile.web.components.FormattedDateModel;
import org.headsupdev.agile.web.components.FormattedDurationModel;
import org.headsupdev.agile.web.components.StripedListView;
import org.headsupdev.agile.web.components.ProjectTreeListView;
import org.headsupdev.agile.security.permission.ProjectListPermission;
import org.headsupdev.agile.storage.StoredProject;
import org.headsupdev.agile.storage.HibernateStorage;
import org.headsupdev.agile.storage.ci.Build;
import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.api.Permission;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.PageParameters;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.model.Model;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.*;

/**
 * Continuous integration home page
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class CI
    extends HeadsUpPage
{
    public void layout() {
        super.layout();
        add( CSSPackageResource.getHeaderContribution( CI.class, "ci.css" ) );

        final boolean projectList = getProject().equals( StoredProject.getDefault() );
        if ( projectList )
        {
            requirePermission( new ProjectListPermission() );
        }

        renderTopLinks( projectList );

        WebMarkupContainer builds = new WebMarkupContainer( "buildlist" );
        if ( projectList )
        {
            builds.setVisible( false );
        }
        else
        {
            builds.add( new StripedListView<Build>( "builds", getApp().getBuildsForProject( getProject() ) )
            {
                protected void populateItem( ListItem<Build> listItem )
                {
                    super.populateItem( listItem );

                    Build build = listItem.getModelObject();
                    renderBuild( build, build.getProject(), false, false, listItem );
                }
            } );
        }
        add( builds );

        add( createDownloadButton() );

        WebMarkupContainer projects = new WebMarkupContainer( "projectlist" );
        projects.add( new ProjectTreeListView( "projects", getProject() )
        {
            protected void populateProjectItem( ListItem listItem, Project project )
            {
                renderBuild( getApp().getLatestBuildForProject( project ), project,
                    true, CIBuilder.isProjectQueued( project ), listItem );

            }
        } );
        add( projects.setVisible( ( projectList || getProject().getChildProjects().size() > 0 ) &&
            Manager.getSecurityInstance().userHasPermission( getSession().getUser(), new ProjectListPermission(),
                    getProject() ) ) );
    }

    @Override
    public String getTitle()
    {
        return "Builds for project " + getProject().getAlias();
    }

    protected Component createDownloadButton()
    {
        WebMarkupContainer button = new WebMarkupContainer( "download" );
        if ( getProject().equals( StoredProject.getDefault() ) )
        {
            return button.setVisible( false );
        }

        UploadApplicationEvent upload = CIApplication.getLatestUploadEvent(getProject());
        if ( upload == null )
        {
            return button.setVisible( false );
        }

        ExternalLink link = new ExternalLink( "link", upload.getLink() );
        link.add( new Label( "label", getButtonLabel( upload ) ) );
        button.add( link );

        return button;
    }

    protected String getButtonLabel( UploadApplicationEvent upload )
    {
        return "Download (build" + upload.getBuildNumber() + ")";
    }

    void renderTopLinks( boolean projectList )
    {
        if ( !projectList )
        {
            Build latest = getApp().getLatestBuildForProject( getProject() );
            int status = Build.BUILD_SUCCEEDED;
            if ( latest != null )
            {
                status = latest.getStatus();
            }

            WebMarkupContainer building = new WebMarkupContainer( "building" );
            WebMarkupContainer queued = new WebMarkupContainer( "queued" );
            add( building.setVisible( status == Build.BUILD_RUNNING ) );
            add( queued.setVisible( CIBuilder.isProjectQueued( getProject() ) ) );

            if ( userHasPermission( ( (HeadsUpSession) getPage().getSession() ).getUser(),
                    new BuildForcePermission(), getProject() ) && CIApplication.getHandlerFactory().supportsBuilding( getProject() ) )
            {
                if ( status != Build.BUILD_RUNNING && !CIBuilder.isProjectQueued( getProject() ) )
                {
                    addLink( new DynamicMenuLink( "build" )
                    {
                        public void onClick()
                        {
                            buildProject( getProject() );
                        }
                    } );

                    final PropertyTree buildConfigs = Manager.getStorageInstance().getGlobalConfiguration().
                            getApplicationConfigurationForProject( CIApplication.ID, getProject() ).getSubTree( "schedule" );
                    List<String> configNames = new LinkedList<String>( buildConfigs.getSubTreeIds() );
                    if ( configNames.size() > 1 )
                    {
                        configNames.remove( "default" );
                        Collections.sort( configNames );
                        configNames.add( 0, "default" );
                    }
                    add( new ListView<String>( "buildConfigs", configNames )
                    {
                        @Override
                        protected void populateItem( ListItem<String> listItem )
                        {
                            final String configId = listItem.getModelObject();
                            final PropertyTree config = buildConfigs.getSubTree( configId );
                            String configName = config.getProperty( "name" );

                            if ( configName == null )
                            {
                                if ( !configId.equals( "default" ) )
                                {
                                   listItem.setVisible( false );
                                    return;
                                }

                                configName = "project";
                            }
                            else
                            {
                                configName = "\"" + configName + "\"";
                            }
                            Link build = new Link( "build" )
                            {
                                @Override
                                public void onClick()
                                {
                                    buildProject( getProject(), configId, config );
                                }
                            };
                            build.add( new Label( "name", configName ) );
                            listItem.add( build );
                        }
                    } );
                }
                else
                {
                    add( new WebMarkupContainer( "buildConfigs" ).setVisible( false ) );
                }

                if ( status == Build.BUILD_RUNNING )
                {
                    building.add( new Link( "cancel" )
                    {
                        public void onClick()
                        {
                            cancelBuild( getProject() );
                        }
                    } );
                    addLink( new DynamicMenuLink( "cancel" )
                    {
                        public void onClick()
                        {
                            cancelBuild( getProject() );
                        }
                    } );
                }
                else
                {
                    add( new WebMarkupContainer( "cancel" ).setVisible( false ) );
                }

                if ( CIBuilder.isProjectQueued( getProject() ) )
                {
                    queued.add( new Link( "remove" )
                    {
                        public void onClick()
                        {
                            dequeueProject( getProject() );
                        }
                    } );
                    addLink( new DynamicMenuLink( "dequeue" )
                    {
                        public void onClick()
                        {
                            dequeueProject( getProject() );
                        }
                    } );
                }
                else
                {
                    add( new WebMarkupContainer( "remove" ).setVisible( false ) );
                }
            }
            else
            {
                add( new WebMarkupContainer( "build" ).setVisible( false ) );
                add( new WebMarkupContainer( "buildConfigs" ).setVisible( false ) );
                building.add( new WebMarkupContainer( "cancel" ).setVisible( false ) );
                queued.add( new WebMarkupContainer( "remove" ).setVisible( false ) );
            }
        }
        else
        {
            add( new WebMarkupContainer( "building" ).setVisible( false ) );
            add( new WebMarkupContainer( "queued" ).setVisible( false ) );
            add( new WebMarkupContainer( "build" ).setVisible( false ) );
            add( new WebMarkupContainer( "buildConfigs" ).setVisible( false ) );
        }
    }

    private void renderBuild( final Build build, final Project project, final boolean projectList,
            final boolean queued, final ListItem listItem )
    {
        if ( projectList ) {
            PageParameters params = new PageParameters();
            params.add( "project", project.getId() );
            Link link = new BookmarkablePageLink<CI>( "project-link", getClass(), params );
            link.add( new Label( "name", project.getAlias() ) );
            listItem.add( link );
        }
        else
        {
            listItem.add( new Label( "name", project.getAlias() ) );
        }
        if ( build == null || build.getConfigName() == null )
        {
            listItem.add( new Label( "config", "" ) );
        }
        else
        {
            listItem.add( new Label( "config", build.getConfigName() ) );
        }

        final boolean canForce = projectList && userHasPermission( ( (HeadsUpSession) getPage().getSession() ).getUser(),
            new BuildForcePermission(), project ) && CIApplication.getHandlerFactory().supportsBuilding( project );
        WebMarkupContainer statusLink = new Link( "status-link" )
        {
            public void onClick()
            {
                if ( queued )
                {
                    dequeueProject( project );
                }
                else if ( build != null && build.getStatus() == Build.BUILD_RUNNING )
                {
                    cancelBuild( project );
                }
                else
                {
                    buildProject( project );
                }
            }
        };

        if ( build != null )
        {
            WebMarkupContainer status;
            if ( canForce )
            {
                status = statusLink;

                listItem.add( new WebMarkupContainer( "status-icon" ).setVisible( false ) );
            }
            else
            {
                status = new WebMarkupContainer( "status-icon" );

                listItem.add( statusLink.setVisible( false ) );
            }

            status.add( new AttributeModifier( "class", new Model<String>()
            {
                public String getObject()
                {
                    if ( queued )
                    {
                        if ( canForce )
                        {
                            return "status-queued status-remove";
                        }
                        return "status-queued";
                    }
                    else
                    {
                        switch ( build.getStatus() )
                        {
                            case Build.BUILD_SUCCEEDED:
                                if ( canForce )
                                {
                                    return "status-passed status-force";
                                }
                                return "status-passed";
                            case Build.BUILD_FAILED:
                            case Build.BUILD_CANCELLED:
                                if ( canForce )
                                {
                                    return "status-failed status-force";
                                }
                                return "status-failed";
                            default:
                                if ( canForce )
                                {
                                    return "status-running status-remove";
                                }
                                return "status-running";
                        }
                    }
                }
            } ) );
            listItem.add( status );
            listItem.add( new Label( "start", new FormattedDateModel( build.getStartTime(),
                    getSession().getTimeZone() ) ) );
            listItem.add( new Label( "duration", new FormattedDurationModel( build.getStartTime(),
                build.getEndTime() )
            {
                public String getObject() {
                    if ( build.getEndTime() == null )
                    {
                        return super.getObject() + "...";
                    }
                    return super.getObject();
                }
            } ) );

            listItem.add( new Label( "tests", String.valueOf( build.getTests() ) )
                    .add( new CITestStatusModifier( "tests", build, "tests" ) ) );
            listItem.add( new Label( "failures", String.valueOf( build.getFailures() ) )
                    .add( new CITestStatusModifier( "failures", build, "failures" ) ) );
            listItem.add( new Label( "errors", String.valueOf( build.getErrors() ) )
                    .add( new CITestStatusModifier( "errors", build, "errors" ) ) );
            listItem.add( new Label( "warnings", String.valueOf( build.getWarnings() ) )
                    .add( new CITestStatusModifier( "warnings", build, "warnings" ) ) );

            PageParameters params = new PageParameters();
            params.add( "project", project.getId() );
            params.add( "id", String.valueOf( build.getId() ) );
            BookmarkablePageLink link = new BookmarkablePageLink<View>( "buildId-link", View.class, params );
            link.add( new Label( "buildId-label", String.valueOf( build.getId() ) ) );
            listItem.add( link );
        }
        else
        {
            statusLink.add( new AttributeModifier( "class", new Model<String>()
            {
                public String getObject()
                {
                    if ( queued )
                    {
                        return "status-queued status-force";
                    }
                    return "status-force";
                }
            } ) );
            WebMarkupContainer statusIcon = new WebMarkupContainer( "status-icon" );
            statusIcon.add( new AttributeModifier( "class", new Model<String>()
            {
                public String getObject()
                {
                    if ( queued )
                    {
                        return "status-queued";
                    }
                    return "";
                }
            } ) );

            listItem.add( statusIcon.setVisible( !canForce ) );
            listItem.add( statusLink.setVisible( canForce ) );
            listItem.add( new Label( "start", "" ) );
            listItem.add( new Label( "duration", "" ) );

            listItem.add( new Label( "tests", "" ) );
            listItem.add( new Label( "failures", "" ) );
            listItem.add( new Label( "errors", "" ) );
            listItem.add( new Label( "warnings", "" ) );

            listItem.add( new WebMarkupContainer( "buildId-link" ).setVisible( false ) );
        }
    }

    protected void buildProject( Project project )
    {
        CIApplication.getBuilder().buildProject( project, false );
    }

    protected void buildProject( Project project, String id, PropertyTree config )
    {
        CIApplication.getBuilder().buildProject( project, id, config, false );
    }

    protected void dequeueProject( Project project )
    {
        CIApplication.getBuilder().dequeueProject( project );
    }

    protected void cancelBuild( Project project )
    {
        Build b = getApp().getLatestBuildForProject( project );
        if ( b.getEndTime() == null )
        {
            b.setEndTime( new Date() );
            b.setStatus( Build.BUILD_CANCELLED );
            saveBuild( b );

            // TODO actually cancel a build - if possible
        }
    }

    public Permission getRequiredPermission() {
        return new BuildListPermission();
    }

    CIApplication getApp()
    {
        return (CIApplication) getHeadsUpApplication();
    }

    private void saveBuild( Build build )
    {
        HibernateStorage storage = (HibernateStorage) Manager.getStorageInstance();
        Session session = storage.getHibernateSession();
        Transaction tx = session.beginTransaction();
        session.update( build );
        tx.commit();
        storage.closeSession();
    }
}
