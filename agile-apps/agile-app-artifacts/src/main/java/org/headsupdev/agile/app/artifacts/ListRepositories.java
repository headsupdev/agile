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

package org.headsupdev.agile.app.artifacts;

import org.headsupdev.agile.api.Permission;
import org.headsupdev.agile.web.HeadsUpPage;
import org.headsupdev.agile.api.mime.Mime;
import org.headsupdev.agile.security.permission.RepositoryReadPermission;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.ResourceReference;

/**
 * Repository browser main page
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class ListRepositories
    extends HeadsUpPage
{
    public void layout() {
        super.layout();
        add( CSSPackageResource.getHeaderContribution( getClass(), "repository.css" ) );

        Mime folder = Mime.get( "folder" );

        BookmarkablePageLink release =
            new BookmarkablePageLink( "release-icon-link", ReleaseRepository.class, getProjectPageParameters() );
        release.add( new Image( "release-icon", new ResourceReference( Mime.class, folder.getIconName() ) ) );
        add( release );
        BookmarkablePageLink snapshot =
            new BookmarkablePageLink( "snapshot-icon-link", SnapshotRepository.class, getProjectPageParameters() );
        snapshot.add( new Image( "snapshot-icon", new ResourceReference( Mime.class, folder.getIconName() ) ) );
        add( snapshot );
        BookmarkablePageLink external =
            new BookmarkablePageLink( "external-icon-link", ExternalRepository.class, getProjectPageParameters() );
        external.add( new Image( "external-icon", new ResourceReference( Mime.class, folder.getIconName() ) ) );
        add( external );
        BookmarkablePageLink projects =
            new BookmarkablePageLink( "projects-icon-link", ProjectsRepository.class, getProjectPageParameters() );
        projects.add( new Image( "projects-icon", new ResourceReference( Mime.class, folder.getIconName() ) ) );
        add( projects );
        BookmarkablePageLink apps =
                new BookmarkablePageLink( "apps-icon-link", AppsRepository.class, getProjectPageParameters() );
        apps.add( new Image( "apps-icon", new ResourceReference( Mime.class, folder.getIconName() ) ) );
        add( apps );

        add( new BookmarkablePageLink( "release-link", ReleaseRepository.class, getProjectPageParameters() ) );
        add( new BookmarkablePageLink( "snapshot-link", SnapshotRepository.class, getProjectPageParameters() ) );
        add( new BookmarkablePageLink( "external-link", ExternalRepository.class, getProjectPageParameters() ) );
        add( new BookmarkablePageLink( "projects-link", ProjectsRepository.class, getProjectPageParameters() ) );
        add( new BookmarkablePageLink( "apps-link", AppsRepository.class, getProjectPageParameters() ) );
    }

    @Override
    public String getTitle()
    {
        return "Browse Repositories";
    }

    public Permission getRequiredPermission()
    {
        return new RepositoryReadPermission();
    }
}