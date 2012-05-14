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

package org.headsupdev.agile.app.admin.project;

import org.headsupdev.agile.api.EclipseProject;
import org.headsupdev.agile.storage.StoredEclipseProject;
import org.headsupdev.agile.storage.StoredMavenTwoProject;
import org.headsupdev.agile.storage.StoredProject;

import java.io.File;

/**
 * TODO document me
 *
 * @author Andrew Williams
 * @since 1.0
 */
public class EclipseProjectImporter
        extends ProjectImporter
{
    public EclipseProjectImporter( String id, final AddProject page, final File dir )
    {
        super( id, page, dir );
    }

    private void parseProject()
    {
        File file = new File( getCheckoutDir(), ".project" );
        if ( file.exists() )
        {
            EclipseProject project = new StoredEclipseProject( file );

            setName( project.getName() );
        }
    }

    @Override
    public boolean requiresInput()
    {
        return false;
    }

    @Override
    public String getTypeName()
    {
        return "Eclipse";
    }

    public StoredProject importProjects( File dir, StoredMavenTwoProject parent, String scm, String username,
                                         String password )
    {
        File file = new File( dir, ".project" );
        if ( !file.exists() )
        {
            System.out.println("null");
            return null;
        }

        StoredEclipseProject project = new StoredEclipseProject( file );

        project.setScm( scm );
        project.setScmUsername( username );
        project.setScmPassword( password );

        return project;
    }
}
