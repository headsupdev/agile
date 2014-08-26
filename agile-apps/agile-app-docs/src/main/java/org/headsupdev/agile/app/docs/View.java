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
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.DownloadLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.headsupdev.agile.api.*;
import org.headsupdev.agile.api.mime.Mime;
import org.headsupdev.agile.app.docs.permission.DocViewPermission;
import org.headsupdev.agile.storage.Attachment;
import org.headsupdev.agile.storage.Comment;
import org.headsupdev.agile.storage.HibernateStorage;
import org.headsupdev.agile.storage.docs.Document;
import org.headsupdev.agile.web.BookmarkableMenuLink;
import org.headsupdev.agile.web.HeadsUpPage;
import org.headsupdev.agile.web.HeadsUpSession;
import org.headsupdev.agile.web.components.FormattedDateModel;
import org.headsupdev.agile.web.components.GravatarLinkPanel;
import org.headsupdev.agile.web.components.MarkedUpTextModel;
import org.headsupdev.agile.web.dialogs.ConfirmDialog;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.io.File;
import java.util.*;

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

    public Permission getRequiredPermission()
    {
        return new DocViewPermission();
    }

    public void layout()
    {
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

        PageParameters titledParameters = getPageParameters();
        if ( !titledParameters.containsKey( "page" ) )
        {
            titledParameters.add( "page", title );
        }

        if ( doc == null )
        {
            addLink( new BookmarkableMenuLink( getPageClass( "docs/edit" ), getPageParameters(), "create" ) );
            add( new WebMarkupContainer( "content" ).setVisible( false ) );

            WebMarkupContainer notfound = new WebMarkupContainer( "notfound" );
            notfound.add( new Label( "page", title ) );
            notfound.add( new Label( "project", getProject().getAlias() ) );
            notfound.add( new BookmarkablePageLink( "create", getPageClass( "docs/edit" ), titledParameters ) );
            add( notfound );

            details.setVisible( false );
            return;
        }

        addLink( new BookmarkableMenuLink( getPageClass( "docs/edit" ), titledParameters, "edit" ) );

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

        final List<Attachment> attachmentList = new LinkedList<Attachment>();
        attachmentList.addAll( doc.getAttachments() );
        Collections.sort( attachmentList, new Comparator<Attachment>()
        {
            public int compare( Attachment attachment1, Attachment attachment2 )
            {
                return attachment1.getCreated().compareTo( attachment2.getCreated() );
            }
        } );
        details.add( new ListView<Attachment>( "attachments", attachmentList )
        {

            private Attachment attachment;

            protected void populateItem( ListItem<Attachment> listItem )
            {
                attachment = listItem.getModelObject();
                PageParameters params = new PageParameters();
                listItem.add( new GravatarLinkPanel( "avatar", attachment.getUser(), HeadsUpPage.DEFAULT_AVATAR_EDGE_LENGTH ) );
                params.add( "username", attachment.getUser().getUsername() );
                params.add( "silent", "true" );
                BookmarkablePageLink usernameLink = new BookmarkablePageLink( "usernameLink", View.this.getPageClass( "account" ), params );
                usernameLink.add( new Label( "username", attachment.getUser().getFullnameOrUsername() ) );
                listItem.add( usernameLink );
                listItem.add( new Label( "created", new FormattedDateModel( attachment.getCreated(),
                        ( (HeadsUpSession) getSession() ).getTimeZone() ) ) );

                File file = attachment.getFile( getStorage() );
                Mime mime = Mime.get( file.getName() );
                listItem.add( new Image( "attachment-icon", new ResourceReference( Mime.class, mime.getIconName() ) ) );

                Link download = new DownloadLink( "attachment-link", file );
                download.add( new Label( "attachment-label", attachment.getFilename() ) );
                listItem.add( download );

                listItem.add( new AjaxFallbackLink( "attachment-delete" )
                {
                    @Override
                    public void onClick( AjaxRequestTarget target )
                    {
                        ConfirmDialog dialog = new ConfirmDialog( HeadsUpPage.DIALOG_PANEL_ID, "Delete Attachment", "delete this attachment" )
                        {
                            @Override
                            public void onDialogConfirmed()
                            {
                                attachment = (Attachment) ( (HibernateStorage) getStorage() ).getHibernateSession().merge( attachment );
                                doc = (Document) ( (HibernateStorage) getStorage() ).getHibernateSession().merge( doc );
                                attachmentList.remove( attachment );
                                doc.getAttachments().remove( attachment );
                                ((HibernateStorage) getStorage() ).delete( attachment );
                                attachment.getFile( getStorage() ).delete();
                            }
                        };
                        showDialog( dialog, target );
                    }
                } );

                Comment comment = attachment.getComment();
                if ( comment != null )
                {
                    Label commentLabel = new Label( "comment", new MarkedUpTextModel( comment.getComment(), getProject() ) );
                    commentLabel.setEscapeModelStrings( false );
                    listItem.add( commentLabel );
                }
                else
                {
                    listItem.add( new WebMarkupContainer( "comment" ).setVisible( false ) );
                }
            }
        } );

        final List<Comment> commentList = new LinkedList<Comment>();
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
                listItem.add( new CommentPanel( "comment", listItem.getModel(), getProject(), commentList, doc ){
                    @Override
                    public void showConfirmDialog( ConfirmDialog dialog, AjaxRequestTarget target )
                    {
                        showDialog( dialog, target );
                    }
                });
            }
        } );
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
        return title + PAGE_TITLE_SEPARATOR + super.getPageTitle();
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
