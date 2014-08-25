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

package org.headsupdev.agile.app.docs;

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
import org.apache.wicket.model.Model;
import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.api.Storage;
import org.headsupdev.agile.api.User;
import org.headsupdev.agile.app.docs.permission.DocEditPermission;
import org.headsupdev.agile.storage.Comment;
import org.headsupdev.agile.storage.HibernateStorage;
import org.headsupdev.agile.storage.docs.Document;
import org.headsupdev.agile.storage.resource.DurationWorked;
import org.headsupdev.agile.web.HeadsUpPage;
import org.headsupdev.agile.web.HeadsUpSession;
import org.headsupdev.agile.web.components.FormattedDateModel;
import org.headsupdev.agile.web.components.MarkedUpTextModel;
import org.headsupdev.agile.web.dialogs.ConfirmDialog;

import java.util.Date;
import java.util.List;

/**
 * TODO: Document me
 * <p/>
 * Created: 10/07/2014
 *
 * @author Gordon Edwards
 * @since 1.0
 */
public class CommentPanel
        extends Panel
{
    private Document doc;
    private IModel model;
    private List commentList;
    private Project project;
    public Link remove;
    private Comment comment;

    public CommentPanel( String id, Comment comment, Project project )
    {
        super( id, new Model( comment ) );
        this.project = project;

        layout();
    }

    public CommentPanel( String id, DurationWorked worked, Project project )
    {
        super( id, new Model( worked ) );
        this.project = project;

        layout();
    }

    public CommentPanel( String id, IModel model, Project project, List commentList, Document doc )
    {
        super( id, model );
        this.project = project;
        this.commentList = commentList;
        this.doc = doc;
        layout();
    }

    private void layout()
    {
        User currentUser = ( (HeadsUpSession) getSession() ).getUser();
        boolean userHasPermission = Manager.getSecurityInstance().userHasPermission( currentUser, new DocEditPermission(), project );

        Object o = getDefaultModel().getObject();

        WebMarkupContainer commentTitle = new WebMarkupContainer( "comment-title" );
        if ( o instanceof Comment )
        {
            comment = (Comment) o;
            add( new Image( "icon", new ResourceReference( HeadsUpPage.class, "images/comment.png" ) ) );

            PageParameters params = new PageParameters();
            params.put( "project", project );
            params.put( "page", doc.getName() );
            params.put( "commentId", comment.getId() );
            Link edit = new BookmarkablePageLink( "editComment", EditComment.class, params );

            commentTitle.add( edit.setVisible( userHasPermission ) );

            remove = new AjaxFallbackLink( "removeComment" )
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
                            doc.getComments().remove( comm );
                            Document d = (Document) ( (HibernateStorage) storage ).merge( doc );
                            commentList.remove( comm );
                            d.setUpdated( new Date() );
                            ( (HibernateStorage) storage ).delete( comm );
                        }
                    };
                    showConfirmDialog( dialog, target );
                }
            };
            commentTitle.add( remove.setVisible( userHasPermission ) );

            commentTitle.add( new Label( "username", comment.getUser().getFullnameOrUsername() ) );
            commentTitle.add( new Label( "created", new FormattedDateModel( comment.getCreated(),
                    ( (HeadsUpSession) getSession() ).getTimeZone() ) ) );
            add( new Label( "comment", new MarkedUpTextModel( comment.getComment(), project ) )
                    .setEscapeModelStrings( false ) );
        }
        else
        {
            commentTitle.setVisible( false );
            add( new WebMarkupContainer( "comment" ).setVisible( false ) );
        }
        add( commentTitle );
    }

    public void showConfirmDialog( ConfirmDialog dialog, AjaxRequestTarget target )
    {
    }
}
