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

import org.headsupdev.agile.api.AntProject;
import org.headsupdev.agile.storage.StoredAntProject;
import org.headsupdev.agile.storage.StoredMavenTwoProject;
import org.headsupdev.agile.storage.StoredProject;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.PropertyModel;

import java.io.File;

/**
 * TODO document me
 *
 * @author Andrew Williams
 * @since 1.0
 */
public class AntProjectImporter
        extends ProjectImporter
{
    private String org, module, revision;
    private boolean nameProvided, orgProvided, moduleProvided, revisionProvided, requiresInput;

    public AntProjectImporter( String id, final AddProject page, final File dir )
    {
        super( id, page, dir );
    }

    private void parseProject()
    {
        File build = new File( getCheckoutDir(), "build.xml" );
        if ( build.exists() )
        {
            AntProject project = new StoredAntProject( build );

            setName( project.getName() );

            File ivy = new File( getCheckoutDir(), "ivy.xml" );
            if ( ivy.exists() )
            {
                org = project.getOrganisation();
                module = project.getModule();
                revision = project.getVersion();
            }
        }

        nameProvided = getName() != null && getName().trim().length() > 0;
        orgProvided = org != null && org.trim().length() > 0;
        moduleProvided = module != null && module.trim().length() > 0;
        revisionProvided = revision != null && revision.trim().length() > 0;
        requiresInput = !nameProvided || !orgProvided || !moduleProvided || !revisionProvided;
    }

    @Override
    public boolean requiresInput()
    {
        return requiresInput;
    }

    @Override
    protected void layoutForm( Form form )
    {
        parseProject();
        super.layoutForm( form );

        form.add( new TextField( "org", new PropertyModel( this, "org" ) ).setRequired( false )
                .setEnabled( !orgProvided ) );
        form.add( new TextField( "module", new PropertyModel( this, "module" ) ).setRequired( false )
                .setEnabled( !moduleProvided ) );
        form.add( new TextField( "revision", new PropertyModel( this, "revision" ) ).setRequired( false )
                .setEnabled( !revisionProvided ) );
    }

    @Override
    public String getTypeName()
    {
        return "Ant";
    }

    public StoredProject importProjects( File dir, StoredMavenTwoProject parent, String scm, String username,
                                         String password )
    {
        File build = new File( dir, "build.xml" );
        if ( !build.exists() )
        {
            return null;
        }

        StoredAntProject project = new StoredAntProject( build );
        project.setId( StoredProject.encodeId( getName() ) );

        project.setName( getName() );
        project.setModule( module );
        project.setOrganisation( org );
        project.setVersion( revision );

        project.setScm( scm );
        project.setScmUsername( username );
        project.setScmPassword( password );

        return project;
    }
}
