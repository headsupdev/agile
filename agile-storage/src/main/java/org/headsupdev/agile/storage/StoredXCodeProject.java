/*
 * HeadsUp Agile
 * Copyright 2009-2013 Heads Up Development Ltd.
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
import org.headsupdev.support.java.IOUtil;
import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.DiscriminatorValue;
import java.io.*;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

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

        loadFromProjectFile( projectFile );

        if ( id == null )
        {
            this.id = encodeId(name);
        }

    }

    protected String replaceVariables( String in )
    {
        return in.replace( "$(SRCROOT)/", "" );
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

        return stripComments( replaceVariables( ret ) );
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
            if ( infoFileName != null )
            {
                infoFile = new File( projectFile.getParentFile().getParentFile(), infoFileName );
            }
            else
            {
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

            File podFile = new File( projectFile.getParentFile().getParentFile(), "Podfile" );
            loadFromPodFile( podFile );
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
            if ( infoFile != null && infoFile.exists() )
            {
                Manager.getLogger( getClass().getName() ).info( "Loading extra XCode metadata from " +
                        infoFile.getPath() );
                boolean foundShortString = false;
                platform = XCODE_PLATFORM_MACOSX;

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
                        platform = XCODE_PLATFORM_IOS;
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

    protected void loadFromPodFile( File podFile )
    {
        BufferedReader in = null;
        try
        {
            if ( podFile != null && podFile.exists() )
            {
                Manager.getLogger( getClass().getName() ).info( "Loading CocoaPods metadata from " +
                        podFile.getPath() );

                String line;
                boolean first = true;
                StringBuilder dependencies = new StringBuilder();
                in = new BufferedReader( new InputStreamReader( new FileInputStream( podFile ) ) );
                while ( ( line = in.readLine() ) != null )
                {
                    if ( line.startsWith( "pod ") )
                    {
                        String[] parts = line.substring( 4 ).split( "," );
                        if ( parts.length > 2 )
                        {
                            continue;
                        }

                        CocoaPodDependency dep;
                        if ( parts.length == 1 )
                        {
                            dep = new CocoaPodDependency( trimValue( parts[0] ), "" );
                        }
                        else
                        {
                            dep = new CocoaPodDependency( trimValue( parts[0] ), trimValue( parts[1] ) );
                        }

                        String depStr = dep.getName() + ":" + dep.getVersion();
                        if ( !first )
                        {
                            dependencies.append( ',' );
                        }

                        dependencies.append( depStr );
                        first = false;
                    }
                }
                this.dependencies = dependencies.toString();
            }
        }
        catch ( IOException e )
        {
            Manager.getLogger( getClass().getName() ).error( "Error parsing CocoaPods file", e );
        }
        finally
        {
            if ( in != null )
            {
                IOUtil.close(in);
            }
        }
    }

    private String trimValue( String part )
    {
        if ( part == null )
        {
            return null;
        }

        part = part.trim();
        if ( part.startsWith( "'" ) && part.length() >= 2 )
        {
            return part.substring( 1, part.length() - 1 );
        }
        return part;
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
                ret.add( new CocoaPodDependency( values[0], "" ) );
            }
            else
            {
                ret.add( new CocoaPodDependency( values[0], values[1] ) );
            }
        }

        return ret;
    }

    public String getTypeName()
    {
        return "XCode";
    }
}

class CocoaPodDependency
    implements XCodeDependency, Serializable
{
    private String name, version;

    public CocoaPodDependency( String name, String version )
    {
        this.name = name;
        this.version = version;
    }

    public String getName()
    {
        return name;
    }

    public String getVersion()
    {
        return version;
    }
}
