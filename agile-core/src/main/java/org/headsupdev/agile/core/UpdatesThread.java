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

package org.headsupdev.agile.core;

import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEnclosure;
import org.headsupdev.agile.api.Task;
import org.headsupdev.agile.api.Manager;

import java.net.URL;
import java.net.URLConnection;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Collections;

import org.headsupdev.support.java.Base64;

/**
 * A thread that sits and looks for agile updates (if that configuration option is on at the time)...
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class UpdatesThread
    extends Thread
{
    private boolean running = true;

    public void run()
    {
        // sleep 5 minutes before first looking
        try
        {
            Thread.sleep( 5 * 60 * 1000 );
        }
        catch ( InterruptedException e )
        {
            // ignore and continue
        }
        while ( running )
        {
            // TODO should we list only the most recent update?
            // we should only return the first, most recent release - beta releases will need any stable updates first
            boolean found = false;
            if ( PrivateConfiguration.getUpdatesEnabled() || PrivateConfiguration.getBetaUpdatesEnabled() )
            {
                Task task = new UpdateCheckTask();
                Manager.getInstance().addTask( task );

                if ( PrivateConfiguration.getUpdatesEnabled() )
                {
                    found = checkForUpdates( PrivateConfiguration.UPDATE_FEED_URL, false );
                }
                if ( !found && PrivateConfiguration.getBetaUpdatesEnabled() )
                {
                    checkForUpdates( PrivateConfiguration.BETA_UPDATE_FEED_URL, true );
                }

                Manager.getInstance().removeTask( task );
            }

            try
            {
                Thread.sleep( 7 * 24 * 60 * 60 * 1000l );
            }
            catch ( InterruptedException e )
            {
                // ignore and continue
            }
        }
    }

    public void cancel()
    {
        running = false;
        interrupt();
    }

    private static boolean checkForUpdates( String urlStr, boolean beta )
    {
        try
        {
            URL url = new URL( urlStr );

            URLConnection conn = url.openConnection();
            conn.connect();

            SyndFeedInput parser = new SyndFeedInput();
            SyndFeed feed = parser.build( new InputStreamReader( conn.getInputStream() ) );

            if ( feed.getEntries().size() == 0 )
            {
                return false;
            }

            String currentDistFile = Manager.getStorageInstance().getGlobalConfiguration().getBuildVersion() + ".tar.gz";
            List<SyndEntry> feeds = feed.getEntries();
            Collections.reverse( feeds );
            for ( SyndEntry entry : feeds )
            {
                if ( Manager.getStorageInstance().getGlobalConfiguration().getBuildDate().before( entry.getPublishedDate() ) &&
                    entry.getEnclosures().size() > 0 )
                {
                    SyndEnclosure enclosure = (SyndEnclosure) entry.getEnclosures().get( 0 );
                    if ( enclosure.getUrl().contains( currentDistFile ) )
                    {
                        continue;
                    }

                    UpdateDetails update = new UpdateDetails( entry.getUri(), entry.getTitle(),
                        entry.getDescription().getValue(), ( (SyndEnclosure) entry.getEnclosures().get( 0 ) ).getUrl(),
                        ( (SyndEnclosure) entry.getEnclosures().get( 0 ) ).getLength(), beta );
                    ( (DefaultManager) Manager.getInstance() ).addAvailableUpdate( update );
                }
            }
        }
        catch ( Exception e )
        {
            Manager.getLogger( UpdatesThread.class.getName() ).error( "Error when checking for update", e );
        }

        return false;
    }
}
