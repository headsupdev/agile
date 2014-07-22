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

import org.apache.wicket.markup.html.link.Link;
import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.storage.Comment;
import org.headsupdev.agile.storage.resource.DurationWorked;
import org.headsupdev.agile.web.HeadsUpPage;
import org.headsupdev.agile.web.HeadsUpSession;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.api.Storage;
import org.headsupdev.agile.storage.Comment;
import org.headsupdev.agile.storage.HibernateStorage;
import org.headsupdev.agile.storage.docs.Document;
import org.headsupdev.agile.storage.resource.DurationWorked;
import org.headsupdev.agile.web.HeadsUpPage;
import org.headsupdev.agile.web.HeadsUpSession;
import org.headsupdev.agile.web.components.FormattedDateModel;
import org.headsupdev.agile.web.components.MarkedUpTextModel;

import java.util.Iterator;
import java.util.List;

/**
 * TODO: Document me
 * <p/>
 * Created: 10/07/2014
 *
 * @author Gordon Edwards
 * @since 1.0
 */
public class CommentPanel extends Panel
{
    private Document doc;
    private IModel model;
    private List commentList;
    private Project project;
    public Link remove;
    private Storage storage;

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

    public CommentPanel( String id, IModel model, Project project, List commentList, Document doc, Storage storage )
    {
        super( id, model );
        this.project = project;
        this.commentList = commentList;
        this.doc = doc;
        this.storage = storage;
        layout();
    }
    
    private void layout()
    {
        Object o = getDefaultModel().getObject();

        WebMarkupContainer commentTitle = new WebMarkupContainer( "comment-title" );
        WebMarkupContainer workedTitle = new WebMarkupContainer( "worked-title" );
        if ( o instanceof Comment )
        {
            final Comment comment = (Comment) o;
            add( new Image( "icon", new ResourceReference( HeadsUpPage.class, "images/comment.png" ) ) );


            Link edit = new Link( "editComment" )
            {
                @Override
                public void onClick()
                {
                    getPage().getPageParameters().put( "commentId", comment.getId() );
                    setResponsePage( EditComment.class, getPage().getPageParameters() );
                }
            };
            add( edit );

            remove = new Link( "removeComment" )
            {
                @Override
                public void onClick()
                {
                    commentList.remove( model.getObject() );

                    Iterator<Comment> iterator = doc.getComments().iterator();
                    while ( iterator.hasNext() )
                    {
                        Comment current = iterator.next();
                        if ( comment.getId() == current.getId() )
                        {
                            iterator.remove();
                        }
                    }
                    doc = (Document) ( (HibernateStorage) storage ).getHibernateSession().merge( doc );
                }
            };
            add( remove );

            commentTitle.add( new Label( "username", comment.getUser().getFullnameOrUsername() ) );
            commentTitle.add( new Label( "created", new FormattedDateModel( comment.getCreated(),
                    ( (HeadsUpSession) getSession() ).getTimeZone() ) ) );
            add( new Label( "comment", new MarkedUpTextModel( comment.getComment(), project ) )
                    .setEscapeModelStrings( false ) );

            workedTitle.setVisible( false );
        }
        else if ( o instanceof DurationWorked )
        {
            add( new Image( "icon", new ResourceReference( HeadsUpPage.class, "images/worked.png" ) ) );
            remove = new Link( "removeComment" )
            {
                @Override
                public void onClick()
                {
                    Iterator<Comment> iterator = commentList.iterator();

                    while ( iterator.hasNext() )
                    {
                        Comment comment = iterator.next();
                        if ( comment.equals( model.getObject() ) )
                        {
                            iterator.remove();
                        }
                    }
                }
            };
            add( remove );
            DurationWorked worked = (DurationWorked) o;
            if ( worked.getWorked() == null || worked.getWorked().getHours() == 0 )
            {
                setVisible( false );
                return;
            }
            String time = "";
            if ( worked.getWorked() != null )
            {
                time = worked.getWorked().toString();
            }
            workedTitle.add( new Label( "worked", time ) );
            workedTitle.add( new Label( "username", worked.getUser().getFullnameOrUsername() ) );
            workedTitle.add( new Label( "created", new FormattedDateModel( worked.getDay(),
                    ( (HeadsUpSession) getSession() ).getTimeZone() ) ) );

            commentTitle.setVisible( false );

            Comment comment = worked.getComment();
            if ( comment != null )
            {
                Label commentLabel = new Label( "comment", new MarkedUpTextModel( comment.getComment(), project ) );
                commentLabel.setEscapeModelStrings( false );
                add( commentLabel );
            }
            else
            {
                add( new WebMarkupContainer( "comment" ).setVisible( false ) );
            }
        }
        else
        {
            commentTitle.setVisible( false );
            workedTitle.setVisible( false );
            add( new WebMarkupContainer( "comment" ).setVisible( false ) );
        }
        add( commentTitle );
        add( workedTitle );
    }
}
