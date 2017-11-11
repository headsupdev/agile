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

import org.headsupdev.agile.storage.StoredCommandLineProject;
import org.headsupdev.agile.storage.StoredMavenTwoProject;
import org.headsupdev.agile.storage.StoredProject;

import java.io.File;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.PropertyModel;

/**
 * TODO Document me!
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class ProjectImporter
    extends Maven2ProjectImporter
{
    private String name;

    public ProjectImporter( String id, final AddProject page, final File dir )
    {
        super( id, page, dir );
    }

    protected void layoutForm( Form form )
    {
        form.add( new TextField( "name", new PropertyModel( this, "name" ) ).setRequired( true )
                .setEnabled( getName() == null || getName().trim().length() == 0 ) );
    }

    @Override
    public boolean requiresInput()
    {
        return true;
    }

    @Override
    public String getTypeName()
    {
        return "Unknown";
    }

    protected String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public StoredProject importProjects( File dir, StoredMavenTwoProject parent, String scm, String username,
                                         String password ) {
        String id = StoredProject.encodeId( name );

        StoredProject child = createProject( id, name );
        child.setScm( scm );
        child.setScmUsername( username );
        child.setScmPassword( password );

        return child;
    }

    protected StoredProject createProject( String id, String name )
    {
        return new StoredCommandLineProject( id, name );
    }
}
