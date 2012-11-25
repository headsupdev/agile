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

package org.headsupdev.agile.app.issues;

import org.headsupdev.agile.api.LinkProvider;
import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.api.MenuLink;
import org.headsupdev.agile.api.Page;
import org.headsupdev.agile.api.Permission;
import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.api.SimpleMenuLink;
import org.headsupdev.agile.app.issues.event.CloseIssueEvent;
import org.headsupdev.agile.app.issues.event.CreateIssueEvent;
import org.headsupdev.agile.app.issues.event.UpdateIssueEvent;
import org.headsupdev.agile.app.issues.permission.IssueEditPermission;
import org.headsupdev.agile.app.issues.permission.IssueListPermission;
import org.headsupdev.agile.app.issues.permission.IssueViewPermission;
import org.headsupdev.agile.storage.Comment;
import org.headsupdev.agile.storage.HibernateStorage;
import org.headsupdev.agile.storage.issues.Issue;
import org.headsupdev.agile.storage.issues.Milestone;
import org.headsupdev.agile.web.WebApplication;
import org.headsupdev.agile.web.components.issues.IssueFilterPanel;
import org.headsupdev.agile.web.wicket.SortableEntityProvider;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * The application descriptor for the issues application
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class IssuesApplication
    extends WebApplication
{
    public static final String ID = "issues";

    public static final int QUERY_ASSIGNMENT_ALL = 0;
    public static final int QUERY_ASSIGNMENT_ME = 1;
    public static final int QUERY_ASSIGNMENT_SOMEONE = 2;
    public static final int QUERY_ASSIGNMENT_NONE = 3;

    List<MenuLink> links;
    List<String> eventTypes;

    private final SimpleMenuLink menuItemCreate = new SimpleMenuLink( "create" );

    public IssuesApplication()
    {
        links = new LinkedList<MenuLink>();
        links.add( menuItemCreate );

        eventTypes = new LinkedList<String>();
        eventTypes.add( "closeissue" );
        eventTypes.add( "createissue" );
        eventTypes.add( "updateissue" );
    }

    public SimpleMenuLink getMenuItemCreate()
    {
        return menuItemCreate;
    }

    public String getName()
    {
        return "Issues";
    }

    public String getApplicationId()
    {
        return ID;
    }

    public String getDescription()
    {
        return "The " + Manager.getStorageInstance().getGlobalConfiguration().getProductName() + " issues application";
    }

    public List<MenuLink> getLinks( Project project )
    {
        return links;
    }

    public List<String> getEventTypes() {
        return eventTypes;
    }

    @Override
    public Class<? extends Page>[] getPages() {
        return new Class[]{ AssignIssue.class, CloseIssue.class, CreateAttachment.class, CreateComment.class,
            CreateIssue.class, CreateRelationship.class, EditIssue.class, Issues.class, ReopenIssue.class,
            ResolveIssue.class, BeginIssue.class, ProgressIssue.class, ViewIssue.class };
    }

    @Override
    public Class<? extends Page> getHomePage() {
        return Issues.class;
    }

    @Override
    public Permission[] getPermissions() {
        return new Permission[]{ new IssueEditPermission(), new IssueListPermission(), new IssueViewPermission() };
    }

    @Override
    public LinkProvider[] getLinkProviders()
    {
        return new LinkProvider[]{ new IssueLinkProvider() };
    }

    public static SortableEntityProvider<Issue> getIssueProvider( final IssueFilterPanel filter )
    {
        return new SortableEntityProvider<Issue>() {
            @Override
            protected Criteria createCriteria() {
                Session session = ( (HibernateStorage) Manager.getStorageInstance() ).getHibernateSession();

                Criteria c = session.createCriteria( Issue.class );
                c.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );
                c.add( filter.getStatusCriterion() );
                Criterion assignmentRestriction = filter.getAssignmentCriterion();
                if ( assignmentRestriction != null )
                {
                    c.add( assignmentRestriction );
                }

                return c;
            }

            @Override
            protected List<Order> getDefaultOrder() {
                return Arrays.asList( Order.asc( "priority" ),
                    Order.asc( "rank" ), Order.asc( "status" ), Order.asc( "id.id" ) );
            }
        };
    }

    public static SortableEntityProvider<Issue> getIssueProviderForProject( final Project project,
                                                                            final IssueFilterPanel filter )
    {
        return new SortableEntityProvider<Issue>() {
            @Override
            protected Criteria createCriteria() {
                Session session = ( (HibernateStorage) Manager.getStorageInstance() ).getHibernateSession();

                Criteria c = session.createCriteria( Issue.class );
                c.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );
                c.add( filter.getStatusCriterion() );
                Criterion assignmentRestriction = filter.getAssignmentCriterion();
                if ( assignmentRestriction != null )
                {
                    c.add( assignmentRestriction );
                }

                c.add( Restrictions.eq( "id.project", project ) );
                return c;
            }

            @Override
            protected List<Order> getDefaultOrder() {
                return Arrays.asList( Order.asc( "priority" ),
                    Order.asc( "rank" ), Order.asc( "status" ), Order.asc( "id.id" ) );
            }
        };
    }

    public static Issue getIssue( long id, Project project )
    {
        Session session = ( (HibernateStorage) Manager.getStorageInstance() ).getHibernateSession();

        Query q = session.createQuery( "from Issue i where id.id = :id and id.project.id = :pid" );
        q.setLong( "id", id );
        q.setString( "pid", project.getId() );
        return (Issue) q.uniqueResult();
    }

    public static Comment getComment( long id )
    {
        Session session = ( (HibernateStorage) Manager.getStorageInstance() ).getHibernateSession();

        Query q = session.createQuery( "from Comment c where id.id = :id" );
        q.setLong( "id", id );
        return (Comment) q.uniqueResult();
    }

    public void addIssue( Issue issue )
    {
        Session session = ( (HibernateStorage) Manager.getStorageInstance() ).getHibernateSession();

        Transaction tx = session.beginTransaction();
        session.save( issue );
        tx.commit();
    }

    public List<Milestone> getMilestones()
    {
        Session session = ( (HibernateStorage) Manager.getStorageInstance() ).getHibernateSession();

        Transaction tx = session.beginTransaction();
        Query q = session.createQuery( "from Milestone m where completed is null" );
        List<Milestone> list = q.list();
        tx.commit();

        return list;
    }

    public List<Milestone> getMilestonesForProjectOrParent( Project project )
    {
        StringBuffer projectIds = new StringBuffer( "(" );
        Project p = project;
        while ( p != null )
        {
            projectIds.append( "'" );
            projectIds.append( p.getId() );
            projectIds.append( "'" );

            p = p.getParent();
            if ( p != null )
            {
                projectIds.append( "," );
            }
        }
        projectIds.append( ")" );

        Session session = ( (HibernateStorage) Manager.getStorageInstance() ).getHibernateSession();

        Transaction tx = session.beginTransaction();
        Query q = session.createQuery( "from Milestone m where id.project.id in " + projectIds.toString() +
                " and completed is null" );
        List<Milestone> list = q.list();
        tx.commit();

        return list;
    }

    public List<Milestone> getMilestonesForProject( Project project )
    {
        Session session = ( (HibernateStorage) Manager.getStorageInstance() ).getHibernateSession();

        Transaction tx = session.beginTransaction();
        Query q = session.createQuery( "from Milestone m where id.project.id = :pid and completed is null" );
        q.setString( "pid", project.getId() );
        List<Milestone> list = q.list();
        tx.commit();

        return list;
    }

    public Class[] getPersistantClasses() {
        return new Class[] { CloseIssueEvent.class, CreateIssueEvent.class, UpdateIssueEvent.class };
    }
}