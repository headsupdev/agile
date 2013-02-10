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

package org.headsupdev.agile.storage;

import org.headsupdev.agile.api.AntProject;
import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.api.logging.Logger;
import org.headsupdev.agile.api.rest.Publish;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.io.*;
import java.util.Date;

/**
 * TODO document me
 *
 * @author Andrew Williams
 * @since 1.0
 */
@Entity
@DiscriminatorValue( "ant" )
@Indexed( index = "AntProjects" )
public class StoredAntProject
    extends StoredProject
    implements AntProject
{
    @Field(index = Index.TOKENIZED)
    @Publish
    protected String organisation, module, version;

    public StoredAntProject()
    {
    }

    public StoredAntProject( File projectFile )
    {
        this( projectFile, null );
    }

    public StoredAntProject( File projectFile, String id )
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
        Logger log = Manager.getLogger( StoredAntProject.class.getName() );
        // here we load build.xml and ivy.xml from the parent directory as our metadata is split between two files...

        SAXBuilder builder = new SAXBuilder();

        File build = new File( projectFile.getParentFile(), "build.xml" );
        if ( build.exists() )
        {
            try
            {
                Document doc = builder.build( build ).getDocument();

                String name = doc.getRootElement().getAttributeValue( "name" );
                if ( name != null && name.trim().length() > 0 )
                {
                    setName( name );
                }
            }
            catch ( JDOMException e )
            {
                log.error( "Error parsing ant project metadata", e );
            }
            catch ( IOException e )
            {
                log.error( "Error parsing ant project metadata", e );
            }
        }

        File ivy = new File( projectFile.getParentFile(), "ivy.xml" );
        if ( ivy.exists() )
        {
            try
            {
                Document doc = builder.build( ivy ).getDocument();
                Element info = doc.getRootElement().getChild( "info" );

                String org = info.getAttributeValue( "organisation" );
                if ( org != null && org.trim().length() > 0 )
                {
                    setOrganisation( org );
                }
                String module = info.getAttributeValue( "module" );
                if ( module != null && module.trim().length() > 0 )
                {
                    setModule( module );
                }
                String version = info.getAttributeValue( "revision" );
                if ( version != null && version.trim().length() > 0 )
                {
                    setVersion( version );
                }
            }
            catch ( JDOMException e )
            {
                log.error( "Error parsing ant ivy metadata", e );
            }
            catch ( IOException e )
            {
                log.error( "Error parsing ant ivy metadata", e );
            }
        }
    }

    public String getOrganisation()
    {
        return organisation;
    }

    public void setOrganisation( String organisation )
    {
        this.organisation = organisation;
    }

    public String getModule()
    {
        return module;
    }

    public void setModule( String module )
    {
        this.module = module;
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion( String version )
    {
        this.version = version;
    }

    public void fileModified( String path, File file )
    {
        if ( path.equals( "build.xml" ) || path.equals( "ivy.xml" ) )
        {
            loadFromProjectFile( file );
            setUpdated( new Date() );

            ( (HibernateStorage) Manager.getStorageInstance() ).merge( this );
            Manager.getInstance().fireProjectModified( this );
        }

    }

    public boolean foundMetadata( File directory )
    {
        return ( new File( directory, "build.xml" ) ).exists();
    }

    public String getTypeName()
    {
        return "Ant";
    }
}
