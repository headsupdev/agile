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

import org.headsupdev.agile.storage.StoredProject;
import org.headsupdev.agile.web.components.StripedDataView;
import org.headsupdev.agile.web.wicket.StyledPagingNavigator;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.OrderByBorder;
import org.headsupdev.agile.storage.issues.Issue;
import org.headsupdev.agile.web.HeadsUpPage;
import org.apache.wicket.model.Model;

import java.util.Iterator;

/**
 * A panel that displays a formatted, coloured list of the issues passed in
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class IssueListPanel extends Panel
{
    private static final int ITEMS_PER_PAGE = 25;
    private StyledPagingNavigator pagingHeader, pagingFooter;

    public IssueListPanel( String id, final SortableDataProvider<Issue> issues, final HeadsUpPage page, final boolean hideProject,
                           final boolean hideMilestone )
    {
        super( id );
        add( CSSPackageResource.getHeaderContribution( getClass(), "issue.css" ) );

        final boolean timeEnabled = Boolean.parseBoolean( page.getProject().getConfigurationValue(
                StoredProject.CONFIGURATION_TIMETRACKING_ENABLED ) );

        add( new WebMarkupContainer( "hours-header" ).setVisible( timeEnabled ) );
        final DataView dataView = new StripedDataView<Issue>( "issues", issues, ITEMS_PER_PAGE )
        {
            protected void populateItem( final Item<Issue> item )
            {
                super.populateItem( item );
                Issue issue = item.getModelObject();

                item.add( new IssuePanelRow( "issue", issue, page, hideProject, hideMilestone, false ) );
            }
        };
        add( dataView );
        
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
                    cols --;
                }

                return cols;
            }
        } );

        pagingFooter = new StyledPagingNavigator( "footerPaging", dataView );
        pagingFooter.setOutputMarkupPlaceholderTag( true );
        add( pagingFooter.add( colspanModifier ).setVisible( issues.size() > ITEMS_PER_PAGE ) );
        pagingHeader = new StyledPagingNavigator( "headerPaging", dataView );
        pagingHeader.setOutputMarkupPlaceholderTag( true );
        add( pagingHeader.add( colspanModifier ).setVisible( issues.size() > ITEMS_PER_PAGE ) );

        add( new OrderByBorder( "orderById", "id.id", issues ) );
        add( new OrderByBorder( "orderBySummary", "summary", issues ) );
        add( new OrderByBorder( "orderByStatus", "status", issues ) );
        add( new OrderByBorder( "orderByPriority", "priority", issues ) );
        add( new OrderByBorder( "orderByOrder", "rank", issues ) );
        add( new OrderByBorder( "orderByAssigned", "assignee", issues ) );
        add( new OrderByBorder( "orderByMilestone", "milestone.name", issues ).setVisible( !hideMilestone ) );
        add( new OrderByBorder( "orderByProject", "id.project.id", issues ).setVisible( !hideProject ) );

        final WebMarkupContainer totalRow = new WebMarkupContainer( "totals" );
        add( totalRow.setVisible( issues.size() > ITEMS_PER_PAGE || timeEnabled ) );

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
                    cols --;
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
        }).setVisible( timeEnabled ) );
    }
}
