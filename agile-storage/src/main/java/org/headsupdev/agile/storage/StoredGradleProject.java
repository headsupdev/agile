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

package org.headsupdev.agile.storage;

import org.headsupdev.agile.api.GradleProject;
import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.api.logging.Logger;
import org.headsupdev.agile.api.rest.Publish;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;

/**
 * Implementation of the Gradle project interface, handles parsing and storage.
 *
 * @author Andrew Williams
 * @since 2.0
 */
@Entity
@DiscriminatorValue( "gradle" )
@Indexed( index = "GradleProjects" )
public class StoredGradleProject
    extends StoredProject
    implements GradleProject
{
    @Field(index = Index.TOKENIZED)
    @Publish
    protected String description, groupId, version;

    public StoredGradleProject()
    {
    }

    public StoredGradleProject( File projectFile )
    {
        this( projectFile, null );
    }

    public StoredGradleProject( File projectFile, String id )
    {
        this.id = id;

        loadFromProjectFile( projectFile );

        if ( id == null )
        {
            this.id = encodeId( name );
        }

    }

    protected void loadFromProjectFile( File projectFile )
    {
        Logger log = Manager.getLogger( StoredGradleProject.class.getName() );
        // here we load build.xml and ivy.xml from the parent directory as our metadata is split between two files...

        File build = new File( projectFile.getParentFile(), "build.gradle" );
        if ( build.exists() )
        {
            try
            {
                BufferedReader reader = new BufferedReader( new FileReader( build ) );
                String line;
                while ( ( line = reader.readLine() ) != null )
                {
                    line = line.trim();

                    if ( line.startsWith( "name" ) )
                    {
                        String value = getStringValueFromLine( line );
                        if ( value != null )
                        {
                            setName( value );
                        }
                    }
                    else if ( line.startsWith( "description" ) )
                    {
                        String value = getStringValueFromLine( line );
                        if ( value != null )
                        {
                            description = value;
                        }
                    }
                    else if ( line.startsWith( "group" ) )
                    {
                        String value = getStringValueFromLine( line );
                        if ( value != null )
                        {
                            groupId = value;
                        }
                    }
                    else if ( line.startsWith( "version" ) )
                    {
                        String value = getStringValueFromLine( line );
                        if ( value != null )
                        {
                            version = value;
                        }
                    }
                }
            }
            catch ( IOException e )
            {
                log.error( "Error parsing gradle project metadata", e );
            }
        }
    }

    protected String getStringValueFromLine( String line )
    {
        int start = line.indexOf( "\"" ) + 1;
        if ( start == 0 )
        {
            return null;
        }
        int end = line.indexOf( "\"", start + 1 );

        if ( end == -1 )
        {
            return line.substring( start );
        }
        else
        {
            return line.substring( start, end );
        }
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription( String description )
    {
        this.description = description;
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion( String version )
    {
        this.version = version;
    }

    public String getGroup()
    {
        return groupId;
    }

    public void setGroup( String group )
    {
        this.groupId = group;
    }

    public void fileModified( String path, File file )
    {
        if ( path.equals( "build.gradle" ) )
        {
            loadFromProjectFile( file );
            setUpdated( new Date() );

            ( (HibernateStorage) Manager.getStorageInstance() ).merge( this );
            Manager.getInstance().fireProjectModified( this );
        }

    }

    public boolean foundMetadata( File directory )
    {
        return ( new File( directory, "build.gradle" ) ).exists();
    }

    public String getTypeName()
    {
        return "Gradle";
    }
}
