/*
 * HeadsUp Agile
 * Copyright 2009-2013 Heads Up Development.
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
import org.apache.wicket.PageParameters;
import org.headsupdev.agile.app.files.permission.FileViewPermission;
import org.headsupdev.agile.web.HeadsUpPage;
import org.headsupdev.agile.web.HibernateRequestCycle;
import org.headsupdev.agile.web.BookmarkableMenuLink;
import org.headsupdev.agile.web.MountPoint;
import org.headsupdev.agile.api.Permission;
import org.headsupdev.agile.api.Project;
import org.apache.wicket.markup.html.CSSPackageResource;

import java.io.File;

/**
 * Browse page for viewing changesets
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@MountPoint( "change" )
public class BrowseChange
    extends HeadsUpPage
{
    private String revision;

    public Permission getRequiredPermission()
    {
        return new FileViewPermission();
    }

    public void layout()
    {
        super.layout();

        add( CSSPackageResource.getHeaderContribution( getClass(), "change.css" ) );

        Project project = getProject();
        String id = getPageParameters().getString( "id" );

        if ( project == null )
        {
            notFoundError();
            return;
        }

        String prefix = "";
        Project root = getProject();
        File searchDir = getStorage().getWorkingDirectory( getProject() );
        while ( root.getParent() != null )
        {
            prefix = searchDir.getName() + File.separatorChar + prefix;
            root = root.getParent();
            searchDir = searchDir.getParentFile();
        }
        final String stripPrefix = prefix;

        ChangeSet changeSet = Manager.getInstance().getScmService().getChangeSet( root, id );
        if ( changeSet == null )
        {
            notFoundError();
            return;
        }

        revision = changeSet.getId();
        ( (HibernateRequestCycle) getRequestCycle() ).getHibernateSession().refresh( changeSet );

        if ( changeSet.getPrevious() != null )
        {
            PageParameters params = getProjectPageParameters();
            params.add( "id", changeSet.getPrevious().getId() );

            addLink( new BookmarkableMenuLink( getClass(), params, "\u25c0 previous changeset" ) );
        }
        if ( changeSet.getNext() != null )
        {
            PageParameters params = getProjectPageParameters();
            params.add( "id", changeSet.getNext().getId() );

            addLink( new BookmarkableMenuLink( getClass(), params, "\u25ba next changeset" ) );
        }

        add( new ChangeSetPanel( "changeset", changeSet, stripPrefix ) );
    }

    @Override
    public String getPageTitle()
    {
        return "Changeset:" + revision + PAGE_TITLE_SEPARATOR + super.getPageTitle();
    }
}

