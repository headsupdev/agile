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

package org.headsupdev.agile.api;

import org.headsupdev.agile.api.logging.Logger;
import org.headsupdev.support.java.StringUtil;
import org.headsupdev.support.java.Base64;

import java.util.*;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;

/**
 * Configuration for the front pages for HeadsUp Agile
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class HeadsUpConfiguration
    extends PropertyTree
{
    private static final String KEY_BASE_URL = "baseUrl";
    private static final String KEY_DATA_DIR = "dataDir";
    private static final String KEY_LOG_ERRORS = "log.errors";

    private static final String KEY_SMTP_HOST = "smtp.host";
    private static final String KEY_SMTP_FROM = "smtp.from";
    private static final String KEY_SMTP_USERNAME = "smtp.username";
    private static final String KEY_SMTP_PASSWORD = "smtp.password";

    private static final String KEY_TIMEZONE_ID = "timezone.id";
    private static final String KEY_USERS_USE_NAMES = "userList.useNames";

    private static final String KEY_PROJECTS = "projects";
    private static final String KEY_APPLICATIONS = "applications";

    private static final String BUILD_KEY_VERSION = "build.version";
    private static final String BUILD_KEY_DATE = "build.date";

    private static boolean debug = "true".equals( System.getProperty( "agile.runtime.debug" ) );
    private static boolean verbose = "true".equals( System.getProperty( "agile.runtime.verbose" ) );
    private static boolean color = "true".equals( System.getProperty( "agile.runtime.color" ) );

    private static Properties buildProperties;
    private static Date parsedBuildDate;
    private static TimeZone defaultTimeZone;

    static
    {
        buildProperties = new Properties();
        try
        {
            buildProperties.load( HeadsUpConfiguration.class.getResource( "build.properties" ).openStream() );
        }
        catch ( IOException e )
        {
            // we cannot log yet as we are created before the manager is loaded
            System.err.println( "Unable to load build properties" );
            e.printStackTrace();
        }
    }

    public HeadsUpConfiguration( Map<String, String> properties )
    {
        super( properties );
    }

    public String getProductName()
    {
        return "HeadsUp Agile";
    }

    // must have a trailing slash...
    public String getProductUrl()
    {
        return "http://headsupdev.github.com/agile/";
    }

    public void setProperty( String key, String value )
    {
        super.setProperty( key, value );
        Manager.getStorageInstance().setConfigurationItem( key, value );
    }

    public String removeProperty( String key )
    {
        String ret = super.removeProperty( key );
        Manager.getStorageInstance().removeConfigurationItem( key );
        return ret;
    }

    public String getBaseUrl()
    {
        return getProperty( KEY_BASE_URL, "http://localhost:8069/" );
    }

    public void setBaseUrl( String baseUrl )
    {
        if ( baseUrl.charAt( baseUrl.length() - 1 ) != '/' )
        {
            baseUrl = baseUrl + '/';
        }

        setProperty( KEY_BASE_URL, baseUrl );
    }

    public String getFullUrl( String path )
    {
        String base = getBaseUrl();

        if ( path == null || path.length() == 0 )
        {
            return base;
        }

        if ( path.charAt( 0 ) == '/' )
        {
            return base + path.substring( 1 );
        }

        return base + path;
    }

    public File getDataDir()
    {
        File deflt = new File( System.getProperty( "user.home" ), ".headsupagile" );
        String dir = getProperty( KEY_DATA_DIR, deflt.getPath() );

        return new File( dir );
    }

    public void setDataDir( File dir )
    {
        setProperty( KEY_DATA_DIR, dir.getPath() );
    }

    public File getInstallDir()
    {
        return new File( "." ).getAbsoluteFile();
    }

    public String getSmtpHost()
    {
        return getProperty( KEY_SMTP_HOST, null );
    }

    public void setSmtpHost( String smtpHost )
    {
        setProperty( KEY_SMTP_HOST, smtpHost );
    }

    public String getSmtpFrom()
    {
        return getProperty( KEY_SMTP_FROM, null );
    }

    public void setSmtpFrom( String smtpFrom )
    {
        setProperty( KEY_SMTP_FROM, smtpFrom );
    }

    public String getSmtpUsername()
    {
        return getProperty( KEY_SMTP_USERNAME, null );
    }

    public void setSmtpUsername( String smtpUsername )
    {
        setProperty( KEY_SMTP_USERNAME, smtpUsername );
    }

    public String getSmtpPassword()
    {
        String password = getProperty( KEY_SMTP_PASSWORD, null );
        if ( password == null )
        {
            return null;
        }

        return new String( Base64.decodeBase64( password.getBytes() ) );
    }

    public void setSmtpPassword( String smtpPassword )
    {
        if ( smtpPassword == null )
        {
            setProperty( KEY_SMTP_PASSWORD, null );
        }
        else
        {
            setProperty( KEY_SMTP_PASSWORD, new String( Base64.encodeBase64( smtpPassword.getBytes() ) ) );
        }
    }

    public TimeZone getDefaultTimeZone()
    {
        String timeZoneId = getProperty( KEY_TIMEZONE_ID, null );
        if ( StringUtil.isEmpty( timeZoneId ) )
        {
            if ( defaultTimeZone == null )
            {
                return TimeZone.getDefault();
            }

            return defaultTimeZone;
        }

        return TimeZone.getTimeZone( timeZoneId );
    }

    public void setDefaultTimeZone( TimeZone timeZone )
    {
        defaultTimeZone = timeZone;
    }

    public boolean getUseFullnamesForUsers()
    {
        return Boolean.parseBoolean( getProperty( KEY_USERS_USE_NAMES, "false" ) );
    }

//    public String getMavenHome()
//    {
//        String defaultHome = "";
//        File autoMavenHome = FileUtil.lookupGrandparentInPath( "mvn" );
//        if ( autoMavenHome != null )
//        {
//            defaultHome = autoMavenHome.getAbsolutePath();
//        }
//
//        return getProperty( KEY_MAVEN_HOME, defaultHome );
//    }

//    public void setMavenHome( String mavenHome )
//    {
//        setProperty( KEY_MAVEN_HOME, mavenHome );
//    }

    public boolean getLogErrors()
    {
        return Boolean.parseBoolean( getProperty( KEY_LOG_ERRORS, "true" ) );
    }

    public void setLogErrors( boolean log )
    {
        setProperty( KEY_LOG_ERRORS, String.valueOf( log ) );
    }

    public PropertyTree getApplicationConfiguration( Application app )
    {
        return getApplicationConfiguration( app.getApplicationId() );
    }

    public PropertyTree getApplicationConfiguration( String appId )
    {
        return getSubTree( KEY_APPLICATIONS ).getSubTree( appId );
    }

    private String encodeProjectId( String projectId )
    {
        return projectId.replace( ".", "+" );
    }

    public PropertyTree getApplicationConfigurationForProject( Application app, Project project )
    {
        return getApplicationConfigurationForProject( app.getApplicationId(), project );
    }

    public PropertyTree getApplicationConfigurationForProject( String appId, Project project )
    {
        String pid = encodeProjectId( project.getId() );
        return getApplicationConfiguration( appId ).getSubTree( KEY_PROJECTS ).getSubTree( pid );
    }

    public PropertyTree getProjectConfiguration( Project project )
    {
        String pid = encodeProjectId( project.getId() );
        return getSubTree( KEY_PROJECTS ).getSubTree( pid );
    }

    public String getBuildVersion()
    {
        return buildProperties.getProperty( BUILD_KEY_VERSION );
    }

    public Date getBuildDate()
    {
        if ( parsedBuildDate == null )
        {
            DateFormat formatter = new SimpleDateFormat( "yyyy/MM/dd kk:mm" );
            try
            {
                parsedBuildDate = formatter.parse( buildProperties.getProperty( BUILD_KEY_DATE ) );
            }
            catch ( ParseException e )
            {
                Manager.getLogger( "HeadsUpConfiguration" ).error( "Failed to parse build date", e );
                return new Date();
            }
        }

        return parsedBuildDate;
    }

    public static void setDebug( boolean debug )
    {
        String status = "[DEBUG]";
        if ( !debug )
        {
            status = "[ERROR]";
        }

        Manager.getLogger( "" ).error( "Log level changed to " + status );
        HeadsUpConfiguration.debug = debug;
    }

    public static boolean isDebug()
    {
        return debug;
    }

    public static boolean isVerbose()
    {
        return verbose || debug;
    }

    public static boolean isColorConsole()
    {
        return color;
    }
}
