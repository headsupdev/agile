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

import org.headsupdev.agile.api.ConfigurationItem;
import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.api.PropertyTree;
import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.api.logging.Logger;

import java.util.*;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

/**
 * TODO document me
 *
 * @author Andrew Williams
 * @since 1.0
 */
public class CIScheduler {
    private Map<Project, List<JobDetail>> schedules;
    private Scheduler scheduler;
    private Logger log = Manager.getLogger( getClass().getName() );

    public CIScheduler() {
        schedules = new HashMap<Project, List<JobDetail>>();

        SchedulerFactory schedFact = new StdSchedulerFactory();

        try
        {
            scheduler = schedFact.getScheduler();
            scheduler.start();
        }
        catch ( SchedulerException e )
        {
            log.fatalError( "Failed to start build scheduler", e );
        }
    }

    public void resetProject( Project project )
    {
        PropertyTree schedules = Manager.getStorageInstance().getGlobalConfiguration().
                    getApplicationConfigurationForProject( CIApplication.ID, project ).getSubTree( "schedule" );

        for ( JobDetail job : getScheduleList( project ) )
        {
            try
            {
                scheduler.deleteJob( job.getName(), null );
            }
            catch ( SchedulerException e )
            {
                log.error( "Failed to clean old job (" + job.getName() + ") from scheduler", e );
            }
        }

        int count = 0;
        try
        {
            count = Integer.parseInt( schedules.getProperty( "count" ) );
        }
        catch ( NumberFormatException e )
        {
            // just use 0
        }
        for ( int i = 0; i < count; i++ )
        {
            PropertyTree config = schedules.getSubTree( String.valueOf( i ) );
            JobDetail job = new JobDetail( "job:" + project.getId() + ":" + ( i + 1 ), null, BuildJob.class );
            job.getJobDataMap().put( "project", project.getId() );
            job.getJobDataMap().put( "id", i );

            String cron = config.getProperty( CIApplication.CONFIGURATION_CRON_EXPRESSION.getKey() );
            if ( cron == null )
            {
                cron = (String) CIApplication.CONFIGURATION_CRON_EXPRESSION.getDefault();
            }
            if ( ConfigurationItem.CRON_VALUE_NEVER.equals( cron ) )
            {
                return;
            }

            try
            {
                Trigger trigger = new CronTrigger( "schedule:" + project.getId() + ":" + ( i + 1 ), null, cron );
                trigger.setStartTime( new Date() );

                scheduler.scheduleJob( job, trigger );
                getScheduleList( project ).add( job );
            }
            catch ( Exception e )
            {
                log.error( "Failed to add new job (" + job.getName() + ") to scheduler", e );
            }
        }
    }

    public List<JobDetail> getScheduleList( Project project )
    {
        List<JobDetail> list = schedules.get( project );

        if ( list == null )
        {
            list = new LinkedList<JobDetail>();
            schedules.put( project, list );
        }

        return list;
    }

    static void runCron( Project project, int id )
    {
        PropertyTree schedules = Manager.getStorageInstance().getGlobalConfiguration().
                    getApplicationConfigurationForProject( CIApplication.ID, project ).getSubTree( "schedule" );
        PropertyTree config = schedules.getSubTree( String.valueOf( id ) );

        CIApplication.getBuilder().queueProject( project, String.valueOf( id ), config, true );
    }
}

