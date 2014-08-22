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

package org.headsupdev.agile.app.issues;

import org.headsupdev.agile.api.*;
import org.headsupdev.agile.api.rest.Api;
import org.headsupdev.agile.app.issues.dao.SortableIssuesProvider;
import org.headsupdev.agile.app.issues.event.CloseIssueEvent;
import org.headsupdev.agile.app.issues.event.CommentEvent;
import org.headsupdev.agile.app.issues.event.ProgressEvent;
import org.headsupdev.agile.app.issues.event.CreateIssueEvent;
import org.headsupdev.agile.app.issues.event.UpdateIssueEvent;
import org.headsupdev.agile.app.issues.permission.IssueEditPermission;
import org.headsupdev.agile.app.issues.permission.IssueListPermission;
import org.headsupdev.agile.app.issues.permission.IssueViewPermission;
import org.headsupdev.agile.app.issues.rest.IssuesApi;
import org.headsupdev.agile.storage.Comment;
import org.headsupdev.agile.storage.HibernateStorage;
import org.headsupdev.agile.storage.issues.Issue;
import org.headsupdev.agile.storage.resource.DurationWorked;
import org.headsupdev.agile.web.WebApplication;
import org.headsupdev.agile.web.components.issues.IssueFilterPanel;
import org.headsupdev.agile.web.wicket.SortableEntityProvider;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

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
        eventTypes.add( "comment" );
        eventTypes.add( "progress" );
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

    public List<String> getEventTypes()
    {
        return eventTypes;
    }

    @Override
    public Class<? extends Page>[] getPages()
    {
        return new Class[]{AssignIssue.class, CloseIssue.class, CreateAttachment.class, CreateComment.class,
                CreateIssue.class, CreateRelationship.class, EditIssue.class, EditComment.class, Issues.class, ReopenIssue.class,
                ResolveIssue.class, ProgressIssue.class, EditProgressIssue.class, ViewIssue.class};
    }

    @Override
    public Class<? extends Page> getHomePage()
    {
        return Issues.class;
    }

    @Override
    public Class<? extends Api>[] getApis()
    {
        return new Class[]{IssuesApi.class};
    }

    @Override
    public Permission[] getPermissions()
    {
        return new Permission[]{new IssueEditPermission(), new IssueListPermission(), new IssueViewPermission()};
    }

    @Override
    public LinkProvider[] getLinkProviders()
    {
        return new LinkProvider[]{new IssueLinkProvider()};
    }

    public static SortableEntityProvider<Issue> getIssueProvider( final IssueFilterPanel filter )
    {
        return new SortableIssuesProvider( filter );
    }

    public static SortableEntityProvider<Issue> getIssueProviderForProject( final Project project,
                                                                            final IssueFilterPanel filter )
    {
        return new SortableIssuesProvider( filter )
        {
            @Override
            protected Criteria createCriteria()
            {
                Criteria c = super.createCriteria();

                c.add( Restrictions.eq( "id.project", project ) );
                return c;
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

    public static DurationWorked getDurationWorked( long id )
    {
        Session session = ( (HibernateStorage) Manager.getStorageInstance() ).getHibernateSession();

        Query q = session.createQuery( "from DurationWorked c where id.id = :id" );
        q.setLong( "id", id );
        return (DurationWorked) q.uniqueResult();
    }

    public void addIssue( Issue issue )
    {
        Session session = ( (HibernateStorage) Manager.getStorageInstance() ).getHibernateSession();

        Transaction tx = session.beginTransaction();
        session.save( issue );
        tx.commit();
    }

    public Class[] getPersistantClasses()
    {
        return new Class[]{CloseIssueEvent.class, CreateIssueEvent.class, UpdateIssueEvent.class, CommentEvent.class, ProgressEvent.class};
    }
}