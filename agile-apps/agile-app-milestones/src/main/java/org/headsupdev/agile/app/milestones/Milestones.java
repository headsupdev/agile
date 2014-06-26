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

package org.headsupdev.agile.app.milestones;

import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.headsupdev.agile.api.Permission;
import org.headsupdev.agile.app.milestones.entityproviders.GroupedMilestoneProvider;
import org.headsupdev.agile.app.milestones.entityproviders.MilestoneGroupProvider;
import org.headsupdev.agile.app.milestones.permission.MilestoneListPermission;
import org.headsupdev.agile.security.permission.ProjectListPermission;
import org.headsupdev.agile.storage.StoredProject;
import org.headsupdev.agile.storage.issues.Milestone;
import org.headsupdev.agile.storage.issues.MilestoneGroup;
import org.headsupdev.agile.web.HeadsUpPage;
import org.headsupdev.agile.web.components.PercentagePanel;
import org.headsupdev.agile.web.components.StripedDataView;
import org.headsupdev.agile.web.components.milestones.MilestoneListPanel;
import org.headsupdev.agile.web.wicket.SortableEntityProvider;

import java.util.List;

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
    private SortableEntityProvider<Milestone> ungroupedProvider;
    private boolean hasGroups = false;
    private boolean hideProject = false;

    private Component ungrouped, ungroupedMilestones;

    public Permission getRequiredPermission()
    {
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

        final MilestoneFilterPanel filter = new MilestoneFilterPanel( "filter", getSession().getUser() );
        add( filter );

        hideProject = !getProject().equals( StoredProject.getDefault() );

        SortableEntityProvider<MilestoneGroup> provider = new MilestoneGroupProvider( getProject(), filter );
        List<MilestoneGroup> groups = MilestonesApplication.getMilestoneGroups( getProject(), filter );
        hasGroups = groups.size() > 0;

        add( new StripedDataView<MilestoneGroup>( "group", provider )
        {
            @Override
            protected void populateItem( Item<MilestoneGroup> item )
            {
                MilestoneGroup group = item.getModelObject();
                PageParameters params = new PageParameters();
                params.add( "project", group.getProject().getId() );
                params.add( "id", group.getName() );
                BookmarkablePageLink nameLink = new BookmarkablePageLink( "grouplink",
                        getPageClass( "milestones/viewgroup" ), params );
                nameLink.add( new Label( "name", group.getName() ) );
                item.add( nameLink );

                double part = group.getCompleteness();
                int percent = (int) ( part * 100 );
                Panel panel = new PercentagePanel( "bar", percent );
                item.add( panel );

                SortableEntityProvider<Milestone> provider = new GroupedMilestoneProvider( group, filter );
                item.add( new MilestoneListPanel( "milestones", provider, Milestones.this, hideProject, group ) );
            }
        } );

        // TODO figure why the contents of this one does not update dynamically when those above do...
        if ( getProject().equals( StoredProject.getDefault() ) )
        {
            ungroupedProvider = new GroupedMilestoneProvider( null, filter );
        }
        else
        {
            ungroupedProvider = new GroupedMilestoneProvider( null, getProject(), filter );
        }

        add( ungrouped = new WebMarkupContainer( "ungrouped" ) );
        ungrouped.setOutputMarkupPlaceholderTag( true );
        add( ungroupedMilestones = new MilestoneListPanel( "milestones", ungroupedProvider, this, hideProject, null) );
        ungroupedMilestones.setOutputMarkupPlaceholderTag( true );
    }

    @Override
    protected void onBeforeRender()
    {
        super.onBeforeRender();

        boolean hasUngrouped = ungroupedProvider.size() > 0;
        ungrouped.setVisible( hasUngrouped && hasGroups );
        ungroupedMilestones.setVisible( hasUngrouped );
    }

    @Override
    public String getTitle()
    {
        return null;
    }
}
