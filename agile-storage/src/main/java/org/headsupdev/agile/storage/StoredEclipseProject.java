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

import org.headsupdev.agile.api.EclipseProject;
import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.api.logging.Logger;
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
import java.util.List;

/**
 * TODO document me
 *
 * @author Andrew Williams
 * @since 1.0
 */
@Entity
@DiscriminatorValue( "eclipse" )
@Indexed( index = "EclipseProjects" )
public class StoredEclipseProject
    extends StoredProject
    implements EclipseProject
{
    @Field(index = Index.TOKENIZED)
    protected String nature;

    public StoredEclipseProject()
    {
    }

    public StoredEclipseProject( File projectFile )
    {
        this( projectFile, null );
    }

    public StoredEclipseProject( File projectFile, String id )
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
        Logger log = Manager.getLogger( StoredEclipseProject.class.getName() );

        SAXBuilder builder = new SAXBuilder();
        if ( projectFile.exists() )
        {
            try
            {
                Document doc = builder.build( projectFile ).getDocument();

                String name = doc.getRootElement().getChildText( "name" );
                if ( name != null && name.trim().length() > 0 )
                {
                    setName( name );
                }

                Element natures = doc.getRootElement().getChild( "natures" );
                if ( natures != null )
                {
                    List natureList = natures.getChildren( "nature" );
                    if ( natureList.size() > 0 )
                    {
                        String nature = ( (Element) natureList.get( 0 ) ).getValue();

                        if ( nature != null && nature.trim().length() > 0 )
                        {
                            setNature( nature );
                        }
                    }
                }
            }
            catch ( JDOMException e )
            {
                log.error( "Error parsing eclipse project metadata", e );
            }
            catch ( IOException e )
            {
                log.error( "Error parsing eclipse project metadata", e );
            }
        }
    }

    public String getNature()
    {
        return nature;
    }

    public void setNature( String nature )
    {
        this.nature = nature;
    }

    public void fileModified( String path, File file )
    {
        if ( path.equals( ".project" ) )
        {
            loadFromProjectFile( file );
            setUpdated( new Date() );

            ( (HibernateStorage) Manager.getStorageInstance() ).merge( this );
            Manager.getInstance().fireProjectModified( this );
        }

    }

    public boolean foundMetadata( File directory )
    {
        return ( new File( directory, ".project" ) ).exists();
    }

    public String getTypeName()
    {
        if ( nature != null && nature.trim().length() > 0 )
        {
            return "Eclipse (" + getNatureTypeName() + ")";
        }

        return "Eclipse";
    }

    private String getNatureTypeName()
    {
        if ( nature == null || nature.trim().length() == 0 )
        {
            return null;
        }

        String ret = nature;
        int lastDot = ret.lastIndexOf( '.' );
        if ( lastDot > -1 )
        {
            ret = ret.substring( lastDot + 1 );
        }

        if ( ret.endsWith( "nature" ) )
        {
            ret = ret.substring( 0, ret.length() - 6 );
        }

        return ret.substring( 0, 1 ).toUpperCase() + ret.substring( 1 );
    }
}
