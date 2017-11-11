/*
 * HeadsUp Agile
 * Copyright 2009-2014 Heads Up Development.
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

import org.headsupdev.agile.api.*;
import org.headsupdev.agile.api.rest.Publish;
import org.headsupdev.agile.storage.project.CocoaPodDependency;
import org.headsupdev.agile.storage.project.XCodeProjectParser;
import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.DiscriminatorValue;
import java.io.*;
import java.util.*;

/**
 * An implementation of the XCodeProject model
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@Entity
@DiscriminatorValue( "xcode" )
@Indexed( index = "XCodeProjects" )
public class StoredXCodeProject
    extends StoredProject
    implements XCodeProject
{
    @Field(index = Index.TOKENIZED)
    @Publish
    protected String version;

    @Field(index = Index.TOKENIZED)
    @Publish
    protected String bundleId;

    @Field(index = Index.TOKENIZED)
    @Publish
    protected String platform;

    @Type( type = "text" )
    @Publish
    protected String dependencies;

    public StoredXCodeProject()
    {
    }

    public StoredXCodeProject( File projectFile )
    {
        this( projectFile, null );
    }

    public StoredXCodeProject( File projectFile, String id )
    {
        this.id = id;

        XCodeProjectParser parser = new XCodeProjectParser();
        parser.parseProjectFile( projectFile, this );

        if ( id == null )
        {
            this.id = encodeId(name);
        }

    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion( String version )
    {
        this.version = version;
    }

    public String getBundleId()
    {
        return bundleId;
    }

    public void setBundleId( String bundleId )
    {
        this.bundleId = bundleId;
    }

    public String getPlatform()
    {
        return platform;
    }

    public void setPlatform( String platform )
    {
        this.platform = platform;
    }

    public void fileModified( String path, File file )
    {
        XCodeProjectParser parser = new XCodeProjectParser();

        if ( path.endsWith( "project.pbxproj" ) )
        {
            parser.parseProjectFile( file, this );
            setUpdated( new Date() );

            ( (HibernateStorage) Manager.getStorageInstance() ).merge( this );
            Manager.getInstance().fireProjectModified( this );
        }
        else if ( path.endsWith( "Info.plist" ) && file != null && file.getParentFile() != null )
        {
            // check the parent dir for the xcode project metadata
            for ( File xcodeproj : file.getParentFile().listFiles() )
            {
                if ( xcodeproj.isDirectory() )
                {
                    File pbxproj = new File( xcodeproj, "project.pbxproj" );
                    if ( pbxproj.exists() )
                    {
                        parser.parseProjectFile( pbxproj, this );
                        setUpdated( new Date() );

                        ( (HibernateStorage) Manager.getStorageInstance() ).merge( this );
                        Manager.getInstance().fireProjectModified( this );

                        break;
                    }
                }
            }
        }
    }

    public boolean foundMetadata( File directory )
    {
        File[] files = directory.listFiles();
        if ( files == null || files.length == 0 )
        {
            return false;
        }

        for ( File file : files )
        {
            if ( file.isDirectory() && ( new File( file, "project.pbxproj" ) ).exists() )
            {
                return true;
            }
        }

        return false;
    }

    public List<XCodeDependency> getDependencies()
    {
        List<XCodeDependency> ret = new LinkedList<XCodeDependency>();
        if ( dependencies == null || dependencies.length() == 0 )
        {
            return ret;
        }

        String[] dependencyList = dependencies.split( "," );
        for ( String dependency : dependencyList )
        {
            final String[] values = dependency.split( ":" );
            if ( values.length < 2 )
            {
                ret.add( new CocoaPodDependency( values[0] ) );
            }
            else
            {
                ret.add( new CocoaPodDependency( values[0], values[1] ) );
            }
        }

        return ret;
    }

    public void setDependencies( List<XCodeDependency> depList )
    {
        StringBuilder dependencies = new StringBuilder();
        boolean first = true;

        for ( XCodeDependency dep : depList )
        {
            String depStr = dep.getName();
            if ( !dep.getVersion().equals( XCodeDependency.UNVERSIONED ) )
            {
                depStr += ":" + dep.getVersion();
            }
            if ( !first )
            {
                dependencies.append( ',' );
            }

            dependencies.append( depStr );
            first = false;
        }

        this.dependencies = dependencies.toString();
    }

    public String getTypeName()
    {
        return "XCode";
    }
}

