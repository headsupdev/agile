/*
 * HeadsUp Agile
 * Copyright 2009-2013 Heads Up Development Ltd.
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

package org.headsupdev.agile.app.history;

import org.headsupdev.agile.api.*;
import org.headsupdev.agile.api.rest.Api;
import org.headsupdev.agile.app.history.rest.ActivityApi;
import org.headsupdev.agile.web.WebApplication;
import org.headsupdev.agile.storage.HibernateStorage;
import org.headsupdev.agile.app.history.permission.HistoryViewPermission;
import org.headsupdev.agile.app.history.feed.HistoryFeed;

import java.util.List;
import java.util.LinkedList;
import java.util.Date;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.Query;

/**
 * The history application shows a filtered history view
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class HistoryApplication
    extends WebApplication
{
    List<MenuLink> links;

    public HistoryApplication()
    {
        links = new LinkedList<MenuLink>();
    }

    public String getName()
    {
        return "Activity";
    }

    public String getApplicationId()
    {
        return "activity";
    }

    public String getDescription()
    {
        return "The " + Manager.getStorageInstance().getGlobalConfiguration().getProductName() + " activity application";
    }

    public List<MenuLink> getLinks( Project project )
    {
        return links;
    }

    @Override
    public Class<? extends Page>[] getPages()
    {
        return new Class[]{ History.class, ShowEvent.class, ShowEventBody.class, GroupedActivity.class,
                HistoryFeed.class };
    }


    @Override
    public Class<? extends Api>[] getApis()
    {
        return new Class[]{ ActivityApi.class };
    }

    @Override
    public Class<? extends Page> getHomePage()
    {
        return History.class;
    }

    @Override
    public Permission[] getPermissions() {
        return new Permission[] { new HistoryViewPermission() };
    }

    public static Event getEvent( long id )
    {
        Session session = ( (HibernateStorage) Manager.getStorageInstance() ).getHibernateSession();

        Transaction tx = session.beginTransaction();
        Query q = session.createQuery( "from StoredEvent e where e.id = " + id );
        Event ret = (Event) q.uniqueResult();
        tx.commit();

        return ret;
    }

    public List<Event> getEvents( long before, List<String> types, int count )
    {
        if ( types == null || types.size() == 0 )
        {
            return new LinkedList<Event>();
        }

        Session session = ( (HibernateStorage) Manager.getStorageInstance() ).getHibernateSession();

        Query q = session.createQuery( "from StoredEvent e where e.class in (:types) and e.time < :before order by time desc" );
        q.setParameterList( "types", types );
        q.setTimestamp( "before", new Date( before ) );
        q.setMaxResults( count );
        return q.list();
    }

    public List<Event> getEventsForProject( Project project, long before, List<String> types, int count )
    {
        if ( types == null || types.size() == 0 )
        {
            return new LinkedList<Event>();
        }

        Session session = ( (HibernateStorage) Manager.getStorageInstance() ).getHibernateSession();

        Query q = session.createQuery( "from StoredEvent e where e.class in (:types) and project.id = :pid and e.time < :before order by time desc" );
        q.setParameterList( "types", types );
        q.setString( "pid", project.getId() );
        q.setTimestamp( "before", new Date( before ) );
        q.setMaxResults( count );
        return q.list();
    }
}