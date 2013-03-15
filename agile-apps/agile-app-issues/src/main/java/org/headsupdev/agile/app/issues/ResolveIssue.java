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

package org.headsupdev.agile.app.issues;

import org.headsupdev.agile.api.Event;
import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.app.issues.event.UpdateIssueEvent;
import org.headsupdev.agile.storage.Comment;
import org.headsupdev.agile.storage.HibernateStorage;
import org.headsupdev.agile.storage.StoredProject;
import org.headsupdev.agile.storage.issues.Duration;
import org.headsupdev.agile.storage.resource.DurationWorked;
import org.headsupdev.agile.storage.issues.Issue;
import org.headsupdev.agile.storage.issues.IssueRelationship;
import org.headsupdev.agile.web.MountPoint;
import org.headsupdev.agile.web.components.DurationEditPanel;
import org.headsupdev.agile.web.components.ProjectTreeDropDownChoice;
import org.headsupdev.agile.web.components.issues.IssueUtils;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import java.util.Date;

/**
 * Add a comment for an issue
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@MountPoint("resolve")
public class ResolveIssue
        extends CreateComment
{
    private int resolution, duplicate;
    private TextField duplicateField;
    private Project duplicateProject;

    private ProjectTreeDropDownChoice duplicateProjectField;

    private Duration additionalTime;
    private Duration originalTime;

    protected void layoutChild( Form form )
    {
        boolean timeEnabled = Boolean.parseBoolean( getProject().getConfigurationValue( StoredProject.CONFIGURATION_TIMETRACKING_ENABLED ) );
        boolean timeRequired = Boolean.parseBoolean( getProject().getConfigurationValue( StoredProject.CONFIGURATION_TIMETRACKING_REQUIRED ) );
        boolean timeBurndown = Boolean.parseBoolean( getProject().getConfigurationValue( StoredProject.CONFIGURATION_TIMETRACKING_BURNDOWN ) );

        form.add( new DropDownChoice<Integer>( "resolution", new PropertyModel<Integer>( this, "resolution" ), IssueUtils.getResolutions() )
        {
            protected boolean wantOnSelectionChangedNotifications()
            {
                return true;
            }

            @Override
            protected void onSelectionChanged( Integer newSelection )
            {
                super.onSelectionChanged( newSelection );

                duplicateField.setVisible( newSelection == Issue.RESOLUTION_DUPLICATE );
                duplicateProjectField.setVisible( newSelection == Issue.RESOLUTION_DUPLICATE );

                if ( newSelection != Issue.RESOLUTION_FIXED )
                {
                    additionalTime.setTime( 0 );
                }
                else
                {
                    Duration initialAdditionalTime = getInitialAdditionalTime();
                    additionalTime.setTimeUnit( initialAdditionalTime.getTimeUnit() );
                    additionalTime.setTime( initialAdditionalTime.getTime() );
                }
            }
        }.setChoiceRenderer( new IChoiceRenderer<Integer>()
        {
            public Object getDisplayValue( Integer integer )
            {
                return IssueUtils.getResolutionName( integer );
            }

            public String getIdValue( Integer integer, int i )
            {
                return integer.toString();
            }
        } ) );

        duplicateField = new TextField<Integer>( "duplicate", new PropertyModel<Integer>( this, "duplicate" ) );
        duplicateField.setVisible( false );
        duplicateField.setOutputMarkupId( true );
        form.add( duplicateField );

        duplicateProject = getIssue().getProject();
        duplicateProjectField = new ProjectTreeDropDownChoice( "duplicate-project", new PropertyModel( this, "duplicateProject" ) );
        duplicateProjectField.setVisible( false );
        duplicateProjectField.setOutputMarkupId( true );
        form.add( duplicateProjectField );

        originalTime = getIssue().getTimeRequired();
        WebMarkupContainer time = new WebMarkupContainer( "time" );
        time.setVisible( timeEnabled );

        time.add( new WebMarkupContainer( "burndown" ).setVisible( timeBurndown ) );
        time.add( new WebMarkupContainer( "total" ).setVisible( !timeBurndown ) );

        additionalTime = getInitialAdditionalTime();
        time.add( new DurationEditPanel( "timeWorked", new Model<Duration>( additionalTime ) ).setRequired( timeRequired ) );

        form.add( time );

        setSubmitLabel( "Resolve Issue" );
    }

    /**
     * this now returns a copy because we don't want the models to be altered through this return object.
     * @return
     */
    protected Duration getInitialAdditionalTime()
    {
        boolean timeBurndown = Boolean.parseBoolean( getProject().getConfigurationValue( StoredProject.CONFIGURATION_TIMETRACKING_BURNDOWN ) );

        if ( timeBurndown )
        {
            if ( getIssue().getTimeRequired() != null )
            {
                return new Duration( getIssue().getTimeRequired() );
            }
            else if ( getIssue().getTimeEstimate() != null )
            {
                return new Duration( getIssue().getTimeEstimate() );
            }
        }

        return new Duration( 0 );
    }

    protected void submitChild( Comment comment )
    {
        boolean timeEnabled = Boolean.parseBoolean( getProject().getConfigurationValue( StoredProject.CONFIGURATION_TIMETRACKING_ENABLED ) );
        boolean timeBurndown = Boolean.parseBoolean( getProject().getConfigurationValue( StoredProject.CONFIGURATION_TIMETRACKING_BURNDOWN ) );

        getIssue().setResolution( resolution );
        if ( duplicate != 0 )
        {
            Issue dupe = IssuesApplication.getIssue( duplicate, duplicateProject );

            IssueRelationship relationship = new IssueRelationship( getIssue(), dupe, IssueRelationship.TYPE_DUPLICATE );
            ( (HibernateStorage) getStorage() ).save( relationship );

            getIssue().getRelationships().add( relationship );
        }

        if ( timeBurndown )
        {
            if ( timeEnabled )
            {
                DurationWorked worked = new DurationWorked();
                worked.setWorked( additionalTime );
                worked.setUpdatedRequired( new Duration( 0 ) );
                worked.setDay( new Date() );
                if ( willChildConsumeComment() )
                {
                    worked.setComment( comment );
                }

                worked.setUser( getSession().getUser() );
                ( (HibernateStorage) getStorage() ).save( worked );
                getIssue().getTimeWorked().add( worked );
                getIssue().setTimeRequired( new Duration( 0 ) );
            }
        }
        else
        {
            getIssue().setTimeRequired( additionalTime );
            if ( timeEnabled && originalTime != null && additionalTime.getHours() > originalTime.getHours() )
            {
                double hours = additionalTime.getHours() - originalTime.getHours();

                DurationWorked worked = new DurationWorked();
                worked.setWorked( new Duration( hours ) );
                worked.setDay( new Date() );
                if ( willChildConsumeComment() )
                {
                    worked.setComment( comment );
                }

                worked.setUser( getSession().getUser() );
                ( (HibernateStorage) getStorage() ).save( worked );
                getIssue().getTimeWorked().add( worked );
            }
        }

        getIssue().setStatus( Issue.STATUS_RESOLVED );
    }

    @Override
    protected boolean willChildConsumeComment()
    {
        return additionalTime.getTimeAsMinutes() > 0;
    }

    protected Event getUpdateEvent( Comment comment )
    {
        return new UpdateIssueEvent( getIssue(), getIssue().getProject(), getSession().getUser(), comment,
                "resolved" );
    }
}
