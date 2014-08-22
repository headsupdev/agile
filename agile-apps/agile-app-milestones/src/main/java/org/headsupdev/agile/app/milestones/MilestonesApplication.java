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

import org.headsupdev.agile.api.*;
import org.headsupdev.agile.api.rest.Api;
import org.headsupdev.agile.app.milestones.event.*;
import org.headsupdev.agile.app.milestones.rest.MilestonesApi;
import org.headsupdev.agile.storage.Comment;
import org.headsupdev.agile.storage.StoredProject;
import org.headsupdev.agile.storage.issues.Milestone;
import org.headsupdev.agile.storage.HibernateStorage;
import org.headsupdev.agile.storage.issues.MilestoneGroup;
import org.headsupdev.agile.web.WebApplication;
import org.headsupdev.agile.app.milestones.permission.MilestoneViewPermission;
import org.headsupdev.agile.app.milestones.permission.MilestoneListPermission;
import org.headsupdev.agile.app.milestones.permission.MilestoneEditPermission;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
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
    public Class<? extends Api>[] getApis()
    {
        return new Class[]{ MilestonesApi.class };
    }

    @Override
    public Class<? extends Page>[] getPages() {
        return new Class[]{ CompleteMilestone.class, CreateComment.class, EditComment.class, CreateMilestone.class, EditMilestone.class,
                Milestones.class, ViewMilestone.class, CreateMilestoneGroup.class, EditMilestoneGroup.class,
                ViewMilestoneGroup.class, ReopenMilestone.class };
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
        return new LinkProvider[]{ new MilestoneLinkProvider(), new MilestoneGroupLinkProvider() };
    }

    public static List<MilestoneGroup> getMilestoneGroups( Project project, MilestoneFilter filter )
    {
        Session session = ( (HibernateStorage) Manager.getStorageInstance() ).getHibernateSession();

        Criteria c = session.createCriteria( MilestoneGroup.class );
        c.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );

        Criterion completed = filter.getCompletedCriterion();
        if ( completed != null )
        {
            c.add( completed );
        }

        if ( project != null && !project.equals( StoredProject.getDefault() ) )
        {
            c.add( Restrictions.eq( "name.project", project) );
        }

        return (List<MilestoneGroup>) c.list();
    }
    public static Comment getComment( long id )
    {
        Session session = ( (HibernateStorage) Manager.getStorageInstance() ).getHibernateSession();

        Query q = session.createQuery( "from Comment c where id.id = :id" );
        q.setLong( "id", id );
        return (Comment) q.uniqueResult();
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