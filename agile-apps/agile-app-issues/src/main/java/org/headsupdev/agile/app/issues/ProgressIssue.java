/*
 * HeadsUp Agile
 * Copyright 2009-2013 Heads Up Development.
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

package org.headsupdev.agile.app.issues;

import org.headsupdev.agile.api.Event;
import org.headsupdev.agile.app.issues.event.ProgressEvent;
import org.headsupdev.agile.app.issues.event.UpdateIssueEvent;
import org.headsupdev.agile.storage.Comment;
import org.headsupdev.agile.storage.HibernateStorage;
import org.headsupdev.agile.storage.StoredProject;
import org.headsupdev.agile.storage.issues.Duration;
import org.headsupdev.agile.storage.issues.Issue;
import org.headsupdev.agile.storage.resource.DurationWorked;
import org.headsupdev.agile.web.MountPoint;
import org.headsupdev.agile.web.components.DateTimeWithSecondField;
import org.headsupdev.agile.web.components.DurationEditPanel;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import java.util.Date;

/**
 * A page that allows you to register time worked against an issue.
 * If using burndown you can enter a new estimate for the work remaining.
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@MountPoint("progress")
public class ProgressIssue
        extends CreateComment
{
    private DurationWorked duration = new DurationWorked();

    @Override
    protected void layoutChild( Form form )
    {
        boolean timeRequired = Boolean.parseBoolean( getProject().getConfigurationValue( StoredProject.CONFIGURATION_TIMETRACKING_REQUIRED ) );
        boolean timeBurndown = Boolean.parseBoolean( getProject().getConfigurationValue( StoredProject.CONFIGURATION_TIMETRACKING_BURNDOWN ) );

        duration.setWorked( new Duration( 0, Duration.UNIT_HOURS ) );
        form.add( new DurationEditPanel( "timeWorked", new Model<Duration>( duration.getWorked() ) ).setRequired( timeRequired ) );

        WebMarkupContainer update = new WebMarkupContainer( "update" );
        update.setVisible( timeBurndown );

        if ( getIssue().getTimeRequired() == null )
        {
            getIssue().setTimeRequired( new Duration( 0, Duration.UNIT_HOURS ) );
        }
        update.add( new DurationEditPanel( "timeRequired", new Model<Duration>( getIssue().getTimeRequired() ) ).setRequired( timeRequired ) );
        form.add( update );

        duration.setDay( new Date() );
        form.add( new DateTimeWithSecondField( "day", new PropertyModel<Date>( duration, "day" ) ) );

        setSubmitLabel( "Progress Issue" );

    }

    protected Event getUpdateEvent( Comment comment )
    {
        return new ProgressEvent( getIssue(), getIssue().getProject(), getSession().getUser(), duration,
                "progressed" );
    }

    protected void submitChild( Comment comment )
    {
        duration.setUser( getSession().getUser() );
        duration.setIssue( getIssue() );
        if ( willChildConsumeComment() )
        {
            duration.setComment( comment );
        }

        ( (HibernateStorage) getStorage() ).save( duration );
        getIssue().getTimeWorked().add( duration );

        boolean timeBurndown = Boolean.parseBoolean( getProject().getConfigurationValue( StoredProject.CONFIGURATION_TIMETRACKING_BURNDOWN ) );
        if ( timeBurndown )
        {
            duration.setUpdatedRequired( getIssue().getTimeRequired() );
        }
        else
        {
            double hours = 0;
            if ( getIssue().getTimeRequired() == null )
            {
                if ( getIssue().getTimeEstimate() != null )
                {
                    hours = getIssue().getTimeEstimate().getHours();
                }
            }
            else
            {
                hours = getIssue().getTimeRequired().getHours();
            }
            hours += duration.getWorked().getHours();

            Duration required = new Duration( hours );
            duration.setUpdatedRequired( required );

            getIssue().setTimeRequired( required );
        }

        if ( getIssue().getAssignee() == null )
        {
            getIssue().setAssignee(getSession().getUser());
        }

        getIssue().getWatchers().add( getSession().getUser() );
        getIssue().setStatus(Issue.STATUS_INPROGRESS);
    }

    @Override
    protected boolean willChildConsumeComment()
    {
        return duration.getWorked().getTimeAsMinutes() > 0;
    }

}
