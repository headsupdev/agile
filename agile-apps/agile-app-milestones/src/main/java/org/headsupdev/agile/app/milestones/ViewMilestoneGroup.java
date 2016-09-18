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

import org.apache.wicket.PageParameters;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.util.value.ValueMap;
import org.headsupdev.agile.api.MenuLink;
import org.headsupdev.agile.api.Permission;
import org.headsupdev.agile.storage.dao.MilestoneGroupsDAO;
import org.headsupdev.agile.app.milestones.entityproviders.MilestoneProvider;
import org.headsupdev.agile.app.milestones.permission.MilestoneViewPermission;
import org.headsupdev.agile.storage.Comment;
import org.headsupdev.agile.storage.StoredProject;
import org.headsupdev.agile.storage.issues.Milestone;
import org.headsupdev.agile.storage.issues.MilestoneGroup;
import org.headsupdev.agile.web.*;
import org.headsupdev.agile.web.components.FormattedDateModel;
import org.headsupdev.agile.web.components.MarkedUpTextModel;
import org.headsupdev.agile.web.components.milestones.MilestoneListPanel;
import org.headsupdev.agile.web.wicket.SortableEntityProvider;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import java.util.*;

/**
 * Milestone Group view page
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 2.0
 */
@MountPoint("viewgroup")
public class ViewMilestoneGroup
        extends HeadsUpPage
{
    private MilestoneGroupsDAO dao = new MilestoneGroupsDAO();

    private MilestoneGroup group;
    private HeadsUpPage page;
    private MilestoneFilterPanel filter;

    public Permission getRequiredPermission()
    {
        return new MilestoneViewPermission();
    }

    public void layout()
    {
        super.layout();
        page = this;
        add( CSSPackageResource.getHeaderContribution( getClass(), "milestone.css" ) );

        String name = getPageParameters().getString( "id" );

        group = dao.find(name, getProject());
        if ( group == null )
        {
            notFoundError();
            return;
        }

        addLinks( getLinks( group ) );
        addDetails();

        List<Comment> commentList = new LinkedList<Comment>();
        commentList.addAll( group.getComments() );
        Collections.sort( commentList, new Comparator<Comment>()
        {
            public int compare( Comment comment1, Comment comment2 )
            {
                return comment1.getCreated().compareTo( comment2.getCreated() );
            }
        } );
        add( new ListView<Comment>( "comments", commentList )
        {
            protected void populateItem( ListItem<Comment> listItem )
            {
                Comment comment = listItem.getModelObject();
                listItem.add( new Image( "icon", new ResourceReference( HeadsUpPage.class, "images/comment.png" ) ) );
                listItem.add( new Label( "username", comment.getUser().getFullnameOrUsername() ) );
                listItem.add( new Label( "created", new FormattedDateModel( comment.getCreated(),
                        ( (HeadsUpSession) getSession() ).getTimeZone() ) ) );

                listItem.add( new Label( "comment", new MarkedUpTextModel( comment.getComment(), getProject() ) )
                        .setEscapeModelStrings( false ) );
            }
        } );

        filter = new MilestoneFilterPanel( "filter", getFilterButton(), getSession().getUser() )
        {
            @Override
            public Criterion getCompletedCriterion()
            {
                Criterion c = super.getCompletedCriterion();

                if ( c == null )
                {
                    c = Restrictions.eq( "group", group );
                }
                else
                {
                    c = Restrictions.and( c, Restrictions.eq( "group", group ) );
                }

                return c;
            }

            @Override
            public void invalidDatePeriod()
            {
                warn( "Invalid date period" );
            }
        };
        if ( group.isCompleted() )
        {
            filter.setFilters(0, false, true);
        }
        else
        {
            filter.setFilters(0, true, false);
        }
        add( filter );

        boolean hideProject = true;
        final SortableEntityProvider<Milestone> provider;
        if ( getProject().equals( StoredProject.getDefault() ) )
        {
            provider = new MilestoneProvider( filter );
            hideProject = false;
        }
        else
        {
            provider = new MilestoneProvider( getProject(), filter );
        }

        add( new MilestoneListPanel( "milestonelist", provider, this, hideProject, group ) );

        boolean timeEnabled = Boolean.parseBoolean( group.getProject().getConfigurationValue(
                StoredProject.CONFIGURATION_TIMETRACKING_ENABLED ) ) && group.hasValidTimePeriod();
        add( new Image( "graph", new ResourceReference( "groupburndown.png" ), getPageParameters() ).setVisible( timeEnabled ) );
        add( new WorkRemainingTable( "table", group ).setVisible( timeEnabled ) );

        ValueMap params = new ValueMap();
        params.put( "project", getProject().getId() );
        params.put( "groupId", group.getName() );
        params.put( "silent", true );
        add( new ResourceLink( "exportgroup", new ResourceReference( "export-worked.csv" ), params ).setVisible( timeEnabled ) );
    }

    public MilestoneGroup getMilestoneGroup()
    {
        return group;
    }

    @Override
    public String getPageTitle()
    {
        return "Milestonegroup:" + group.getName() + PAGE_TITLE_SEPARATOR + super.getPageTitle();
    }

    protected void addDetails()
    {
        add( new MilestoneGroupPanel( "group", group ) );
    }

    public static List<MenuLink> getLinks( MilestoneGroup group )
    {
        List<MenuLink> links = new LinkedList<MenuLink>();
        PageParameters addMilestoneParams = new PageParameters();
        addMilestoneParams.add( "project", group.getProject().getId() );
        addMilestoneParams.add( "group", group.getName() );

        PageParameters pageParams = new PageParameters();
        pageParams.add( "project", group.getProject().getId() );
        pageParams.add( "id", group.getName() );

        links.add( new BookmarkableMenuLink( RenderUtil.getPageClass( "milestones/create" ), addMilestoneParams, "add milestone" ) );
        links.add( new BookmarkableMenuLink( RenderUtil.getPageClass( "milestones/editgroup" ), pageParams, "edit" ) );
        if ( group.getCompletedDate() == null )
        {
//            links.add( new BookmarkableMenuLink( RenderUtil.getPageClass( "milestones/commentgroup" ), pageParams, "comment" ) );
        }

        return links;
    }

    @Override
    public boolean hasFilter()
    {
        return true;
    }
}

