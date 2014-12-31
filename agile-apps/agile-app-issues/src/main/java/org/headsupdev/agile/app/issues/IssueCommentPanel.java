/*
 * HeadsUp Agile
 * Copyright 2014 Heads Up Development Ltd.
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

package org.headsupdev.agile.app.issues;

import org.apache.wicket.PageParameters;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.api.Storage;
import org.headsupdev.agile.api.User;
import org.headsupdev.agile.app.issues.permission.IssueEditPermission;
import org.headsupdev.agile.storage.Comment;
import org.headsupdev.agile.storage.HibernateStorage;
import org.headsupdev.agile.storage.issues.Issue;
import org.headsupdev.agile.storage.resource.DurationWorked;
import org.headsupdev.agile.web.HeadsUpPage;
import org.headsupdev.agile.web.HeadsUpSession;
import org.headsupdev.agile.web.components.*;
import org.headsupdev.agile.web.dialogs.ConfirmDialog;

import java.util.Date;
import java.util.List;

/**
 * A comment panel for issues which includes progress
 *
 * Created: 27/08/2014
 *
 * @author Gordon Edwards
 * @since 2.1
 */

public class IssueCommentPanel
        extends CommentPanel<Issue>
{
    public IssueCommentPanel( String id, IModel model, Project project, List commentList, Issue commentableEntity )
    {
        super( id, model, project, commentList, commentableEntity, new IssueEditPermission() );
        Object o = getDefaultModel().getObject();

        if ( o instanceof DurationWorked )
        {
            addProgressPanel();
        }
        else
        {
            add( new WebMarkupContainer( "worked-title" ).setVisible( false ) );
        }
    }

    public void addProgressPanel()
    {
        final DurationWorked duration = (DurationWorked) getDefaultModel().getObject();
        add( new Image( "icon", new ResourceReference( HeadsUpPage.class, "images/worked.png" ) ) );
        PageParameters params = new PageParameters();
        params.put( "project", project );
        params.put( "id", commentable.getId() );
        params.put( "durationId", duration.getId() );
        Link edit = new BookmarkablePageLink( "editComment", EditProgressIssue.class, params );
        User currentUser = ( (HeadsUpSession) getSession() ).getUser();
        boolean userHasPermission = Manager.getSecurityInstance().userHasPermission( currentUser, permission, project );
        WebMarkupContainer workedTitle = new WebMarkupContainer( "worked-title" );
        workedTitle.add( edit.setVisible( userHasPermission ) );
        workedTitle.add( new GravatarLinkPanel( "gravatar", duration.getUser(), HeadsUpPage.SMALL_AVATAR_EDGE_LENGTH ) );

        Link remove = new AjaxFallbackLink( "removeComment" )
        {
            @Override
            public void onClick( AjaxRequestTarget target )
            {
                ConfirmDialog dialog = new ConfirmDialog( HeadsUpPage.DIALOG_PANEL_ID, "Delete Progress", "delete this progress" )
                {
                    @Override
                    public void onDialogConfirmed()
                    {
                        Storage storage = Manager.getStorageInstance();
                        DurationWorked dur = (DurationWorked) ( (HibernateStorage) storage ).merge( duration );
                        commentable.getTimeWorked().remove( dur );
                        commentable.setUpdated( new Date() );
                        commentList.remove( dur );
                        dur.setIssue( null );
                        ( (HibernateStorage) storage ).delete( dur );
                    }
                };
                showConfirmDialog( dialog, target );
            }
        };
        workedTitle.add( remove.setVisible( userHasPermission ) );
        if ( duration.getWorked() == null || duration.getWorked().getHours() == 0 )
        {
            setVisible( false );
            return;
        }
        String time = duration.getWorked().toString();
        workedTitle.add( new Label( "worked", time ) );

        workedTitle.add( new AccountFallbackLink( "usernameLink", duration.getUser() ) );
        workedTitle.add( new Label( "created", new FormattedDateModel( duration.getDay(),
                ( (HeadsUpSession) getSession() ).getTimeZone() ) ) );

        Comment comment = duration.getComment();
        if ( comment != null )
        {
            Label commentLabel = new Label( "comment", new MarkedUpTextModel( comment.getComment(), project ) );
            commentLabel.setEscapeModelStrings( false );
            add( commentLabel );

            if ( comment.getEditor() != null )
            {
                workedTitle.add( new AccountFallbackLink( "editorLink", duration.getComment().getEditor() ) );
                workedTitle.add( new Label( "updated", new FormattedDateModel( duration.getComment().getUpdated(),
                        ( (HeadsUpSession) getSession() ).getTimeZone() ) ) );
            }
            else
            {
                workedTitle.add( new WebMarkupContainer( "editorLink" ).setVisible( false ) );
                workedTitle.add( new WebMarkupContainer( "updated" ).setVisible( false ) );
            }
        }
        else
        {
            workedTitle.add( new WebMarkupContainer( "editorLink" ).setVisible( false ) );
            workedTitle.add( new WebMarkupContainer( "updated" ).setVisible( false ) );
            add( new WebMarkupContainer( "comment" ).setVisible( false ) );
        }
        add( workedTitle );
    }
}
