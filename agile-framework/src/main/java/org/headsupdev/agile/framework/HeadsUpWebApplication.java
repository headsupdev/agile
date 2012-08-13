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

package org.headsupdev.agile.framework;

import org.headsupdev.agile.api.HeadsUpConfiguration;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.PageExpiredException;
import org.apache.wicket.*;
import org.apache.wicket.application.IClassResolver;
import org.apache.wicket.request.IRequestCycleProcessor;

import java.util.*;

import org.headsupdev.agile.api.Application;
import org.headsupdev.agile.api.Page;
import org.headsupdev.agile.web.*;
import org.headsupdev.agile.web.wicket.HeadsUpRequestCycleProcessor;
import org.headsupdev.agile.web.wicket.HeadsUpPageRequestTargetUrlCodingStrategy;
import org.headsupdev.agile.web.wicket.HeadsUpResourceRequestTargetUrlCodingStrategy;
import org.headsupdev.agile.framework.error.ErrorExpiredPage;

/**
 * The wicket web application that builds the framework etc
 *
 * @author Andrew Williams
 * @since 1.0
 */
public class HeadsUpWebApplication
    extends WebApplication
{
    private static HeadsUpWebApplication instance;
    private static List<Application> toAdd = new LinkedList<Application>();
    private Class<? extends Page> homePage = LoadingPage.class;

    public static HeadsUpWebApplication get()
    {
        return instance;
    }

    public static void addApplication( Application app )
    {
        if ( instance != null )
        {
            instance.doAddApplication( app );
        }
        else
        {
            toAdd.add( app );
        }
    }

    public static void removeApplication( Application app )
    {
        if ( instance != null )
        {
            instance.doRemoveApplication( app );
        }
        else
        {
            toAdd.remove( app );
        }
    }

    public void init()
    {
        IClassResolver resolver = new HeadsUpClassResolver( getApplicationSettings().getClassResolver() );
        getApplicationSettings().setClassResolver( resolver );
        RenderUtil.setClassResolver( resolver );
        try {
            getSessionSettings().setPageFactory( new HeadsUpPageFactory( getSessionSettings().getPageFactory() ) );
        } catch ( Exception e ) {
            e.printStackTrace();
        }

        getApplicationSettings().setPageExpiredErrorPage( ErrorExpiredPage.class );
        getDebugSettings().setAjaxDebugModeEnabled( false );
        getMarkupSettings().setStripWicketTags( true );

        mount( new HeadsUpPageRequestTargetUrlCodingStrategy( "/hosted", Hosted.class ) );
        instance = this;
        for ( Application app : toAdd )
        {
            doAddApplication( app );
        }
    }

    @Override
    public String getConfigurationType()
    {
        if ( HeadsUpConfiguration.isDebug() )
        {
            return WebApplication.DEVELOPMENT;
        }
        else
        {
            return WebApplication.DEPLOYMENT;
        }
    }

    void doAddApplication( Application app )
    {
        // this line is needed for re-starting apps through the felix console - resource referenced need it
        org.apache.wicket.Application.set( this );
        ApplicationPageMapper.get().addApplication( app );

        if ( ApplicationPageMapper.isHomeApp( app ) )
        {
            if ( homePage == LoadingPage.class || !LoadingPage.class.equals( app.getHomePage() ) )
            {
                homePage = app.getHomePage();
            }
        }
        else
        {
            mount( new HeadsUpPageRequestTargetUrlCodingStrategy( "/" + app.getApplicationId(), app.getHomePage() ) );
        }

        Class<? extends Page>[] pageList = app.getPages();
        for ( Class<? extends Page> page : pageList )
        {
            if ( page.isAnnotationPresent( MountPoint.class ) )
            {
                String id = page.getAnnotation( MountPoint.class ).value();

                if ( ApplicationPageMapper.isHomeApp( app ) )
                {
                    mount( new HeadsUpPageRequestTargetUrlCodingStrategy( "/" + id, page ) );
                }
                else
                {
                    mount( new HeadsUpPageRequestTargetUrlCodingStrategy( "/" + app.getApplicationId() + "/" + id,
                        page ) );
                }
            }
        }

        Class[] resourceList = app.getResources();
        for ( Class resource : resourceList )
        {
            if ( resource.isAnnotationPresent( MountPoint.class ) )
            {
                String id = ( (MountPoint) resource.getAnnotation( MountPoint.class ) ).value();

                try
                {
                    getSharedResources().add( id, (Resource) resource.newInstance() );

                    if ( ApplicationPageMapper.isHomeApp( app ) )
                    {
                        mount( new HeadsUpResourceRequestTargetUrlCodingStrategy( "/" + id,
                            new ResourceReference( id ).getSharedResourceKey() ) );
                    }
                    else
                    {
                        mount( new HeadsUpResourceRequestTargetUrlCodingStrategy( "/" + app.getApplicationId() + "/" + id,
                            new ResourceReference( id ).getSharedResourceKey() ) );
                    }
                }
                catch ( Exception e )
                {
                    e.printStackTrace();
                }
            }
        }
    }

    void doRemoveApplication( Application app )
    {
        Class<? extends Page>[] pageList = app.getPages();
        for ( Class<? extends Page> page : pageList )
        {
            if ( page.isAnnotationPresent( MountPoint.class ) )
            {
                String id = page.getAnnotation( MountPoint.class ).value();

                if ( ApplicationPageMapper.isHomeApp( app ) )
                {
                    unmount( "/" + id );
                }
                else
                {
                    unmount( "/" + app.getApplicationId() + "/" + id );
                }
            }
        }

        if ( ApplicationPageMapper.isHomeApp( app ) )
        {
            homePage = LoadingPage.class;
        }
        else
        {
            unmount( "/" + app.getApplicationId() );
        }

        Class[] resourceList = app.getResources();
        for ( Class resource : resourceList )
        {
            if ( resource.isAnnotationPresent( MountPoint.class ) )
            {
                String id = ( (MountPoint) resource.getAnnotation( MountPoint.class ) ).value();

                try
                {
                    getSharedResources().remove( id );
                    if ( ApplicationPageMapper.isHomeApp( app ) )
                    {
                        unmount( "/" + id );
                    }
                    else
                    {
                        unmount( "/" + app.getApplicationId() + "/" + id );
                    }
                }
                catch ( Exception e )
                {
                    e.printStackTrace();
                }
            }
        }

        ApplicationPageMapper.get().removeApplication( app );
    }

    public Session newSession( Request request, Response response )
    {
        return new HeadsUpSession( request );
    }

    public RequestCycle newRequestCycle( Request request, Response response )
    {
        return new HibernateRequestCycle( this, request, response )
        {
            @Override
            public final org.apache.wicket.Page onRuntimeException( final org.apache.wicket.Page cause,
                                                                    final RuntimeException e )
            {
                Class<? extends Page> errorClass;
                if ( e instanceof PageExpiredException)
                {
                    errorClass = ApplicationPageMapper.get().getPageClass( "expired" );
                }
                else
                {
                    errorClass = ApplicationPageMapper.get().getPageClass( "error" );
                }

                if ( errorClass == null ) {
                    // TODO replace this with some loading screen
                    return super.onRuntimeException( cause, e );
                }

                ErrorPage errorPage = (ErrorPage) getSessionSettings().getPageFactory().newPage( errorClass );
                errorPage.setError( e );
                return errorPage;
            }
        };
    }

    protected IRequestCycleProcessor newRequestCycleProcessor() {
        return new HeadsUpRequestCycleProcessor();
    }

    public Class getHomePage()
    {
        return homePage;
    }
}
