/*
 * HeadsUp Agile
 * Copyright 2013 Heads Up Development.
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

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.PropertyModel;
import org.headsupdev.agile.api.GradleProject;
import org.headsupdev.agile.storage.StoredGradleProject;
import org.headsupdev.agile.storage.StoredMavenTwoProject;
import org.headsupdev.agile.storage.StoredProject;

import java.io.File;

/**
 * Importing Gradle projects
 *
 * @author Andrew Williams
 * @since 2.0
 */
public class GradleProjectImporter
        extends ProjectImporter
{
    private String group, version;
    private boolean nameProvided, groupProvided, versionProvided, requiresInput;

    public GradleProjectImporter( String id, final AddProject page, final File dir )
    {
        super( id, page, dir );
    }

    private void parseProject()
    {
        File build = new File( getCheckoutDir(), "build.gradle" );
        if ( build.exists() )
        {
            GradleProject project = new StoredGradleProject( build );

            setName( project.getName() );
            group = project.getGroup();
            version = project.getVersion();
        }

        nameProvided = getName() != null && getName().trim().length() > 0;
        groupProvided = group != null && group.trim().length() > 0;
        versionProvided = version != null && version.trim().length() > 0;
        requiresInput = !nameProvided || !groupProvided || !versionProvided;
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

        form.add( new TextField( "group", new PropertyModel( this, "group" ) ).setRequired( false )
                .setEnabled( !groupProvided ) );
        form.add( new TextField( "version", new PropertyModel( this, "version" ) ).setRequired( false )
                .setEnabled( !versionProvided ) );
    }

    @Override
    public String getTypeName()
    {
        return "Gradle";
    }

    public StoredProject importProjects( File dir, StoredMavenTwoProject parent, String scm, String username,
                                         String password )
    {
        File build = new File( dir, "build.gradle" );
        if ( !build.exists() )
        {
            return null;
        }

        StoredGradleProject project = new StoredGradleProject( build );
        project.setId( StoredProject.encodeId( getName() ) );

        project.setName( getName() );
        project.setGroup( group );
        project.setVersion( version );

        project.setScm( scm );
        project.setScmUsername( username );
        project.setScmPassword( password );

        return project;
    }
}
