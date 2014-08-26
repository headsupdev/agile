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

package org.headsupdev.agile.web.components.issues;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.model.PropertyModel;
import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.api.Page;
import org.headsupdev.agile.api.Permission;
import org.headsupdev.agile.api.User;
import org.headsupdev.agile.storage.HibernateStorage;
import org.headsupdev.agile.storage.StoredProject;
import org.headsupdev.agile.storage.issues.Issue;
import org.headsupdev.agile.storage.issues.Milestone;
import org.headsupdev.agile.web.HeadsUpPage;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.headsupdev.agile.web.HeadsUpSession;
import org.headsupdev.agile.web.components.GravatarLinkPanel;
import org.headsupdev.agile.web.components.UserDropDownChoice;
import org.headsupdev.agile.web.components.milestones.MilestoneDropDownChoice;

import java.util.List;

/**
 * A single row from an issue list table
 * <p/>
 * Created: 12/09/2011
 *
 * @author Andrew Williams
 * @since 1.0
 */
public class IssuePanelRow
    extends Panel
{
    private Issue issue;
    public IssuePanelRow( String id, Issue issue, final HeadsUpPage page, final boolean hideProject,
                          final boolean hideMilestone, boolean hideAssignee )
    {
        super( id );
        issue = (Issue) ((HibernateStorage) Manager.getStorageInstance() ).getHibernateSession().load( Issue.class,
            issue.getInternalId() );
        this.issue = issue;
        add( CSSPackageResource.getHeaderContribution( getClass(), "issue.css" ) );

        final boolean timeEnabled = Boolean.parseBoolean( page.getProject().getConfigurationValue(
                StoredProject.CONFIGURATION_TIMETRACKING_ENABLED ) );

        PageParameters params = new PageParameters();
        params.add( "project", issue.getProject().getId() );
        params.add( "id", String.valueOf( issue.getId() ) );

        WebMarkupContainer cell = new WebMarkupContainer( "id-cell" );

        Class<? extends Page> viewClass = page.getPageClass( "issues/view" );

        add( new Label("checkbox","") );
        if ( viewClass != null )
        {
            Link idLink = new BookmarkablePageLink( "id-link", viewClass, params );
            idLink.add( new Label( "id-label", String.valueOf( issue.getId() ) ) );
            cell.add( idLink );
            cell.add( new WebMarkupContainer( "id-label" ).setVisible( false ) );
        }
        else
        {
            cell.add( new WebMarkupContainer( "id-link" ).setVisible( false ) );
            cell.add( new Label( "id-label", String.valueOf( issue.getId() ) ) );
        }
        add(cell);

        cell = new WebMarkupContainer( "summary-cell" );

        if ( viewClass != null )
        {
            Link summaryLink = new BookmarkablePageLink( "summary-link", viewClass, params );
            summaryLink.add( new Label( "summary-label", issue.getSummary() ) );
            cell.add( summaryLink );
            cell.add( new WebMarkupContainer( "summary-label" ).setVisible( false ) );
        }
        else
        {
            cell.add( new WebMarkupContainer( "summary-link" ).setVisible( false ) );
            cell.add( new Label( "summary-label", issue.getSummary() ) );
        }
        add( cell );

        Label label = new Label( "project", ( issue.getProject() == null ) ? "" : issue.getProject().toString() );
        add( label.setVisible( !hideProject ) );

        add( new Label( "order", issue.getOrder() == null ? "" : String.valueOf( issue.getOrder() ) ) );
        add( new Label( "status", IssueUtils.getStatusDescription( issue ) ) );

        WebMarkupContainer type = new WebMarkupContainer( "type" );
        add(type);
        final String issueType = IssueUtils.getTypeName( issue.getType() );
        String typeIcon = "type/" + issueType + ".png";
        type.add( new Image( "type-icon", new ResourceReference( IssueListPanel.class, typeIcon ) ).add(
            new AttributeModifier( "alt", true, new Model<String>() {
                @Override
                public String getObject() {
                    return issueType;
                }
            } ) ).add(
            new AttributeModifier( "title", true, new Model<String>() {
                @Override
                public String getObject() {
                    return issueType;
                }
            } ) )
        );

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
            type.add( ico );
        }
        else
        {
            type.add( new Image( "priority-icon", new ResourceReference( IssueListPanel.class, image ) ) );
        }

        if ( ( (HibernateStorage) Manager.getStorageInstance() ).getResourceManager().isIssueMissingEstimate( issue ) )
        {
            if ( ( (HibernateStorage) Manager.getStorageInstance() ).getResourceManager().isIssueSeriouslyMissingEstimate( issue ) )
            {
                type.add( new Image( "overworked-icon", new ResourceReference( HeadsUpPage.class, "images/fail.png" ) ) );
            }
            else
            {
                type.add( new Image( "overworked-icon", new ResourceReference( HeadsUpPage.class, "images/warn.png" ) ) );
            }
        }
        else
        {
            type.add( new WebMarkupContainer( "overworked-icon" ).setVisible( false ) );
        }

        boolean attachments = issue.getAttachments().size() > 0;
        Image attachImage = new Image( "attachment-icon", new ResourceReference( IssueListPanel.class, "attach.png" ) );
        type.add( attachImage.setVisible( attachments ) );

        cell = new WebMarkupContainer( "assigned-cell" );
        if ( !hideAssignee )
        {
            if ( issue.getAssignee() != null )
            {
                params = new PageParameters();
                params.add( "project", issue.getProject().getId() );
                params.add( "username", issue.getAssignee().getUsername() );

                Class<? extends Page> userClass = page.getPageClass( "account" );
                if ( userClass != null )
                {
                    cell.add( new GravatarLinkPanel( "gravatar", issue.getAssignee(), HeadsUpPage.DEFAULT_ICON_EDGE_LENGTH ) );
                    Link assignedLink = new BookmarkablePageLink( "assigned-link", userClass, params );
                    assignedLink.add( new Label( "assigned-label", issue.getAssignee().getFullnameOrUsername() ) );
                    cell.add( assignedLink );
                    cell.add( new WebMarkupContainer( "assigned-label" ).setVisible( false ) );
                    cell.add( new WebMarkupContainer( "assign-link" ).setVisible( false ) );
                    cell.add( new WebMarkupContainer( "assign-choice" ).setVisible( false ) );
                }
                else
                {
                    cell.add( new WebMarkupContainer( "gravatar" ).setVisible( false ) );
                    cell.add( new WebMarkupContainer( "assigned-link" ).setVisible( false ) );
                    cell.add( new Label( "assigned-label", issue.getAssignee().getFullnameOrUsername() ) );
                    cell.add( new WebMarkupContainer( "assign-link" ).setVisible( false ) );
                    cell.add( new WebMarkupContainer( "assign-choice" ).setVisible( false ) );
                }
            }
            else
            {
                final UserDropDownChoice assignChoice = new UserDropDownChoice( "assign-choice" ) {
                    @Override
                    protected boolean wantOnSelectionChangedNotifications()
                    {
                        return true;
                    }

                    @Override
                    protected void onSelectionChanged( User newSelection )
                    {
                        IssuePanelRow.this.issue = (Issue) ( (HibernateStorage) Manager.getStorageInstance() ).getHibernateSession().merge( IssuePanelRow.this.issue );

                        // if we have an assignee that is not watching then add them to the watchers
                        if ( newSelection != null && !IssuePanelRow.this.issue.getWatchers().contains( newSelection ) )
                        {
                            IssuePanelRow.this.issue.getWatchers().add( newSelection );
                        }

                        IssuePanelRow.this.issue.setAssignee( newSelection );
                    }
                };
                assignChoice.setModel( new PropertyModel<User>( issue, "assignee" ) );
                Link assignLink = new AjaxFallbackLink( "assign-link" )
                {
                    @Override
                    public void onClick( AjaxRequestTarget ajaxRequestTarget )
                    {
                        this.setVisible( false );
                        ajaxRequestTarget.addComponent( this );
                        assignChoice.setVisible( true );
                        ajaxRequestTarget.addComponent( assignChoice );
                    }
                };
                cell.add( new WebMarkupContainer( "gravatar" ).setVisible( false ) );
                cell.add( assignLink.setOutputMarkupId( true ).setVisible( canEditIssue() ) );
                cell.add( assignChoice.setOutputMarkupId( true ).setOutputMarkupPlaceholderTag( true ).setVisible( false ) );
                cell.add( new WebMarkupContainer( "assigned-label" ).setVisible( false ) );
                cell.add( new WebMarkupContainer( "assigned-link" ).setVisible( false ) );
            }
        }
        else
        {
            cell.add( new WebMarkupContainer( "assigned-link" ).setVisible( false ) );
            cell.add( new WebMarkupContainer( "assigned-label" ).setVisible( false ) );
            cell.add( new WebMarkupContainer( "assign-link" ).setVisible( false ) );
            cell.add( new WebMarkupContainer( "assign-choice" ).setVisible( false ) );
        }
        add( cell.setVisible( !hideAssignee ) );

        cell = new WebMarkupContainer( "milestone-cell" );
        Milestone milestone = issue.getMilestone();
        if ( milestone == null )
        {
            cell.add( new WebMarkupContainer( "milestone-link" ).setVisible( false ) );
            cell.add( new WebMarkupContainer( "milestone-label" ).setVisible( false ) );

            final MilestoneDropDownChoice milestoneChoice = new MilestoneDropDownChoice( "setmilestone-choice", issue.getProject() )
            {
                @Override
                protected boolean wantOnSelectionChangedNotifications()
                {
                    return true;
                }

                @Override
                protected void onSelectionChanged( Milestone newSelection )
                {
                    IssuePanelRow.this.issue = (Issue) ( (HibernateStorage) Manager.getStorageInstance() ).getHibernateSession().merge( IssuePanelRow.this.issue );

                    Milestone newMilestone = (Milestone) ( (HibernateStorage) Manager.getStorageInstance() ).getHibernateSession().merge( newSelection );
                    IssuePanelRow.this.issue.setMilestone( newMilestone );
                }
            };
            milestoneChoice.setModel( new PropertyModel<Milestone>( issue, "milestone" ) );
            Link setMilestoneLink = new AjaxFallbackLink( "setmilestone-link" )
            {
                @Override
                public void onClick( AjaxRequestTarget ajaxRequestTarget )
                {
                    this.setVisible( false );
                    ajaxRequestTarget.addComponent( this );
                    milestoneChoice.setVisible( true );
                    ajaxRequestTarget.addComponent( milestoneChoice );
                }
            };
            cell.add( setMilestoneLink.setOutputMarkupId( true ).setVisible( canEditIssue() ) );
            cell.add( milestoneChoice.setOutputMarkupId( true ).setOutputMarkupPlaceholderTag( true ).setVisible( false ) );
        }
        else
        {
            params = new PageParameters();
            params.add( "project", milestone.getProject().getId() );
            params.add( "id", milestone.getName() );

            Class<? extends Page> milestoneClass = page.getPageClass( "milestones/view" );
            if ( milestoneClass != null )
            {
                Link milestoneLink = new BookmarkablePageLink( "milestone-link", milestoneClass, params );
                milestoneLink.add( new Label( "milestone-label", milestone.toString() ) );
                cell.add( milestoneLink );
                cell.add( new WebMarkupContainer( "milestone-label" ).setVisible( false ) );
            }
            else
            {
                cell.add( new WebMarkupContainer( "milestone-link" ).setVisible( false ) );
                cell.add( new Label( "milestone-label", milestone.toString() ) );
            }

            cell.add( new WebMarkupContainer( "setmilestone-link" ).setVisible( false ) );
            cell.add( new WebMarkupContainer( "setmilestone-choice" ).setVisible( false ) );
        }
        add(cell.setVisible(!hideMilestone));

        cell = new WebMarkupContainer( "hours-cell" );
        cell.add( new Label( "hours", new IssueHoursRemainingModel( issue ) ) );
        add( cell.setVisible( timeEnabled ) );
    }

    private static Permission editIssuePerm = new Permission()
    {
        public String getId()
        {
            return "ISSUE-EDIT";
        }

        public String getDescription()
        {
            return null;
        }

        public List<String> getDefaultRoles()
        {
            return null;
        }
    };

    protected boolean canEditIssue()
    {
        return Manager.getSecurityInstance().userHasPermission( ( (HeadsUpSession) getSession() ).getUser(),
                editIssuePerm, issue.getProject() );
    }
}
