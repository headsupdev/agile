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

import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.api.User;
import org.headsupdev.agile.api.service.ChangeSet;
import org.headsupdev.agile.storage.TransactionalScmChangeSet;
import org.headsupdev.agile.web.HeadsUpSession;
import org.headsupdev.agile.web.RenderUtil;
import org.headsupdev.agile.web.components.StripedListView;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.ResourceReference;
import org.headsupdev.agile.web.components.FormattedDateModel;
import org.headsupdev.agile.web.components.FormattedDurationModel;
import org.headsupdev.agile.storage.ci.Build;
import org.apache.wicket.model.CompoundPropertyModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * TODO document me
 *
 * @author Andrew Williams
 * @since 1.0
 */
public class BuildPanel
    extends Panel
{
    public BuildPanel( String id, final Build build )
    {
        super( id );

        add( new Label( "id", String.valueOf( build.getId() ) ) );
        add( new Label( "start", new FormattedDateModel( build.getStartTime(),
                ( (HeadsUpSession) getSession() ).getTimeZone() ) ) );
        add( new Label( "duration", new FormattedDurationModel( build.getStartTime(), build.getEndTime() ) ) );

        PageParameters params = new PageParameters();
        params.add( "project", build.getProject().getId() );
        params.add( "id", build.getRevision() );
        Link revisionLink = new BookmarkablePageLink( "revision-link", RenderUtil.getPageClass( "files/change" ), params );
        add( revisionLink.add( new Label( "revision", build.getRevision() ) ) );

        String icon;
        switch ( build.getStatus() )
        {
            case Build.BUILD_SUCCEEDED:
                icon = "passed.png";
                break;
            case Build.BUILD_FAILED:
            case Build.BUILD_CANCELLED:
                icon = "failed.png";
                break;
            default:
                icon = "running.png";
        }
        add( new Image( "status", new ResourceReference( View.class, icon ) ) );
        add( new Label( "tests", String.valueOf( build.getTests() ) ).add( new CITestStatusModifier( "tests", build, "tests" ) ) );
        add( new Label( "failures", String.valueOf( build.getFailures() ) ).add( new CITestStatusModifier( "failures", build, "failures" ) ) );
        add( new Label( "errors", String.valueOf( build.getErrors() ) ).add( new CITestStatusModifier( "errors", build, "errors" ) ) );

        params = new PageParameters();
        params.add( "project", build.getProject().getId() );
        params.add( "id", ""+build.getId() );

        WebMarkupContainer warningCell = new WebMarkupContainer( "warning-cell" );
        Link warningLink = new BookmarkablePageLink( "warning-link", RenderUtil.getPageClass( "docs/analyze" ), params );
        warningCell.add( warningLink.add( new Label( "warnings", String.valueOf( build.getWarnings() ) ) ) );
        add( warningCell.add( new CITestStatusModifier( "warnings", build, "warnings" ) ) );


        Project root = build.getProject();
        while ( root.getParent() != null )
        {
            root = root.getParent();
        }
        Build passed = CIApplication.getPreviousLastChangePassed( build, build.getProject() );
        List<ChangeSet> changes;
        if ( passed != null )
        {
            params = new PageParameters();
            params.add( "project", build.getProject().getId() );
            params.add( "id",  String.valueOf( passed.getId() ) );
            revisionLink = new BookmarkablePageLink( "passedBuild-link", RenderUtil.getPageClass( "builds/view" ), params );
            add( revisionLink.add( new Label( "passedBuild", String.valueOf( passed.getId() ) ) ) );
            add( new WebMarkupContainer( "passedBuild" ).setVisible( false ) );

            changes = Manager.getInstance().getScmService().getChangesBetweenRevisions( passed.getRevision(),
                    build.getRevision(), root );

            params = new PageParameters();
            params.add( "project", build.getProject().getId() );
            params.add( "id", passed.getRevision() );
            revisionLink = new BookmarkablePageLink( "passedRevision-link", RenderUtil.getPageClass( "files/change" ), params );
            add( revisionLink.add( new Label( "passedRevision", passed.getRevision() ) ) );
        }
        else
        {
            changes = new ArrayList<ChangeSet>();

            add( new Label( "passedBuild", "n/a" ) );
            add( new WebMarkupContainer( "passedBuild-link" ).setVisible( false ) );

            add( new WebMarkupContainer( "passedRevision" ).setVisible( false ) );
        }

        add( new StripedListView<ChangeSet>( "changes", changes )
        {
            @Override
            protected void populateItem( ListItem<ChangeSet> listItem )
            {
                super.populateItem( listItem );

                ChangeSet set = listItem.getModelObject();

                listItem.setModel( new CompoundPropertyModel<ChangeSet>( set ) );
                String author = set.getAuthor();
                User user = Manager.getSecurityInstance().getUserByUsernameEmailOrFullname( author );
                if ( user != null )
                {
                    author = user.getFullnameOrUsername();
                }

                PageParameters params = new PageParameters();
                if ( user != null )
                {
                    params.add( "username", user.getUsername() );
                    Link authorLink = new BookmarkablePageLink( "author-link", RenderUtil.getPageClass( "account" ), params );
                    authorLink.add( new Label( "author", author ) );
                    listItem.add( authorLink );
                    listItem.add( new WebMarkupContainer( "authorName" ).setVisible( false ) );
                }
                else
                {
                    listItem.add( new WebMarkupContainer( "author-link" ).setVisible( false ) );
                    listItem.add( new Label( "authorName", author ) );
                }
                listItem.add( new Label( "comment" ) );

                if ( set instanceof TransactionalScmChangeSet )
                {
                    params = new PageParameters();
                    params.add( "project", build.getProject().getId() );
                    params.add( "id", ((TransactionalScmChangeSet) set).getRevision() );
                    Link revisionLink = new BookmarkablePageLink( "revision-link", RenderUtil.getPageClass( "files/change" ), params );
                    revisionLink.add( new Label( "revision" ) );
                    listItem.add( revisionLink );
                }
                else
                {
                    listItem.add( new WebMarkupContainer( "revision-link" ).setVisible( false ) );
                }
                listItem.add( new Label( "date", new FormattedDurationModel( set.getDate(), new Date() ) ) );
            }
        }.setVisible( changes.size() > 0 ) );
        WebMarkupContainer noChanges = new WebMarkupContainer( "nochanges" );
        if ( passed != null )
        {
            params = new PageParameters();
            params.add( "project", build.getProject().getId() );
            params.add( "id",  String.valueOf( passed.getId() ) );
            revisionLink = new BookmarkablePageLink( "passedBuild-link", RenderUtil.getPageClass( "builds/view" ), params );
            noChanges.add( revisionLink.add( new Label( "passedBuild", String.valueOf( passed.getId() ) ) ) );
            noChanges.add( new WebMarkupContainer( "passedBuild" ).setVisible( false ) );
        }
        else
        {
            noChanges.add( new Label( "passedBuild", "n/a" ) );
            noChanges.add( new WebMarkupContainer( "passedBuild-link" ).setVisible( false ) );
        }

        add( noChanges.setVisible( changes.size() == 0 ) );
    }
}
