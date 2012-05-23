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

package org.headsupdev.agile.app.history.feed;

import org.headsupdev.agile.api.HeadsUpConfiguration;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.Query;

import java.util.List;
import java.util.ArrayList;
import java.util.Date;

import com.sun.syndication.feed.synd.*;
import org.headsupdev.agile.web.components.MarkedUpTextModel;
import org.headsupdev.agile.web.feed.AbstractFeed;
import org.headsupdev.agile.web.feed.RomeModule;
import org.headsupdev.agile.web.feed.RomeModuleImpl;
import org.headsupdev.agile.web.MountPoint;
import org.headsupdev.agile.app.history.permission.HistoryViewPermission;
import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.api.Permission;
import org.headsupdev.agile.api.Event;
import org.headsupdev.agile.storage.StoredProject;
import org.headsupdev.agile.storage.HibernateStorage;

/**
 * A simple feed page that reports the recent activity
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@MountPoint( "feed.xml" )
public class HistoryFeed
   extends AbstractFeed
{
    public Permission getRequiredPermission() {
        return new HistoryViewPermission();
    }

    public String getTitle() {
        String title = Manager.getStorageInstance().getGlobalConfiguration().getProductName() + " Activity Feed";
        if ( !getProject().equals( StoredProject.getDefault() ) )
        {
            title += " :: " + getProject().getAlias();
        }
        return title;
    }

    public String getDescription() {
        String description = Manager.getStorageInstance().getGlobalConfiguration().getProductName() + " Activity feed for ";
        if ( getProject().equals( StoredProject.getDefault() ) )
        {
            description += "all projects";
        }
        else
        {
            description += "project " + getProject().getAlias();
        }
        return description;
    }

    protected void populateFeed(SyndFeed feed) {
        long before;
        try
        {
            before = getPageParameters().getLong( "before" );
        }
        catch ( Exception e ) // NumberFormatException or a wicket wrapped NumberFormatException
        {
            before = Long.MAX_VALUE;
        }

        Session session = ( (HibernateStorage) Manager.getStorageInstance() ).getHibernateSession();
        Transaction tx = session.beginTransaction();
        Query q;
        if ( getProject().equals( StoredProject.getDefault() ) )
        {
            q = session.createQuery( "from StoredEvent e where e.time < :before order by time desc" );
            q.setTimestamp( "before", new Date( before ) );
        }
        else
        {
            q = session.createQuery( "from StoredEvent e where project.id = :pid and e.time < :before order by time desc" );
            q.setString( "pid", getProject().getId() );
            q.setTimestamp( "before", new Date( before ) );
        }
        q.setMaxResults( 50 );
        List<Event> list = q.list();
        tx.commit();

        List<SyndEntry> entries = new ArrayList<SyndEntry>();
        for ( Event event : list )
        {
            SyndEntry entry = new SyndEntryImpl();

            if ( getProject().equals( StoredProject.getDefault() ) )
            {
                if ( event.getProject() == null ) {
                    entry.setTitle( event.getTitle() + " (Unknown)" );
                } else {
                    entry.setTitle( event.getTitle() + " (" + event.getProject().getAlias() + ")" );
                }
            }
            else
            {
                entry.setTitle( event.getTitle() );
            }
            entry.setLink( Manager.getStorageInstance().getGlobalConfiguration().getFullUrl( "/activity/event/id/" + event.getId() ) );
            entry.setPublishedDate( event.getTime() );
            SyndContent content = new SyndContentImpl();
            content.setType( "text/html" );
            content.setValue( (String) new MarkedUpTextModel( event.getSummary(), event.getProject() ).getObject() );
            entry.setDescription( content );

            RomeModule module = new RomeModuleImpl();
            module.setId( String.valueOf( event.getId() ) );
            module.setType( event.getClass().getName().substring( event.getClass().getPackage().getName().length() + 1 ) );
            module.setTime( event.getTime().getTime() );
            entry.getModules().add( module );

            entries.add( entry );
        }

        feed.setEntries( entries );
    }
}
