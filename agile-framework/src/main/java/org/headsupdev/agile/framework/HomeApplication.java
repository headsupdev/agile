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

package org.headsupdev.agile.framework;

import org.headsupdev.agile.api.*;
import org.headsupdev.agile.api.rest.Api;
import org.headsupdev.agile.framework.rest.ConfigurationApi;
import org.headsupdev.agile.web.LoadingPage;
import org.headsupdev.agile.web.AbstractApplication;
import org.headsupdev.agile.web.QRCode;
import org.headsupdev.agile.web.SystemEvent;
import org.headsupdev.agile.framework.rest.ProjectApi;
import org.headsupdev.agile.framework.error.*;
import org.headsupdev.agile.framework.webdav.UploadArtifactEvent;
import org.headsupdev.agile.framework.webdav.Artifact;
import org.headsupdev.agile.security.permission.*;
import org.headsupdev.agile.runtime.HeadsUpRuntime;

import java.util.List;
import java.util.LinkedList;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * A placeholder application for the framework pages
 *
 * @author Andrew Williams
 * @since 1.0
 */
public class HomeApplication
    extends AbstractApplication
{
    private static Class<? extends Page>[] pages;
    private static Class<? extends Api>[] apis;
    private List<String> events = new LinkedList<String>();

    private static BundleContext context;

    static
    {
        setupPages();
        setupApis();
    }

    protected static void setupPages()
    {
        Class<? extends Page>[] pages = (Class<? extends Page>[]) new Class[11];
        pages[0] = LoadingPage.class;
        pages[1] = Login.class;
        pages[2] = Logout.class;
        pages[3] = Tasks.class;
        pages[4] = Updates.class;
        pages[5] = Setup.class;
        pages[6] = About.class;
        pages[7] = Error404Page.class;
        pages[8] = ErrorExpiredPage.class;
        pages[9] = ErrorInternalPage.class;
        pages[10] = ManualUpdate.class;

        setPages( pages );
    }

    public static void setPages( Class<? extends Page>[] pages )
    {
        HomeApplication.pages = pages;
    }

    protected static void setupApis()
    {
        Class<? extends Api>[] apis = (Class<? extends Api>[]) new Class[2];
        apis[0] = ProjectApi.class;
        apis[1] = ConfigurationApi.class;

        setApis( apis );
    }

    public static void setApis( Class<? extends Api>[] apis )
    {
        HomeApplication.apis = apis;
    }

    public HomeApplication()
    {
        events.add( "system" );
    }

    public String getName()
    {
        return "Home";
    }

    public String getApplicationId()
    {
        return "home";
    }

    @Override
    public Class[] getResources() {
        return new Class[]{ DynamicEmbed.class, QRCode.class };
    }

    public Class<? extends Page>[] getPages()
    {
        return (Class<? extends Page>[]) pages;
    }

    public Class<? extends Api>[] getApis()
    {
        return (Class<? extends Api>[]) apis;
    }

    public Class<? extends Page> getHomePage()
    {
        return LoadingPage.class;
    }

    @Override
    public Permission[] getPermissions()
    {
        return new Permission[] { new AdminPermission(), new ProjectListPermission(), new RepositoryReadPermission(),
            new RepositoryWritePermission(), new RepositoryReadAppPermission(), new RepositoryWriteAppPermission(),
            new TaskListPermission(), new AccountCreatePermission() };
    }

    public String getDescription()
    {
        return null;
    }

    @Override
    public List<String> getEventTypes()
    {
        return events;
    }

    public List<MenuLink> getLinks( Project project )
    {
        return new LinkedList<MenuLink>();
    }

    public Class[] getPersistantClasses()
    {
        return new Class[] { UploadArtifactEvent.class, SystemEvent.class, Artifact.class };
    }

    public BundleContext getContext()
    {
        return context;
    }

    static void setContext( BundleContext context )
    {
        HomeApplication.context = context;
    }

    public static HeadsUpRuntime getHeadsUpRuntime()
    {
        ServiceReference sr = context.getServiceReference( HeadsUpRuntime.class.getName() );
        return (HeadsUpRuntime) context.getService( sr );
    }
}
