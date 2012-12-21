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

import org.apache.wicket.model.PropertyModel;
import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.api.User;
import org.headsupdev.agile.api.service.ChangeSet;
import org.headsupdev.agile.storage.HibernateStorage;
import org.headsupdev.agile.storage.StoredProject;
import org.headsupdev.agile.storage.issues.Issue;
import org.headsupdev.agile.storage.issues.IssueRelationship;
import org.headsupdev.agile.storage.issues.Milestone;
import org.headsupdev.agile.web.ApplicationPageMapper;
import org.headsupdev.agile.web.HeadsUpSession;
import org.headsupdev.agile.web.RenderUtil;
import org.headsupdev.agile.web.components.FormattedDateModel;
import org.headsupdev.agile.web.components.MarkedUpTextModel;
import org.headsupdev.agile.web.components.issues.IssueHoursEstimateModel;
import org.headsupdev.agile.web.components.issues.IssueHoursRequiredModel;
import org.headsupdev.agile.web.components.issues.IssueListPanel;
import org.headsupdev.agile.web.components.issues.IssueStatusModifier;
import org.headsupdev.agile.web.components.issues.IssueUtils;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * A panel which shows the details of an issue with icons and links to milestones etc
 *
 * @author Andrew Williams
 * @since 1.0
 */
