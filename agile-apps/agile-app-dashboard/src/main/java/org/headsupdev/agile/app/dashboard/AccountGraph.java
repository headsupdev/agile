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

package org.headsupdev.agile.app.dashboard;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.*;
import java.util.List;

import org.headsupdev.agile.api.Event;
import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.app.dashboard.permission.MemberViewPermission;
import org.headsupdev.agile.web.CachedImageResource;
import org.headsupdev.agile.web.WebUtil;
import org.headsupdev.agile.web.MountPoint;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebResponse;

/**
 * Draws a graph to show a users activity (pulls events that can be attributed to a certain user)
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@MountPoint( "member.png" )
public class AccountGraph
    extends CachedImageResource
{
    final int HEIGHT = 120, WIDTH = 196, PAD = 15;
    private int CHANGES = 0, ISSUES = 1, DOCS = 2, MILESTONES = 3;

    protected byte[] getImageData()
    {
        RequestCycle req = RequestCycle.get();

        String silentStr = getParameters().getString( "silent" );
        boolean silent = silentStr != null && silentStr.toLowerCase().equals( "true" );
        WebUtil.authenticate(  (WebRequest) req.getRequest(), (WebResponse) req.getResponse(),
                new MemberViewPermission(), null, silent );

        // actually get the image data if we authenticated OK
        return super.getImageData();
    }

    protected void renderImage( Graphics g ) {
        g.setColor( ActivityGraph.background );
        g.fillRect( 0, 0, getWidth(), getHeight() );
        ( (Graphics2D) g ).setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );

        String username = getParameters().getString( "username" );
        if ( username == null || username.length() == 0 )
        {
            return;
        }
        org.headsupdev.agile.api.User user = Manager.getSecurityInstance().getUserByUsername( username );
        if ( user == null )
        {
            return;
        }

        Date now = new Date();
        Calendar cal = new GregorianCalendar();
        cal.setTime( now );

        cal.set( Calendar.HOUR_OF_DAY, 0 ); // read from the beginning of the first day
        cal.set( Calendar.MINUTE, 0 );
        cal.set( Calendar.SECOND, 0 );
        cal.add( Calendar.DATE, -28 );
        Date start = cal.getTime();

        List<Event> events =
            Manager.getStorageInstance().getEventsForUser( user, start, now );

        int[][] totals = getTotalsForEvents( events, now );
        int graphTop = getTotalMax( totals );
        if ( graphTop == 0 )
        {
            graphTop = 1;
        }

        g.setColor( ActivityGraph.background );
        g.fillRect( 0, 0, WIDTH + PAD * 2, HEIGHT + PAD * 2 );

        for ( int i = 0; i < totals.length; i++ )
        {
            int x = PAD + (int) Math.round( (double) ( i * ( (double) WIDTH / 28 ) ) );
            int x2 = PAD + (int) Math.round( (double) ( ( i + 1 ) * ( (double) WIDTH / 28 ) ) );
            int mlestonesHeight = ( (int) ( HEIGHT * ( (double) ( totals[i][MILESTONES] ) / graphTop ) ) );
            int docHeight = ( (int) ( HEIGHT * ( (double) ( totals[i][DOCS] ) / graphTop ) ) );
            int issueHeight = ( (int) ( HEIGHT * ( (double) ( totals[i][ISSUES] ) / graphTop ) ) );
            int changeHeight = ( (int) ( HEIGHT * ( (double) ( totals[i][CHANGES] ) / graphTop ) ) );

            g.setColor( ActivityGraph.change );
            g.fillRect( x, PAD + HEIGHT - changeHeight, x2 - x, changeHeight );
            g.setColor( ActivityGraph.issue3 );
            g.fillRect( x, PAD + HEIGHT - changeHeight - issueHeight, x2 - x, issueHeight );
            g.setColor( ActivityGraph.doc3 );
            g.fillRect( x, PAD + HEIGHT - changeHeight - issueHeight - docHeight, x2 - x, docHeight );
            g.setColor( ActivityGraph.milestone3 );
            g.fillRect( x, PAD + HEIGHT - changeHeight - issueHeight - docHeight - mlestonesHeight, x2 - x, mlestonesHeight );
        }

        g.setColor( Color.BLACK );
        g.drawRect( PAD - 1, PAD - 1, WIDTH + 1, HEIGHT + 1 );
        g.setFont( g.getFont().deriveFont( Font.BOLD, 14.0f ) );
        drawString( g, getTitle(), getWidth(), 12, PAD, Component.CENTER_ALIGNMENT );
        g.setFont( g.getFont().deriveFont( Font.PLAIN, 10.0f ) );

        drawString( g, getTimeCaption(), getWidth(), 145, PAD, Component.CENTER_ALIGNMENT );
        drawString( g, "28", 0, 145, PAD, Component.LEFT_ALIGNMENT );
        drawString( g, "0", getWidth(), 145, PAD, Component.RIGHT_ALIGNMENT );

        drawString( g, "events", getHeight(), 13, PAD, Component.CENTER_ALIGNMENT, -Math.PI / 2 );
        drawString( g, "0", getHeight(), 13, PAD, Component.LEFT_ALIGNMENT, -Math.PI / 2 );
        drawString( g, String.valueOf( graphTop ), getHeight(), 13, PAD, Component.RIGHT_ALIGNMENT, -Math.PI / 2 );
    }

    public int getWidth()
    {
        return WIDTH + PAD * 2;
    }

    public int getHeight()
    {
        return HEIGHT + PAD * 2;
    }

    private int[][] getTotalsForEvents( List<Event> events, Date now )
    {
        int[][] ret = new int[28][4];

        Calendar cal = new GregorianCalendar();
        cal.setTime( now );
        cal.set( Calendar.HOUR_OF_DAY, 0 );
        cal.set( Calendar.MINUTE, 0 );
        cal.set( Calendar.SECOND, 0 );

        Date divStart = cal.getTime();
        ListIterator<Event> eventIter = events.listIterator();
        for ( int c = 27; c >= 0; c-- )
        {
            ret[c][CHANGES] = 0;
            ret[c][ISSUES] = 0;
            ret[c][DOCS] = 0;
            ret[c][MILESTONES] = 0;

            while ( eventIter.hasNext() )
            {
                Event next = eventIter.next();
                if ( divStart.before( next.getTime() ) )
                {
                    if ( next.getApplicationId().equals( "files" ) )
                    {
                        ret[c][CHANGES]++;
                    }
                    else if ( next.getApplicationId().equals( "issues" ) )
                    {
                        ret[c][ISSUES]++;
                    }
                    else if ( next.getApplicationId().equals( "docs" ) )
                    {
                        ret[c][DOCS]++;
                    }
                    else if ( next.getApplicationId().equals( "milestones" ) )
                    {
                        ret[c][MILESTONES]++;
                    }
                }
                else
                {
                    eventIter.previous();
                    break;
                }
            }

            cal.add( Calendar.DATE, -1 );
            divStart = cal.getTime();
        }

        return ret;
    }

    private int getTotalMax( int[][] totals )
    {
        int ret = totals[0][CHANGES] + totals[0][ISSUES] + totals[0][DOCS] + totals[0][MILESTONES];

        for ( int c = 1; c < totals.length; c++ )
        {
            ret = Math.max( ret, totals[c][CHANGES] + totals[c][ISSUES] + totals[c][DOCS] + totals[c][MILESTONES] );
        }

        return ret;
    }

    protected String getTitle()
    {
        return "Activity This Month";
    }

    protected String getTimeCaption()
    {
        return "days ago";
    }

    private void drawString( Graphics g, String title, int w, int h, int pad, float align )
    {
        drawString( g, title, w, h, pad, align, 0.0 );
    }

    private void drawString( Graphics g, String title, int w, int h, int pad, float align, double rotate )
    {
        if ( rotate != 0.0 )
        {
            Graphics2D g2 = (Graphics2D) g;
            AffineTransform transform = AffineTransform.getRotateInstance( rotate );
            if ( rotate > 0 )
            {
                transform.preConcatenate( AffineTransform.getTranslateInstance( h * 2, 0 ) );
            }
            else
            {
                transform.concatenate( AffineTransform.getTranslateInstance( -w, 0 ) );
            }
            g2.setTransform( transform );
        }

        int strW = g.getFontMetrics().stringWidth( title );
        int x;
        if ( align == Component.CENTER_ALIGNMENT )
        {
            x = w / 2 - strW / 2;
        }
        else if ( align == Component.RIGHT_ALIGNMENT )
        {
            x = w - pad - strW;
        }
        else
        {
            x = pad;
        }

        g.drawString( title, x, h );
    }
}
