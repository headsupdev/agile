/*
 * HeadsUp Agile
 * Copyright 2013 Heads Up Development Ltd.
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

package org.headsupdev.agile.app.history.rest;

import org.apache.wicket.markup.html.PackageResource;
import org.headsupdev.agile.api.Event;
import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.api.Permission;
import org.headsupdev.agile.api.rest.Publish;
import org.headsupdev.agile.app.history.permission.HistoryViewPermission;
import org.headsupdev.agile.storage.HibernateStorage;
import org.headsupdev.agile.storage.StoredProject;
import org.headsupdev.agile.web.HeadsUpPage;
import org.headsupdev.agile.web.MountPoint;
import org.headsupdev.agile.web.components.MarkedUpTextModel;
import org.headsupdev.agile.web.rest.HeadsUpApi;

import org.apache.wicket.PageParameters;
import org.apache.wicket.model.Model;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A search API that provides a simple list of details for any results matching the given query.
 * <p/>
 * Created: 16/02/2013
 *
 * @author Andrew Williams
 * @since 2.0
 */
@MountPoint( "activity" )
public class ActivityApi
        extends HeadsUpApi
{
    public ActivityApi( PageParameters params )
    {
        super( params );
    }

    @Override
    public Permission getRequiredPermission()
    {
        return new HistoryViewPermission();
    }

    @Override
    public void doGet( PageParameters pageParameters )
    {
        long before;
        try
        {
            before = pageParameters.getLong( "before" );
        }
        catch ( Exception e ) // NumberFormatException or a wicket wrapped NumberFormatException
        {
            before = Long.MAX_VALUE;
        }

        Session session = ( (HibernateStorage) Manager.getStorageInstance() ).getHibernateSession();
        Transaction tx = session.beginTransaction();
        Query q = createQuery( new Date( before ), session );

        List<Event> list = q.list();
        tx.commit();

        ArrayList<Item> entries = new ArrayList<Item>();
        for ( Event event : list )
        {
            Item item = new Item();

            item.id = event.getId();
            if ( event.getProject() != null )
            {
                item.projectId = event.getProject().getId();
            }
            item.type = getClassName( event );

            item.title = event.getTitle();
            item.link = getURLForPath( "/activity/event/id/" + event.getId() );
            item.date = event.getTime();
            item.description = new MarkedUpTextModel( event.getSummary(), event.getProject() ).getObject();

            String image = "images/events/" + item.type + ".png";
            if ( !PackageResource.exists( HeadsUpPage.class, image, null, null ) ) {
                image = "images/events/StoredEvent.png";
            }
            item.icon = getURLForPath( "/resources/" + HeadsUpPage.class.getCanonicalName() + "/" + image );

            entries.add( item );
        }

        setModel( new Model<ArrayList<Item>>( entries ) );
    }

    protected Query createQuery( Date before, Session session )
    {
        Query q;
        if ( getProject().equals( StoredProject.getDefault() ) )
        {
            q = session.createQuery( "from StoredEvent e where e.time < :before order by time desc" );
            q.setTimestamp( "before", before );
        }
        else
        {
            q = session.createQuery( "from StoredEvent e where project.id = :pid and e.time < :before order by time desc" );
            q.setString( "pid", getProject().getId() );
            q.setTimestamp( "before", before );
        }
        q.setMaxResults( 50 );

        return q;
    }

    protected class Item
            implements Serializable
    {
        @Publish
        public long id;

        @Publish
        public String type, title, projectId;

        @Publish
        public Date date;

        @Publish
        public String description, link, icon;
    }
}
