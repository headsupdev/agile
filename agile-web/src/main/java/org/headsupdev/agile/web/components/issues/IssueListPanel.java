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

package org.headsupdev.agile.web.components.issues;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.OrderByBorder;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.api.User;
import org.headsupdev.agile.storage.HibernateStorage;
import org.headsupdev.agile.storage.StoredProject;
import org.headsupdev.agile.storage.issues.Duration;
import org.headsupdev.agile.storage.issues.Issue;
import org.headsupdev.agile.storage.issues.Milestone;
import org.headsupdev.agile.web.DurationTextField;
import org.headsupdev.agile.web.HeadsUpPage;
import org.headsupdev.agile.web.HeadsUpSession;
import org.headsupdev.agile.web.components.HeadsUpTooltip;
import org.headsupdev.agile.web.components.IssueTypeDropDownChoice;
import org.headsupdev.agile.web.components.StripedDataView;
import org.headsupdev.agile.web.components.UserDropDownChoice;
import org.headsupdev.agile.web.components.milestones.MilestoneDropDownChoice;
import org.headsupdev.agile.web.wicket.StyledPagingNavigator;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.wicketstuff.animator.Animator;
import org.wicketstuff.animator.IAnimatorSubject;
import org.wicketstuff.animator.MarkupIdModel;

import java.util.Iterator;

//import org.headsupdev.agile.app.issues.event.CreateIssueEvent;
//import org.headsupdev.agile.app.issues.permission.IssueEditPermission;

