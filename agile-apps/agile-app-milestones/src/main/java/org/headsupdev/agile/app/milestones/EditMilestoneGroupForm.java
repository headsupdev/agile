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
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.app.milestones.entityproviders.MilestoneProvider;
import org.headsupdev.agile.storage.DurationWorkedUtil;
import org.headsupdev.agile.storage.HibernateUtil;
import org.headsupdev.agile.storage.StoredProject;
import org.headsupdev.agile.storage.issues.Milestone;
import org.headsupdev.agile.storage.issues.MilestoneGroup;
import org.headsupdev.agile.web.HeadsUpPage;
import org.headsupdev.agile.web.HeadsUpSession;
import org.headsupdev.agile.web.components.*;
import org.headsupdev.agile.web.wicket.SortableEntityProvider;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * The form used when editing / creating a milestone group
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class EditMilestoneGroupForm
        extends Panel
{
    public EditMilestoneGroupForm( String id, final MilestoneGroup group, boolean creating, HeadsUpPage owner)
    {
        super( id );

        add( new MilestoneGroupForm( "edit", group, creating, owner, this ) );
    }

    public void onSubmit()
    {
        // allow others to override
    }
}

class MilestoneGroupForm
        extends Form<MilestoneGroup>
{
    MilestoneGroup group;
    HeadsUpPage owner;
    EditMilestoneGroupForm parent;
    boolean creating;
    private List<Milestone> milestones;

    public MilestoneGroupForm( String id, final MilestoneGroup group, boolean creating, final HeadsUpPage owner, EditMilestoneGroupForm parent )
    {
        super( id );
        this.group = group;
        this.creating = creating;
        this.owner = owner;
        this.parent = parent;
        setModel( new CompoundPropertyModel<MilestoneGroup>( group ) );
        milestones = getMilestones( group.getProject() );

        add( new Label( "project", group.getProject().getAlias() ) );
        if ( creating )
        {
            add( new TextField<String>( "name" ).add( new IdPatternValidator() ).setRequired( true ) );
            add( new WebMarkupContainer( "name-label" ).setVisible( false ) );
//            add( new Label( "created", new FormattedDateModel( new Date() ) ) );
        }
        else
        {
            add( new Label( "name-label", group.getName() ) );
            add( new WebMarkupContainer( "name" ).setVisible( false ) );
//            add( new Label( "created", new FormattedDateModel( milestone.getCreated() ) ) );
        }

        add( new TextArea( "description" ) );

        CheckGroup<Milestone> checkGroup = new CheckGroup<Milestone>( "milestones" );
        add( checkGroup );

        checkGroup.add( new StripedListView<Milestone>( "list", milestones )
        {
            protected void populateItem( ListItem<Milestone> listItem )
            {
                super.populateItem( listItem );
                Milestone milestone = listItem.getModelObject();
                listItem.add( new Label( "id", milestone.getName() ) );
                listItem.add( new Check<Milestone>( "check", listItem.getModel() ) );

                double part = DurationWorkedUtil.getMilestoneCompleteness( milestone );
                int percent = (int) ( part * 100 );
                Panel panel = new PercentagePanel( "bar", percent );
                listItem.add( panel );

                int total = milestone.getIssues().size();
                int open = milestone.getOpenIssues().size();
                Label label = new Label( "issues", String.valueOf( total ) );
                listItem.add( label );

                // TODO display project if "all" project for group
                label = new Label( "open", String.valueOf( open ) );
                listItem.add( label );

                label = new Label( "due", new FormattedDateModel( milestone.getDueDate(),
                        ( (HeadsUpSession) getSession() ).getTimeZone() ) );
                label.add( new org.headsupdev.agile.web.components.milestones.MilestoneStatusModifier( "due", milestone ) );
                listItem.add( label );
            }
        } );

    }

    public void onSubmit()
    {
        Session session = HibernateUtil.getCurrentSession();

        for ( Milestone milestone : milestones )
        {
            if ( group.getMilestones().contains( milestone ) )
            {
                milestone.setGroup( group );
            }
            else
            {
                milestone.setGroup( null );
            }

            session.merge( milestone );
        }

        if ( !creating )
        {
            group = (MilestoneGroup) session.merge( group );
        }
        group.setUpdated( new Date() );

        parent.onSubmit();

        PageParameters params = new PageParameters();
        params.add( "project", group.getProject().getId() );
        params.add( "id", group.getName() );
        setResponsePage( owner.getPageClass( "milestones/viewgroup" ), params );
    }

    private List<Milestone> getMilestones( Project project )
    {
        SortableEntityProvider<Milestone> milestoneProvider = getMilestoneProvider( project );
        List<Milestone> ret = new ArrayList<Milestone>( milestoneProvider.size() );

        Iterator<Milestone> iterator = milestoneProvider.iterator( 0, milestoneProvider.size() );
        while ( iterator.hasNext() )
        {
            ret.add( iterator.next() );
        }

        return ret;
    }

    private SortableEntityProvider<Milestone> getMilestoneProvider( Project project )
    {
        MilestoneFilter filter = new MilestoneFilter()
        {
            public Criterion getCompletedCriterion()
            {
                return Restrictions.isNull( "completed" );
            }

            public Criterion getDueCriterion()
            {
                if ( creating )
                {
                    return Restrictions.isNull( "group" );
                }

                return Restrictions.or( Restrictions.isNull( "group" ), Restrictions.eq( "group", group ) );
            }
        };
        if ( project.equals( StoredProject.getDefault() ) )
        {
            return new MilestoneProvider( filter );
        }
        else
        {
            return new MilestoneProvider( project, filter );
        }
    }
}