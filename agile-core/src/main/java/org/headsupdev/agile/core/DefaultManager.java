/*
 * HeadsUp Agile
 * Copyright 2009-2014 Heads Up Development.
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

package org.headsupdev.agile.core;

import org.headsupdev.agile.api.service.ScmService;
import org.headsupdev.agile.core.notifiers.hipchat.HipchatNotifier;
import org.headsupdev.agile.core.notifiers.irc.ProjectCommand;
import org.headsupdev.agile.storage.*;
import org.headsupdev.support.java.StringUtil;
import org.headsupdev.irc.IRCCommand;
import org.headsupdev.irc.IRCServiceManager;
import org.headsupdev.irc.impl.DefaultIRCServiceManager;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.Query;
import org.headsupdev.agile.api.logging.Logger;
import org.headsupdev.agile.api.*;
import org.headsupdev.agile.core.logging.HeadsUpLoggerManager;
import org.headsupdev.agile.core.notifiers.IRCNotifier;
import org.headsupdev.agile.core.notifiers.TwitterNotifier;
import org.headsupdev.agile.core.notifiers.EmailNotifier;

import java.util.*;
import java.io.Serializable;
import java.io.File;

/**
 * The default HeadsUp manager giving central access to the core objects and listeners.
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class DefaultManager
    extends Manager
    implements Serializable
{
    private List<ProjectListener> projectListeners = new LinkedList<ProjectListener>();

    transient private List<UpdateDetails> availableUpdates = new LinkedList<UpdateDetails>();
    transient private UpdatesThread updatesThread;

    // currently we do not have any way of reviving dead tasks, so we don't store them
    transient private List<Task> tasks = new LinkedList<Task>();

    static private Map<String,Class<? extends Notifier>> availableNotifiers;

    static private Map<String, LinkProvider> linkProviders = new HashMap<String,LinkProvider>();

    private Date installDate;
    // TODO load this value from the maven version somehow, (not a properties file!)
    private double installVersion = 2.0;

    private Map<Project, List<Notifier>> notifiers = new HashMap<Project, List<Notifier>>();
    
    private ScmService scmService;

    public void load()
    {
        new DefaultIRCServiceManager().addCommand( new ProjectCommand() );

        availableNotifiers = getNotifierList();

        new Thread( "InitNotifiers" )
        {
            public void run()
            {
                initNotifiers( StoredProject.getDefault() );
                for ( Project project : Manager.getStorageInstance().getProjects() )
                {
                    initNotifiers( project );
                }
            }
        }.start();

        updatesThread = newUpdatesThreadInstance();
        updatesThread.start();
    }

    protected UpdatesThread newUpdatesThreadInstance()
    {
        return new UpdatesThread();
    }

    protected Map<String, Class<? extends Notifier>> getNotifierList()
    {
        Map<String, Class<? extends Notifier>> notifiers = new HashMap<String, Class<? extends Notifier>>();
        notifiers.put( "irc", IRCNotifier.class );
        notifiers.put( "email", EmailNotifier.class );
        notifiers.put( "twitter", TwitterNotifier.class );
        notifiers.put( "hipchat", HipchatNotifier.class );

        return notifiers;
    }

    private Notifier loadNotifier( String id )
    {
        try
        {
            Class<? extends Notifier> notifierClass = availableNotifiers.get( id );
            return notifierClass.newInstance();
        }
        catch ( Exception e )
        {
            getLoggerForComponent( getClass().getName() ).error( "Failed to load notifier " + id, e );
        }

        return null;
    }

    private void initNotifiers( Project project )
    {
        Set<String> notifierIds = PrivateConfiguration.getNotifierList( project );
        List<Notifier> notifiers = this.notifiers.get( project );
        if ( notifiers == null )
        {
            notifiers = new LinkedList<Notifier>();
            this.notifiers.put( project, notifiers );
        }

        for ( String notifierId : availableNotifiers.keySet() )
        {
            if ( notifierIds.contains( notifierId ) )
            {
                Notifier notifier = loadNotifier( notifierId );
                if ( notifier == null )
                {
                    continue;
                }

                notifier.setConfiguration( PrivateConfiguration.getNotifierConfiguration( notifier.getId(), project ) );
                notifiers.add( notifier );

                try
                {
                    notifier.start();
                }
                catch ( Exception e )
                {
                    getLoggerForComponent( getClass().getName() ).error( "Error starting notifier", e );
                }
            }
        }
    }

    public void unload()
    {
        updatesThread.cancel();
        updatesThread = null;

        deinitNotifiers( StoredProject.getDefault() );
        for ( Project project : Manager.getStorageInstance().getProjects() )
        {
            deinitNotifiers( project );
        }
    }

    private void deinitNotifiers( Project project )
    {
        List<Notifier> notifiers = this.notifiers.get( project );
        if ( notifiers == null )
        {
            return;
        }

        for ( Notifier notifier : notifiers )
        {
            try
            {
                notifier.stop();
            }
            catch ( Exception e )
            {
                getLoggerForComponent( getClass().getName() ).error( "Error stopping notifier", e );
            }
        }
    }

    public static void applicationAdded( Application application )
    {
        for ( LinkProvider provider : application.getLinkProviders() )
        {
            linkProviders.put( provider.getId(), provider );
        }

        for ( IRCCommand command : application.getIRCCommands() )
        {
            ( (DefaultIRCServiceManager) IRCServiceManager.getInstance() ).addCommand( command );
        }
    }

    public static void applicationRemoved( Application application )
    {
        for ( LinkProvider provider : application.getLinkProviders() )
        {
            linkProviders.remove( provider.getId() );
        }

        for ( IRCCommand command : application.getIRCCommands() )
        {
            ( (DefaultIRCServiceManager) IRCServiceManager.getInstance() ).removeCommand( command );
        }
    }

    public void fireProjectAdded( final Project proj )
    {
        ( new StorageThread() {
            public void runWithSession()
            {
                Enumeration<ProjectListener> listeners = new Vector<ProjectListener>( projectListeners ).elements();
                while ( listeners.hasMoreElements() )
                {
                    ProjectListener listener = listeners.nextElement();
                    listener.projectAdded( proj );
                }
            }
        }).start();
    }

    public void fireProjectModified( final Project proj )
    {
        ( new StorageThread() {
            public void runWithSession()
            {
                Enumeration<ProjectListener> listeners = new Vector<ProjectListener>( projectListeners ).elements();
                while ( listeners.hasMoreElements() )
                {
                    ProjectListener listener = listeners.nextElement();
                    listener.projectModified( proj );
                }
            }
        }).start();
    }

    public void fireProjectFileModified( final Project proj, final String path, final File file )
    {
        ( new StorageThread() {
            public void runWithSession()
            {
                Enumeration<ProjectListener> listeners = new Vector<ProjectListener>( projectListeners ).elements();
                while ( listeners.hasMoreElements() )
                {
                    ProjectListener listener = listeners.nextElement();
                    listener.projectFileModified( proj, path, file );
                }
            }
        }).start();
    }

    public void addProjectListener( ProjectListener listener )
    {
        projectListeners.add( listener );
    }

    public void removeProjectListener( ProjectListener listener )
    {
        projectListeners.remove( listener );
    }

    public List<Notifier> getNotifiers( Project project )
    {
        return notifiers.get( project );
    }

    public List<String> getAvailableNotifiers()
    {
        List<String> ret = new LinkedList<String>( availableNotifiers.keySet() );
        Collections.sort( ret );

        return ret;
    }

    public void addNotifier( String notifierId, Project project )
    {
        List<Notifier> nots = notifiers.get( project );
        if ( nots == null )
        {
            nots = new LinkedList<Notifier>();
            notifiers.put( project, nots );
        }

        Notifier notifier = loadNotifier( notifierId );
        if ( notifier == null )
        {
            return;
        }
        nots.add( notifier );

        PrivateConfiguration.addNotifierConfiguration( notifierId, notifier.getConfiguration(), project );
    }

    public void removeNotifier( Notifier notifier, Project project )
    {
        notifiers.get( project ).remove( notifier );
        PrivateConfiguration.removeNotifierConfiguration( notifier.getId(), project );
        notifier.setConfiguration( null );
    }

    public void fireEventAdded( final Event event )
    {
        new Thread( "Notifiers" )
        {
            public void run()
            {
                Project project = event.getProject();

                while ( project != null && !project.equals( StoredProject.getDefault() ) )
                {
                    sendNotification( event, project );

                    project = project.getParent();
                }

                sendNotification( event, StoredProject.getDefault() );
                sendSubscriptions( event );
            }
        }.start();
    }

    private void sendNotification( Event event, Project project )
    {
        if ( notifiers.get( project ) != null )
        {
            for ( Notifier notifier : notifiers.get( project ) )
            {
                if ( notifier.getIgnoredEvents().contains( event.getType() ) )
                {
                    getLoggerForComponent( getClass().getName() ).debug( "Ignoring event " + event.getType() + " for " +
                            notifier.getId() + " notifier in project " + project.getId() );
                    continue;
                }

                try
                {
                    getLoggerForComponent( getClass().getName() ).debug( "Running " + notifier.getId() +
                            " notifier for event " + event.getType() + " in project " + project.getId() );
                    notifier.eventAdded( event );
                }
                catch ( Exception e )
                {
                    // we do not want broken notifiers to kill the process that spawned them!!!
                    getLoggerForComponent( getClass().getName() ).error( "Problem found in notifier " + notifier.getId(), e );
                }
            }
        }
    }

    private void sendSubscriptions( Event event )
    {
        String from = Manager.getStorageInstance().getGlobalConfiguration().getSmtpHost();
        if ( StringUtil.isEmpty( from ) )
        {
            from = "noreply@headsupdev.com";
        }

        Session session = HibernateUtil.getCurrentSession();
        for ( User user : Manager.getSecurityInstance().getUsers() )
        {
            user = (User) session.load( StoredUser.class, user.getUsername() );
            if ( user.isDisabled() )
            {
                continue;
            }

            // TODO a configurable system for controlling what a user gets sent
            if ( ( event.getUsername() == null || !event.getUsername().equals( user.getUsername() ) ) &&
                    event.shouldNotify( user ) )
            {
                if ( !StringUtil.isEmpty( user.getEmail() ) )
                {
                    getLoggerForComponent( getClass().getName() ).info( "Emailing event to user " + user.getUsername() + " at " + user.getEmail() );
                    try
                    {
                        ( (EmailNotifier) getNotifierList().get( "email" ).newInstance() ).sendEventEmail( event, user.getEmail(), from, EmailNotifier.FooterType.Subscription );
                    }
                    catch ( Exception e )
                    {
                        // we do not want broken notifiers to kill the process that spawned them!!!
                        getLoggerForComponent( getClass().getName() ).error( "Problem found sending email subscriptions", e );
                    }
                }
                else
                {
                    getLoggerForComponent( getClass().getName() ).warn( "No email for user " + user.getUsername() );
                }
            }
        }
    }

    public Map<String, LinkProvider> getLinkProviders()
    {
        return linkProviders;
    }

    public void addLinkProvider( String key, LinkProvider provider )
    {
        linkProviders.put( key, provider );
    }

    public List<Task> getTasks()
    {
        return Collections.unmodifiableList( tasks );
    }

    public void addTask( Task task )
    {
        getLogger( getClass().getName() ).info( "Task start: " + task.getTitle() );
        tasks.add( task );
    }

    public void removeTask( Task task )
    {
        getLogger( getClass().getName() ).info( "Task stop : " + task.getTitle() );
        tasks.remove( task );
    }

    public Date getInstallDate()
    {
        if ( installDate == null )
        {
            Session session = HibernateUtil.getCurrentSession();
            Transaction tx = session.beginTransaction();

            Query q = session.createQuery( "Select time from StoredEvent e where e.class != 'filechangeset' order by time" );
            q.setMaxResults( 1 );
            List<Date> list = q.list();

            tx.commit();

            if ( list == null || list.size() == 0 )
            {
                installDate = new Date();
            }
            else
            {
                installDate = list.get( 0 );
            }
        }

        return installDate;
    }

    public double getInstallVersion()
    {
        return installVersion;
    }

    public void setupCompleted()
    {

    }

    public boolean isUpdateAvailable()
    {
        return availableUpdates.size() > 0;
    }

    public void addAvailableUpdate( UpdateDetails update )
    {
        for ( UpdateDetails stored : availableUpdates )
        {
            if ( stored.getId().equals( update.getId() ) )
            {
                return;
            }
        }

        availableUpdates.add( 0, update );
    }

    public List<UpdateDetails> getAvailableUpdates()
    {
        return availableUpdates;
    }

    public void checkForUpdates()
    {
        updatesThread.interrupt();
    }

    protected Logger getLoggerForComponent( String component )
    {
        return HeadsUpLoggerManager.getInstance().getLoggerForComponent( component );
    }

    @Override
    public ScmService getScmService()
    {
        return scmService;
    }

    void setScmService( ScmService service )
    {
        scmService = service;
    }
}
