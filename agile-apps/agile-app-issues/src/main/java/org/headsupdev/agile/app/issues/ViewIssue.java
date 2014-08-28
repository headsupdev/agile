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

package org.headsupdev.agile.app.issues;

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
import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.api.MenuLink;
import org.headsupdev.agile.api.Permission;
import org.headsupdev.agile.api.User;
import org.headsupdev.agile.api.mime.Mime;
import org.headsupdev.agile.app.issues.permission.IssueEditPermission;
import org.headsupdev.agile.app.issues.permission.IssueViewPermission;
import org.headsupdev.agile.storage.Attachment;
import org.headsupdev.agile.storage.Comment;
import org.headsupdev.agile.storage.HibernateStorage;
import org.headsupdev.agile.storage.StoredProject;
import org.headsupdev.agile.storage.issues.Issue;
import org.headsupdev.agile.storage.resource.DurationWorked;
import org.headsupdev.agile.web.*;
import org.headsupdev.agile.web.components.EmbeddedFilePanel;
import org.headsupdev.agile.web.components.FormattedDateModel;
import org.headsupdev.agile.web.components.GravatarLinkPanel;
import org.headsupdev.agile.web.components.MarkedUpTextModel;
import org.headsupdev.agile.web.components.issues.IssueListPanel;
import org.headsupdev.agile.web.dialogs.ConfirmDialog;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.io.File;
import java.util.*;

