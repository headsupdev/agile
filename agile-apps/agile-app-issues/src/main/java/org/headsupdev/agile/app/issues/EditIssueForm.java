/*
 * HeadsUp Agile
 * Copyright 2009-2014 Heads Up Development Ltd.
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

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.headsupdev.agile.api.User;
import org.headsupdev.agile.storage.Attachment;
import org.headsupdev.agile.storage.HibernateStorage;
import org.headsupdev.agile.storage.StoredProject;
import org.headsupdev.agile.storage.issues.Duration;
import org.headsupdev.agile.storage.issues.Issue;
import org.headsupdev.agile.storage.issues.Milestone;
import org.headsupdev.agile.storage.resource.DurationWorked;
import org.headsupdev.agile.web.HeadsUpPage;
import org.headsupdev.agile.web.HeadsUpSession;
import org.headsupdev.agile.web.components.*;
import org.headsupdev.agile.web.components.issues.IssueListPanel;
import org.headsupdev.agile.web.components.issues.IssueUtils;
import org.headsupdev.agile.web.components.milestones.MilestoneDropDownChoice;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;

/**
 * The form used when editing / creating an issue
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class EditIssueForm
        extends Panel
{
    public EditIssueForm( String id, final Issue issue, boolean creating, HeadsUpPage owner )
    {
        super( id );
        add( CSSPackageResource.getHeaderContribution( IssueListPanel.class, "issue.css" ) );

        add( new IssueForm( "edit", issue, creating, owner, this ) );
    }

    public void onSubmit( Issue issue )
    {
        // allow others to override
    }
}

class IssueForm
        extends Form<Issue>
{
    private Issue issue;
    private User oldAssignee;
    private Duration oldTimeRequired;
    private HeadsUpPage owner;
    private EditIssueForm parent;
    private boolean creating;
    private AttachmentPanel attachmentPanel;
    private CheckBox toggleWatchers;
    private User currentUser;

    public IssueForm( String id, final Issue issue, final boolean creating, final HeadsUpPage owner, EditIssueForm parent )
    {
        super( id );
        this.issue = issue;
        this.owner = owner;
        this.parent = parent;
        this.creating = creating;

        this.oldAssignee = issue.getAssignee();
        currentUser = ( (HeadsUpSession) getSession() ).getUser();
        if ( issue.getTimeRequired() != null )
        {
            this.oldTimeRequired = new Duration( issue.getTimeRequired() );
        }

        add( new Subheader( "subHeader", "Edit ", issue ).setVisible( !creating ) );
        setModel( new CompoundPropertyModel<Issue>( issue ) );

        add( new Label( "project", issue.getProject().getAlias() ) );
        add( new IssueTypeDropDownChoice( "type", IssueUtils.getTypes() ) );
        add( new DropDownChoice<Integer>( "priority", IssueUtils.getPriorities() )
        {
            public boolean isNullValid()
            {
                return false;
            }
        }.setChoiceRenderer( new IChoiceRenderer<Integer>()
        {
            public Object getDisplayValue( Integer i )
            {
                return IssueUtils.getPriorityName( i );
            }

            public String getIdValue( Integer o, int i )
            {
                return o.toString();
            }
        } ) );
        add( new TextField( "version" ) );
        add( new MilestoneDropDownChoice( "milestone", issue.getProject(), issue.getMilestone() ).setNullValid( true ) );

        Label status = new Label( "status", new Model<String>()
        {
            @Override
            public String getObject()
            {
                return IssueUtils.getStatusName( issue.getStatus() );
            }

        } );
        add( status );

        add( new GravatarLinkPanel( "reporterGravatar", issue.getReporter(), HeadsUpPage.DEFAULT_AVATAR_EDGE_LENGTH ) );
        final DropDownChoice<User> assignees = new UserDropDownChoice( "assignee", issue.getAssignee() );
        assignees.setNullValid( true );
        add( assignees );

        toggleWatchers = new CheckBox( "toggleWatchers", new Model<Boolean>()
        {

            @Override
            public void setObject( Boolean object )
            {
                currentUser.setPreference( "issue.automaticallyWatch", object );
            }

            @Override
            public Boolean getObject()
            {
                return currentUser.getPreference( "issue.automaticallyWatch", true );
            }
        } );

        toggleWatchers.setVisible( creating );
        add( toggleWatchers );

        Button assignToMe = new Button( "assignToMe" )
        {
            @Override
            public void onSubmit()
            {
                issue.setAssignee( currentUser );
                issue.getWatchers().add( currentUser );
                assignees.setChoices( new LinkedList<User>( owner.getSecurityManager().getRealUsers() ) );
                assignees.setModelObject( currentUser );
                assignees.modelChanged();

                super.onSubmit();
            }
        };
        assignToMe.setVisible( issue.getStatus() < Issue.STATUS_RESOLVED &&
                !( currentUser.equals( issue.getAssignee() ) ) );
        add( assignToMe.setDefaultFormProcessing( false ) );

        if ( creating )
        {
            add( new WebMarkupContainer( "created" ).setVisible( false ) );
            add( new WebMarkupContainer( "updated" ).setVisible( false ) );
        }
        else
        {
            add( new Label( "created", new FormattedDateModel( issue.getCreated(),
                    ( (HeadsUpSession) getSession() ).getTimeZone() ) ) );
            add( new Label( "updated", new FormattedDateModel( issue.getUpdated(),
                    ( (HeadsUpSession) getSession() ).getTimeZone() ) ) );
        }
        add( new TextField( "order" ).setRequired( false ) );

        ListView<User> watchers = new ListView<User>( "watchers", new ArrayList<User>( issue.getWatchers() ) )
        {
            @Override
            protected void populateItem( ListItem<User> listItem )
            {
                listItem.add( new GravatarLinkPanel( "gravatar", listItem.getModelObject(), HeadsUpPage.DEFAULT_AVATAR_EDGE_LENGTH ) );
            }
        };
        add( watchers.setVisible( !creating ) );

        add( new TextField( "summary" ).setRequired( true ) );
        add( new TextField( "environment" ) );

        boolean useTime = Boolean.parseBoolean( issue.getProject().getConfigurationValue( StoredProject.CONFIGURATION_TIMETRACKING_ENABLED ) );
        boolean required = useTime && Boolean.parseBoolean( issue.getProject().getConfigurationValue( StoredProject.CONFIGURATION_TIMETRACKING_REQUIRED ) );
        Duration timeEstimated = issue.getTimeEstimate();
        if ( timeEstimated == null )
        {
            timeEstimated = new Duration( 0, Duration.UNIT_HOURS );
            issue.setTimeEstimate( timeEstimated );
        }
        add( new DurationEditPanel( "timeEstimated", new Model<Duration>( issue.getTimeEstimate() ) ).setRequired( required ).setVisible( useTime ) );

        boolean showRemain = !creating &&
                Boolean.parseBoolean( issue.getProject().getConfigurationValue( StoredProject.CONFIGURATION_TIMETRACKING_BURNDOWN ) );
        boolean resolved = issue.getStatus() >= Issue.STATUS_RESOLVED;

        Duration timeRequired = issue.getTimeRequired();
        if ( timeRequired == null )
        {
            timeRequired = new Duration( 0, Duration.UNIT_HOURS );
            issue.setTimeRequired( timeRequired );
        }
        add( new DurationEditPanel( "timeRequired", new Model<Duration>( timeRequired ) )
                .setRequired( required )
                .setVisible( useTime && ( resolved || showRemain ) ) );
        add( new CheckBox( "includeInInitialEstimates" ).setVisible( useTime ) );

        add( new TextArea( "body" ) );
        add( new TextArea( "testNotes" ) );

        // if we're creating allow adding of new attachments
        if ( creating )
        {
            add( attachmentPanel = new AttachmentPanel( "attachment", owner ) );
        }
        else
        {
            add( new WebMarkupContainer( "attachment" ).setVisible( false ) );
        }
        add( new OnePressSubmitButton( "issueSubmit" ) );
    }

    public void onSubmit()
    {
        if ( attachmentPanel != null )
        {
            for ( Attachment attachment : attachmentPanel.getAttachments() )
            {
                if ( attachment != null )
                {
                    issue.getAttachments().add( attachment );
                }
            }
        }

        if ( !creating )
        {
            issue = (Issue) ( (HibernateStorage) owner.getStorage() ).getHibernateSession().merge( issue );

            // if we are updating our total required then log the change
            if ( issue.getTimeRequired() != null && !issue.getTimeRequired().equals( oldTimeRequired ) )
            {
                DurationWorked simulate = new DurationWorked();
                simulate.setUpdatedRequired( issue.getTimeRequired() );
                simulate.setDay( new Date() );
                simulate.setIssue( issue );
                simulate.setUser( currentUser );

                ( (HibernateStorage) owner.getStorage() ).save( simulate );
                issue.getTimeWorked().add( simulate );
            }
        }
        else if ( Boolean.parseBoolean( issue.getProject().getConfigurationValue( StoredProject.CONFIGURATION_TIMETRACKING_BURNDOWN ) ) )
        {
            issue.setTimeRequired( issue.getTimeEstimate() );
        }

        if ( creating )
        {
            if ( toggleWatchers.getModelObject() )
            {
                issue.getWatchers().add( currentUser );
            }
        }

        issue.setUpdated( new Date() );
        if ( issue.getMilestone() != null )
        {
            Milestone milestone = issue.getMilestone();
            if ( creating )
            {
                milestone = (Milestone) ( (HibernateStorage) owner.getStorage() ).getHibernateSession().merge( milestone );
            }
            if ( !milestone.getIssues().contains( issue ) )
            {
                milestone.getIssues().add( issue );
            }
        }

        // if we have an assignee that is not watching then add them to the watchers - assuming they have not just opted out :)
        if ( issue.getAssignee() != null && !issue.getWatchers().contains( issue.getAssignee() ) )
        {
            if ( oldAssignee == null || !issue.getAssignee().equals( oldAssignee ) )
            {
                issue.getWatchers().add( issue.getAssignee() );
            }
        }

        parent.onSubmit( issue );

        PageParameters params = new PageParameters();
        params.add( "project", issue.getProject().getId() );
        params.add( "id", String.valueOf( issue.getId() ) );
        setResponsePage( owner.getPageClass( "issues/view" ), params );
    }
}
