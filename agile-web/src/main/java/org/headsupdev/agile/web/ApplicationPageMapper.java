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

package org.headsupdev.agile.web;

import org.headsupdev.agile.api.*;
import org.headsupdev.agile.api.logging.Logger;
import org.headsupdev.agile.core.PrivateConfiguration;
import org.headsupdev.agile.security.permission.AdminPermission;
import org.headsupdev.agile.web.wicket.HeadsUpRequestCodingStrategy;

import java.io.File;
import java.util.*;

/**
 * A class that handles the available application -> page mappings and their url paths
 *
 * @author Andrew Williams
 * @since 1.0
 */
public class ApplicationPageMapper
    implements ProjectListener
{
    private List<Application> applications;
    private Map<String,Application> applicationMap;
    private Map<String,Class<? extends Page>> pages;
    private Map<Class<? extends org.apache.wicket.Page>,Application> pageApplications;

    private Application adminApp;
    private Application supportApp;
    private Application searchApp;

    private static ApplicationPageMapper instance = new ApplicationPageMapper();
    private static Logger log;

    public static ApplicationPageMapper get()
    {
        return instance;
    }

    private ApplicationPageMapper()
    {
        applications = new LinkedList<Application>();
        applicationMap = new HashMap<String,Application>();
        pages = new HashMap<String,Class<? extends Page>>();
        pageApplications = new HashMap<Class<? extends org.apache.wicket.Page>,Application>();
        
        Manager.getInstance().addProjectListener( this );
        log = Manager.getLogger( getClass().getSimpleName() );
    }

    public void addApplication( Application app )
    {
        if ( app.getApplicationId().equals( "admin" ) )
        {
            adminApp = app;
        }
        else if ( app.getApplicationId().equals( "support" ) )
        {
            supportApp = app;
        }
        else if ( app.getApplicationId().equals( "search" ) )
        {
            searchApp = app;
        }
        else
        {
            applications.add( app );
            Collections.sort( applications, new ApplicationComparator() );
        }

        applicationMap.put( app.getApplicationId(), app );
        pageApplications.put( app.getHomePage(), app );
        if ( isHomeApp( app ) )
        {
            // don't add the loading page after the dashboard
            if ( pages.get( "" ) == null || !"home".equals( app.getApplicationId() ) )
            {
                pages.put( "", app.getHomePage() );
            }
        }
        else
        {
            pages.put( app.getApplicationId(), app.getHomePage() );
        }

        Class<? extends Page>[] pageList = app.getPages();
        for ( Class<? extends Page> page : pageList )
        {
            pageApplications.put( page, app );

            if ( page.isAnnotationPresent( MountPoint.class ) )
            {
                String id = page.getAnnotation( MountPoint.class ).value();

                if ( isHomeApp( app ) )
                {
                    pages.put( id, page );
                }
                else
                {
                    pages.put( app.getApplicationId() + "/" + id, page );
                }
            }
        }
    }

    public void removeApplication( Application app )
    {
        if ( app.getApplicationId().equals( "admin" ) )
        {
            adminApp = null;
        }
        else if ( app.getApplicationId().equals( "support" ) )
        {
            supportApp = null;
        }
        else if ( app.getApplicationId().equals( "search" ) )
        {
            searchApp = null;
        }
        else
        {
            applications.remove( app );
        }
        applicationMap.remove( app.getApplicationId() );

        pageApplications.remove( app.getHomePage() );
        if ( isHomeApp( app ) )
        {
            pages.remove( "" );
        }
        else
        {
            pages.remove( app.getApplicationId() );
        }

        Class<? extends Page>[] pageList = app.getPages();
        for ( Class<? extends Page> page : pageList )
        {
            pageApplications.remove( page );

            if ( page.isAnnotationPresent( MountPoint.class ) )
            {
                String id = page.getAnnotation( MountPoint.class ).value();

                if ( isHomeApp( app ) )
                {
                    pages.remove( id );
                }
                else
                {
                    pages.remove( app.getApplicationId() + "/" + id );
                }
            }
        }
    }

    public Application getApplication( String path )
    {
        return applicationMap.get( path );
    }

    public List<Application> getApplications()
    {
        return getApplications( null );
    }

    public List<Application> getApplications( User user )
    {
        if ( user == null )
        {
            return applications;
        }

        List<Application> userApplications = new ArrayList<Application>( applications );
        if ( PrivateConfiguration.isInstalled() && // don't ask if we are admin before roles are created
                adminApp != null &&
                Manager.getSecurityInstance().userHasPermission( user, new AdminPermission(), null ) )
        {
            userApplications.add( adminApp );
        }
        return userApplications;
    }

    public List<String> getApplicationIds()
    {
        List<String> apps = new LinkedList<String>( applicationMap.keySet() );

        Collections.sort( apps, new ApplicationIdComparator() );
        return apps;
    }

    public Class<? extends Page> getPageClass( String path )
    {
        if ( path.startsWith( "home/" ) )
        {
            path = path.substring( 5 );
        }
        if ( path.startsWith( "dashboard/" ) )
        {
            path = path.substring( 10 );
        }

        if ( path.endsWith( "/" ) )
        {
            path = path.substring( 0, path.length() - 1 );
        }

        Class<? extends Page> ret = pages.get( path );
        if ( ret == null && ( path.equals( "" ) || path.equals( "home" ) || path.equals( "dashboard" ) ) )
        {
            ret = LoadingPage.class;
        }

        // handle a complete failure to link to the requested page...
        if ( ret == null )
        {
            log.error( "Unable to find page class for path \"" + path + "\"" );

            return getPageClass( "filenotfound" );
        }
        return ret;
    }

    public Application getApplication( Class<? extends org.apache.wicket.Page> page )
    {
        return pageApplications.get( page );
    }

    public Application getAdminApp()
    {
        return adminApp;
    }

    public Application getSupportApp()
    {
        return supportApp;
    }

    public Application getSearchApp()
    {
        return searchApp;
    }

    public Set<String> getMountPaths()
    {
        return pages.keySet();
    }

    public static boolean isHomeApp( Application app )
    {
        if ( app == null ) {
            return false; // the home app is special and should not be polluted by bad apps
        }

        return ( "home".equals( app.getApplicationId() ) || "dashboard".equals( app.getApplicationId( ) ) );
    }

    public void projectAdded( Project project )
    {
        HeadsUpRequestCodingStrategy.updateProjectIds();
    }

    public void projectModified( Project project )
    {
        // ignore, unless we support changing id...
    }

    public void projectFileModified( Project project, String path, File file )
    {
        // ignore, unless we support changing id...
    }

    public void projectRemoved( Project project )
    {
        HeadsUpRequestCodingStrategy.updateProjectIds();
    }
}