/**
 * A panel that displays a formatted, coloured list of the issues passed in
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class IssueListPanel
        extends Panel
{
    private static final int ITEMS_PER_PAGE = 25;
    private final boolean hideMilestone;
    private final boolean hideProject;
    private StyledPagingNavigator pagingHeader, pagingFooter;

    private Issue quickIssue;
    private HeadsUpPage page;
    private Milestone milestone;
    private final WebMarkupContainer rowAdd;
    private WebMarkupContainer quickAdd;
    private Component icon;

    public IssueListPanel( String id, final SortableDataProvider<Issue> issues, final HeadsUpPage page, final boolean hideProject,
                           final boolean hideMilestone, final Milestone milestone )
    {
        super( id );
        add( CSSPackageResource.getHeaderContribution( getClass(), "issue.css" ) );

        this.page = page;
        this.milestone = milestone;
        this.hideMilestone = hideMilestone;
        this.hideProject = hideProject;

        quickIssue = createIssue();

        final boolean timeEnabled = Boolean.parseBoolean( page.getProject().getConfigurationValue(
                StoredProject.CONFIGURATION_TIMETRACKING_ENABLED ) );


        rowAdd = new WebMarkupContainer( "rowAdd" );
        rowAdd.setMarkupId( "rowAdd" );

        Form<Issue> inlineForm = getInlineForm();
        add( inlineForm );

        inlineForm.add( new WebMarkupContainer( "hours-header" ).setVisible( timeEnabled ) );
        final DataView dataView = new StripedDataView<Issue>( "issues", issues, ITEMS_PER_PAGE )
        {
            protected void populateItem( final Item<Issue> item )
            {
                super.populateItem( item );
                Issue issue = item.getModelObject();

                item.add( new IssuePanelRow( "issue", issue, page, hideProject, hideMilestone, false ) );
            }
        };
        inlineForm.add( dataView );

        AttributeModifier colspanModifier = new AttributeModifier( "colspan", true, new Model<Integer>()
        {
            @Override
            public Integer getObject()
            {
                int cols = 9;
                if ( hideMilestone )
                {
                    cols--;
                }
                if ( hideProject )
                {
                    cols--;
                }

                return cols;
            }
        } );

        pagingFooter = new StyledPagingNavigator( "footerPaging", dataView );
        pagingFooter.setOutputMarkupPlaceholderTag( true );
        inlineForm.add( pagingFooter.add( colspanModifier ).setVisible( issues.size() > ITEMS_PER_PAGE ) );
        pagingHeader = new StyledPagingNavigator( "headerPaging", dataView );
        pagingHeader.setOutputMarkupPlaceholderTag( true );
        inlineForm.add( pagingHeader.add( colspanModifier ).setVisible( issues.size() > ITEMS_PER_PAGE ) );

        inlineForm.add( new OrderByBorder( "orderById", "id.id", issues ) );

        inlineForm.add( quickAdd );

        inlineForm.add( new OrderByBorder( "orderBySummary", "summary", issues ) );
        inlineForm.add( new OrderByBorder( "orderByStatus", "status", issues ) );
        inlineForm.add( new OrderByBorder( "orderByPriority", "priority", issues ) );
        inlineForm.add( new OrderByBorder( "orderByOrder", "rank", issues ) );
        inlineForm.add( new OrderByBorder( "orderByAssigned", "assignee", issues ) );
        inlineForm.add( new OrderByBorder( "orderByMilestone", "milestone.name", issues ).setVisible( !hideMilestone ) );
        inlineForm.add( new OrderByBorder( "orderByProject", "id.project.id", issues ).setVisible( !hideProject ) );

        final WebMarkupContainer totalRow = new WebMarkupContainer( "totals" );
        inlineForm.add( totalRow.setVisible( issues.size() > ITEMS_PER_PAGE || timeEnabled ) );

        final WebMarkupContainer allCell = new WebMarkupContainer( "allCell" );
        totalRow.add( allCell.add( new AttributeModifier( "colspan", true, new Model<Integer>()
        {
            @Override
            public Integer getObject()
            {
                int cols = 7;
                if ( hideMilestone )
                {
                    cols--;
                }
                if ( hideProject )
                {
                    cols--;
                }

                return cols;
            }
        } ) ) );
        allCell.add( new Link( "allLink" )
        {
            @Override
            public void onClick()
            {
                dataView.setItemsPerPage( Integer.MAX_VALUE );

                setVisible( false );
                totalRow.setVisible( timeEnabled );
                pagingFooter.setVisible( false );
                pagingHeader.setVisible( false );
            }
        }.setVisible( issues.size() > ITEMS_PER_PAGE ) );

        totalRow.add( new WebMarkupContainer( "requiredlabel" ).setVisible( timeEnabled ) );
        totalRow.add( new Label( "hours", new IssueTotalHoursModel( (Iterator<Issue>) issues.iterator( 0, issues.size() ),
                page.getProject() )
        {
            @Override
            public String getObject()
            {
                setIssues( issues.iterator( 0, dataView.getItemCount() ) );
// we could do this if the total is suppose to reflect the current page
//                setIssues( issues.iterator( dataView.getCurrentPage() * dataView.getItemsPerPage(), dataView.getItemsPerPage() ) );
                return super.getObject();
            }
        } ).setVisible( timeEnabled ) );
    }

    private Form<Issue> getInlineForm()
    {
        CompoundPropertyModel<Issue> formPropertyModel = new CompoundPropertyModel<Issue>( quickIssue )
        {
            @Override
            public Issue getObject()
            {
                return quickIssue;
            }
        };
        Form<Issue> inlineForm = new Form<Issue>( "IssueInlineForm", formPropertyModel )
        {
            @Override
            protected void onSubmit()
            {
                super.onSubmit();
                quickIssue.setReporter( page.getSession().getUser() );
                quickIssue.getWatchers().add( page.getSession().getUser() );
                quickIssue.setTimeRequired( quickIssue.getTimeEstimate() );
                saveIssue( quickIssue );
                //TODO: problem with dependency below
//                page.getHeadsUpApplication().addEvent( new CreateIssueEvent( quickIssue, quickIssue.getProject() ) );
                quickIssue = createIssue();
            }
        };

        Component[] rowAddComponents = new Component[9];
        rowAddComponents[0] = new WebMarkupContainer( "submit" ).setMarkupId( "add" );
        rowAddComponents[1] = new TextField<String>( "summary" ).setRequired( true ).setMarkupId( "summary" );
        rowAddComponents[2] = new Label( "status", IssueUtils.getStatusName( Issue.STATUS_NEW ) ).setMarkupId( "status" );
        rowAddComponents[3] = new IssueTypeDropDownChoice( "type", IssueUtils.getTypes() ).setRequired( true ).setMarkupId( "type" );
        rowAddComponents[4] = new TextField<Integer>( "rank", new Model<Integer>()
        {
            @Override
            public void setObject( Integer object )
            {
                quickIssue.setOrder( object );
            }

            @Override
            public Integer getObject()
            {
                if ( quickIssue.getOrder() == null || quickIssue.getOrder().equals( Issue.ORDER_NO_ORDER ) )
                {
                    return null;
                }
                return quickIssue.getOrder();
            }
        } ).setType( Integer.class ).setMarkupId( "rank" );
        rowAddComponents[5] = new UserDropDownChoice( "assignee" ).setMarkupId( "assignee" );
        rowAddComponents[6] = new Label( "project", page.getProject().toString() ).setVisible( !hideProject ).setMarkupId( "pro" );
        rowAddComponents[7] = new MilestoneDropDownChoice( "milestone", page.getProject() ).setNullValid( true ).setVisible( !hideMilestone ).setMarkupId( "milestone" );
        rowAddComponents[8] = new DurationTextField( "timeEstimate", new Model<Duration>()
        {
            @Override
            public void setObject( Duration duration )
            {
                quickIssue.setTimeEstimate( duration );
            }

            @Override
            public Duration getObject()
            {
                if ( quickIssue.getTimeEstimate().equals( new Duration( 0 ) ) )
                {
                    return null;
                }
                return quickIssue.getTimeEstimate();
            }
        } ).setType( Duration.class ).setMarkupId( "timeEstimate" );

        addAnimatorToForm( rowAddComponents );
        inlineForm.add( rowAdd );
        return inlineForm;
    }

    private void addAnimatorToForm( Component[] rowAddComponents )
    {
        User currentUser = ( (HeadsUpSession) getSession() ).getUser();
        quickAdd = new WebMarkupContainer( "quickAdd" );
        quickAdd.add( new HeadsUpTooltip( "Quick-add an issue" ) );
        quickAdd.setVisible( Permissions.canEditIssue( currentUser, page.getProject() ) );

        icon = new WebMarkupContainer( "icon" );

        Animator animator = new Animator();
        animator.addCssStyleSubject( new MarkupIdModel( rowAdd.setMarkupId( "rowAdd" ) ), "rowhidden", "rowshown" );


        for ( Component rowAddComponent : rowAddComponents )
        {
            rowAdd.add( rowAddComponent );
            if ( rowAddComponent.isVisible() )
            {
                animator.addCssStyleSubject( new MarkupIdModel( rowAddComponent ), "hidden", "shown" );
            }
        }
        animator.addSubject( new IAnimatorSubject()
        {
            public String getJavaScript()
            {
                return "moveIconBackground";
            }
        } );
        animator.attachTo( quickAdd, "onclick", Animator.Action.toggle() );
        quickAdd.add( icon );

    }

    private Issue createIssue()
    {
        Issue issue = new Issue( page.getProject() );
        issue.setTimeEstimate( new Duration( 0, Duration.UNIT_HOURS ) );
        issue.setMilestone( milestone );
        return issue;
    }

    private void saveIssue( Issue issue )
    {
        Session session = ( (HibernateStorage) Manager.getStorageInstance() ).getHibernateSession();
        Transaction tx = session.beginTransaction();
        session.save( issue );
        tx.commit();
    }
}
