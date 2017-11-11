/*
 * HeadsUp Agile
 * Copyright 2009-2012 Heads Up Development.
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

package org.headsupdev.agile.app.admin;

import org.headsupdev.agile.app.admin.configuration.*;
import org.headsupdev.agile.app.admin.event.AccountAddEvent;
import org.headsupdev.agile.app.admin.event.ProjectAddEvent;
import org.headsupdev.agile.app.admin.event.UpdateProjectEvent;
import org.headsupdev.agile.app.admin.project.AddProject;
import org.headsupdev.agile.storage.HibernateStorage;
import org.headsupdev.agile.web.WebApplication;
import org.headsupdev.agile.api.*;
import org.headsupdev.agile.security.DefaultSecurityManager;
import org.headsupdev.agile.runtime.HeadsUpRuntime;

import java.util.List;
import java.util.LinkedList;
import java.io.File;

import org.headsupdev.agile.web.wicket.HeadsUpRequestCodingStrategy;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * The admin application for HeadsUp manages the configuration and administration
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class AdminApplication
    extends WebApplication
    implements ProjectListener
{
    public static final String ID = "admin";

    private static BundleContext context;

    protected List<MenuLink> links;
    private List<String> eventTypes;

    public AdminApplication()
    {
        links = new LinkedList<MenuLink>();

        links.add( new SimpleMenuLink( "add-project" ) );
        links.add( new SimpleMenuLink( "add-account" ) );
        links.add( new SimpleMenuLink( "permissions" ) );
        links.add( new SimpleMenuLink( "membership" ) );
        links.add( new SimpleMenuLink( "configuration" ) );
        links.add( new SimpleMenuLink( "stats" ) );

        // include system events here
        eventTypes = new LinkedList<String>();
        eventTypes.add( "projectadd" );
        eventTypes.add( "accountadd" );
        eventTypes.add( "updateproject" );
//        eventTypes.add( "system" );

        Manager.getInstance().addProjectListener( this );
    }

    @Override
    public void start( BundleContext bc )
    {
        super.start( bc );

        AdminApplication.context = bc;
    }

    public HeadsUpRuntime getHeadsUpRuntime()
    {
        ServiceReference sr = context.getServiceReference( HeadsUpRuntime.class.getName() );
        return (HeadsUpRuntime) context.getService( sr );
    }

    public String getName()
    {
        return "Admin";
    }

    public String getApplicationId()
    {
        return ID;
    }

    public String getDescription()
    {
        return "The " + Manager.getStorageInstance().getGlobalConfiguration().getProductName() + " admin application";
    }

    public List<MenuLink> getLinks( Project project )
    {
        if ( Manager.getStorageInstance().getGlobalConfiguration().getLogErrors() )
        {
            List<MenuLink> ret = new LinkedList<MenuLink>( links );
            ret.add( new SimpleMenuLink( "errors" ) );

            return ret;
        }

        return links;
    }

    public List<String> getEventTypes() {
        return eventTypes;
    }

    public Class[] getPersistantClasses() {
        return new Class[] { ProjectAddEvent.class, UpdateProjectEvent.class, AccountAddEvent.class };
    }

    @Override
    public Class<? extends Page>[] getPages() {
        return new Class[]{ AddAccount.class, Admin.class, Errors.class, Permissions.class, Membership.class,
            Statistics.class, AddProject.class, AddRole.class, ApplicationsConfiguration.class,
            ProjectConfiguration.class, NotifiersConfiguration.class, SystemConfiguration.class,
            UpdatesConfiguration.class, Export.class };
    }

    @Override
    public Class<? extends Page> getHomePage() {
        return Admin.class;
    }

    public SystemConfigurationSource getConfigurationSource()
    {
        return SystemConfigurationSource.getInstance();
    }

    public void addProject( Project project, org.apache.wicket.Page page )
    {
        Manager.getStorageInstance().addProject( project );
        addProjectPermission( project );

        HeadsUpRequestCodingStrategy.addProject( project );
    }

    private void addProjectPermission( Project project )
    {
        List<User> users = Manager.getSecurityInstance().getUsers();
        HibernateStorage storage = (HibernateStorage) Manager.getStorageInstance();
        project.getUsers().addAll( users );

        for ( User user : users )
        {
            user.getProjects().add( project );
            project.getUsers().add( user );

            storage.update( user );
        }
        storage.update( project );

        // recurse
        if ( project.getChildProjects() != null )
        {
            for ( Project child : project.getChildProjects() )
            {
                addProjectPermission( child );
            }
        }
    }

    public void addUser( User user )
    {
        ( (DefaultSecurityManager) Manager.getSecurityInstance() ).addUser( user );
        addEvent( new AccountAddEvent( user ) );
    }

    public void projectAdded( Project project )
    {
        addEvent( new ProjectAddEvent( project ) );
    }

    public void projectModified( Project project )
    {
        addEvent( new UpdateProjectEvent( project ) );
    }

    public void projectFileModified( Project project, String path, File file )
    {
        project.fileModified( path, file );
    }

    public void projectRemoved( Project project ) {
    }
}
