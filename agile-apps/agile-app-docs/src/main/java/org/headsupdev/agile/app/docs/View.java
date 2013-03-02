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

import org.apache.wicket.PageParameters;
import org.headsupdev.agile.api.*;
import org.headsupdev.agile.api.mime.Mime;
import org.headsupdev.agile.storage.HibernateStorage;
import org.headsupdev.agile.web.HeadsUpPage;
import org.headsupdev.agile.web.BookmarkableMenuLink;
import org.headsupdev.agile.web.HeadsUpSession;
import org.headsupdev.agile.web.components.CommentPanel;
import org.headsupdev.agile.web.components.FormattedDateModel;
import org.headsupdev.agile.app.docs.permission.DocViewPermission;
import org.headsupdev.agile.storage.docs.Document;
import org.headsupdev.agile.storage.Attachment;
import org.headsupdev.agile.storage.Comment;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.link.DownloadLink;
import org.apache.wicket.ResourceReference;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;
import java.util.LinkedList;
import java.util.Collections;
import java.util.Comparator;
import java.io.File;

/**
 * Documents home page
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class View
    extends HeadsUpPage
{
    private String title;
    private boolean watching = false;
    private Document doc;

    public Permission getRequiredPermission() {
        return new DocViewPermission();
    }

    public void layout() {
        super.layout();
        add( CSSPackageResource.getHeaderContribution( getClass(), "doc.css" ) );

        title = getPageParameters().getString( "page" );
        if ( title == null || title.length() == 0 )
        {
            title = Document.DEFAULT_PAGE;
        }

        doc = DocsApplication.getDocument( title, getProject() );
        View.layoutMenuItems( this );

        WebMarkupContainer details = new WebMarkupContainer( "details" );
        add( details );

        if ( doc == null )
        {
            addLink( new BookmarkableMenuLink( getPageClass( "docs/edit" ), getPageParameters(), "create" ) );
            add( new WebMarkupContainer( "content" ).setVisible( false ) );

            WebMarkupContainer notfound = new WebMarkupContainer( "notfound" );
            notfound.add( new Label( "page", title ) );
            notfound.add( new Label( "project", getProject().getAlias() ) );
            PageParameters createParams = getPageParameters();
            if ( !createParams.containsKey( "page" ) )
            {
                createParams.add( "page", title );
            }
            notfound.add( new BookmarkablePageLink( "create", getPageClass( "docs/edit" ), createParams ) );
            add( notfound );

            details.setVisible( false );
            return;
        }

        addLink( new BookmarkableMenuLink( getPageClass( "docs/edit" ), getPageParameters(), "edit" ) );

        watching = doc.getWatchers().contains( getSession().getUser() );
        addLink( new MenuLink()
        {
            public String getLabel()
            {
                if ( watching )
                {
                    return "unwatch";
                }

                return "watch";
            }

            public void onClick()
            {
                toggleWatching();
            }
        } );

        addLink( new BookmarkableMenuLink( getPageClass( "docs/comment" ), getPageParameters(), "comment" ) );
        addLink( new BookmarkableMenuLink( getPageClass( "docs/attach" ), getPageParameters(), "attach" ) );
        add( new WebMarkupContainer( "notfound" ).setVisible( false ) );
        add( new Label( "content", getContent( doc ) ).setEscapeModelStrings( false ) );

        List<Attachment> attachmentList = new LinkedList<Attachment>();
        attachmentList.addAll( doc.getAttachments() );
        Collections.sort( attachmentList, new Comparator<Attachment>() {
            public int compare( Attachment attachment1, Attachment attachment2 )
            {
                return attachment1.getCreated().compareTo( attachment2.getCreated() );
            }
        } );
        details.add( new ListView<Attachment>( "attachments", attachmentList )
        {
            protected void populateItem( ListItem<Attachment> listItem )
            {
                Attachment attachment = listItem.getModelObject();
                listItem.add( new Label( "username", attachment.getUser().getFullnameOrUsername() ) );
                listItem.add( new Label( "created", new FormattedDateModel( attachment.getCreated(),
                        ( (HeadsUpSession) getSession() ).getTimeZone() ) ) );

                File file = attachment.getFile( getStorage() );
                Mime mime = Mime.get( file.getName() );
                listItem.add( new Image( "attachment-icon", new ResourceReference( Mime.class, mime.getIconName() ) ) );

                Link download = new DownloadLink( "attachment-link", file );
                download.add( new Label( "attachment-label", attachment.getFilename() ) );
                listItem.add( download );
            }
        } );

        List<Comment> commentList = new LinkedList<Comment>();
        commentList.addAll( doc.getComments() );
        Collections.sort( commentList, new Comparator<Comment>()
        {
            public int compare( Comment comment1, Comment comment2 )
            {
                return comment1.getCreated().compareTo( comment2.getCreated() );
            }
        } );
        details.add( new ListView<Comment>( "comments", commentList )
        {
            protected void populateItem( ListItem<Comment> listItem )
            {
                listItem.add( new CommentPanel( "comment", listItem.getModel(), getProject() ) );
            }
        });
    }

    public static void layoutMenuItems( HeadsUpPage page )
    {
        if ( page.getProject() instanceof MavenTwoProject )
        {
            page.addLink( new SimpleMenuLink( "maven-site" ) );
        }
    }

    @Override
    public String getPageTitle()
    {
        return super.getPageTitle() + " :: " + title;
    }

    public static String getContent( Document doc )
    {
        return DocumentRenderer.markupLinks( doc.getContent(), doc.getProject(),
                Manager.getInstance().getLinkProviders() );
    }

    void toggleWatching()
    {
        if ( watching )
        {
            doc.getWatchers().remove( getSession().getUser() );
        }
        else
        {
            doc.getWatchers().add( getSession().getUser() );
        }

        Session session = ( (HibernateStorage) Manager.getStorageInstance() ).getHibernateSession();
        Transaction tx = session.beginTransaction();
        session.update( doc );
        tx.commit();

        watching = !watching;
    }
}
