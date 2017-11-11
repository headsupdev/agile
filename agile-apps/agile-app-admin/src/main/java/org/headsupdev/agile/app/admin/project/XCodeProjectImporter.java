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

package org.headsupdev.agile.app.admin.project;

import java.io.File;

import org.headsupdev.agile.storage.StoredMavenTwoProject;
import org.headsupdev.agile.storage.StoredProject;
import org.headsupdev.agile.storage.StoredXCodeProject;

/**
 * TODO Document me!
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class XCodeProjectImporter
    extends Maven2ProjectImporter
{
    public XCodeProjectImporter( String id, final AddProject page, final File dir ) {
        super( id, page, dir );
    }

    @Override
    public String getTypeName()
    {
        return "XCode";
    }

    public StoredProject importProjects( File dir, StoredMavenTwoProject parent, String scm, String username,
                                         String password )
    {
        File projectDir = null;
        for ( File child : dir.listFiles() )
        {
            if ( !child.isDirectory() )
            {
                continue;
            }

            if ( child.getName().toLowerCase().endsWith( ".xcodeproj" ) )
            {
                projectDir = child;
                break;
            }
        }
        if ( projectDir == null )
        {
            return null;
        }

        File projectFile = new File( projectDir, "project.pbxproj" );
        if ( !projectFile.exists() )
        {
            return null;
        }

        StoredProject project = new StoredXCodeProject( projectFile );
        project.setScm( scm );
        project.setScmUsername( username );
        project.setScmPassword( password );

        return project;
    }
}
