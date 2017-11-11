/*
 * HeadsUp Agile
 * Copyright 2014 Heads Up Development.
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

import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.headsupdev.agile.api.*;
import org.headsupdev.agile.storage.Comment;
import org.headsupdev.agile.storage.CommentableEntity;
import org.headsupdev.agile.storage.HibernateStorage;
import org.headsupdev.agile.web.HeadsUpPage;
import org.headsupdev.agile.web.HeadsUpSession;
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
        WebMarkupContainer commentTitle = new WebMarkupContainer( "comment-title" );

        if ( o instanceof Comment )
        {
            User currentUser = ( (HeadsUpSession) getSession() ).getUser();
            boolean userHasPermission = Manager.getSecurityInstance().userHasPermission( currentUser, permission, project );
            comment = (Comment) o;
            add( new Image( "icon", new ResourceReference( HeadsUpPage.class, "images/comment.png" ) ) );
            if ( getEditLink() != null )
            {
                Link edit = getEditLink();
                commentTitle.add( edit.setVisible( userHasPermission ) );
            }
            else
            {
                commentTitle.add( new WebMarkupContainer( "editComment" ).setVisible( false ) );
            }
            commentTitle.add( new GravatarLinkPanel( "gravatar", comment.getUser(), HeadsUpPage.SMALL_AVATAR_EDGE_LENGTH ) );

            commentTitle.add( new AccountFallbackLink( "usernameLink", comment.getUser() ) );
            commentTitle.add( new Label( "created", new FormattedDateModel( comment.getCreated(),
                    ( (HeadsUpSession) getSession() ).getTimeZone() ) ) );
            if ( comment.getEditor() != null )
            {
                commentTitle.add( new AccountFallbackLink( "editorLink", comment.getEditor() ) );
                commentTitle.add( new Label( "updated", new FormattedDateModel( comment.getUpdated(),
                        ( (HeadsUpSession) getSession() ).getTimeZone() ) ) );
            }
            else
            {
                commentTitle.add( new WebMarkupContainer( "editorLink" ).setVisible( false ) );
                commentTitle.add( new WebMarkupContainer( "updated" ).setVisible( false ) );
            }
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
                            commentable.removeComment( comm );
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
        }
        else
        {
            commentTitle.setVisible( false );
        }
        add( commentTitle );

    }

    public void showConfirmDialog( ConfirmDialog dialog, AjaxRequestTarget target )
    {
    }

    public Link getEditLink()
    {
        return null;
    }

    public Comment getComment()
    {
        return comment;
    }

}
