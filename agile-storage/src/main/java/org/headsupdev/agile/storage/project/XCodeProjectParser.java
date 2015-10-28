/*
 * HeadsUp Agile
 * Copyright 2013-2014 Heads Up Development Ltd.
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

package org.headsupdev.agile.storage.project;

import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.api.XCodeDependency;
import org.headsupdev.agile.api.XCodeProject;
import org.headsupdev.agile.api.logging.Logger;
import org.headsupdev.agile.storage.StoredXCodeProject;

import org.headsupdev.support.java.IOUtil;

import java.io.*;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The main parsing code for .xcproj bundles
 * <p/>
 * Created: 07/01/2014
 *
 * @author Andrew Williams
 * @since 2.0
 */
public class XCodeProjectParser
{
    private static final String COMMENT_START = "/*";
    private static final String COMMENT_END = "*/";

    private Logger log = Manager.getLogger( getClass().getName() );
    private Map<String, String> variables = new HashMap<String, String>();

    public void parseProjectFile( File projectFile, StoredXCodeProject project )
    {
        log.info( "Parsing XCode metadata from " + projectFile.getPath() );

        String firstTarget = getFirstTarget( projectFile );
        String buildConfigurationList = getBuildConfigurationListForTarget( projectFile, firstTarget, variables );
        String configuration = getFirstBuildConfigurationInList( projectFile, buildConfigurationList );

        BufferedReader in = null;
        try
        {
            in = new BufferedReader( new InputStreamReader( new FileInputStream( projectFile ), "UTF-8" ) );

            // skip to buildConfiguration
            String line;
            while ( ( line = in.readLine() ) != null && !line.contains( configuration ) )
            {
                if ( line.contains( "targets =" ) )
                {
                    String next = in.readLine();
                    String targetName = getCommentContent( next );
                    variables.put( "TARGET_NAME", targetName );
                }
            }

            String infoFileName = null;
            while ( ( line = in.readLine() ) != null )
            {
                if ( line.contains( "INFOPLIST_FILE =" ) )
                {
                    infoFileName = getProjectFileValue( line, variables );
                }
                else if ( line.contains( "PRODUCT_NAME =" ) )
                {
                    String name = getProjectFileValue( line, variables );
                    variables.put( "PRODUCT_NAME", replaceVariables( name ) );
                    project.setName( name );
                }
                else if ( line.contains( "PRODUCT_BUNDLE_IDENTIFIER =" ) )
                {
                    String name = getProjectFileValue( line, variables );
                    variables.put( "PRODUCT_BUNDLE_IDENTIFIER", replaceVariables( name ) );
                    project.setBundleId( name );
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

            loadFromInfoFile( infoFile, project );

            File podFile = new File( projectFile.getParentFile().getParentFile(), "Podfile" );
            loadFromPodFile( podFile, project );
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

    protected String replaceVariables( String in )
    {
        String ret = in.replace( "$(SRCROOT)/", "" );

        for ( String key : variables.keySet() )
        {
            ret = ret.replace( "$(" + key + ")", variables.get( key ) );
            ret = ret.replace( "${" + key + "}", variables.get( key ) );

            ret = ret.replace( "$(" + key + ":rfc1034identifier)", getRFC1034( variables.get( key ) ) );
            ret = ret.replace( "${" + key + ":rfc1034identifier}", getRFC1034( variables.get( key ) ) );
        }

        return ret;
    }

    public String getRFC1034( String in )
    {
        return in.replaceAll( "[^A-Za-z0-9]", "_" );
    }

    protected String stripComments( String in )
    {
        if ( in == null )
        {
            return null;
        }
        String out = in;

        int pos = in.indexOf( COMMENT_START );
        if ( pos >= 0 )
        {
            out = in.substring( 0, pos );
        }

        return out.trim();
    }

    private String getCommentContent( String next )
    {
        int start = next.indexOf( COMMENT_START );
        if ( start == -1 )
        {
            return null;
        }

        int end = next.indexOf( COMMENT_END, start );
        return next.substring( start + 2, end ).trim();
    }

    private String getProjectFileValue( String line, Map<String, String> variables )
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

        return stripComments(replaceVariables(ret));
    }

    protected String getFirstTarget( File file )
    {
        BufferedReader in = null;
        try
        {
            in = new BufferedReader( new InputStreamReader( new FileInputStream( file ) ) );

            String line;
            while ( ( line = in.readLine() ) != null && !line.contains( "targets = (" ) )
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
            while ( ( line = in.readLine() ) != null && !line.contains( buildConfigList ) )
            {
                // ignore these lines
            }

            while ( ( line = in.readLine() ) != null && !line.contains( buildConfigList ) )
            {
                // ignore these lines again
            }

            while ( ( line = in.readLine() ) != null && !line.contains( "buildConfigurations" ) )
            {
                // ignore these lines
            }

            return stripComments(in.readLine());
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

    protected String getBuildConfigurationListForTarget( File file, String target, Map<String, String> variables )
    {
        BufferedReader in = null;
        try
        {
            in = new BufferedReader( new InputStreamReader( new FileInputStream( file ) ) );

            String line;
            while ( ( line = in.readLine() ) != null && !line.contains( target ) )
            {
                // ignore these lines
            }

            while ( ( line = in.readLine() ) != null && !line.contains( "buildConfigurationList" ) )
            {
                // ignore these lines
            }

            return getProjectFileValue( line, variables );
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

    protected void loadFromInfoFile( File infoFile, StoredXCodeProject project )
    {
        BufferedReader in = null;
        try
        {
            if ( infoFile != null && infoFile.exists() )
            {
                Manager.getLogger( getClass().getName() ).info( "Loading extra XCode metadata from " +
                        infoFile.getPath() );
                boolean foundShortString = false;
                project.setPlatform( XCodeProject.XCODE_PLATFORM_MACOSX );

                String line;
                in = new BufferedReader( new InputStreamReader( new FileInputStream( infoFile ), "UTF-8" ) );
                while ( ( line = in.readLine() ) != null )
                {
                    if ( line.contains( ">CFBundleShortVersionString<") )
                    {
                        foundShortString = true;

                        String versionString = in.readLine();
                        int start = versionString.indexOf( '>' ) + 1;
                        int stop = versionString.indexOf( '<', start );
                        project.setVersion( replaceVariables( versionString.substring( start, stop ) ) );
                    }
                    else if ( line.contains( ">CFBundleVersion<") )
                    {
                        if ( !foundShortString )
                        {
                            String versionString = in.readLine();
                            int start = versionString.indexOf( '>' ) + 1;
                            int stop = versionString.indexOf( '<', start );
                            project.setVersion( replaceVariables( versionString.substring( start, stop ) ) );
                        }
                    }
                    else if ( line.contains( ">CFBundleIdentifier<" ) )
                    {
                        String bundleString = in.readLine();
                        int start = bundleString.indexOf( '>' ) + 1;
                        int stop = bundleString.indexOf( '<', start );
                        project.setBundleId(replaceVariables(bundleString.substring(start, stop)));
                    }
                    else if ( line.contains( ">LSRequiresIPhoneOS<" ) )
                    {
                        project.setPlatform( XCodeProject.XCODE_PLATFORM_IOS );
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

    protected void loadFromPodFile( File podFile, StoredXCodeProject project )
    {
        BufferedReader in = null;
        try
        {
            if ( podFile != null && podFile.exists() )
            {
                Manager.getLogger( getClass().getName() ).info( "Loading CocoaPods metadata from " +
                        podFile.getPath() );

                String line;
                List<XCodeDependency> dependencies = new LinkedList<XCodeDependency>();
                in = new BufferedReader( new InputStreamReader( new FileInputStream( podFile ), "UTF-8" ) );
                while ( ( line = in.readLine() ) != null )
                {
                    if ( line.startsWith( "pod ") )
                    {
                        String[] parts = line.substring( 4 ).split( "," );
                        if ( parts.length > 2 )
                        {
                            continue;
                        }

                        if ( parts.length == 1 )
                        {
                            dependencies.add( new CocoaPodDependency( trimValue( parts[0] ), "" ) );
                        }
                        else
                        {
                            dependencies.add( new CocoaPodDependency( trimValue( parts[0] ), trimValue( parts[1] ) ) );
                        }
                    }
                }
                project.setDependencies( dependencies );
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
}
