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

import org.headsupdev.agile.api.logging.Logger;
import org.headsupdev.support.java.StringUtil;
import org.hibernate.Criteria;
import org.hibernate.Transaction;
import org.hibernate.Session;
import org.hibernate.Query;

import java.util.*;
import java.io.*;
import java.nio.channels.FileChannel;

import org.headsupdev.agile.api.*;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

/**
 * Backing the data to a real database using hibernate.
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class HibernateStorage
    implements Storage, Serializable
{
    private static ThreadLocal<org.hibernate.classic.Session> threadSession = new ThreadLocal<org.hibernate.classic.Session>();
    private static ThreadLocal<Object> currentScope = new ThreadLocal<Object>();
    private static Map<Object, org.hibernate.classic.Session> sessions = new HashMap<Object, org.hibernate.classic.Session>();

    private static HeadsUpConfiguration globalConfig;

    private static final long RECENT_PROJECTS_OFFSET = 1000l * 60 * 60 * 24 * 6;
    private static final long ACTIVE_PROJECTS_OFFSET = 1000l * 60 * 60 * 24 * 30;
    private static Set<String> activeProjectIds;
    private static Map<String, Date> latestProjectActivity;
    private static Map<String, Set<String>> recentProjectIds;
    private static Map<String, Map<String, Date>> latestUserProjectActivity;

    private static boolean loaded = false;
    protected void load()
    {
        if ( loaded )
        {
            return;
        }
        loaded = true;

        activeProjectIds = new HashSet<String>();
        latestProjectActivity = new HashMap<String, Date>();
        List<Event> activeEvents = getEvents( new Date( System.currentTimeMillis() - ACTIVE_PROJECTS_OFFSET ),
                new Date() );
        for ( Event event : activeEvents )
        {
            addActiveProjectForEvent( event );
        }

        recentProjectIds = new HashMap<String, Set<String>>();
        latestUserProjectActivity = new HashMap<String, Map<String, Date>>();
        List<Event> recentEvents = getEvents( new Date( System.currentTimeMillis() - RECENT_PROJECTS_OFFSET ) );
        for ( Event event : recentEvents )
        {
            addRecentProjectForEvent( event );
        }

        scheduleRemoveInactiveProjects();
    }

    public HibernateStorage()
    {
        load();
    }

    protected static void removeInactiveProjects()
    {
        Date oldestActiveDate = new Date( System.currentTimeMillis() - ACTIVE_PROJECTS_OFFSET );
        for ( String projectId : latestProjectActivity.keySet() )
        {
            Date latestActivity = latestProjectActivity.get( projectId );
            if ( latestActivity.before( oldestActiveDate ) )
            {
                latestProjectActivity.remove( projectId );
            }
        }

        Date oldestRecentDate = new Date( System.currentTimeMillis() - RECENT_PROJECTS_OFFSET );
        for ( String username : latestUserProjectActivity.keySet() )
        {
            Map<String, Date> latestUserActivity = latestUserProjectActivity.get( username );

            for ( String projectId : latestUserActivity.keySet() )
            {
                Date latestRecent = latestUserActivity.get( projectId );
                if ( latestRecent.before( oldestRecentDate ) )
                {
                    latestUserActivity.remove( projectId );
                }
            }
        }
    }

    protected static void scheduleRemoveInactiveProjects()
    {
        new Thread()
        {
            public void run()
            {
                while ( true )
                {
                    try
                    {
                        Thread.sleep( 1000l * 60 * 60 * 6 );
                    }
                    catch ( InterruptedException e )
                    {
                     // ignore
                    }

                    Logger log = Manager.getLogger( "HibernateStorage-InactiveProjects" );
                    log.debug( "Removing inactive or non-recent projects" );
                    removeInactiveProjects();
                }
            }
        }.start();
    }

    protected static void addActiveProjectForEvent( Event event )
    {
        if ( event.getProject() == null || event.getProject().getId().equals( Project.ALL_PROJECT_ID ) )
        {
            return;
        }

        String projectId = event.getProject().getId();

        activeProjectIds.add( projectId );
        latestProjectActivity.put( projectId, event.getTime() );
    }

    protected static void addRecentProjectForEvent( Event event )
    {
        if ( event.getProject() == null || event.getProject().getId().equals( Project.ALL_PROJECT_ID ) )
        {
            return;
        }
        String projectId = event.getProject().getId();

        if ( event.getUsername() == null )
        {
            return;
        }

        Set<String> userRecentProjectIds = recentProjectIds.get( event.getUsername() );
        if ( userRecentProjectIds == null )
        {
            userRecentProjectIds = new HashSet<String>();
            recentProjectIds.put( event.getUsername(), userRecentProjectIds );
        }
        userRecentProjectIds.add( projectId );

        Map<String, Date> projectActivity = latestUserProjectActivity.get( event.getUsername() );
        if ( projectActivity == null )
        {
            projectActivity = new HashMap<String, Date>();
            latestUserProjectActivity.put( event.getUsername(), projectActivity );
        }
        projectActivity.put( projectId, event.getTime() );
    }

    public Session getHibernateSession()
    {
        return getCurrentSession();
    }

    static Session getCurrentSession()
    {
        return HibernateUtil.getCurrentSession();
    }

    public void enterScope( Object scope )
    {
        currentScope.set( scope );
    }
    
    public void exitScope( Object scope )
    {
        synchronized (sessions)
        {
            if ( sessions.containsKey( currentScope.get() ) )
            {
                Session session = sessions.remove( currentScope.get() );
                session.close();
            }
        }
        
        currentScope.set( null );
    }

    public static Object getCurrentScope()
    {
        return currentScope.get();
    }

    public static Map<Object, org.hibernate.classic.Session> getManagedSessions()
    {
        return sessions;
    }

    // TODO try and move this out - impact?
    public void closeSession()
    {
        Session s = threadSession.get();
        if ( s != null )
        {
            s.close();
            threadSession.set( null );
        }
    }

    public File getDataDirectory()
    {
        return getGlobalConfiguration().getDataDir();
    }

    public File getWorkingDirectory( Project project )
    {
        File checkoutDir = new File( getDataDirectory(), "checkout" );
        if ( !checkoutDir.exists() )
        {
            checkoutDir.mkdir();
        }

        String rootId = project.getId();
        String rootScm = project.getScm();
        Project parent = project;
        while ( parent.getParent() != null )
        {
            parent = parent.getParent();

            rootId = parent.getId();
            rootScm = parent.getScm();
        }

        String folders = "";
        if ( !project.getScm().equals( rootScm ) )
        {
            folders = File.separatorChar + project.getScm().substring( rootScm.length() );
        }

        return new File( checkoutDir, rootId + folders );
    }

    public void copyWorkingDirectory( Project project, File dest )
        throws IOException
    {
        File src = getWorkingDirectory( project );

        copyFiles( src, dest );
    }

    private void copyFiles( File src, File dest )
        throws IOException
    {
        if ( src.isDirectory() )
        {
            dest.mkdirs();
            String[] files = src.list();

            for ( String fileName : files )
            {
                File src1 = new File( src, fileName );
                File dest1 = new File( dest, fileName );

                copyFiles( src1, dest1 );
            }
        }
        else
        {
            FileChannel sourceChannel = new FileInputStream( src ).getChannel();
            FileChannel targetChannel = new FileOutputStream( dest ).getChannel();
            sourceChannel.transferTo( 0, sourceChannel.size(), targetChannel );
            sourceChannel.close();
            targetChannel.close();
        }
    }

    public File getApplicationDataDirectory( Application app )
    {
        if ( app == null )
        {
            return getDataDirectory();
        }

        File ret = new File( getDataDirectory(), app.getApplicationId() );

        if ( !ret.exists() )
        {
            ret.mkdir();
        }

        return ret;
    }
    
    public String getConfigurationItem( String name )
    {
        Session session = getHibernateSession();
        Transaction tx = session.beginTransaction();
        StoredConfigurationItem item = (StoredConfigurationItem) session.createQuery( "from StoredConfigurationItem i where name = '" +
            name + "'" ).uniqueResult();
        tx.commit();

        if ( item == null )
        {
            return null;
        }
        return item.getValue();
    }

    public void setConfigurationItem( String name, String value )
    {
        Session session = getHibernateSession();
        Transaction tx = session.beginTransaction();
        StoredConfigurationItem item = (StoredConfigurationItem) session.createQuery( "from StoredConfigurationItem i where name = '" +
            name + "'" ).uniqueResult();

        boolean requiresReload = false;
        if ( item == null )
        {
            item = new StoredConfigurationItem( name, value );
            requiresReload = true;
        }
        else
        {
            item.setValue( value );
        }

        session.saveOrUpdate( item );
        tx.commit();

        if ( requiresReload )
        {
            reloadGlobalConfiguration();
        }
    }

    public void removeConfigurationItem( String name )
    {
        Session session = getHibernateSession();
        Transaction tx = session.beginTransaction();

        session.createQuery( "delete from StoredConfigurationItem i where name = '" + name + "'" ).executeUpdate();

        tx.commit();
    }

    public Map<String, String> getConfigurationItems( String prefix )
    {
        Map<String, String> ret = new HashMap<String, String>();
        Session session = getHibernateSession();
        Transaction tx = session.beginTransaction();
        List<StoredConfigurationItem> items = (List<StoredConfigurationItem>) session.createQuery( "from StoredConfigurationItem i where name like '" +
            prefix + "%'" ).list();
        tx.commit();

        for ( StoredConfigurationItem item : items )
        {
            ret.put( item.getName(), item.getValue() );
        }

        return ret;
    }

    public void setConfigurationItems( Map<String, String> items )
    {
        Session session = getHibernateSession();
        Transaction tx = session.beginTransaction();

        for ( String name : items.keySet() )
        {
            StoredConfigurationItem item = (StoredConfigurationItem) session.createQuery( "from StoredConfigurationItem i where name = '" +
                name + "'" ).uniqueResult();

            if ( item == null )
            {
                item = new StoredConfigurationItem( name, items.get( name ) );
            }
            else
            {
                item.setValue( items.get( name ) );
            }

            session.saveOrUpdate( item );
        }
        tx.commit();
    }

    public HeadsUpConfiguration getGlobalConfiguration()
    {
        if ( globalConfig == null )
        {
            globalConfig = new HeadsUpConfiguration( getConfigurationItems( "" ) );
        }

        return globalConfig;
    }

    private void reloadGlobalConfiguration()
    {
        globalConfig = null;
    }

    public Project getProject( String id )
    {
        Session session = getHibernateSession();
        Project ret = (Project) session.createQuery( "from StoredProject p where id = '" + id + "'" ).uniqueResult();

        return ret;
    }

    public List<Project> getProjects()
    {
        Session session = getHibernateSession();
        List<Project> list = session.createQuery( "from StoredProject p where id != '" + Project.ALL_PROJECT_ID + "' order by name" ).list();

        return list;
    }

    public List<Project> getRootProjects()
    {
        return getRootProjects( false );
    }

    public List<Project> getRootProjects( boolean withDisabled )
    {
        Session session = getHibernateSession();

        return getProjectCriteria( session, withDisabled, null ).list();
    }

    public List<Project> getActiveRootProjects()
    {
        Session session = getHibernateSession();

        return getProjectCriteria( session, false, activeProjectIds ).list();
    }

    public List<Project> getRecentRootProjects( User user )
    {
        Session session = getHibernateSession();

        Set<String> recentIds = recentProjectIds.get( user.getUsername() );
        if ( recentIds == null )
        {
            return new ArrayList<Project>();
        }
        return getProjectCriteria( session, false, recentIds ).list();
    }

    protected Criteria getProjectCriteria( Session session, boolean disabled, Set<String> ids )
    {
        Criteria criteria = session.createCriteria( Project.class );
        criteria.add( Restrictions.not( Restrictions.eq( "id", Project.ALL_PROJECT_ID ) ) );
        criteria.add( Restrictions.isNull( "parent" ) );

        if ( !disabled )
        {
            criteria.add( Restrictions.or( Restrictions.isNull( "disabled" ), Restrictions.eq( "disabled", false ) ) );
        }

        if ( ids != null )
        {
            criteria.add( Restrictions.in( "id", ids ) );
        }

        criteria.addOrder( Order.asc( "name" ) );
        return criteria;
    }

    public List<Event> getEvents( Date start )
    {
        Session session = getHibernateSession();
        Query q = session.createQuery( "from StoredEvent e where time >= :start order by time desc" );
        q.setDate( "start", start );
        List<Event> list = q.list();

        return list;
    }

    public List<Event> getEvents( Date start, Date end )
    {
        Session session = getHibernateSession();
        Query q = session.createQuery( "from StoredEvent e where time >= :start and time < :end order by time desc" );
        q.setDate( "start", start );
        q.setDate( "end", end );
        List<Event> list = q.list();

        return list;
    }

    public List<Event> getEvents( Application app, Date start, Date end )
    {
        Session session = getHibernateSession();
        Query q = session.createQuery( "from StoredEvent e where applicationId = :appId and time >= :start and time < :end order by time desc" );
        q.setString( "appId", app.getApplicationId() );
        q.setTimestamp( "start", start );
        q.setTimestamp( "end", end );
        List<Event> list = q.list();

        return list;
    }

    public List<Event> getEventsForProject( Project project, Date start, Date end )
    {
        return doGetEventsForProject( project, null, start, end, false );
    }

    public List<Event> getEventsForProject( Project project, Application app, Date start, Date end )
    {
        return doGetEventsForProject( project, app, start, end, false );
    }

    public List<Event> getEventsForProjectTree( Project project, Date start, Date end )
    {
        return doGetEventsForProject( project, null, start, end, true );
    }

    public List<Event> getEventsForProjectTree( Project project, Application app, Date start, Date end )
    {
        return doGetEventsForProject( project, app, start, end, true );
    }

    private List<Event> doGetEventsForProject( Project project, Application app, Date start, Date end, boolean tree )
    {
        String query = "from StoredEvent e where project.id = :pid";
        if ( tree )
        {
            query = "from StoredEvent e where project.id in (:pids)";
        }

        if ( app != null )
        {
            query += " and applicationId = :appId";
        }

        if ( start != null )
        {
            query += " and time >= :start and time < :end";
        }
        query += " order by time desc";

        Session session = getHibernateSession();
        Transaction tx = session.beginTransaction();
        Query q = session.createQuery( query );

        if ( tree )
        {
            project = (Project) session.merge( project );
            List<String> projects = new LinkedList<String>();
            doListProjectIds( project, projects );
            q.setParameterList( "pids", projects );
        }
        else
        {
            q.setString( "pid", project.getId() );
        }

        if ( app != null )
        {
            q.setString( "appId", app.getApplicationId() );
        }

        if ( start != null )
        {
            q.setTimestamp( "start", start );
            q.setTimestamp( "end", end );
        }

        List<Event> list = q.list();
        tx.commit();

        return list;
    }

    private void doListProjectIds( Project project, List<String> projects )
    {
        projects.add( project.getId() );
        for ( Project child : project.getChildProjects() )
        {
            doListProjectIds( child, projects );
        }
    }

    public List<Event> getEventsForUser( User user, Date start, Date stop )
    {
        Session session = getHibernateSession();
        Transaction tx = session.beginTransaction();

        Query q = session.createQuery( "from StoredEvent e where time >= :start and time < :end and " +
                "(username = :username or username like :emailLike or username like :nameLike) order by time desc" );
        q.setTimestamp( "start", start );
        q.setTimestamp( "end", stop );
        q.setString( "username", user.getUsername() );

        if ( !StringUtil.isEmpty( user.getEmail() ) )
        {
            q.setString( "emailLike", "%<" + user.getEmail() + ">" );
        }
        else
        {
            // a silly fallback for now
            q.setString( "emailLike", user.getUsername() );
        }

        if ( !StringUtil.isEmpty( user.getFullname() ) )
        {
            q.setString( "nameLike", user.getFullname() + " <%" );
        }
        else
        {
            // a silly fallback for now
            q.setString( "nameLike", user.getUsername() );
        }

        List<Event> list = q.list();
        tx.commit();

        return list;
    }

    public void addProject( final Project proj )
    {
        save( proj );

        if ( proj.equals( StoredProject.getDefault() ) )
        {
            return;
        }
        Manager.getInstance().fireProjectAdded( proj );

        addChildProjects( proj );
    }

    private void addChildProjects( Project proj )
    {
        for ( Project child : proj.getChildProjects() )
        {
            save( child );
            Manager.getInstance().fireProjectAdded( child );

            addChildProjects( child );
        }
    }

    public void addEvent( Event event )
    {
        addActiveProjectForEvent( event );
        addRecentProjectForEvent( event );

        save( event );
    }

    public Object save( Object o )
    {
        Session session = getHibernateSession();
        Transaction tx = session.beginTransaction();
        Object ret = session.save( o );
        tx.commit();

        return ret;
    }

    public void update( Object o )
    {
        Session session = getHibernateSession();
        Transaction tx = session.beginTransaction();
        session.saveOrUpdate( o );
        tx.commit();
    }

    public Object merge( Object o )
    {
        Session session = getHibernateSession();
        Transaction tx = session.beginTransaction();
        Object ret = session.merge( o );
        tx.commit();

        return ret;
    }

    public void delete( Object o )
    {
        Session session = getHibernateSession();
        Transaction tx = session.beginTransaction();
        session.delete( o );
        tx.commit();
    }
}
