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

package org.headsupdev.agile.app.files;

import org.headsupdev.agile.app.files.permission.FileListPermission;
import org.headsupdev.agile.web.components.StripedListView;
import org.headsupdev.agile.web.components.FormattedDurationModel;
import org.headsupdev.agile.web.HeadsUpPage;
import org.headsupdev.agile.web.BookmarkableMenuLink;
import org.headsupdev.agile.web.MountPoint;
import org.headsupdev.agile.api.Permission;
import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.api.User;
import org.headsupdev.agile.storage.ScmChange;
import org.headsupdev.agile.storage.TransactionalScmChangeSet;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.PageParameters;

import java.util.Date;
import java.io.File;

/**
 * TODO document me
 *
 * @author Andrew Williams
 * @since 1.0
 */
@MountPoint( "history" )
public class BrowseHistory
    extends HeadsUpPage
{
    private String name;

    public Permission getRequiredPermission() {
        return new FileListPermission();
    }

    @Override
    public void layout()
    {
        super.layout();

        add( CSSPackageResource.getHeaderContribution( getClass(), "browse.css" ) );

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

        addLink( new BookmarkableMenuLink( BrowseFile.class, getPageParameters(), "view" ) );

        name = new File( path.replace( ':', File.separatorChar ) ).getName();
        add( new StripedListView<ScmChange>( "history-items", BrowseApplication.getChanges( project, path.replace( ':', File.separatorChar ) ) )
        {
            protected void populateItem( ListItem<ScmChange> listItem )
            {
                super.populateItem( listItem );

                ScmChange change = listItem.getModelObject();

                String revision = change.getRevision();
                if ( change.getSet() instanceof TransactionalScmChangeSet)
                {
                    revision = ( (TransactionalScmChangeSet) change.getSet() ).getRevision();
                }
                String author = change.getSet().getAuthor();
                String comment = change.getSet().getComment();
                Date modified = change.getSet().getDate();

                // TODO link this just to the 1 file's changelog
                PageParameters params = getProjectPageParameters();
                params.add( "id", revision);
                Link revisionLink = new BookmarkablePageLink( "revision-link", getPageClass( "files/change" ), params );
                revisionLink.add( new Label( "revision-label", revision ) );
                listItem.add( revisionLink );

                Link authorLink;
                User user = getSecurityManager().getUserByUsername( author );
                if ( user != null )
                {
                    params = new PageParameters();
                    params.add( "username", author );
                    authorLink = new BookmarkablePageLink( "author-link", getPageClass( "account" ), params );
                }
                else
                {
                    authorLink = new Link( "author-link" )
                    {
                        public void onClick()
                        {
                        }
                    };
                }
                authorLink.add( new Label( "author-label", author ) );
                listItem.add( authorLink );

                listItem.add( new Label( "comment", comment ) );
                listItem.add( new Label( "modified", new FormattedDurationModel( modified, new Date() )
                {
                    public String getObject()
                    {
                        return super.getObject() + " ago";
                    }
                } ) );

                params = getProjectPageParameters();
                params.add( "id", revision);
                Link filesLink = new BookmarkablePageLink( "files-link", getPageClass( "files/change" ), params );
                filesLink.add( new Label( "files-label", String.valueOf( change.getSet().getChanges().size() ) ) );
                listItem.add( filesLink );
            }
        });
    }

    @Override
    public String getTitle()
    {
        return "Changes for " + name;
    }
}
