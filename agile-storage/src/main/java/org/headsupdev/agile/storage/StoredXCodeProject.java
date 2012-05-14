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

import org.headsupdev.support.java.IOUtil;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.DiscriminatorValue;
import java.io.*;
import java.util.Date;

import org.headsupdev.agile.api.XCodeProject;
import org.headsupdev.agile.api.Manager;

/**
 * TODO Document me!
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
    protected String version;

    @Field(index = Index.TOKENIZED)
    protected String bundleId;

    @Field(index = Index.TOKENIZED)
    protected String platform;

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

        loadFromProjectFile( projectFile );

        if ( id == null )
        {
            this.id = encodeId(name);
        }

    }

    protected String stripComments( String in )
    {
        String out = in;

        int pos = in.indexOf( "/*" );
        if ( pos >= 0 )
        {
            out = in.substring( 0, pos );
        }

        return out.trim();
    }

    private String getProjectFileValue( String line )
    {
        String ret;
        int start = line.indexOf( "=" ) + 1;
        int end = line.indexOf( ";", start );

        if ( end == -1 )
        {
            ret = line.substring( start );
        }
        else
        {
            ret = line.substring( start, end );
        }

        ret = ret.trim();
        if ( ret.length() > 2 )
        {
            if ( ret.charAt( 0 ) == '"' && ret.charAt( ret.length() - 1 ) == '"' )
            {
                ret = ret.substring( 1, ret.length() - 1 );
            }
        }

        return stripComments(ret);
    }

    protected String getFirstTarget( File file )
    {
        BufferedReader in = null;
        try
        {
            in = new BufferedReader( new InputStreamReader( new FileInputStream( file ) ) );

            String line;
            while ( ( line = in.readLine() ) != null && line.indexOf( "targets = (" ) == -1 )
            {
                // ignore these lines
            }

            return stripComments( in.readLine() );
        }
        catch ( IOException e )
        {
            Manager.getLogger( getClass().getName() ).error( "Error parsing xcode metadata", e );
        }
        finally
        {
            if ( in != null )
            {
                IOUtil.close( in );
            }
        }

        return null;
    }

    protected String getFirstBuildConfigurationInList( File file, String buildConfigList )
    {
        BufferedReader in = null;
        try
        {
            in = new BufferedReader( new InputStreamReader( new FileInputStream( file ) ) );

            String line;
            while ( ( line = in.readLine() ) != null && line.indexOf( buildConfigList ) == -1 )
            {
                // ignore these lines
            }

            while ( ( line = in.readLine() ) != null && line.indexOf( buildConfigList ) == -1 )
            {
                // ignore these lines again
            }

            while ( ( line = in.readLine() ) != null && line.indexOf( "buildConfigurations" ) == -1 )
            {
                // ignore these lines
            }

            return stripComments( in.readLine() );
        }
        catch ( IOException e )
        {
            Manager.getLogger( getClass().getName() ).error( "Error parsing xcode metadata", e );
        }
        finally
        {
            if ( in != null )
            {
                IOUtil.close( in );
            }
        }

        return null;
    }

    protected String getBuildConfigurationListForTarget( File file, String target )
    {
        BufferedReader in = null;
        try
        {
            in = new BufferedReader( new InputStreamReader( new FileInputStream( file ) ) );

            String line;
            while ( ( line = in.readLine() ) != null && line.indexOf( target ) == -1 )
            {
                // ignore these lines
            }

            while ( ( line = in.readLine() ) != null && line.indexOf( "buildConfigurationList" ) == -1 )
            {
                // ignore these lines
            }

            return getProjectFileValue( line );
        }
        catch ( IOException e )
        {
            Manager.getLogger( getClass().getName() ).error( "Error parsing xcode metadata", e );
        }
        finally
        {
            if ( in != null )
            {
                IOUtil.close( in );
            }
        }

        return null;
    }

    protected void loadFromProjectFile( File projectFile )
    {
        Manager.getLogger( getClass().getName() ).info( "Parsing XCode metadata from " + projectFile.getPath() );

        String firstTarget = getFirstTarget( projectFile );

        String buildConfigurationList = getBuildConfigurationListForTarget( projectFile, firstTarget );

        String configuration = getFirstBuildConfigurationInList( projectFile, buildConfigurationList );

        BufferedReader in = null;
        try
        {
            in = new BufferedReader( new InputStreamReader( new FileInputStream( projectFile ) ) );

            // skip to buildConfiguration
            String line;
            while ( ( line = in.readLine() ) != null && line.indexOf( configuration ) == -1 )
            {
                // ignore these lines
            }

            String infoFileName = null;
            while ( ( line = in.readLine() ) != null )
            {
                if ( line.indexOf( "INFOPLIST_FILE =" ) != -1 )
                {
                    infoFileName = getProjectFileValue( line );
                }
                else if ( line.indexOf( "PRODUCT_NAME =" ) != -1 )
                {
                    name = getProjectFileValue( line );
                    break; // note that if we parse further blocks a break will not be enough
                }
            }
            IOUtil.close( in );
            in = null;

            File infoFile = null;
            if ( infoFileName != null ) {
                infoFile = new File( projectFile.getParentFile().getParentFile(), infoFileName );
            } else {
                for ( File possible : projectFile.getParentFile().getParentFile().listFiles() )
                {
                    if ( possible.getName().endsWith( "Info.plist" ) )
                    {
                        infoFile = possible;
                        break;
                    }
                }
            }

            loadFromInfoFile( infoFile );
        }
        catch ( IOException e )
        {
            Manager.getLogger( getClass().getName() ).error( "Error parsing xcode metadata", e );
        }
        finally
        {
            if ( in != null )
            {
                IOUtil.close( in );
            }
        }
    }

    protected void loadFromInfoFile( File infoFile )
    {
        BufferedReader in = null;
        try
        {
            platform = XCODE_PLATFORM_MACOSX;
            if ( infoFile != null ) {
                Manager.getLogger( getClass().getName() ).info( "Loading extra XCode metadata from " +
                        infoFile.getPath() );
                boolean foundShortString = false;

                String line;
                in = new BufferedReader( new InputStreamReader( new FileInputStream( infoFile ) ) );
                while ( ( line = in.readLine() ) != null )
                {
                    if ( line.contains( ">CFBundleShortVersionString<") )
                    {
                        foundShortString = true;

                        String versionString = in.readLine();
                        int start = versionString.indexOf( '>' ) + 1;
                        int stop = versionString.indexOf( '<', start );
                        this.version = versionString.substring( start, stop );
                    }
                    else if ( line.contains( ">CFBundleVersion<") )
                    {
                        if ( !foundShortString )
                        {
                            String versionString = in.readLine();
                            int start = versionString.indexOf( '>' ) + 1;
                            int stop = versionString.indexOf( '<', start );
                            this.version = versionString.substring( start, stop );
                        }
                    }
                    else if ( line.contains( ">CFBundleIdentifier<" ) )
                    {
                        String bundleString = in.readLine();
                        int start = bundleString.indexOf( '>' ) + 1;
                        int stop = bundleString.indexOf( '<', start );
                        this.bundleId = bundleString.substring( start, stop );
                    }
                    else if ( line.contains( ">LSRequiresIPhoneOS<" ) )
                    {
                        platform = "iPhone";
                    }
                }
            }
        }
        catch ( IOException e )
        {
            Manager.getLogger( getClass().getName() ).error( "Error parsing project info file", e );
        }
        finally
        {
            if ( in != null )
            {
                IOUtil.close(in);
            }
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
        if ( path.endsWith( "project.pbxproj" ) )
        {
            loadFromProjectFile( file );
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
                        loadFromProjectFile( pbxproj );
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

    public String getTypeName()
    {
        return "XCode";
    }
}
