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

import org.headsupdev.agile.api.*;
import org.headsupdev.agile.app.milestones.event.*;
import org.headsupdev.agile.storage.issues.Milestone;
import org.headsupdev.agile.storage.HibernateStorage;
import org.headsupdev.agile.storage.issues.MilestoneComparator;
import org.headsupdev.agile.storage.issues.MilestoneGroup;
import org.headsupdev.agile.web.WebApplication;
import org.headsupdev.agile.app.milestones.permission.MilestoneViewPermission;
import org.headsupdev.agile.app.milestones.permission.MilestoneListPermission;
import org.headsupdev.agile.app.milestones.permission.MilestoneEditPermission;
import org.headsupdev.agile.web.wicket.SortableEntityProvider;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.Query;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import java.util.*;

/**
 * The application descriptor for the milestones application
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class MilestonesApplication
    extends WebApplication
{
    public static final String ID = "milestones";

    public static final int QUERY_DUE_ALL = 0;
    public static final int QUERY_DUE_DEFINED = 1;
    public static final int QUERY_DUE_SOON = 2;
    public static final int QUERY_DUE_OVERDUE = 3;

    List<MenuLink> links;
    List<String> eventTypes;

    public MilestonesApplication()
    {
        links = new LinkedList<MenuLink>();
        links.add( new SimpleMenuLink( "create" ) );
        links.add( new SimpleMenuLink( "creategroup" ) );

        eventTypes = new LinkedList<String>();
        eventTypes.add( "completemilestone" );
        eventTypes.add( "createmilestone" );
        eventTypes.add( "updatemilestone" );
        eventTypes.add( "createmilestonegroup" );
        eventTypes.add( "updatemilestonegroup" );
    }

    public String getName()
    {
        return "Milestones";
    }

    public String getApplicationId()
    {
        return ID;
    }

    public String getDescription()
    {
        return "The " + Manager.getStorageInstance().getGlobalConfiguration().getProductName() + " milestones application";
    }

    public List<MenuLink> getLinks( Project project )
    {
        return links;
    }

    public List<String> getEventTypes() {
        return eventTypes;
    }

    @Override
    public Class<? extends Page> getHomePage() {
        return Milestones.class;
    }

    @Override
    public Class<? extends Page>[] getPages() {
        return new Class[]{ CompleteMilestone.class, CreateComment.class, CreateMilestone.class, EditMilestone.class,
                Milestones.class, ViewMilestone.class, CreateMilestoneGroup.class, EditMilestoneGroup.class,
                ViewMilestoneGroup.class };
    }

    @Override
    public Class[] getResources()
    {
        return new Class[]{ ExportDurationWorked.class, BurndownGraph.class, GroupBurndownGraph.class };
    }

    @Override
    public Permission[] getPermissions() {
        return new Permission[]{ new MilestoneEditPermission(), new MilestoneListPermission(),
            new MilestoneViewPermission() };
    }

    @Override
    public LinkProvider[] getLinkProviders()
    {
        return new LinkProvider[]{ new MilestoneLinkProvider() };
    }

    public static SortableEntityProvider<Milestone> getMilestoneProvider( final MilestoneFilter filter )
    {
        return new SortableEntityProvider<Milestone>() {
            @Override
            protected Criteria createCriteria() {
                Session session = ( (HibernateStorage) Manager.getStorageInstance() ).getHibernateSession();

                Criteria c = session.createCriteria( Milestone.class );
                c.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );

                Criterion completed = filter.getCompletedCriterion();
                if ( completed != null )
                {
                    c.add( completed );
                }

                Criterion dueQuery = filter.getDueCriterion();
                if ( dueQuery != null )
                {
                    c.add( dueQuery );
                }

                return c;
            }

            @Override
            protected List<Order> getDefaultOrder() {
                return Arrays.asList( Order.asc( "due" ), Order.desc( "name.name" ) );
            }

            @Override
            public String getCountProperty() {
                return "name.name";
            }

            @Override
            public Iterator<Milestone> iterator( int start, int limit )
            {
                if ( getSort() != null && !getSort().getProperty().equals( "due" ) )
                {
                    return super.iterator( start, limit );
                }

                Iterator<Milestone> iter = super.iterator( 0, size() );
                List<Milestone> all = new ArrayList<Milestone>();

                while ( iter.hasNext() )
                {
                    all.add( iter.next() );
                }

                Collections.sort( all, new MilestoneComparator( getSort() == null || getSort().isAscending() ) );
                return all.subList( start, start + limit ).iterator();
            }
        };
    }

    public static SortableEntityProvider<Milestone> getMilestoneProviderForProject( final Project project,
                                                                                    final MilestoneFilter filter )
    {
        return new SortableEntityProvider<Milestone>() {
            @Override
            protected Criteria createCriteria() {
                Session session = ( (HibernateStorage) Manager.getStorageInstance() ).getHibernateSession();

                Criteria c = session.createCriteria( Milestone.class );
                c.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );

                Criterion completed = filter.getCompletedCriterion();
                if ( completed != null )
                {
                    c.add( completed );
                }

                Criterion dueQuery = filter.getDueCriterion();
                if ( dueQuery != null )
                {
                    c.add( dueQuery );
                }

                c.add( Restrictions.eq( "name.project", project ) );
                return c;
            }

            @Override
            protected List<Order> getDefaultOrder() {
                return Arrays.asList( Order.asc( "due" ), Order.desc( "name.name" ) );
            }

            @Override
            public String getCountProperty() {
                return "name.name";
            }

            @Override
            public Iterator<Milestone> iterator( int start, int limit )
            {
                if ( getSort() != null && !getSort().getProperty().equals( "due" ) )
                {
                    return super.iterator( start, limit );
                }

                Iterator<Milestone> iter = super.iterator( 0, size() );
                List<Milestone> all = new ArrayList<Milestone>();

                while ( iter.hasNext() )
                {
                    all.add( iter.next() );
                }

                Collections.sort( all, new MilestoneComparator() );
                return all.subList( start, start + limit ).iterator();
            }
        };
    }

    public static Milestone getMilestone( String name, Project project )
    {
        Session session = ( (HibernateStorage) Manager.getStorageInstance() ).getHibernateSession();

        Query q = session.createQuery( "from Milestone m where name.name = :name and name.project.id = :pid" );
        q.setString( "name", name );
        q.setString( "pid", project.getId() );

        return (Milestone) q.uniqueResult();
    }

    public static MilestoneGroup getMilestoneGroup( String name, Project project )
    {
        Session session = ( (HibernateStorage) Manager.getStorageInstance() ).getHibernateSession();

        Query q = session.createQuery( "from MilestoneGroup g where name.name = :name and name.project.id = :pid" );
        q.setString( "name", name );
        q.setString( "pid", project.getId() );

        return (MilestoneGroup) q.uniqueResult();
    }

    public void addMilestone( Milestone milestone )
    {
        Session session = ( (HibernateStorage) Manager.getStorageInstance() ).getHibernateSession();

        Transaction tx = session.beginTransaction();
        session.save( milestone );
        tx.commit();
    }

    public void addMilestoneGroup( MilestoneGroup group )
    {
        Session session = ( (HibernateStorage) Manager.getStorageInstance() ).getHibernateSession();

        Transaction tx = session.beginTransaction();
        session.save( group );
        tx.commit();
    }

    public Class[] getPersistantClasses() {
        return new Class[] { CompleteMilestoneEvent.class, CreateMilestoneEvent.class, UpdateMilestoneEvent.class,
                CreateMilestoneGroupEvent.class, UpdateMilestoneGroupEvent.class };
    }
}