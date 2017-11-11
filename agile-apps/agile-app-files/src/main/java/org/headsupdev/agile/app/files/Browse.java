/*
 * HeadsUp Agile
 * Copyright 2009-2014 Heads Up Development.
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

package org.headsupdev.agile.app.files;

import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.api.service.ChangeSet;
import org.headsupdev.agile.scm.HeadsUpScmManager;
import org.headsupdev.agile.scm.ScmVariant;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.headsupdev.support.java.StringUtil;
import org.headsupdev.agile.security.permission.ProjectListPermission;
import org.headsupdev.agile.api.mime.Mime;
import org.headsupdev.agile.api.Permission;
import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.api.User;
import org.headsupdev.agile.web.components.*;
import org.headsupdev.agile.app.files.permission.FileListPermission;
import org.headsupdev.agile.web.HeadsUpPage;
import org.headsupdev.agile.storage.StoredProject;

import java.io.File;
import java.io.FilenameFilter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

/**
 * Browse main page
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class Browse
    extends HeadsUpPage
{
    private String pathTitle;

    public Permission getRequiredPermission()
    {
        return new FileListPermission();
    }

    public void layout()
    {
        super.layout();
        final Class browseClass = getClass();

        add( CSSPackageResource.getHeaderContribution( getClass(), "browse.css" ) );
        final BrowseApplication app = ( (BrowseApplication) getHeadsUpApplication() );

        if ( getProject().equals( StoredProject.getDefault() ) )
        {
            requirePermission( new ProjectListPermission() );
            add( new Label( "header", "<a href=\"/" + getProject().getId() + "/files/\">" + getProject().getAlias() + "</a>" ).setEscapeModelStrings( false ) );
            pathTitle = getProject().getAlias();
            
            WebMarkupContainer parent = new WebMarkupContainer( "parent" );
            add( parent.setVisible( false ) );

            add( new StripedListView<Project>( "browse-items", getStorage().getRootProjects() )
            {
                protected void populateItem( ListItem<Project> listItem )
                {
                    super.populateItem( listItem );

                    Project project = listItem.getModelObject();
                    PageParameters params = new PageParameters();
                    params.add( "project", project.getId() );

                    Mime mime = Mime.get( "folder" );
                    BookmarkablePageLink iconLink = new BookmarkablePageLink( "browse-icon-link", browseClass, params );
                    iconLink.add( new Image( "browse-icon", new ResourceReference( Mime.class, mime.getIconName() ) ) );
                    listItem.add( iconLink );
                    BookmarkablePageLink link = new BookmarkablePageLink( "browse-link", browseClass, params );
                    link.add( new Label( "browse-label", project.getAlias() ) );
                    listItem.add( link );

                    String author, comment;
                    Date modified;
                    if ( project.getRevision() != null )
                    {
                        ChangeSet change = Manager.getInstance().getScmService().getChangeSet(project, project.getRevision());
                        author = change.getAuthor();
                        comment = change.getComment();
                        modified = change.getDate();
                    }
                    else
                    {
                        author = "";
                        comment = "";
                        modified = FormattedDateModel.NO_DATE;
                    }

                    User user = getSecurityManager().getUserByUsernameEmailOrFullname( author );
                    if ( user != null )
                    {
                        listItem.add( new AccountFallbackLink( "author-link", author ) );
                        listItem.add( new GravatarLinkPanel( "avatar", user, HeadsUpPage.SMALL_AVATAR_EDGE_LENGTH ) );
                        listItem.add( new WebMarkupContainer( "author-label" ).setVisible( false ) );
                    }
                    else
                    {
                        listItem.add( new Label( "author-label", author ) );
                        listItem.add( new GravatarLinkPanel( "avatar", null, HeadsUpPage.SMALL_AVATAR_EDGE_LENGTH ) );
                        listItem.add( new WebMarkupContainer( "author-link" ).setVisible( false ) );
                    }

                    listItem.add( new Label( "comment", new MarkedUpTextModel( comment, getProject() ) )
                        .setEscapeModelStrings( false ) );
                    Link revisionLink;
                    if ( project.getRevision() != null )
                    {
                        params = new PageParameters();
                        params.add( "project", project.getId() );
                        params.add( "id", project.getRevision() );
                        revisionLink = new BookmarkablePageLink( "revision-link", getPageClass( "files/change" ), params );
                        revisionLink.add( new Label( "revision-label", project.getRevision() ) );

                        listItem.add( revisionLink );
                        listItem.add( new WebMarkupContainer( "revision-unknown" ).setVisible( false ) );
                    }
                    else
                    {
                        listItem.add( new WebMarkupContainer( "revision-unknown" ).setVisible( true ) );
                        listItem.add( new WebMarkupContainer( "revision-link" ).setVisible( false ) );
                    }
                    listItem.add( new Label( "size", "" ) );

                    if ( modified.equals( FormattedDateModel.NO_DATE ) )
                    {
                        listItem.add( new Label( "modified", "" ) );
                    }
                    else
                    {
                        listItem.add( new Label( "modified", new FormattedDurationModel( modified, new Date() )
                        {
                            public String getObject()
                            {
                                return super.getObject() + " ago";
                            }
                        } ) );
                    }
                }
            }.setInverted( true ) );
        }
        else
        {
            File path = getStorage().getWorkingDirectory( getProject() );
            String pathStr = getPageParameters().getString( "path" );
            if ( pathStr == null )
            {
                pathStr = "";
                pathTitle = "";
            }
            else
            {
                path = new File( path, pathStr.replace( ':', File.separatorChar ) );
                pathTitle = pathStr.replace( ':', File.separatorChar );
            }
            if ( pathStr.length() > 1 && pathStr.charAt( pathStr.length() - 1 ) != ':' )
            {
                pathStr += ':';
            }
            final String dir = pathStr;

            if ( !path.exists() )
            {
                notFoundError();
                return;
            }

            StringBuilder header = new StringBuilder( "<a href=\"/");
            header.append( getProject().getId() );
            header.append( "/files/\">" );
            header.append( getProject().getAlias() );
            header.append( "</a>" );

            String[] components = pathStr.split( ":" );
            StringBuilder partialPath = new StringBuilder();
            for ( int i = 0; i < components.length; i++ ) {
                if ( i > 0 ) {
                    partialPath.append( ":" );
                }
                partialPath.append( components[i] );

                String filePath = partialPath.toString();
                try
                {
                    filePath = URLEncoder.encode( filePath, "UTF-8" );
                    // funny little hack here, guess the decoding is not right
                    filePath = filePath.replace( "+", "%20" );
                }
                catch ( UnsupportedEncodingException e )
                {
                    // ignore
                }


                header.append( "/" );
                header.append( "<a href=\"/files/");
                header.append( getProject().getId() );
                header.append( "/path/" );
                header.append( filePath );
                header.append( "/\">" );
                header.append( components[i] );
                header.append( "</a>");
            }

            add( new Label( "header", header.toString() ).setEscapeModelStrings( false ) );

            File[] fileArr = path.listFiles( new FilenameFilter()
            {
                public boolean accept( File dir, String name )
                {
                    ScmVariant variant = HeadsUpScmManager.getInstance().getScmVariant( getProject().getScm() );

                    // hide any files that the scm variant suggests should be hidden
                    return !variant.getIgnoredFileNames().contains( name );
                }
            } );
            List<File> files;
            if ( fileArr == null )
            {
                files = new LinkedList<File>();
            }
            else
            {
                files = Arrays.asList( fileArr );
            }
            Collections.sort( files );

            Mime folderUp = Mime.get( "parent-folder" );
            WebMarkupContainer parent = new WebMarkupContainer( "parent" );
            PageParameters params = new PageParameters();
            if ( StringUtil.isEmpty( pathStr ) )
            {
                params.add( "project", "all" );
            }
            else
            {
                params.add( "project", getProject().getId() );

                String parentPath = new File( pathStr.replace( ':', File.separatorChar ) ).getParent();
                if ( parentPath != null )
                {
                    parentPath = parentPath.replace( File.separatorChar, ':' );
                    params.add( "path", parentPath + ":" );
                }
            }
            Link link = new BookmarkablePageLink( "parent-link", browseClass, params );
            parent.add( link );
            Link iconLink = new BookmarkablePageLink( "parent-icon-link", browseClass, params );
            iconLink.add( new Image( "parent-icon", new ResourceReference( Mime.class, folderUp.getIconName() ) ) );
            parent.add( iconLink );
            add( parent );

            add( new StripedListView<File>( "browse-items", files )
            {
                protected void populateItem( ListItem<File> listItem )
                {
                    super.populateItem( listItem );

                    File file = listItem.getModelObject();
                    Link link, iconLink;
                    Mime mime;
                    if ( file.isDirectory() )
                    {
                        PageParameters params = getProjectPageParameters();
                        params.add( "path", dir + file.getName() + ":" );
                        iconLink = new BookmarkablePageLink( "browse-icon-link", browseClass, params );
                        link = new BookmarkablePageLink( "browse-link", browseClass, params );
                        mime = Mime.get( "folder" );
                    }
                    else
                    {
                        mime = Mime.get( file.getName() );

                        PageParameters params = getProjectPageParameters();
                        params.add( "path", dir + file.getName() );
                        iconLink = new BookmarkablePageLink( "browse-icon-link", BrowseFile.class, params );
                        link = new BookmarkablePageLink( "browse-link", BrowseFile.class, params );
                    }
                    link.add( new Label( "browse-label", file.getName() ) );
                    listItem.add( link );
                    iconLink.add( new Image( "browse-icon", new ResourceReference( Mime.class, mime.getIconName() ) ) );
                    listItem.add( iconLink );

                    String author, comment;
                    Date modified;

                    String searchPath = dir.replace( ':', File.separatorChar ) + file.getName();
                    File searchDir = getStorage().getWorkingDirectory( getProject() );
                    Project root = getProject();
                    while ( root.getParent() != null )
                    {
                        root = root.getParent();
                        searchPath = searchDir.getName() + File.separatorChar + searchPath;
                        searchDir = searchDir.getParentFile();
                    }
                    Map<String, org.headsupdev.agile.storage.files.File> projectMap = app.getProjectFileMap( root );
                    String fileRevision = null;
                    if ( projectMap != null )
                    {
                        org.headsupdev.agile.storage.files.File fileMeta = projectMap.get( searchPath );
                        if ( fileMeta != null )
                        {
                            fileRevision = fileMeta.getRevision();
                        }
                    }
                    if ( fileRevision != null )
                    {
                        ChangeSet change = Manager.getInstance().getScmService().getChangeSet( root, fileRevision );
                        author = change.getAuthor();
                        comment = change.getComment();
                        modified = change.getDate();
                    }
                    else
                    {
                        author = "";
                        comment = "";
                        modified = FormattedDateModel.NO_DATE;
                    }

                    User user = getSecurityManager().getUserByUsernameEmailOrFullname( author );
                    if ( user != null )
                    {
                        listItem.add( new AccountFallbackLink( "author-link", author ) );
                        listItem.add( new GravatarLinkPanel( "avatar", user, HeadsUpPage.SMALL_AVATAR_EDGE_LENGTH ) );
                        listItem.add( new WebMarkupContainer( "author-label" ).setVisible( false ) );
                    }
                    else
                    {
                        listItem.add( new WebMarkupContainer( "author-link" ).setVisible( false ) );
                        listItem.add( new GravatarLinkPanel( "avatar", null, HeadsUpPage.SMALL_AVATAR_EDGE_LENGTH ) );
                        listItem.add( new Label( "author-label", author ) );
                    }

                    listItem.add( new Label( "comment", comment ) );
                    Link revisionLink;
                    if ( fileRevision != null )
                    {
                        PageParameters params = new PageParameters();
                        params.add( "project", getProject().getId() );
                        params.add( "id", fileRevision );
                        revisionLink = new BookmarkablePageLink( "revision-link", getPageClass( "files/change" ), params );
                        revisionLink.add( new Label( "revision-label", fileRevision ) );

                        listItem.add( revisionLink );
                        listItem.add( new WebMarkupContainer( "revision-unknown" ).setVisible( false ) );
                    }
                    else
                    {
                        listItem.add( new WebMarkupContainer( "revision-unknown" ).setVisible( true ) );
                        listItem.add( new WebMarkupContainer( "revision-link" ).setVisible( false ) );
                    }
                    if ( file.isDirectory() )
                    {
                        listItem.add( new Label( "size", "" ) );
                    }
                    else
                    {
                        listItem.add( new Label( "size", new FormattedSizeModel( file.length() ) ) );
                    }

                    if ( modified.equals( FormattedDateModel.NO_DATE ) )
                    {
                        listItem.add( new Label( "modified", "" ) );
                    }
                    else
                    {
                        listItem.add( new Label( "modified", new FormattedDurationModel( modified, new Date() )
                        {
                            public String getObject()
                            {
                                return super.getObject() + " ago";
                            }
                        } ) );
                    }
                }
            } );
        }
    }

    @Override
    public String getPageTitle()
    {
        return pathTitle + PAGE_TITLE_SEPARATOR + super.getPageTitle();
    }
}