public class IssuePanel
        extends Panel
{
    public IssuePanel( String id, final Issue issue )
    {
        super( id );

        add( new Label( "id", String.valueOf( issue.getId() ) ) );
        add( new Label( "project", ( issue.getProject() == null ) ? "" : issue.getProject().toString() ) );

        WebMarkupContainer type = new WebMarkupContainer( "type" );
        type.add( new Label( "typelabel", IssueUtils.getTypeName( issue.getType() ) ) );
        String typeIcon = "type/" + IssueUtils.getTypeName( issue.getType() ) + ".png";
        type.add( new Image( "type-icon", new ResourceReference( IssueListPanel.class, typeIcon ) ) );
        add( type );

        add( new Label( "priority", IssueUtils.getPriorityName( issue.getPriority() ) ) );
        add( new Label( "version", issue.getVersion() ) );
        final Milestone milestone = issue.getMilestone();

        Class userPage = RenderUtil.getPageClass( "account" );
        Class milestonePage = RenderUtil.getPageClass( "milestones/view" );

        PageParameters params = new PageParameters();
        if ( milestone == null )
        {
            add( new WebMarkupContainer( "milestone-link" ).setVisible( false ) );
            add( new Form( "add-form" ).setVisible( false ) );
        }
        else
        {
            params.add( "project", milestone.getProject().getId() );
            params.add( "id", milestone.getName() );
            Link milestoneLink = new BookmarkablePageLink( "milestone-link", milestonePage, params );
            milestoneLink.add( new Label( "milestone", milestone.toString() ) );
            add( milestoneLink );

            Button addIssue = new Button( "milestone-add-issue" )
            {
                @Override
                public void onSubmit()
                {
                    super.onSubmit();

                    PageParameters addIssueParams = new PageParameters();
                    addIssueParams.add( "project", milestone.getProject().getId() );
                    addIssueParams.add( "milestone", milestone.getName() );
                    setResponsePage( RenderUtil.getPageClass( "issues/create" ), addIssueParams );
                }
            };

            add( new Form( "add-form" ).add( addIssue.setDefaultFormProcessing( false ) ) );
        }

        String image = null;
        switch ( issue.getPriority() )
        {
            case Issue.PRIORITY_BLOCKER:
                image = "blocker.png";
                break;
            case Issue.PRIORITY_CRITICAL:
                image = "critical.png";
                break;
            case Issue.PRIORITY_MINOR:
                image = "minor.png";
                break;
            case Issue.PRIORITY_TRIVIAL:
                image = "trivial.png";
                break;
        }
        if ( image == null )
        {
            WebMarkupContainer ico = new WebMarkupContainer( "priority-icon" );
            ico.setVisible( false );
            add( ico );
        }
        else
        {
            add( new Image( "priority-icon", new ResourceReference( IssueListPanel.class, image ) ) );
        }
        add( new Label( "order", issue.getOrder() == null ? "" : String.valueOf( issue.getOrder() ) ).
                setVisible( issue.getOrder() != null ) );
        add( new Label( "watchers", new Model<String>()
        {
            @Override
            public String getObject()
            {
                return IssueUtils.getWatchersDescription( issue, getSessionUser() );
            }
        } ) );

        // TODO move the relationship comparators out of here, they are basically the same but opposite end of relationship compared
        List<IssueRelationship> relationships = new LinkedList<IssueRelationship>();
        relationships.addAll( issue.getRelationships() );
        Collections.sort( relationships, new Comparator<IssueRelationship>()
        {
            public int compare( IssueRelationship relationship1, IssueRelationship relationship2 )
            {
                if ( relationship1.getType() == relationship2.getType() )
                {
                    if ( relationship1.getRelated().getId() == relationship2.getRelated().getId() )
                    {
                        if ( relationship1.getRelated().getProject().equals( issue.getProject() ) )
                        {
                            return Integer.MIN_VALUE;
                        }
                        else if ( relationship2.getRelated().getProject().equals( issue.getProject() ) )
                        {
                            return Integer.MAX_VALUE;
                        }

                        return relationship1.getRelated().getProject().getId().compareToIgnoreCase(
                                relationship2.getRelated().getProject().getId() );
                    }

                    return (int) ( relationship1.getRelated().getId() - relationship2.getRelated().getId() );
                }

                return relationship1.getType() - relationship2.getType();
            }
        } );
        add( new ListView<IssueRelationship>( "relationship", relationships )
        {
            protected void populateItem( ListItem<IssueRelationship> listItem )
            {
                IssueRelationship relationship = listItem.getModelObject();
                listItem.add( new Label( "name", IssueUtils.getRelationshipName( relationship.getType() ) ) );

                Issue related = relationship.getRelated();
                PageParameters params = new PageParameters();
                params.put( "project", related.getProject() );
                params.put( "id", related.getId() );
                BookmarkablePageLink link = new BookmarkablePageLink( "issue-link", ViewIssue.class, params );
                String issueId = String.valueOf( related.getId() );
                if ( !related.getProject().equals( issue.getProject() ) )
                {
                    issueId = related.getProject().getId() + ":" + related.getId();
                }
                link.add( new Label( "issue", "issue:" + issueId ) );
                listItem.add( link.add( new IssueStatusModifier( "relatedstatus", related ) ) );
                listItem.add( new Label( "summary", related.getSummary() )
                        .add( new IssueStatusModifier( "relatedstatus", related ) ) );
            }
        } );
        relationships = new LinkedList<IssueRelationship>();
        relationships.clear();
        relationships.addAll( issue.getReverseRelationships() );
        Collections.sort( relationships, new Comparator<IssueRelationship>()
        {
            public int compare( IssueRelationship relationship1, IssueRelationship relationship2 )
            {
                if ( relationship1.getType() == relationship2.getType() )
                {
                    if ( relationship1.getOwner().getId() == relationship2.getOwner().getId() )
                    {
                        if ( relationship1.getOwner().getProject().equals( issue.getProject() ) )
                        {
                            return Integer.MIN_VALUE;
                        }
                        else if ( relationship2.getOwner().getProject().equals( issue.getProject() ) )
                        {
                            return Integer.MAX_VALUE;
                        }

                        return relationship1.getOwner().getProject().getId().compareToIgnoreCase(
                                relationship2.getOwner().getProject().getId() );
                    }

                    return (int) ( relationship1.getOwner().getId() - relationship2.getOwner().getId() );
                }

                return relationship1.getType() - relationship2.getType();
            }
        } );
        add( new ListView<IssueRelationship>( "reverse-relationship", relationships )
        {
            protected void populateItem( ListItem<IssueRelationship> listItem )
            {
                IssueRelationship relationship = listItem.getModelObject();
                listItem.add( new Label( "name", IssueUtils.getReverseRelationshipName( relationship.getType() ) ) );

                Issue related = relationship.getOwner();
                PageParameters params = new PageParameters();
                params.put( "project", related.getProject() );
                params.put( "id", related.getId() );
                BookmarkablePageLink link = new BookmarkablePageLink( "issue-link", ViewIssue.class, params );
                String issueId = String.valueOf( related.getId() );
                if ( !related.getProject().equals( issue.getProject() ) )
                {
                    issueId = related.getProject().getId() + ":" + related.getId();
                }
                link.add( new Label( "issue", "issue:" + issueId ) );
                listItem.add( link.add( new IssueStatusModifier( "relatedstatus", related ) ) );
                listItem.add( new Label( "summary", related.getSummary() )
                        .add( new IssueStatusModifier( "relatedstatus", related ) ) );
            }
        } );

        add( new ListView<ChangeSet>( "changeset", new ArrayList<ChangeSet>( issue.getChangeSets() ) )
        {
            protected void populateItem( ListItem<ChangeSet> listItem )
            {
                ChangeSet change = listItem.getModelObject();

                PageParameters params = new PageParameters();
                params.put( "project", change.getProject() );
                params.put( "id", change.getId() );
                BookmarkablePageLink link = new BookmarkablePageLink( "set-link",
                        ApplicationPageMapper.get().getPageClass( "files/change" ), params );
                String changeId = String.valueOf( change.getId() );
                if ( !change.getProject().equals( issue.getProject() ) )
                {
                    changeId = change.getProject().getId() + ":" + change.getId();
                }
                link.add( new Label( "set", "change:" + changeId ) );
                listItem.add( link );
                listItem.add( new Label( "summary", MarkedUpTextModel.markUp( change.getComment(), change.getProject() ) ).setEscapeModelStrings( false ) );
            }
        } );

        add( new Label( "status", IssueUtils.getStatusDescription( issue ) ) );

        params = getProjectPageParameters( issue.getProject() );
        params.add( "username", issue.getReporter().getUsername() );
        Link reporterLink = new BookmarkablePageLink( "reporter-link", userPage, params );
        reporterLink.add( new Label( "reporter", issue.getReporter().getFullnameOrUsername() ) );
        add( reporterLink );

        if ( issue.getAssignee() != null )
        {
            params = getProjectPageParameters( issue.getProject() );
            params.add( "username", issue.getAssignee().getUsername() );
            Link assignedLink = new BookmarkablePageLink( "assigned-link", userPage, params );
            assignedLink.add( new Label( "assigned", new PropertyModel( issue, "assignee" ) ) );
            add( assignedLink );

            if ( isSessionUserAssignedToIssue( issue ) )
            {
                addDropIssueButton( issue );
            }
        }
        else
        {
            add( new WebMarkupContainer( "assigned-link" ).setVisible( false ) );
            add( new Form( "drop-issue-form" ).setVisible( false ) );
        }

        add( new Label( "created", new FormattedDateModel( issue.getCreated(),
                ( (HeadsUpSession) getSession() ).getTimeZone() ) ) );
        add( new Label( "updated", new FormattedDateModel( issue.getUpdated(),
                ( (HeadsUpSession) getSession() ).getTimeZone() ) ) );

        boolean useTime = Boolean.parseBoolean( issue.getProject().getConfigurationValue( StoredProject.CONFIGURATION_TIMETRACKING_ENABLED ) );
        add( new Label( "estimate", new IssueHoursEstimateModel( issue )
        {
            @Override
            public String getObject()
            {
                String time = super.getObject();
                if ( issue.getIncludeInInitialEstimates() )
                {
                    time += " (initial)";
                }
                return time;
            }
        } ).setVisible( useTime ) );
        add( new Label( "required", new IssueHoursRequiredModel( issue ) ).setVisible( useTime ) );
        if ( Boolean.parseBoolean( issue.getProject().getConfigurationValue(
                StoredProject.CONFIGURATION_TIMETRACKING_BURNDOWN ) ) )
        {
            add( new Label( "requiredLabel", "Remaining" ) );
        }
        else
        {
            add( new Label( "requiredLabel", "Taken" ) );
        }

        add( new Label( "summary", issue.getSummary() ) );
        add( new Label( "environment", issue.getEnvironment() ) );
        add( new Label( "description", new MarkedUpTextModel( issue.getBody(), issue.getProject() ) )
                .setEscapeModelStrings( false ) );
        add( new Label( "testNotes", new MarkedUpTextModel( issue.getTestNotes(), issue.getProject() ) )
                .setEscapeModelStrings( false ).setVisible( issue.getTestNotes() != null ) );
    }

    private boolean isSessionUserAssignedToIssue( Issue issue )
    {
        return issue.getAssignee().equals( getSessionUser() );
    }

    private User getSessionUser()
    {
        return ((HeadsUpSession) getSession()).getUser();
    }

    private void addDropIssueButton( final Issue issue )
    {
        final Form form = new Form( "drop-issue-form" );

        Button button = new Button( "drop-issue" )
        {
            @Override
            public void onSubmit()
            {
                super.onSubmit();

                issue.setAssignee( null );
                issue.getWatchers().remove( getSessionUser() );
                issue.setStatus( Issue.STATUS_NEW );
                issue.setUpdated( new Date() );

                Session session = ((HibernateStorage) Manager.getStorageInstance()).getHibernateSession();
                Transaction tx = session.beginTransaction();
                session.update( issue );
                tx.commit();

                setResponsePage( ViewIssue.class, getPage().getPageParameters() );

                // TODO Log drop event.
            }
        };

        add( form.add( button.setDefaultFormProcessing( false ) ) );
    }

    protected PageParameters getProjectPageParameters( Project project )
    {
        PageParameters ret = new PageParameters();
        ret.add( "project", project.getId() );

        return ret;
    }
}
