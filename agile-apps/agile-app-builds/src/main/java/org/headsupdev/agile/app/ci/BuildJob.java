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

package org.headsupdev.agile.app.ci;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.api.Manager;

public class BuildJob
    implements Job
{
    public void execute( JobExecutionContext context )
        throws JobExecutionException
    {
        Project project = Manager.getStorageInstance().getProject(
            context.getJobDetail().getJobDataMap().getString( "project" ) );
        int id = context.getJobDetail().getJobDataMap().getInt( "id" );

        CIScheduler.runCron( project, id );
    }
}
