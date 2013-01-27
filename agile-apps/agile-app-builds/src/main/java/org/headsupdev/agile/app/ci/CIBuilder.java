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

package org.headsupdev.agile.app.ci;

import org.headsupdev.support.java.FileUtil;
import org.headsupdev.agile.api.logging.Logger;
import org.headsupdev.agile.api.*;
import org.headsupdev.agile.app.ci.event.BuildFailedEvent;
import org.headsupdev.agile.app.ci.event.BuildSucceededEvent;
import org.headsupdev.agile.storage.HibernateStorage;
import org.headsupdev.agile.storage.ci.Build;

import java.io.*;
import java.util.*;

/**
 * The main CI thread that runs the builds
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class CIBuilder
    implements ProjectListener
{
    private static final List<CIQueuedBuild> pendingBuilds = new LinkedList<CIQueuedBuild>();

    private static boolean building = false;

    private CIApplication application;

    private Logger log = Manager.getLogger( getClass().getName() );

    public void queueProject( Project project )
    {
        queueProject( project, true );
    }

    public void queueProject( Project project, boolean notify )
    {
        PropertyTree config = Manager.getStorageInstance().getGlobalConfiguration().
                getApplicationConfigurationForProject( CIApplication.ID, project ).getSubTree( "schedule.default" );
        queueProject( project, "default", config, notify );
    }

    public void queueProject( Project project, String id, PropertyTree config, boolean notify )
    {
        if ( !CIApplication.getHandlerFactory().supportsBuilding( project ) )
        {
            return;
        }

        if ( !isProjectQueued( project ) )
        {
            synchronized ( pendingBuilds )
            {
                pendingBuilds.add( new CIQueuedBuild( project, id, config, notify ) );
            }

            buildProjects();
        }
    }

    public void dequeueProject( Project project )
    {
        if ( !CIApplication.getHandlerFactory().supportsBuilding( project ))
        {
            return;
        }

        synchronized( pendingBuilds )
        {
            Iterator<CIQueuedBuild> queue = pendingBuilds.iterator();
            while ( queue.hasNext() )
            {
                CIQueuedBuild build = queue.next();

                if ( build != null && build.getProject() != null && build.getProject().equals( project ) )
                {
                    queue.remove();
                }
            }
        }
    }

    public void queueAllProjects()
    {
        Enumeration<Project> projects = new Vector<Project>( Manager.getStorageInstance().getProjects() ).elements();

        while ( projects.hasMoreElements() )
        {
            queueProject( projects.nextElement() );
        }
    }

    public static boolean isProjectQueued( Project project )
    {
        synchronized( pendingBuilds )
        {
            for ( CIQueuedBuild build : pendingBuilds )
            {
                if ( build != null && build.getProject() != null && build.getProject().equals( project ) )
                {
                    return true;
                }
            }
        }

        return false;
    }

    protected void buildProjects()
    {
        if ( building )
        {
            return;
        }

        building = true;
        new Thread()
        {
            public void run()
            {
                try
                {
                    while ( pendingBuilds.size() > 0 )
                    {
                        CIQueuedBuild build;
                        synchronized( pendingBuilds )
                        {
                            try
                            {
                                build = pendingBuilds.remove( 0 );
                            }
                            catch ( NoSuchElementException e )
                            {
                                // changed whilst building - just ignore and try later
                                break;
                            }
                        }

                        buildProject( build );
                    }
                }
                catch ( Exception e )
                {
                    e.printStackTrace();
                }
                finally
                {
                    building = false;
                }
            }
        }.start();
    }

    private void buildProject( CIQueuedBuild queued )
    {
        Project project = queued.getProject();
        PropertyTree config = queued.getConfig();
        if ( queued.getConfigName() == null )
        {
            log.info( "Preparing build for project " + project.getAlias() );
        }
        else
        {
            log.info( "Preparing build \"" + queued.getConfigName() + "\" for project " + project.getAlias() );
        }

        File base = null;
        File projectDir = CIApplication.getProjectDir( project );
        projectDir.mkdirs();

        Task buildTask = new BuildTask( project );
        try
        {
            HibernateStorage storage = (HibernateStorage) Manager.getStorageInstance();
            storage.getHibernateSession();

            Manager.getInstance().addTask( buildTask );
            try
            {
                base = FileUtil.createTempDir( "build-", "", projectDir );
                storage.copyWorkingDirectory( project, base );
            }
            catch ( Exception e )
            {
                log.error( "Unable to prepare project " + project + " for build", e );

                try
                {
                    FileUtil.delete( base );
                }
                catch ( IOException e2 )
                {
                    log.error( "Error removing failed build", e );
                }

                Manager.getInstance().removeTask( buildTask );
                return;
            }
            log.info( "Building project " + project.getAlias() + " in " + base.getPath() );

            Build build = new Build( project, project.getRevision() );
            build.setConfigName( queued.getConfigName() );
            build.setStatus( Build.BUILD_RUNNING );
            long buildId = application.addBuild( build );
            File output = new File( projectDir, buildId + ".txt" );

            CIApplication.getHandlerFactory().getBuildHandler( project ).runBuild( project, config,
                    application.getConfiguration(), base, output, build );

            Event event;
            if ( build.getStatus() != Build.BUILD_SUCCEEDED )
            {
                event = new BuildFailedEvent( build );
            }
            else
            {
                event = new BuildSucceededEvent( build );
            }

            application.addEvent( event, queued.getNotify() );
            storage.closeSession();

            try
            {
                FileUtil.delete( base );
            }
            catch ( IOException e )
            {
                log.error( "Error cleaning up finished build", e );
            }
        }
        finally
        {
            Manager.getInstance().removeTask( buildTask );
        }
    }

    public void projectAdded( Project project ) {
    }

    public void projectModified( Project project ) {
    }

    public void projectFileModified( Project project, String path, File file ) {
        queueProject( project );
    }

    public void projectRemoved(Project project) {
    }

    public void setApplication( CIApplication application )
    {
        this.application = application;

        // clear out un-finished builds caused by forced shutdowns...
        List<Build> runningBuilds = application.getRunningBuilds();
        if ( runningBuilds != null && runningBuilds.size() > 0 )
        {
            for ( Build running : runningBuilds )
            {
                running.setStatus( Build.BUILD_CANCELLED );
                running.setEndTime( running.getStartTime() );
                application.saveBuild( running );
            }
        }
    }
}
