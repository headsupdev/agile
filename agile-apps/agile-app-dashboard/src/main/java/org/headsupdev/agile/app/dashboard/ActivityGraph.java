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

import org.headsupdev.agile.api.*;
import org.headsupdev.agile.api.Event;
import org.headsupdev.agile.app.dashboard.permission.ProjectViewPermission;
import org.headsupdev.agile.storage.issues.Milestone;
import org.headsupdev.agile.storage.HibernateStorage;
import org.headsupdev.agile.web.CachedImageResource;
import org.headsupdev.agile.web.WebUtil;
import org.headsupdev.agile.web.ApplicationPageMapper;
import org.headsupdev.agile.web.MountPoint;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.*;
import java.util.List;

import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.RequestCycle;
import org.hibernate.Query;
import org.hibernate.Session;

/**
 * Class that draws a graph for a project's activity in a wicket Image widget
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@MountPoint( "activity.png" )
public class ActivityGraph
    extends CachedImageResource
{
    public static final String TIME_MONTH = "month";
    public static final String TIME_YEAR = "year";

    final int HEIGHT = 120, WIDTH = 196, PAD = 15;
    public static final Color background = new Color( 0xFF, 0xFF, 0xFF );
    public static final Color success = new Color( 0xDF, 0xFF, 0xDF );
    public static final Color fail = new Color( 0xFF, 0xDF, 0xDF );
    public static final Color issue = new Color( 0xDF, 0xDF, 0xFF );
    public static final Color issue2 = new Color( 0x9F, 0x9F, 0xEF );
    public static final Color issue3 = new Color( 0x5F, 0x5F, 0xDF );
    public static final Color milestone = new Color( 0xBF, 0xDF, 0xD4 );
    public static final Color milestone2 = new Color( 0x88, 0xDF, 0xC2 );
    public static final Color milestone3 = new Color( 0x4F, 0xDF, 0xAF );
    public static final Color doc = new Color( 0xD4, 0xBF, 0xDF );
    public static final Color doc2 = new Color( 0xC2, 0x88, 0xDF );
    public static final Color doc3 = new Color( 0xAF, 0x4F, 0xDF );
    public static final Color change = new Color( 0xDF, 0xAF, 0x4F );
    public static final Color changeLine = Color.BLACK;

    protected byte[] getImageData()
    {
        RequestCycle req = RequestCycle.get();

        String silentStr = getParameters().getString( "silent" );
        boolean silent = silentStr != null && silentStr.toLowerCase().equals( "true" );
        WebUtil.authenticate( (WebRequest) req.getRequest(), (WebResponse) req.getResponse(),
                new ProjectViewPermission(), getProject(), silent );

        // actually get the image data if we authenticated OK
        return super.getImageData();
    }

    protected Project getProject( )
    {
        String projectId = getParameters().getString( "project" );
        if ( projectId == null || projectId.length() == 0 )
        {
            return null;
        }

        return Manager.getStorageInstance().getProject( projectId );
    }

    protected void renderImage( Graphics g )
    {
        g.setColor( background );
        g.fillRect( 0, 0, getWidth(), getHeight() );
        ( (Graphics2D) g ).setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );

        Project project = getProject();
        if ( project == null )
        {
            return;
        }

        boolean tree = getParameters().getBoolean( "tree" );

        int divisions = getDivisions();

        Date now = new Date();
        Calendar cal = new GregorianCalendar();
        cal.setTime( now );

        cal.set( Calendar.HOUR_OF_DAY, 0 ); // read from the beginning of the first day
        cal.set( Calendar.MINUTE, 0 );
        cal.set( Calendar.SECOND, 0 );
        if ( isMonth() )
        {
            cal.add( Calendar.DATE, 1 - divisions );
        }
        else
        {
            cal.set( Calendar.DAY_OF_MONTH, 1 ); // count from the beginning of months
            cal.add( Calendar.MONTH, 1 - divisions );
        }
        Date start = cal.getTime();

        // draw CI results
        java.util.List<Event> ciEvents = getEvents( "builds", project, start, now, tree );
        int[] ciStates = getStatesForCIEvents( ciEvents, now );

        for ( int i = 0; i < ciStates.length; i++ )
        {
            int x = PAD + (int) Math.round( (double) ( i * ( (double) WIDTH / divisions ) ) );
            int x2 = PAD + (int) Math.round( (double) ( ( i + 1 ) * ( (double) WIDTH / divisions ) ) );

            switch ( ciStates[i] )
            {
                case 1:
                    g.setColor( success );
                    break;
                case 0:
                    g.setColor( fail );
                    break;
                default:
                    g.setColor( Color.WHITE );
            }

            g.fillRect( x, PAD, x2 - x, HEIGHT );
        }

        // Draw issue bars
        java.util.List<Event> issueEvents = getEvents( "issues", project, start, now, tree );
        // TODO optimise this, we don't need 3 iterations
        int[] issueCreateTotals = getTotalsForEvents( issueEvents, now, "CreateIssueEvent" );
        int[] issueUpdateTotals = getTotalsForEvents( issueEvents, now, "UpdateIssueEvent" );
        int[] issueCloseTotals = getTotalsForEvents( issueEvents, now, "CloseIssueEvent" );
        int issueMax = getTotalMax( issueCreateTotals ) + getTotalMax( issueUpdateTotals ) + getTotalMax( issueCloseTotals );

        double graphTop = issueMax * 2.2;
        if ( graphTop == 0 )
        {
            graphTop = 1;
        }

        for ( int i = 0; i < issueCreateTotals.length; i++ )
        {
            int x = PAD + (int) Math.round( (double) ( i * ( (double) WIDTH / divisions ) ) );
            int x2 = PAD + (int) Math.round( (double) ( ( i + 1 ) * ( (double) WIDTH / divisions ) ) );

            int createHeight = (int) Math.round( HEIGHT * ( (double) issueCreateTotals[i] / graphTop ) );
            int updateHeight = (int) Math.round( HEIGHT * ( (double) issueUpdateTotals[i] / graphTop ) );
            int closeHeight = (int) Math.round( HEIGHT * ( (double) issueCloseTotals[i] / graphTop ) );
            int combinedHeight = createHeight + updateHeight + closeHeight;

            g.setColor( issue );
            g.fillRect( x, PAD + HEIGHT - combinedHeight, x2 - x, createHeight );
            g.setColor( issue2 );
            g.fillRect( x, PAD + HEIGHT - combinedHeight + createHeight, x2 - x, updateHeight );
            g.setColor( issue3 );
            g.fillRect( x, PAD + HEIGHT - closeHeight, x2 - x, closeHeight );
            g.drawRect( x, PAD + HEIGHT - combinedHeight, x2 - x, combinedHeight );
        }

        // Draw docs bars
        java.util.List<Event> docEvents = getEvents( "docs", project, start, now, tree );
        // TODO optimise this, we don't need 3 iterations
        int[] docCreateTotals = getTotalsForEvents( docEvents, now, "CreateDocumentEvent" );
        int[] docUpdateTotals = getTotalsForEvents( docEvents, now, "UpdateDocumentEvent" );
        int docMax = getTotalMax( docCreateTotals ) + getTotalMax( docUpdateTotals );

        graphTop = docMax * 2.2;
        if ( graphTop == 0 )
        {
            graphTop = 1;
        }

        for ( int i = 0; i < docCreateTotals.length; i++ )
        {
            int x = PAD + (int) Math.round( (double) ( i * ( (double) WIDTH / divisions ) ) );
            int x2 = PAD + (int) Math.round( (double) ( ( i + 1 ) * ( (double) WIDTH / divisions ) ) );

            int createHeight = (int) Math.round( HEIGHT * ( (double) docCreateTotals[i] / graphTop ) );
            int updateHeight = (int) Math.round( HEIGHT * ( (double) docUpdateTotals[i] / graphTop ) );
            int combinedHeight = createHeight + updateHeight;

            g.setColor( doc2 );
            g.fillRect( x, PAD - 1, x2 - x, updateHeight );
            g.setColor( doc );
            g.fillRect( x, PAD + updateHeight - 1, x2 - x, createHeight );
            g.setColor( doc3 );
            g.drawRect( x, PAD - 1, x2 - x, combinedHeight );
        }

        // draw the scm line chart
        java.util.List<Event> scmEvents = getEvents( "files", project, start, now, tree );
        int[] scmTotals = getTotalsForEvents( scmEvents, now );
        int scmMax = getTotalMax( scmTotals );

        graphTop = scmMax;
        if ( graphTop == 0 )
        {
            graphTop = 1;
        }

        // slightly convaluted draw order so dots appear over the lines
        int lastX = 0, lastY = 0;
        for ( int i = 0; i < scmTotals.length; i++ )
        {
            int x = PAD + ( (int) ( (double) (i * ( (double) WIDTH / divisions ) ) + ( (double) WIDTH / ( divisions * 2 ) ) ) );
            int y = PAD + HEIGHT - ( (int) ( HEIGHT * ( (double) scmTotals[i] / graphTop ) ) );

            if ( lastX > 0 )
            {
                g.setColor( changeLine );
                g.drawLine( lastX, lastY, x, y );

                g.setColor( change );
                g.fillOval( lastX - 3, lastY - 3, 6, 6 );
            }

            lastX = x;
            lastY = y;
        }
        g.setColor( change );
        g.fillOval( lastX - 3, lastY - 3, 6, 6 );

        // draw the outline, axis and notes
        g.setColor( background );
        g.fillRect( 0, 0, WIDTH + PAD * 2, PAD );
        g.fillRect( WIDTH + PAD, 0, WIDTH + PAD * 2, HEIGHT + PAD * 2 );
        g.fillRect( 0, HEIGHT + PAD, WIDTH + PAD * 2, HEIGHT + PAD * 2 );
        g.fillRect( 0, 0, PAD, HEIGHT + PAD * 2 );

        g.setColor( Color.BLACK );
        g.drawRect( PAD - 1, PAD - 1, WIDTH + 1, HEIGHT + 1 );

        g.setFont( g.getFont().deriveFont( Font.BOLD, 14.0f ) );
        drawString( g, getTitle(), getWidth() - PAD * 2, 12, PAD, Component.CENTER_ALIGNMENT );
        g.setFont( g.getFont().deriveFont( Font.PLAIN, 10.0f ) );

        drawString( g, getTimeCaption(), getWidth() - PAD * 2, 145, PAD, Component.CENTER_ALIGNMENT );
        drawString( g, String.valueOf( getDivisions() ), 0, 145, PAD, Component.LEFT_ALIGNMENT );
        drawString( g, "0", getWidth() - PAD * 2, 145, PAD, Component.RIGHT_ALIGNMENT );

        if ( scmMax == 0 )
        {
            scmMax = 1;
        }
        if ( docMax == 0 )
        {
            docMax = 1;
        }
        if ( issueMax == 0 )
        {
            issueMax = 1;
        }
        drawString( g, "commits", getHeight(), 13, PAD, Component.CENTER_ALIGNMENT, -Math.PI / 2 );
        drawString( g, "0", getHeight(), 13, PAD, Component.LEFT_ALIGNMENT, -Math.PI / 2 );
        drawString( g, String.valueOf( scmMax ), getHeight(), 13, PAD, Component.RIGHT_ALIGNMENT, -Math.PI / 2 );

        drawString( g, docMax + "  " + issueMax, getHeight(), 217, PAD, Component.CENTER_ALIGNMENT, Math.PI / 2 );
        drawString( g, "issues 0", getHeight(), 217, PAD, Component.RIGHT_ALIGNMENT, Math.PI / 2 );
        drawString( g, "0 docs", getHeight(), 217, PAD, Component.LEFT_ALIGNMENT, Math.PI / 2 );

        // milestone stack
        double completion = getCompletion( project, tree );
        if ( completion == -1 )
        {
            g.setColor( background );
            g.fillRect( WIDTH + PAD * 2 + 1, PAD, PAD - 2, HEIGHT );
        }
        else
        {
            g.setColor( fail );
            g.fillRect( WIDTH + PAD * 2 + 1, PAD, PAD - 2, HEIGHT );

            g.setColor( success );
            int completedHeight = (int) ( HEIGHT * completion );
            g.fillRect( WIDTH + PAD * 2 + 1, PAD + HEIGHT - completedHeight, PAD - 2, completedHeight );
        }

        g.setColor( Color.BLACK );
        g.drawRect( WIDTH + PAD * 2 + 1, PAD - 1, PAD - 2, HEIGHT + 1 );
        drawString( g, "100%", getHeight(), 247, PAD, Component.LEFT_ALIGNMENT, Math.PI / 2 );
        drawString( g, "completion", getHeight(), 247, PAD, Component.CENTER_ALIGNMENT, Math.PI / 2 );
        drawString( g, "0%", getHeight(), 247, PAD, Component.RIGHT_ALIGNMENT, Math.PI / 2 );
    }

    public int getWidth()
    {
        return WIDTH + PAD * 4; // add more space to display the side stack for milestone completion
    }

    public int getHeight()
    {
        return HEIGHT + PAD * 2;
    }

    protected boolean isMonth()
    {
        String time = getParameters().getString( "time" );
        if ( time != null && time.equals( TIME_YEAR ) )
        {
            return false;
        }

        return true;
    }

    private List<Event> getEvents( String app, Project project, Date start, Date end, boolean tree )
    {
        Application application = ApplicationPageMapper.get().getApplication( app );
        if ( application == null )
        {
            // application is still loading, return empty list for now
            // TODO something more clever
            return new LinkedList<Event>();
        }

        if ( tree )
        {
            return application.getEventsForProjectTree( project, start, end );
        }
        else
        {
            return application.getEventsForProject( project, start, end );
        }
    }

    private int[] getTotalsForEvents( List<Event> events, Date now )
    {
        return getTotalsForEvents( events, now, null );
    }

    private int[] getTotalsForEvents( List<Event> events, Date now, String eventClass )
    {
        int divisions = getDivisions();
        int[] ret = new int[divisions];

        Calendar cal = new GregorianCalendar();
        cal.setTime( now );
        cal.set( Calendar.HOUR_OF_DAY, 0 );
        cal.set( Calendar.MINUTE, 0 );
        cal.set( Calendar.SECOND, 0 );
        if ( !isMonth() )
        {
            cal.set( Calendar.DAY_OF_MONTH, 1 );
        }

        Date divStart = cal.getTime();
        ListIterator<Event> eventIter = events.listIterator();
        for ( int c = divisions - 1; c >= 0; c-- )
        {
            int total = 0;

            while ( eventIter.hasNext() )
            {
                Event next = eventIter.next();
                if ( divStart.before( next.getTime() ) )
                {
                    if ( eventClass == null || next.getClass().getName().endsWith( eventClass ) )
                    {
                        total++;
                    }
                }
                else
                {
                    eventIter.previous();
                    break;
                }
            }

            ret[c] = total;

            if ( isMonth() )
            {
                cal.add( Calendar.DATE, -1 );
            }
            else
            {
                cal.add( Calendar.MONTH, -1 );
            }
            divStart = cal.getTime();
        }

        return ret;
    }

    private int[] getStatesForCIEvents( List<Event> events, Date now )
    {
        int divisions = getDivisions();
        int[] ret = new int[divisions];
        Collections.reverse( events );

        Calendar cal = new GregorianCalendar();
        cal.setTime( now );
        cal.set( Calendar.HOUR_OF_DAY, 0 );
        cal.set( Calendar.MINUTE, 0 );
        cal.set( Calendar.SECOND, 0 );
        if ( !isMonth() )
        {
            cal.set( Calendar.DAY_OF_MONTH, 1 );
            cal.add( Calendar.MONTH, -10 );
        }
        else
        {
            cal.add( Calendar.DATE, -26 );
        }

        List<Project> projects = new LinkedList<Project>();
        // TODO we actually need to find what the current state is (look to the previous event)
        ListIterator<Event> eventIter = events.listIterator();
        while ( eventIter.hasNext() )
        {
            Event e = eventIter.next();
            if ( !projects.contains( e.getProject() ) ) {
                projects.add( e.getProject() );
            }
        }

        Date divEnd = cal.getTime();
        eventIter = events.listIterator();
        for ( int c = 0; c < divisions; c++ )
        {
            Map<Project, Integer> failureMap = new HashMap<Project, Integer>();
            Map<Project, Integer> passMap = new HashMap<Project, Integer>();
            Map<Project, Boolean> finalFailures = new HashMap<Project, Boolean>();
            Map<Project, Boolean> finalSuccesses = new HashMap<Project, Boolean>();

            while ( eventIter.hasNext() )
            {
                Event next = eventIter.next();
                if ( divEnd.after( next.getTime() ) )
                {
                    Project p = next.getProject();
                    int failures = 0;
                    if ( failureMap.containsKey( p ) ) {
                        failures = failureMap.get( p );
                    }
                    int passes = 0;
                    if ( passMap.containsKey( p ) ) {
                        passes = passMap.get( p );
                    }
                    boolean finallyFailed = false;
                    if ( finalFailures.containsKey( p ) ) {
                        finallyFailed = finalFailures.get( p );
                    }
                    boolean finallySucceeded = false;
                    if ( finalSuccesses.containsKey( p ) ) {
                        finallySucceeded = finalSuccesses.get( p );
                    }

                    if ( next.getClass().getName().endsWith( "BuildSucceededEvent" ) )
                    {
                        passes++;
                        finallyFailed = false;
                        finallySucceeded = true;
                    }
                    else if ( next.getClass().getName().endsWith( "BuildFailedEvent" ) )
                    {
                        failures++;
                        finallyFailed = true;
                        finallySucceeded = false;
                    }

                    failureMap.put( p, failures );
                    passMap.put( p, passes );
                    finalFailures.put( p, finallyFailed );
                    finalSuccesses.put( p, finallySucceeded );
                }
                else
                {
                    eventIter.previous();
                    break;
                }
            }

            boolean allEndedWell = true;
            int failures = 0;
            int passes = 0;
            for ( Project project : projects )
            {
                if ( failureMap.containsKey( project ) )
                {
                    failures += failureMap.get( project );
                }
                if ( passMap.containsKey( project ) )
                {
                    passes += passMap.get( project );
                }

                boolean finallySucceeded = !finalSuccesses.containsKey( project ) || finalSuccesses.get( project );
                allEndedWell = allEndedWell && finallySucceeded;
            }
            if ( failures <= 0 )
            {
                if ( passes > 0 )
                {
                    ret[c] = 1;
                }
                else
                {
                    ret[c] = -1;
                }
            }
            else
            {
                if ( allEndedWell )
                {
                    ret[c] = 1;
                }
                else
                {
                    ret[c] = 0;
                }
            }

            if ( isMonth() )
            {
                cal.add( Calendar.DATE, 1 );
            }
            else
            {
                cal.add( Calendar.MONTH, 1 );
            }
            divEnd = cal.getTime();
        }

        return ret;
    }

    private int getTotalMax( int[] totals )
    {
        int ret = totals[0];

        for ( int c = 1; c < totals.length; c++ )
        {
            ret = Math.max( ret, totals[c] );
        }

        return ret;
    }

    private int getTotalMin( int[] totals )
    {
        int ret = totals[0];

        for ( int c = 1; c < totals.length; c++ )
        {
            ret = Math.min( ret, totals[c] );
        }

        return ret;
    }

    private double getCompletion( Project project, boolean recurse )
    {
        int completingMilestones = 0;
        double milestoneCompletion = 0.0;
        if ( recurse && project.getChildProjects().size() > 0 )
        {
            for ( Project child : project.getChildProjects() )
            {
                double completion = getCompletion( child, recurse );
                if ( completion >= 0 )
                {
                    completingMilestones++;
                    milestoneCompletion += completion;
                }
            }
        }

        Milestone next = getNextMilestoneForProject( project );
        if ( next == null && completingMilestones == 0 )
        {
            return -1;
        }

        if ( next != null )
        {
            completingMilestones++;
            milestoneCompletion += next.getCompleteness();
        }

        return milestoneCompletion / completingMilestones;
    }

    public Milestone getNextMilestoneForProject( Project project )
    {
        Session session = ( (HibernateStorage) Manager.getStorageInstance() ).getHibernateSession();
        Query q = session.createQuery( "from Milestone m where name.project.id = :pid and completed is null and due is not null order by due" );
        q.setString( "pid", project.getId() );
        List<Milestone> list = q.list();

        if ( list.size() == 0 )
        {
            q = session.createQuery( "from Milestone m where name.project.id = :pid and completed is null" );
            q.setString( "pid", project.getId() );
            list = q.list();

            if ( list.size() == 0 )
            {
                return null;
            }
        }

        return list.get( 0 );
    }

    private int getDivisions()
    {
        if ( isMonth() )
        {
            return 28;
        }

        return 12;
    }

    protected String getTitle()
    {
        if ( isMonth() )
        {
            return "Activity This Month";
        }
        else
        {
            return "Activity This Year";
        }
    }

    protected String getTimeCaption()
    {
        if ( isMonth() )
        {
            return "days ago";
        }
        else
        {
            return "months ago";
        }
    }

    private void drawString( Graphics g, String title, int w, int h, int pad, float align )
    {
        drawString( g, title, w, h, pad, align, 0.0 );
    }

    private void drawString( Graphics g, String title, int w, int h, int pad, float align, double rotate )
    {
        AffineTransform oldTransform = ( (Graphics2D) g ).getTransform();
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
        ( (Graphics2D) g ).setTransform( oldTransform );
    }
}
