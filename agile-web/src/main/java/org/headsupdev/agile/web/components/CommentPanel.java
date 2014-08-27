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

package org.headsupdev.agile.web.components;

import org.apache.wicket.PageParameters;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.headsupdev.agile.api.*;
import org.headsupdev.agile.storage.Comment;
import org.headsupdev.agile.storage.CommentableEntity;
import org.headsupdev.agile.storage.HibernateStorage;
import org.headsupdev.agile.storage.resource.DurationWorked;
import org.headsupdev.agile.web.HeadsUpPage;
import org.headsupdev.agile.web.HeadsUpSession;
import org.headsupdev.agile.web.RenderUtil;
import org.headsupdev.agile.web.dialogs.ConfirmDialog;

import java.util.Date;
import java.util.List;

/**
 * A generic comment panel
 *
 * Created: Created: 27/08/2014
 *
 * @author Gordon Edwards
 * @since 2.1
 */
public class CommentPanel<T extends CommentableEntity>
        extends Panel
{
    protected Permission permission;
    protected T commentable;
    protected List commentList;
    protected Project project;
    private Comment comment;
    protected WebMarkupContainer commentTitle;
    protected WebMarkupContainer workedTitle;

    public CommentPanel( String id, IModel model, Project project, List commentList, T commentable, Permission permission )
    {
        super( id, model );
        this.project = project;
        this.commentList = commentList;
        this.commentable = commentable;
        this.permission = permission;
        layout();
    }

    private void layout()
    {
        Object o = getDefaultModel().getObject();
        commentTitle = new WebMarkupContainer( "comment-title" );
        workedTitle = new WebMarkupContainer( "worked-title" );

        if ( o instanceof Comment )
        {
            User currentUser = ( (HeadsUpSession) getSession() ).getUser();
            boolean userHasPermission = Manager.getSecurityInstance().userHasPermission( currentUser, permission, project );
            comment = (Comment) o;
            add( new Image( "icon", new ResourceReference( HeadsUpPage.class, "images/comment.png" ) ) );
            if ( getLink() != null )
            {
                Link edit = getLink();
                commentTitle.add( edit.setVisible( userHasPermission ) );
            }
            else
            {
                commentTitle.add( new WebMarkupContainer( "editComment" ) );
            }
            commentTitle.add( new GravatarLinkPanel( "gravatar", comment.getUser(), HeadsUpPage.DEFAULT_AVATAR_EDGE_LENGTH ) );

            PageParameters params = new PageParameters();
            params.add( "username", comment.getUser().getUsername() );
            params.add( "silent", "true" );
            BookmarkablePageLink usernameLink = new BookmarkablePageLink( "usernameLink", RenderUtil.getPageClass( "account" ), params );
            usernameLink.add( new Label( "username", comment.getUser().getFullnameOrUsername() ) );
            commentTitle.add( usernameLink );
            commentTitle.add( new Label( "created", new FormattedDateModel( comment.getCreated(),
                    ( (HeadsUpSession) getSession() ).getTimeZone() ) ) );
            add( new Label( "comment", new MarkedUpTextModel( comment.getComment(), project ) )
                    .setEscapeModelStrings( false ) );
            Link remove = new AjaxFallbackLink( "removeComment" )
            {
                @Override
                public void onClick( AjaxRequestTarget target )
                {
                    ConfirmDialog dialog = new ConfirmDialog( HeadsUpPage.DIALOG_PANEL_ID, "Delete Comment", "delete this comment" )
                    {
                        @Override
                        public void onDialogConfirmed()
                        {
                            Storage storage = Manager.getStorageInstance();
                            Comment comm = (Comment) ( (HibernateStorage) storage ).merge( comment );
                            commentable.getComments().remove( comm );
                            T iss = (T) ( (HibernateStorage) storage ).merge( commentable );
                            commentList.remove( comm );
                            iss.setUpdated( new Date() );
                            ( (HibernateStorage) storage ).delete( comm );
                        }
                    };
                    showConfirmDialog( dialog, target );
                }
            };
            commentTitle.add( remove.setVisible( userHasPermission ) );
            workedTitle.setVisible( false );
        }
        else if ( o instanceof DurationWorked )
        {
            addProgressPanel();
        }
//            duration = (DurationWorked) o;
//            add( new Image( "icon", new ResourceReference( HeadsUpPage.class, "images/worked.png" ) ) );
//
//            PageParameters params = new PageParameters();
//            params.put( "project", project );
//            params.put( "id", commentable.getId() );
//            params.put( "durationId", duration.getId() );
//            Link edit = new BookmarkablePageLink( "editComment", EditProgressIssue.class, params );
//            workedTitle.add( edit.setVisible( userHasPermission ) );
//            workedTitle.add( new GravatarLinkPanel( "gravatar", duration.getUser(), HeadsUpPage.DEFAULT_AVATAR_EDGE_LENGTH ) );
//
//            Link remove = new AjaxFallbackLink( "removeComment" )
//            {
//                @Override
//                public void onClick( AjaxRequestTarget target )
//                {
//                    ConfirmDialog dialog = new ConfirmDialog( HeadsUpPage.DIALOG_PANEL_ID, "Delete Progress", "delete this progress" )
//                    {
//                        @Override
//                        public void onDialogConfirmed()
//                        {
//                            Storage storage = Manager.getStorageInstance();
//                            DurationWorked dur = (DurationWorked) ( (HibernateStorage) storage ).merge( duration );
//                            commentable.getTimeWorked().remove( dur );
//                            commentable.setUpdated( new Date() );
//                            commentList.remove( dur );
//                            dur.setIssue( null );
//                            ( (HibernateStorage) storage ).delete( dur );
//                        }
//                    };
//                    showConfirmDialog( dialog, target );
//                }
//            };
//            workedTitle.add( remove.setVisible( userHasPermission ) );
//            DurationWorked worked = (DurationWorked) o;
//            if ( worked.getWorked() == null || worked.getWorked().getHours() == 0 )
//            {
//                setVisible( false );
//                return;
//            }
//            String time = "";
//            if ( worked.getWorked() != null )
//            {
//                time = worked.getWorked().toString();
//            }
//            workedTitle.add( new Label( "worked", time ) );
//            params.add( "username", duration.getUser().getUsername() );
//            params.add( "silent", "true" );
//            BookmarkablePageLink usernameLink = new BookmarkablePageLink( "usernameLink", RenderUtil.getPageClass( "account" ), params );
//            usernameLink.add( new Label( "username", duration.getUser().getFullnameOrUsername() ) );
//            workedTitle.add( usernameLink );
//            workedTitle.add( new Label( "created", new FormattedDateModel( worked.getDay(),
//                    ( (HeadsUpSession) getSession() ).getTimeZone() ) ) );
//
//            commentTitle.setVisible( false );
//
//            Comment comment = worked.getComment();
//            if ( comment != null )
//            {
//                Label commentLabel = new Label( "comment", new MarkedUpTextModel( comment.getComment(), project ) );
//                commentLabel.setEscapeModelStrings( false );
//                add( commentLabel );
//            }
//            else
//            {
//                add( new WebMarkupContainer( "comment" ).setVisible( false ) );
//            }
//        }
//        else
//        {
//            commentTitle.setVisible( false );
//            workedTitle.setVisible( false );
//            add( new WebMarkupContainer( "comment" ).setVisible( false ) );
//        }
        add( commentTitle );
        add( workedTitle );
    }

    public void addProgressPanel()
    {
    }

    public void showConfirmDialog( ConfirmDialog dialog, AjaxRequestTarget target )
    {

    }

    public Link getLink()
    {
        return null;
    }

    public Comment getComment()
    {
        return comment;
    }

}
