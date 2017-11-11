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
import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.api.Permission;
import org.headsupdev.agile.api.User;
import org.headsupdev.agile.api.service.ChangeSet;
import org.headsupdev.agile.app.files.permission.FileViewPermission;
import org.headsupdev.agile.web.components.*;
import org.headsupdev.agile.web.HeadsUpPage;
import org.headsupdev.agile.web.BookmarkableMenuLink;
import org.headsupdev.agile.web.MountPoint;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;

import java.io.*;
import java.io.File;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Date;

/**
 * Browse page for viewing files
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@MountPoint( "view" )
public class BrowseFile
    extends HeadsUpPage
{
    private String name = "";

    public Permission getRequiredPermission() {
        return new FileViewPermission();
    }

    public void layout()
    {
        super.layout();

        add( CSSPackageResource.getHeaderContribution( getClass(), "file.css" ) );

        Project project = getProject();
        String path = getPageParameters().getString( "path" );
        if ( project == null )
        {
            notFoundError();
            return;
        }
        if ( path == null )
        {
            userError( "The path parameter is required" );
            return;
        }

        addLink( new BookmarkableMenuLink( BrowseHistory.class, getPageParameters(), "history" ) );

        StringBuilder header = new StringBuilder( "<a href=\"/");
        header.append( project.getId() );
        header.append( "/files/\">" );
        header.append( project.getAlias() );
        header.append( "</a>" );

        String[] components = path.split( ":" );
        StringBuilder partialPath = new StringBuilder();
        for ( int i = 0; i < components.length; i++ ) {
            if ( i > 0 ) {
                partialPath.append( ":" );
                name += "/";
            }
            partialPath.append( components[i] );

            name += components[i];
            header.append( "/" );
            if ( i >= components.length - 1 ) {
                header.append( components[i] );
            } else {
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

                header.append( "<a href=\"/");
                header.append( project.getId() );
                header.append( "/files/path/" );
                header.append( filePath );
                header.append( "/\">" );
                header.append( components[i] );
                header.append( "</a>");
            }
        }

        final String filePath = path.replace( ':', File.separatorChar );
        final File file = new File( getStorage().getWorkingDirectory( project ), filePath );
        if ( !file.exists() )
        {
            notFoundError();
            return;
        }
        if ( file.isDirectory() )
        {
            userError( "The requested file is a directory and cannot be viewed" );
            return;
        }
        add( new Label( "header", header.toString() ).setEscapeModelStrings( false ) );

        BrowseApplication app = (BrowseApplication) getHeadsUpApplication();
        String searchPath = filePath;
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
            WebMarkupContainer details = new WebMarkupContainer( "details" );
            add( details );

            ChangeSet change = Manager.getInstance().getScmService().getChangeSet( root, fileRevision );
            PageParameters params = new PageParameters();
            params.add( "project", project.getId() );
            params.add( "id", fileRevision );
            Link revisionLink = new BookmarkablePageLink( "revision-link", getPageClass( "files/change" ), params );
            revisionLink.add( new Label( "revision-label", fileRevision ) );

            details.add( revisionLink );
            details.add( new WebMarkupContainer( "revision-unknown" ).setVisible( false ) );

            details.add( new Label( "modified", new FormattedDurationModel( change.getDate(), new Date() )
            {
                public String getObject()
                {
                    return super.getObject() + " ago";
                }
            } ) );

            User user = getSecurityManager().getUserByUsernameEmailOrFullname( change.getAuthor() );
            Label authorLabel = new Label( "author-label", change.getAuthor() );
            if ( user != null )
            {
                details.add( new AccountFallbackLink( "author-link", change.getAuthor() ) );
                details.add( new GravatarLinkPanel( "avatar", user, HeadsUpPage.DEFAULT_AVATAR_EDGE_LENGTH ) );
                details.add( new WebMarkupContainer( "author-label" ).setVisible( false ) );
            }
            else
            {
                details.add( new WebMarkupContainer( "author-link" ).setVisible( false ) );
                details.add( new GravatarLinkPanel( "avatar", null, HeadsUpPage.DEFAULT_AVATAR_EDGE_LENGTH ) );
                details.add( authorLabel );
            }

            details.add( new Label( "size", new FormattedSizeModel( file.length() ) ) );
            details.add( new Label( "comment", change.getComment() ) );
        }
        else
        {
            add( new WebMarkupContainer( "details" ).setVisible( false ) );
        }

        add( new EmbeddedFilePanel( "embed", file, getProject() ) );
    }

    @Override
    public String getPageTitle()
    {
        return "File:" + name + PAGE_TITLE_SEPARATOR + super.getPageTitle();
    }
}
