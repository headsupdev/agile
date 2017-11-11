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

package org.headsupdev.agile.framework;

import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.web.MountPoint;
import org.apache.wicket.markup.html.WebResource;
import org.apache.wicket.util.resource.FileResourceStream;
import org.apache.wicket.util.resource.IResourceStream;

import java.io.File;

/**
 * Resource class to play embedded media in the files app - used for sounds and videos
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@MountPoint( "embed" )
public class DynamicEmbed
    extends WebResource
{
    @Override
    public IResourceStream getResourceStream() {
        String filePath = getParameters().getString( "path" );

        File file = new File( Manager.getStorageInstance().getDataDirectory(), filePath );

        return new FileResourceStream( new File( file.getAbsolutePath().replace( ':', File.separatorChar ) ) );
    }
}
