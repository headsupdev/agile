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

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.headsupdev.agile.app.milestones.entityproviders.GroupedMilestoneProvider;
import org.headsupdev.agile.storage.issues.MilestoneGroup;
import org.headsupdev.agile.web.components.PercentagePanel;
import org.headsupdev.agile.web.components.milestones.MilestoneListPanel;
import org.headsupdev.agile.web.wicket.SortableEntityProvider;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.headsupdev.agile.security.permission.ProjectListPermission;
import org.headsupdev.agile.web.HeadsUpPage;
import org.headsupdev.agile.api.Permission;
import org.headsupdev.agile.storage.StoredProject;
import org.headsupdev.agile.storage.issues.Milestone;
import org.headsupdev.agile.app.milestones.permission.MilestoneListPermission;

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

        final MilestoneFilterPanel filter = new MilestoneFilterPanel( "filter", getSession().getUser() );
        add( filter );

        final boolean hideProject = !getProject().equals( StoredProject.getDefault() );

        List<MilestoneGroup> groups = MilestonesApplication.getMilestoneGroups( getProject() );
        boolean hasGroups = groups.size() > 0;

        add( new ListView<MilestoneGroup>( "group", groups )
        {
            @Override
            protected void populateItem( ListItem<MilestoneGroup>listItem )
            {
                MilestoneGroup group = listItem.getModelObject();
                PageParameters params = getProjectPageParameters();
                params.add( "id", group.getName() );
                BookmarkablePageLink nameLink = new BookmarkablePageLink( "grouplink",
                        getPageClass( "milestones/viewgroup" ), params );
                nameLink.add( new Label( "name", group.getName() ) );
                listItem.add( nameLink );

                double part = group.getCompleteness();
                int percent = (int) ( part * 100 );
                Panel panel = new PercentagePanel( "bar", percent );
                listItem.add( panel );

                SortableEntityProvider<Milestone> provider = new GroupedMilestoneProvider( group, filter );
                listItem.add( new MilestoneListPanel( "milestones", provider, Milestones.this, hideProject ) );
            }
        } );

        final SortableEntityProvider<Milestone> provider;
        if ( getProject().equals( StoredProject.getDefault() ) )
        {
            provider = new GroupedMilestoneProvider( null, filter );
        }
        else
        {
            provider = new GroupedMilestoneProvider( null, getProject(), filter );
        }

        boolean hasUngrouped = provider.size() > 0;
        add( new WebMarkupContainer( "ungrouped" ).setVisible( hasUngrouped && hasGroups ) );
        add( new MilestoneListPanel( "milestones", provider, this, hideProject ).setVisible( hasUngrouped ) );
    }

    @Override
    public String getTitle()
    {
        return null;
    }
}
