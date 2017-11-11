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

package org.headsupdev.agile.app.milestones;

import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.storage.dao.MilestonesDAO;
import org.headsupdev.agile.app.milestones.permission.MilestoneViewPermission;
import org.headsupdev.agile.storage.HibernateStorage;
import org.headsupdev.agile.storage.StoredProject;
import org.headsupdev.agile.storage.issues.Duration;
import org.headsupdev.agile.storage.issues.Issue;
import org.headsupdev.agile.storage.issues.Milestone;
import org.headsupdev.agile.web.CachedImageResource;
import org.headsupdev.agile.web.MountPoint;
import org.headsupdev.agile.web.WebUtil;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebResponse;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;

/**
 * A graph of the milestone burndown over time or time spent on a project to date.
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@MountPoint("burndown.png")
public class BurndownGraph
        extends CachedImageResource
{
    private MilestonesDAO dao = new MilestonesDAO();

    final int HEIGHT = 360, WIDTH = 588, PAD = 15, MONTH_PAD = 20;

    private SimpleDateFormat dateFormat = new SimpleDateFormat( "dd" );
    private SimpleDateFormat monthFormat = new SimpleDateFormat( "MMMM" );

    // TODO cache
    protected Project getProject()
    {
        String projectId = getParameters().getString( "project" );
        if ( projectId == null || projectId.length() == 0 )
        {
            return null;
        }

        return Manager.getStorageInstance().getProject( projectId );
    }

    protected byte[] getImageData()
    {
        RequestCycle req = RequestCycle.get();

        String silentStr = getParameters().getString( "silent" );
        boolean silent = silentStr != null && silentStr.toLowerCase().equals( "true" );
        WebUtil.authenticate( (WebRequest) req.getRequest(), (WebResponse) req.getResponse(),
                new MilestoneViewPermission(), getProject(), silent );

        // actually get the image data if we authenticated OK
        return super.getImageData();
    }

    @Override
    protected void renderImage( Graphics g )
    {
        final boolean burndown = Boolean.parseBoolean( getProject().getConfigurationValue(
                StoredProject.CONFIGURATION_TIMETRACKING_BURNDOWN ) );

        g.setColor( backgroundColor );
        g.fillRect( 0, 0, getWidth(), getHeight() );
        ( (Graphics2D) g ).setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );

        if ( getIssues() == null )
        {
            return;
        }
        double total = getHighestValueForEstimates();

        int spacer = 1;
        if ( total >= 1500 )
        {
            spacer = 500;
        }
        else if ( total >= 400 )
        {
            spacer = 100;
        }
        else if ( total >= 75 )
        {
            spacer = 25;
        }
        else if ( total > 15 )
        {
            spacer = 5;
        }

        g.setColor( Color.GRAY );
        int line = spacer;
        while ( line <= total )
        {
            int y = PAD + HEIGHT - (int) Math.round( ( HEIGHT * ( (double) line / total ) ) );
            g.drawLine( PAD - 1, y, PAD + WIDTH, y );

            line += spacer;
        }

        g.setColor( Color.BLACK );
        g.drawLine( PAD - 1, PAD - 1, PAD - 1, PAD + HEIGHT );
        g.drawLine( PAD - 1, PAD + HEIGHT, PAD + WIDTH, PAD + HEIGHT );
        g.setFont( g.getFont().deriveFont( Font.BOLD, 14.0f ) );
        drawString( g, getTitle(), getWidth(), 12, PAD, Component.CENTER_ALIGNMENT );
        g.setFont( g.getFont().deriveFont( Font.PLAIN, 10.0f ) );

        g.drawLine( PAD - 1, PAD + HEIGHT, PAD - 1, PAD + HEIGHT + 5 );

        final boolean ignoreWeekend = Boolean.parseBoolean( getProject().getConfigurationValue(
                StoredProject.CONFIGURATION_TIMETRACKING_IGNOREWEEKEND ) );
        java.util.List<Date> dates = getDates();

        drawString( g, "time", getHeight() - MONTH_PAD, 13, PAD, Component.CENTER_ALIGNMENT, -Math.PI / 2 );
        drawString( g, "0", getHeight() - MONTH_PAD, 13, PAD, Component.LEFT_ALIGNMENT, -Math.PI / 2 );
        drawString( g, new Duration( total ).toHoursString(), getHeight(), 13, PAD, Component.RIGHT_ALIGNMENT, -Math.PI / 2 );

        int oldMonth = -1;
        Calendar cal = Calendar.getInstance();
        int i = 0;
        for ( Date date : dates )
        {
            int x = PAD + (int) Math.round( ( i * ( (double) WIDTH / dates.size() ) ) );
            int x2 = PAD + (int) Math.round( ( ( i + 1 ) * ( (double) WIDTH / dates.size() ) ) );

            int xc = PAD + ( (int) ( ( i * ( (double) WIDTH / dates.size() ) ) + ( (double) WIDTH / ( dates.size() * 2 ) ) ) );

            double workedTotal = getTotalHoursForDay( date );
            int y = PAD + HEIGHT - ( (int) ( HEIGHT * ( workedTotal / total ) ) );
            g.setColor( hoursColor );
            g.fillRect( x + 5, y, x2 - x - 10, HEIGHT + PAD - y );

            // draw pips on x-axis
            cal.setTime(date);
            boolean newWeek = cal.get( Calendar.DAY_OF_WEEK ) == 1 ||
                    (ignoreWeekend && cal.get( Calendar.DAY_OF_WEEK ) == 6 );
            if ( newWeek )
            {
                g.setColor( Color.BLACK );
                g.fillRect( x2, PAD + HEIGHT, 2, 10 );
            }
            else
            {
                g.setColor( Color.GRAY );
                g.drawLine( x2, PAD + HEIGHT, x2, PAD + HEIGHT + 5 );
            }
            g.setColor( Color.BLACK );

            String dateStr;
            if ( i == 0 )
            {
                dateStr = "Initial";
            }
            else
            {
                dateStr = dateFormat.format( date );
            }
            int strW = g.getFontMetrics().stringWidth( dateStr );
            int textX = xc - ( strW / 2 );
            g.drawString( dateStr, textX, HEIGHT + PAD + 14 );

            if ( i != 0 && cal.get( Calendar.MONTH ) != oldMonth )
            {
                g.drawString( monthFormat.format(date), textX, HEIGHT + PAD + 27 );

                oldMonth = cal.get( Calendar.MONTH );
            }
            i++;
        }

        g.setColor( perfectColor );
        ( (Graphics2D) g ).setStroke( new BasicStroke( 3 ) );
        int xOffset = ( (int) ( ( (double) WIDTH / ( dates.size() * 2 ) ) ) );
        if ( burndown )
        {
            g.drawLine( PAD + xOffset, PAD, PAD + WIDTH - xOffset, HEIGHT + PAD );
        }
        else
        {
            g.drawLine( PAD + xOffset, PAD, PAD + WIDTH - xOffset, PAD );
        }


        drawYCoordsAsLine( convertDurationArray( getEffort() ), total, COLOUR_EFFORT_REQUIRED, g );

    }

    protected double getHighestValueForEstimates()
    {
        double highest = 0;
        Duration[] effortRequired = getEffort();

        if ( effortRequired != null && effortRequired.length > 0 )
        {
            for ( int i = 0; i < effortRequired.length; i++ )
            {
                highest = Math.max( highest, effortRequired[ i ].getHours() );
            }
        }

        return highest;
    }

    protected double[] convertDurationArray( Duration[] durations )
    {
        if ( durations == null || durations.length == 0 )
        {
            return new double[0];
        }

        double[] hours = new double[ durations.length ];
        for ( int i = 0; i < durations.length; i++ )
        {
            hours[ i ] = durations[ i ].getHours();
        }
        return hours;
    }

    protected void drawYCoordsAsLine( double[] yCoords, double maxYValue, Color colour, Graphics g )
    {
        int i = 0, px = 0, py = 0;

        for ( double yCoord : yCoords )
        {
            int xc = PAD + ( (int) ( ( i * ( (double) WIDTH / yCoords.length ) ) + ( (double) WIDTH / ( yCoords.length * 2 ) ) ) );

            int y = PAD + HEIGHT - ( (int) ( HEIGHT * ( yCoord / maxYValue ) ) );
            g.setColor( colour );
            g.fillRect( xc - 2, y - 2, 5, 5 );

            if ( px != 0 )
            {
                g.drawLine( px, py, xc, y );
            }
            px = xc;
            py = y;

            i++;
        }
    }

    private Milestone getMilestone()
    {
        String milestoneId = getParameters().getString( "id" );
        if ( milestoneId == null || milestoneId.length() == 0 )
        {
            return null;
        }

        return dao.find(milestoneId, getProject());
    }

    protected Set<Issue> getIssues()
    {
        return getMilestone().getIssues();
    }

    protected double getTotalHoursForDay( Date day )
    {
        double total = 0;
        if ( getMilestone() == null )
        {
            return total;
        }

        for ( Issue issue : getIssues() )
        {
            total += ( (HibernateStorage) Manager.getStorageInstance() ).getResourceManager().
                    totalWorkedForDay( issue, day ).getHours();
        }

        return total;
    }

    protected java.util.List<Date> getDates()
    {
        return ( (HibernateStorage) Manager.getStorageInstance() ).getResourceManager().
                getMilestoneDates( getMilestone(), true );
    }

    protected Duration[] getEffort()
    {
        return ( (HibernateStorage) Manager.getStorageInstance() ).getResourceManager().
                getMilestoneEffortRequired( getMilestone() );
    }

    @Override
    protected int getWidth()
    {
        return WIDTH + PAD * 2;
    }

    @Override
    protected int getHeight()
    {
        return HEIGHT + PAD * 2 + MONTH_PAD;
    }

    protected String getTitle()
    {
        if ( Boolean.parseBoolean( getProject().getConfigurationValue( StoredProject.CONFIGURATION_TIMETRACKING_BURNDOWN ) ) )
        {
            return "Milestone Burndown";
        }
        else
        {
            return "Milestone Work";
        }
    }

    // TODO move out to graph util
    public static final Color backgroundColor = new Color( 0xFF, 0xFF, 0xFF );
    public static final Color COLOUR_EFFORT_REQUIRED = new Color( 0x5F, 0x5F, 0xDF );
    public static final Color hoursColor = new Color( 0xDF, 0xAF, 0x4F );
    public static final Color perfectColor = new Color( 0x4F, 0xDF, 0xAF );

    protected void drawString( Graphics g, String title, int w, int h, int pad, float align )
    {
        drawString( g, title, w, h, pad, align, 0.0 );
    }

    protected void drawString( Graphics g, String title, int w, int h, int pad, float align, double rotate )
    {
        Graphics2D g2 = (Graphics2D) g;
        AffineTransform tx = g2.getTransform();

        if ( rotate != 0.0 )
        {
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
        g2.setTransform( tx );
    }
}
