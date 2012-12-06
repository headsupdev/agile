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

package org.headsupdev.agile.app.files;

import org.headsupdev.agile.api.service.ScmService;
import org.headsupdev.agile.app.files.event.FileChangeSetEvent;
import org.headsupdev.agile.app.files.permission.FileListPermission;
import org.headsupdev.agile.app.files.permission.FileViewPermission;
import org.headsupdev.agile.storage.HibernateStorage;
import org.headsupdev.agile.storage.files.File;
import org.headsupdev.agile.web.WebApplication;
import org.headsupdev.agile.storage.ScmChange;
import org.headsupdev.agile.api.*;
import org.hibernate.Session;
import org.hibernate.Query;
import org.osgi.framework.BundleContext;

import java.util.*;

/**
 * The files application allows users to browse the checked out files.
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class BrowseApplication
    extends WebApplication
{
    public static final String ID = "files";

    static transient BrowseScmUpdater updater = new BrowseScmUpdater();

    List<MenuLink> links;
    List<String> eventTypes;

    public BrowseApplication()
    {
        links = new LinkedList<MenuLink>();

        eventTypes = new LinkedList<String>();
        eventTypes.add( "filechangeset" );

        updater.setApplication( this );
        Manager.getInstance().addProjectListener( updater );
    }

    @Override
    public void start(BundleContext bc) {
        super.start( bc );

        Dictionary props = new Properties();
        bc.registerService( ScmService.class.getName(), new BrowseScmService(), props );
    }

    public static BrowseScmUpdater getUpdater()
    {
        return updater;
    }

    public String getName()
    {
        return "Files";
    }

    public String getApplicationId()
    {
        return ID;
    }

    public String getDescription()
    {
        return "The " + Manager.getStorageInstance().getGlobalConfiguration().getProductName() + " scm browse application";
    }

    public List<MenuLink> getLinks( Project project )
    {
        return links;
    }

    public List<String> getEventTypes() {
        return eventTypes;
    }

    @Override
    public Class<? extends Page>[] getPages() {
        return new Class[] { Browse.class, BrowseChange.class, BrowseFile.class, BrowseHistory.class, Update.class };
    }

    @Override
    public Class<? extends Page> getHomePage() {
        return Browse.class;
    }

    @Override
    public Permission[] getPermissions() {
        return new Permission[] { new FileListPermission(), new FileViewPermission() };
    }

    @Override
    public LinkProvider[] getLinkProviders()
    {
        return new LinkProvider[]{ new ChangeLinkProvider(), new ChangeLogLinkProvider(), new FileLinkProvider() };
    }

    @Override
    public void stop( BundleContext bc )
        throws Exception
    {
        super.stop( bc );

        updater.stop();
    }

    public List<File> getProjectFiles( Project project )
    {
        Session session = ( (HibernateStorage) Manager.getStorageInstance() ).getHibernateSession();
        Query q = session.createQuery( "from File f where name.project = :project" );
        q.setEntity( "project", project );
        List<File> files = q.list();

        return files;
    }

    public Map<String, File> getProjectFileMap( Project project )
    {
        List<File> files = getProjectFiles( project );
        Map<String, File> ret = new HashMap<String, File>();

        for ( File file : files )
        {
            ret.put( file.getName(), file );
        }
        return ret;
    }

    public static boolean getFileExists( Project project, String fileName )
    {
        Session session = ( (HibernateStorage) Manager.getStorageInstance() ).getHibernateSession();

        Query q = session.createQuery(
                "select count(*) from File f where f.name.project = :project and f.name.name = :name" );
        q.setEntity( "project", project );
        q.setString( "name", fileName );

        return ( (Long) q.uniqueResult() ) > 0;
    }

    public static List<ScmChange> getChanges( Project project, String path )
    {
        Session session = ( (HibernateStorage) Manager.getStorageInstance() ).getHibernateSession();

        String prefix = "";
        java.io.File searchDir = Manager.getStorageInstance().getWorkingDirectory( project );
        while ( project.getParent() != null )
        {
            prefix = searchDir.getName() + java.io.File.separatorChar + prefix;
            project = project.getParent();
            searchDir = searchDir.getParentFile();
        }

        Query q = session.createQuery(
            "from ScmChange c where c.set.id.project = :project and name = :path order by c.set.date desc" );
        q.setEntity( "project", project );
        q.setString( "path", prefix + path );

        return q.list();
    }

    public static boolean getChangeSetExists( Project project, String changeId )
    {
        Session session = ( (HibernateStorage) Manager.getStorageInstance() ).getHibernateSession();

        Query q = session.createQuery(
                "select count(*) from ScmChangeSet s where s.id.project = :project and s.revision = :rev" );
        q.setEntity( "project", project );
        q.setString( "rev", changeId );

        return ( (Long) q.uniqueResult() ) > 0;
    }

    public static boolean getChangeExists( Project project, String changeId )
    {
        Session session = ( (HibernateStorage) Manager.getStorageInstance() ).getHibernateSession();

        // TODO should we check name?
        Query q = session.createQuery(
                "select count(*) from ScmChange c where c.set.id.project = :project and c.revision = :rev" );
        q.setEntity( "project", project );
        q.setString( "rev", changeId );

        return ( (Long) q.uniqueResult() ) > 0;
    }

    public Class[] getPersistantClasses() {
        return new Class[] { FileChangeSetEvent.class };
    }
}
