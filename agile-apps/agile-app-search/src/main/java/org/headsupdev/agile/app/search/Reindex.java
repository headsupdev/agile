/*
 * HeadsUp Agile
 * Copyright 2009-2012 Heads Up Development.
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

package org.headsupdev.agile.app.search;

import org.headsupdev.agile.api.HeadsUpConfiguration;
import org.headsupdev.agile.web.HeadsUpPage;
import org.headsupdev.agile.web.MountPoint;
import org.headsupdev.agile.api.Permission;
import org.headsupdev.agile.api.Task;
import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.security.permission.AdminPermission;
import org.headsupdev.agile.storage.HibernateUtil;
import org.headsupdev.agile.storage.SessionProxy;
import org.hibernate.*;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.annotations.Indexed;

/**
 * A simple page to re-index our search cache
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@MountPoint( "reindex" )
public class Reindex
    extends HeadsUpPage
{
    public Permission getRequiredPermission() {
        return new AdminPermission();
    }

    public void layout()
    {
        super.layout();

        new ReindexThread().start();
    }

    @Override
    public String getTitle()
    {
        return "Reindex Search Cache";
    }
}

class ReindexThread
    extends Thread
{
    static int BATCH_SIZE = 100;
    public void run()
    {
        Task reindex = new ReindexTask();
        Manager.getInstance().addTask( reindex );

        try
        {
            for ( String className: HibernateUtil.getEntityClassNames() )
            {
                Session session = HibernateUtil.getCurrentSession();
                FullTextSession fullTextSession = org.hibernate.search.Search.createFullTextSession(
                    ( (SessionProxy) session ).getRealSession() );
                Transaction tx = fullTextSession.beginTransaction();

                fullTextSession.setFlushMode( FlushMode.MANUAL );
                fullTextSession.setCacheMode( CacheMode.IGNORE );

                Manager.getLogger( getClass().getName() ).debug("  object type " + className);

                //Scrollable results will avoid loading too many objects in memory
                ScrollableResults results = fullTextSession.createCriteria( className )
                    .setFetchSize( BATCH_SIZE )
                    .scroll( ScrollMode.FORWARD_ONLY );

                int index = 0;
                while( results.next() )
                {
                    Object o = results.get( 0 );

                    index++;
                    if ( o.getClass().isAnnotationPresent( Indexed.class ) )
                    {
                        if ( HeadsUpConfiguration.isDebug() )
                        {
                            System.out.print( "." );
                        }
                        fullTextSession.index( o ); //index each element
                    }
                    if ( index % BATCH_SIZE == 0 ) {
                        fullTextSession.flushToIndexes(); //apply changes to indexes
                        fullTextSession.clear(); //clear since the queue is processed
                    }
                }
                tx.commit();

                if ( HeadsUpConfiguration.isDebug() )
                {
                    System.out.println();
                }
            }
        }
        catch ( Exception e )
        {
            Manager.getLogger( getClass().getName() ).error( "Failed to reindex search data", e );
        }

        Manager.getInstance().removeTask( reindex );
    }
}