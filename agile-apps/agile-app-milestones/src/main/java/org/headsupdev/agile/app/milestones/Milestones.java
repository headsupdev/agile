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

package org.headsupdev.agile.app.milestones;

import org.headsupdev.agile.storage.DurationWorkedUtil;
import org.headsupdev.agile.web.HeadsUpSession;
import org.headsupdev.agile.web.components.*;
import org.headsupdev.agile.web.wicket.SortableEntityProvider;
import org.headsupdev.agile.web.wicket.StyledPagingNavigator;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.PageParameters;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.OrderByBorder;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.headsupdev.agile.security.permission.ProjectListPermission;
import org.headsupdev.agile.web.HeadsUpPage;
import org.headsupdev.agile.api.Permission;
import org.headsupdev.agile.storage.StoredProject;
import org.headsupdev.agile.storage.issues.Milestone;
import org.headsupdev.agile.app.milestones.permission.MilestoneListPermission;
import org.apache.wicket.model.Model;

/**
 * Milestones home page
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class Milestones
    extends HeadsUpPage
{
    private static final int ITEMS_PER_PAGE = 25;
    private StyledPagingNavigator pagingHeader, pagingFooter;

    public Permission getRequiredPermission() {
        return new MilestoneListPermission();
    }

    public void layout()
    {
        super.layout();

        if ( getProject().equals( StoredProject.getDefault() ) )
        {
            requirePermission( new ProjectListPermission() );
        }
        add( CSSPackageResource.getHeaderContribution( getClass(), "milestone.css" ) );

        MilestoneFilterPanel filter = new MilestoneFilterPanel( "filter", getSession().getUser() );
        add( filter );

        final boolean hideProject = !getProject().equals( StoredProject.getDefault() );

        final SortableEntityProvider<Milestone> provider;
        if ( getProject().equals( StoredProject.getDefault() ) )
        {
            provider = MilestonesApplication.getMilestoneProvider( filter );
        }
        else
        {
            provider = MilestonesApplication.getMilestoneProviderForProject( getProject(), filter );
        }

        final DataView dataView;
        add( dataView = new StripedDataView<Milestone>( "milestones", provider, ITEMS_PER_PAGE )
        {
            protected void populateItem( Item<Milestone> listItem )
            {
                super.populateItem( listItem );

                Milestone milestone = listItem.getModelObject();
                PageParameters params = new PageParameters();
                params.add( "project", milestone.getProject().getId() );
                params.add( "id", milestone.getName() );

                WebMarkupContainer cell = new WebMarkupContainer( "id-cell" );
                Link idLink = new BookmarkablePageLink( "id-link", getPageClass( "milestones/view" ), params );
                idLink.add( new Label( "id-label", milestone.getName() ) );
                cell.add( idLink );
                listItem.add( cell );

                double part = DurationWorkedUtil.getMilestoneCompleteness(milestone);
                int percent = (int) ( part * 100 );
                Panel panel = new PercentagePanel( "bar", percent );
                listItem.add( panel );

                int total = milestone.getIssues().size();
                int open = milestone.getOpenIssues().size();
                Label label = new Label( "issues", String.valueOf( total ) );
                listItem.add( label );

                label = new Label( "open", String.valueOf( open ) );
                listItem.add( label );

                label = new Label( "project", milestone.getProject().toString() );
                listItem.add( label.setVisible( !hideProject ) );
                label = new Label( "due", new FormattedDateModel( milestone.getDueDate(),
                        ( (HeadsUpSession) getSession() ).getTimeZone() ) );
                label.add( new MilestoneStatusModifier( "due", milestone ) );
                listItem.add( label );
            }
        } );

        AttributeModifier colspanModifier = new AttributeModifier( "colspan", true, new Model<Integer>()
        {
            @Override
            public Integer getObject()
            {
                int cols = 6;
                if ( hideProject )
                {
                    cols --;
                }

                return cols;
            }
        } );

        add( new OrderByBorder( "orderByName", "name.name", provider ) );
        add( new OrderByBorder( "orderByDue", "due", provider ) );
        add( new OrderByBorder( "orderByProject", "name.project.id", provider ).setVisible( !hideProject ) );

        pagingFooter = new StyledPagingNavigator( "footerPaging", dataView );
        pagingFooter.setOutputMarkupPlaceholderTag( true );
        add( pagingFooter.add( colspanModifier ) );
        pagingHeader = new StyledPagingNavigator( "headerPaging", dataView );
        pagingHeader.setOutputMarkupPlaceholderTag( true );
        add( pagingHeader.add( colspanModifier ) );

        final WebMarkupContainer allCell = new WebMarkupContainer( "allCell" );
        add( allCell.add( colspanModifier ).setVisible( provider.size() > ITEMS_PER_PAGE ) );
        allCell.add( new Link( "allLink" )
        {
            @Override
            public void onClick()
            {
                dataView.setItemsPerPage( Integer.MAX_VALUE );

                setVisible( false );
                allCell.setVisible( false );
                pagingFooter.setVisible( false );
                pagingHeader.setVisible( false );
            }
        } );
    }

    @Override
    public String getTitle()
    {
        return null;
    }
}
