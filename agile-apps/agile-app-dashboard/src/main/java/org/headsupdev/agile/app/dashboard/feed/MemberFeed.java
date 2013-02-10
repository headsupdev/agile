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

package org.headsupdev.agile.app.dashboard.feed;

import org.headsupdev.agile.api.*;
import org.headsupdev.agile.web.components.MarkedUpTextModel;
import org.headsupdev.agile.web.MountPoint;
import org.headsupdev.agile.web.feed.AbstractFeed;
import org.headsupdev.agile.web.feed.RomeModule;
import org.headsupdev.agile.web.feed.RomeModuleImpl;
import org.headsupdev.agile.security.permission.ProjectListPermission;
import org.headsupdev.agile.storage.HibernateStorage;
import com.sun.syndication.feed.synd.*;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.*;

/**
 * A simple feed page that reports the loaded projects
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@MountPoint( "member-feed.xml" )
public class MemberFeed
   extends AbstractFeed
{
    private Storage storage = Manager.getStorageInstance();

    public Permission getRequiredPermission() {
        return new ProjectListPermission();
    }

    public String getTitle()
    {
        return Manager.getStorageInstance().getGlobalConfiguration().getProductName() + " Member Feed";
    }

    public String getDescription()
    {
        return Manager.getStorageInstance().getGlobalConfiguration().getProductName() + " Member Feed";
    }

    protected void populateFeed( SyndFeed feed )
    {
        Session session = ( (HibernateStorage) Manager.getStorageInstance() ).getHibernateSession();
        Transaction tx = session.beginTransaction();
        List<User> list = session.createQuery( "from StoredUser u where username != 'anonymous' and (disabled is null or disabled = 0)" ).list();
        tx.commit();

        Collections.sort( list ); // hibernate sort is not case insensitive

        List<SyndEntry> entries = new ArrayList<SyndEntry>();
        for ( User user : list )
        {
            SyndEntry entry = new SyndEntryImpl();

            entry.setTitle( user.getFullname() );
            entry.setLink( storage.getGlobalConfiguration().getFullUrl( "/user/username/" + user.getUsername() ) );
            entry.setPublishedDate( user.getCreated() );
            entry.setUpdatedDate( user.getLastLogin() );
            entry.setAuthor( user.getEmail() );
            SyndContent content = new SyndContentImpl();
            content.setType( "text/html" );
            content.setValue( (String) new MarkedUpTextModel( user.getDescription(), getProject() ).getObject() );
            entry.setDescription( content );

            RomeModule module = new RomeModuleImpl();
            module.setId( user.getUsername() );
            entry.getModules().add( module );

            entries.add( entry );
        }

        feed.setEntries( entries );
    }
}