/**
 * Issue view page
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@MountPoint("view")
public class ViewIssue
        extends HeadsUpPage
{
    private long issueId;
    private Issue issue;
    private boolean watching;
    private IssuePanel issuePanel;

    public Permission getRequiredPermission()
    {
        return new IssueViewPermission();
    }

    public void layout()
    {
        super.layout();
        add( CSSPackageResource.getHeaderContribution( getClass(), "issue.css" ) );
        add( CSSPackageResource.getHeaderContribution( IssueListPanel.class, "issue.css" ) );

        try
        {
            issueId = getPageParameters().getLong( "id" );
        }
        catch ( NumberFormatException e )
        {
            notFoundError();
            return;
        }

        issue = IssuesApplication.getIssue( issueId, getProject() );
        if ( issue == null )
        {
            notFoundError();
            return;
        }

        watching = issue.getWatchers().contains( getSession().getUser() );
        List<MenuLink> links = getLinks( issue );
        if ( issue.getStatus() < Issue.STATUS_CLOSED )
        {
            links.add( 1, new MenuLink()
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
        }
        addLinks( links );
        issuePanel = new IssuePanel( "issue", issue );
        add( issuePanel );

        final List<Attachment> attachmentList = new LinkedList<Attachment>();
        attachmentList.addAll( issue.getAttachments() );
        Collections.sort( attachmentList, new Comparator<Attachment>()
        {
            public int compare( Attachment attachment1, Attachment attachment2 )
            {
                return attachment1.getCreated().compareTo( attachment2.getCreated() );
            }
        } );
        add( new ListView<Attachment>( "attachments", attachmentList )
        {

            private Attachment attachment;

            protected void populateItem( ListItem<Attachment> listItem )
            {
                attachment = listItem.getModelObject();
                PageParameters params = new PageParameters();
                listItem.add( new GravatarLinkPanel( "avatar", attachment.getUser(), HeadsUpPage.DEFAULT_AVATAR_EDGE_LENGTH ) );
                params.add( "username", attachment.getUser().getUsername() );
                params.add( "silent", "true" );
                BookmarkablePageLink usernameLink = new BookmarkablePageLink( "usernameLink", ViewIssue.this.getPageClass( "account" ), params );
                usernameLink.add( new Label( "username", attachment.getUser().getFullnameOrUsername() ) );
                listItem.add( usernameLink );
                listItem.add( new Label( "created", new FormattedDateModel( attachment.getCreated(),
                        ( (HeadsUpSession) getSession() ).getTimeZone() ) ) );

                final File file = attachment.getFile( getStorage() );
                Mime mime = Mime.get( file.getName() );
                listItem.add( new Image( "attachment-icon", new ResourceReference( Mime.class, mime.getIconName() ) ) );

                listItem.add( new EmbeddedFilePanel( "embed", attachment.getFile( getStorage() ), getProject() ) );

                Link download = new DownloadLink( "attachment-link", file );
                download.add( new Label( "attachment-label", attachment.getFilename() ) );
                listItem.add( download );
                User currentUser = ( (HeadsUpSession) getSession() ).getUser();
                listItem.add( new AjaxFallbackLink( "attachment-delete" )
                {
                    @Override
                    public void onClick( AjaxRequestTarget ajaxRequestTarget )
                    {
                        ConfirmDialog dialog = new ConfirmDialog( HeadsUpPage.DIALOG_PANEL_ID, "Delete Attachment", "delete this attachment" )
                        {
                            @Override
                            public void onDialogConfirmed()
                            {
                                attachment = (Attachment) ( (HibernateStorage) getStorage() ).getHibernateSession().merge( attachment );
                                issue = (Issue) ( (HibernateStorage) getStorage() ).getHibernateSession().merge( issue );
                                attachmentList.remove( attachment );
                                issue.getAttachments().remove( attachment );
                                ( (HibernateStorage) getStorage() ).delete( attachment );
                                attachment.getFile( getStorage() ).delete();
                            }
                        };
                        showDialog( dialog, ajaxRequestTarget );
                    }
                }.setVisible( Manager.getSecurityInstance().userHasPermission( currentUser, new IssueEditPermission(), getProject() ) ) );
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

        final List commentList = new LinkedList();
        commentList.addAll( issue.getComments() );
        if ( issue.getTimeWorked() != null &&
                Boolean.parseBoolean( issue.getProject().getConfigurationValue( StoredProject.CONFIGURATION_TIMETRACKING_ENABLED ) ) )
        {
            commentList.addAll( issue.getTimeWorked() );
        }

        Collections.sort( commentList, new Comparator()
        {
            public int compare( Object o1, Object o2 )
            {
                Date date1 = null, date2 = null;
                if ( o1 instanceof Comment )
                {
                    date1 = ( (Comment) o1 ).getCreated();
                }
                else if ( o1 instanceof DurationWorked )
                {
                    date1 = ( (DurationWorked) o1 ).getDay();
                }

                if ( o2 instanceof Comment )
                {
                    date2 = ( (Comment) o2 ).getCreated();
                }
                else if ( o2 instanceof DurationWorked )
                {
                    date2 = ( (DurationWorked) o2 ).getDay();
                }

                if ( date1 == null || date2 == null )
                {
                    if ( date1 == null )
                    {
                        if ( date2 == null )
                        {
                            return 0;
                        }
                        else
                        {
                            return 1;
                        }
                    }
                    else
                    {
                        return -1;
                    }
                }

                return date1.compareTo( date2 );
            }
        } );
        add( new ListView( "comments", commentList )
        {
            protected void populateItem( final ListItem listItem )
            {
                CommentPanel panel = new CommentPanel( "comment", listItem.getModel(), getProject(), commentList, issue ){
                    @Override
                    public void showConfirmDialog( ConfirmDialog dialog, AjaxRequestTarget target )
                    {
                        showDialog( dialog, target );
                    }
                };
                listItem.add( panel );
            }
        } );
    }

    @Override
    public String getPageTitle()
    {
        return "Issue:" + issueId + PAGE_TITLE_SEPARATOR + super.getPageTitle();
    }

    public static List<MenuLink> getLinks( Issue issue )
    {
        List<MenuLink> links = new LinkedList<MenuLink>();
        PageParameters pageParams = new PageParameters();
        pageParams.add( "project", issue.getProject().getId() );
        pageParams.add( "id", String.valueOf( issue.getId() ) );

        if ( issue.getStatus() < Issue.STATUS_CLOSED )
        {
            links.add( new BookmarkableMenuLink( RenderUtil.getPageClass( "issues/edit" ), pageParams, "edit" ) );
            links.add( new BookmarkableMenuLink( RenderUtil.getPageClass( "issues/comment" ), pageParams, "comment" ) );
            links.add( new BookmarkableMenuLink( RenderUtil.getPageClass( "issues/attach" ), pageParams, "attach" ) );
            links.add( new BookmarkableMenuLink( RenderUtil.getPageClass( "issues/relate" ), pageParams, "relate" ) );

            links.add( new BookmarkableMenuLink( RenderUtil.getPageClass( "issues/assign" ), pageParams, "assign" ) );

            if ( issue.getStatus() < Issue.STATUS_RESOLVED )
            {
                if ( Boolean.parseBoolean( issue.getProject().getConfigurationValue( StoredProject.CONFIGURATION_TIMETRACKING_ENABLED ) ) )
                {
                    links.add( new BookmarkableMenuLink( RenderUtil.getPageClass( "issues/progress" ), pageParams, "progress" ) );
                }
                links.add( new BookmarkableMenuLink( RenderUtil.getPageClass( "issues/resolve" ), pageParams, "resolve" ) );
            }
            else
            {
                links.add( new BookmarkableMenuLink( RenderUtil.getPageClass( "issues/reopen" ), pageParams, "unresolve" ) );
            }

            links.add( new BookmarkableMenuLink( RenderUtil.getPageClass( "issues/close" ), pageParams, "close" ) );
        }
        else
        {
            links.add( new BookmarkableMenuLink( RenderUtil.getPageClass( "issues/reopen" ), pageParams, "reopen" ) );
        }

        return links;
    }

    void toggleWatching()
    {
        if ( watching )
        {
            issue.getWatchers().remove( getSession().getUser() );
        }
        else
        {
            issue.getWatchers().add( getSession().getUser() );
        }

        Session session = ( (HibernateStorage) Manager.getStorageInstance() ).getHibernateSession();
        Transaction tx = session.beginTransaction();
        session.update( issue );
        tx.commit();
        setResponsePage( ApplicationPageMapper.get().getPageClass( "issues/view" ), getPageParameters() );
        watching = !watching;
    }
}
