/*
 * HeadsUp Agile
 * Copyright 2009-2012 Heads Up Development.
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
import org.headsupdev.agile.web.MountPoint;
import org.headsupdev.agile.security.permission.RepositoryReadPermission;

import java.io.File;

/**
 * Repository browser release browse page
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@MountPoint( "release" )
public class ReleaseRepository
    extends HeadsUpPage
{
    private String label;

    public Permission getRequiredPermission()
    {
        return new RepositoryReadPermission();
    }

    public String getRepositoryName()
    {
        return "Releases";
    }

    public String getRepositoryId()
    {
        return "release";
    }

    public void layout()
    {
        super.layout();

        String path = getPageParameters().getString( "path" );
        if ( path != null ) {
            path = path.replace( ':', File.separatorChar );
        }

        File repo = new File( new File( getStorage().getDataDirectory( ), "repository" ), getRepositoryId() );
        RepositoryBrowsePanel browsePanel = new RepositoryBrowsePanel( "browse", repo, path, getProject(), getClass() );
        add( browsePanel );

        File fullPath = browsePanel.getResolvedPath();
        if ( fullPath.equals( repo ) )
        {
            label = "/";
        }
        else
        {
            label = fullPath.getAbsolutePath().substring( repo.getAbsolutePath().length() );
        }
    }

    @Override
    public String getTitle()
    {
        return getRepositoryName() + "(" + label + ")";
    }
}

