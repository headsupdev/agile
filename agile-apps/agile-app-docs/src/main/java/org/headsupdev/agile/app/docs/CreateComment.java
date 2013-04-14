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

package org.headsupdev.agile.app.docs;

import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.headsupdev.agile.web.HeadsUpPage;
import org.headsupdev.agile.web.BookmarkableMenuLink;
import org.headsupdev.agile.web.MountPoint;
import org.headsupdev.agile.api.Permission;
import org.headsupdev.agile.storage.HibernateStorage;
import org.headsupdev.agile.storage.Comment;
import org.headsupdev.agile.storage.docs.Document;
import org.headsupdev.agile.app.docs.permission.DocEditPermission;
import org.headsupdev.agile.app.docs.event.UpdateDocumentEvent;

import java.util.Date;

/**
 * Add a comment for a document
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@MountPoint( "comment" )
public class CreateComment
    extends HeadsUpPage
{
    private Document doc;
    private String submitLabel = "Create Comment";

    public Permission getRequiredPermission() {
        return new DocEditPermission();
    }

    public void layout()
    {
        super.layout();
        add( CSSPackageResource.getHeaderContribution( getClass(), "doc.css" ));

        String page = getPageParameters().getString( "page" );
        if ( page == null || page.length() == 0 )
        {
            page = "Welcome";
        }

        Document doc = DocsApplication.getDocument( page, getProject() );
        if ( doc == null )
        {
            notFoundError();
            return;
        }

        addLink( new BookmarkableMenuLink( getPageClass( "docs/" ), getPageParameters(), "view" ) );
        this.doc = doc;

        add( new CommentForm( "comment" ) );
    }

    @Override
    public String getTitle()
    {
        return submitLabel + " for document " + doc.getName();
    }

    public Document getDocument()
    {
        doc = (Document) ( (HibernateStorage) getStorage() ).getHibernateSession().merge( doc );

        return doc;
    }

    protected void layoutChild( Form form )
    {
    }

    protected void submitChild( Comment comment )
    {
    }

    protected boolean willChildConsumeComment()
    {
        return false;
    }

    public void setSubmitLabel( String submitLabel )
    {
        this.submitLabel = submitLabel;
    }

    protected UpdateDocumentEvent getUpdateEvent( Comment comment )
    {
        return new UpdateDocumentEvent( doc, getSession().getUser(), comment, "commented on" );
    }

    class CommentForm
        extends Form<Comment>
    {
        private Comment create = new Comment();
        public CommentForm( String id )
        {
            super( id );

            setModel( new CompoundPropertyModel<Comment>( create ) );
            add( new TextArea( "comment" ) );

            layoutChild( this );

            add( new Button( "submit", new Model<String>()
            {
                public String getObject()
                {
                    return submitLabel;
                }
            } ) );
        }

        public void onSubmit()
        {
            getDocument(); // make sure we are merged into the new session

            Date now = new Date();
            if ( create.getComment() != null )
            {
                create.setUser( CreateComment.this.getSession().getUser() );
                create.setCreated( now );
                ( (HibernateStorage) getStorage() ).save( create );

                if ( !willChildConsumeComment() )
                {
                    doc.getComments().add( create );
                }

            }

            submitChild( create );

            // this line is needed by things that extend our form...
            doc.setUpdated( now );

            getHeadsUpApplication().addEvent( getUpdateEvent( create ) );

            setResponsePage( getPageClass( "docs/" ), getPageParameters() );
        }
    }
}