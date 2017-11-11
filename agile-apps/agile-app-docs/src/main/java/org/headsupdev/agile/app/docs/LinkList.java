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

package org.headsupdev.agile.app.docs;

import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.storage.docs.Document;
import org.headsupdev.agile.web.MountPoint;

/**
 * A resource to return the list of files attached to a page - used for the TinyMCE plugin.
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@MountPoint( "linklist" )
public class LinkList
    extends ImageList
{
    @Override
    protected String getArrayName()
    {
        return "tinyMCELinkList";
    }

    @Override
    protected boolean acceptFile( String filename )
    {
        return true;
    }

    @Override
    protected String getFileUrl( String filename, Project project, Document doc )
    {
        // TODO we should probably move this to a non-image caching link
        return super.getFileUrl( filename, project, doc );
    }
}
