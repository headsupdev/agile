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

package org.headsupdev.agile.storage;

import org.headsupdev.agile.api.ConfigurationItem;
import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.api.PropertyTree;
import org.headsupdev.agile.api.User;
import org.headsupdev.agile.storage.issues.Duration;
import org.headsupdev.agile.storage.issues.DurationWorked;
import org.headsupdev.agile.storage.issues.Issue;
import junit.framework.TestCase;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;

/**
 * TODO Add documentation
 * <p/>
 * Created: 30/04/2012
 *
 * @author roberthewitt
 * @since 2.0-alpha-2-SNAPSHOT
 */
public class DurationWorkedUtilTest
        extends TestCase
{

    public void testLastEstimateForDayExcludeInitialEstimates()
            throws Exception
    {
        Issue issue = new Issue( getProject( "true" ) );
        Calendar calendar = Calendar.getInstance();
        calendar.set( Calendar.YEAR, 2012 );
        calendar.set( Calendar.MONTH, 2 );
        calendar.set( Calendar.DAY_OF_MONTH, 28 );          // wednesday 28th March 2012
        issue.setCreated( calendar.getTime() );             // set created date to today
        issue.setTimeEstimate( new Duration( 5 ) );         // set initial time estimate to 5hrs
        issue.setIncludeInInitialEstimates( false );        // set include in OriginalEstimates to false

        // set some worked time against issue creation -2 days
        DurationWorked durationWorked = new DurationWorked();
        durationWorked.setWorked( new Duration( 1 ) );
        durationWorked.setUpdatedRequired( new Duration( 3 ) );
        calendar.setTime( issue.getCreated() );
        calendar.add( Calendar.DATE, -2 );
        durationWorked.setDay( calendar.getTime() );
        durationWorked.setIssue( issue );
        durationWorked.setUser( null );
        issue.getTimeWorked().add( durationWorked );

        // set some worked time against issue creation  +2 days
        durationWorked = new DurationWorked();
        durationWorked.setWorked( new Duration( 2 ) );
        durationWorked.setUpdatedRequired( new Duration( 2 ) );
        calendar.setTime( issue.getCreated() );
        calendar.add( Calendar.DATE, 2 );
        durationWorked.setDay( calendar.getTime() );
        durationWorked.setIssue( issue );
        durationWorked.setUser( null );
        issue.getTimeWorked().add( durationWorked );

        // issue creation date -3
        calendar.setTime( issue.getCreated() );
        calendar.add( Calendar.DATE, -3 );
        Duration duration = DurationWorkedUtil.lastEstimateForDay( issue, calendar.getTime() );
        assertTrue( duration != null );
        assertEquals( new Integer( 0 ), duration.getTime() );

        // issue creation date -2
        calendar.add( Calendar.DATE, 1 );
        duration = DurationWorkedUtil.lastEstimateForDay( issue, calendar.getTime() );
        assertTrue( duration != null );
        assertEquals( new Integer( 3 ), duration.getTime() );

        // issue creation date -1
        calendar.add( Calendar.DATE, 1 );
        duration = DurationWorkedUtil.lastEstimateForDay( issue, calendar.getTime() );
        assertTrue( duration != null );
        assertEquals( new Integer( 3 ), duration.getTime() );

        // issue created date ...
        calendar.add( Calendar.DATE, 1 );
        duration = DurationWorkedUtil.lastEstimateForDay( issue, calendar.getTime() );
        assertTrue( duration != null );
        assertEquals( new Integer( 3 ), duration.getTime() );

        // issue created date +1
        calendar.add( Calendar.DATE, 1 );
        duration = DurationWorkedUtil.lastEstimateForDay( issue, calendar.getTime() );
        assertTrue( duration != null );
        assertEquals( new Integer( 3 ), duration.getTime() );

        // issue created date +2
        calendar.add( Calendar.DATE, 1 );
        duration = DurationWorkedUtil.lastEstimateForDay( issue, calendar.getTime() );
        assertTrue( duration != null );
        assertEquals( new Integer( 2 ), duration.getTime() );

        // issue created date +3
        calendar.add( Calendar.DATE, 1 );
        duration = DurationWorkedUtil.lastEstimateForDay( issue, calendar.getTime() );
        assertTrue( duration != null );
        assertEquals( new Integer( 2 ), duration.getTime() );
    }


    public void testLastEstimateForDayIncludeInitialEstimates()
            throws Exception
    {
        Issue issue = new Issue( getProject( "true" ) );
        Calendar calendar = Calendar.getInstance();
        calendar.set( Calendar.YEAR, 2012 );
        calendar.set( Calendar.MONTH, 2 );
        calendar.set( Calendar.DAY_OF_MONTH, 28 );          // wednesday 28th March 2012
        issue.setCreated( calendar.getTime() );             // set created date to today
        issue.setTimeEstimate( new Duration( 5 ) );         // set initial time estimate to 5hrs
        issue.setIncludeInInitialEstimates( true );         // set include in OriginalEstimates to true

        // set some worked time against issue creation -2 days
        DurationWorked durationWorked = new DurationWorked();
        durationWorked.setWorked( new Duration( 1 ) );
        durationWorked.setUpdatedRequired( new Duration( 3 ) );
        calendar.setTime( issue.getCreated() );
        calendar.add( Calendar.DATE, -2 );
        durationWorked.setDay( calendar.getTime() );
        durationWorked.setIssue( issue );
        durationWorked.setUser( null );
        issue.getTimeWorked().add( durationWorked );

        // set some worked time against issue creation +2 days
        durationWorked = new DurationWorked();
        durationWorked.setWorked( new Duration( 2 ) );
        durationWorked.setUpdatedRequired( new Duration( 2 ) );
        calendar.setTime( issue.getCreated() );
        calendar.add( Calendar.DATE, 2 );
        durationWorked.setDay( calendar.getTime() );
        durationWorked.setIssue( issue );
        durationWorked.setUser( null );
        issue.getTimeWorked().add( durationWorked );


        // issue creation date -3
        calendar.setTime( issue.getCreated() );
        calendar.add( Calendar.DATE, -3 );
        Duration duration = DurationWorkedUtil.lastEstimateForDay( issue, calendar.getTime() );
        assertTrue( duration != null );
        assertEquals( new Integer( 5 ), duration.getTime() );

        // issue creation date -2
        calendar.add( Calendar.DATE, 1 );
        duration = DurationWorkedUtil.lastEstimateForDay( issue, calendar.getTime() );
        assertTrue( duration != null );
        assertEquals( new Integer( 3 ), duration.getTime() );

        // issue creation date -1
        calendar.add( Calendar.DATE, 1 );
        duration = DurationWorkedUtil.lastEstimateForDay( issue, calendar.getTime() );
        assertTrue( duration != null );
        assertEquals( new Integer( 3 ), duration.getTime() );

        // issue created date ...
        calendar.add( Calendar.DATE, 1 );
        duration = DurationWorkedUtil.lastEstimateForDay( issue, calendar.getTime() );
        assertTrue( duration != null );
        assertEquals( new Integer( 3 ), duration.getTime() );

        // issue created date +1
        calendar.add( Calendar.DATE, 1 );
        duration = DurationWorkedUtil.lastEstimateForDay( issue, calendar.getTime() );
        assertTrue( duration != null );
        assertEquals( new Integer( 3 ), duration.getTime() );

        // issue created date +2
        calendar.add( Calendar.DATE, 1 );
        duration = DurationWorkedUtil.lastEstimateForDay( issue, calendar.getTime() );
        assertTrue( duration != null );
        assertEquals( new Integer( 2 ), duration.getTime() );

        // issue created date +3
        calendar.add( Calendar.DATE, 1 );
        duration = DurationWorkedUtil.lastEstimateForDay( issue, calendar.getTime() );
        assertTrue( duration != null );
        assertEquals( new Integer( 2 ), duration.getTime() );
    }


    public void testLastEstimateForDay_IssueCreatedBeforeDateRespect()
            throws Exception
    {
        Issue issue = new Issue( getProject( "true" ) );
        Calendar calendar = Calendar.getInstance();
        calendar.set( Calendar.YEAR, 2012 );
        calendar.set( Calendar.MONTH, 2 );
        calendar.set( Calendar.DAY_OF_MONTH, 28 );          // wednesday 28th March 2012
        issue.setCreated( calendar.getTime() );             // set created date to today
        issue.setTimeEstimate( new Duration( 5 ) );         // set initial time estimate to 5hrs
        issue.setIncludeInInitialEstimates( true );         // set include in OriginalEstimates to true

        // set some worked time against issue creation +2 days
        DurationWorked durationWorked = new DurationWorked();
        durationWorked.setWorked( new Duration( 1 ) );
        durationWorked.setUpdatedRequired( new Duration( 3 ) );
        calendar.setTime( issue.getCreated() );
        calendar.add( Calendar.DATE, 2 );
        durationWorked.setDay( calendar.getTime() );
        durationWorked.setIssue( issue );
        durationWorked.setUser( null );
        issue.getTimeWorked().add( durationWorked );

        // issue creation date -1
        calendar.setTime( issue.getCreated() );
        calendar.add( Calendar.DATE, -1 );
        Duration duration = DurationWorkedUtil.lastEstimateForDay( issue, calendar.getTime() );
        assertTrue( duration != null );
        assertEquals( new Integer( 5 ), duration.getTime() );

        // issue creation date
        calendar.add( Calendar.DATE, 1 );
        duration = DurationWorkedUtil.lastEstimateForDay( issue, calendar.getTime() );
        assertTrue( duration != null );
        assertEquals( new Integer( 5 ), duration.getTime() );

        // issue creation date +1
        calendar.add( Calendar.DATE, 1 );
        duration = DurationWorkedUtil.lastEstimateForDay( issue, calendar.getTime() );
        assertTrue( duration != null );
        assertEquals( new Integer( 5 ), duration.getTime() );

        // issue creation date +2
        calendar.add( Calendar.DATE, 1 );
        duration = DurationWorkedUtil.lastEstimateForDay( issue, calendar.getTime() );
        assertTrue( duration != null );
        assertEquals( new Integer( 3 ), duration.getTime() );

        // issue creation date +3
        calendar.add( Calendar.DATE, 1 );
        duration = DurationWorkedUtil.lastEstimateForDay( issue, calendar.getTime() );
        assertTrue( duration != null );
        assertEquals( new Integer( 3 ), duration.getTime() );
    }


    public void testLastEstimateForDay_IssueCreatedBeforeDateNORespect()
            throws Exception
    {
        Issue issue = new Issue( getProject( "true" ) );
        Calendar calendar = Calendar.getInstance();
        calendar.set( Calendar.YEAR, 2012 );
        calendar.set( Calendar.MONTH, 2 );
        calendar.set( Calendar.DAY_OF_MONTH, 28 );          // wednesday 28th March 2012
        issue.setCreated( calendar.getTime() );             // set created date to today
        issue.setTimeEstimate( new Duration( 5 ) );         // set initial time estimate to 5hrs
        issue.setIncludeInInitialEstimates( false );        // set include in OriginalEstimates to true

        // set some worked time against issue creation +2 days
        DurationWorked durationWorked = new DurationWorked();
        durationWorked.setWorked( new Duration( 1 ) );
        durationWorked.setUpdatedRequired( new Duration( 3 ) );
        calendar.setTime( issue.getCreated() );
        calendar.add( Calendar.DATE, 2 );
        durationWorked.setDay( calendar.getTime() );
        durationWorked.setIssue( issue );
        durationWorked.setUser( null );
        issue.getTimeWorked().add( durationWorked );

        // issue creation date -1
        calendar.setTime( issue.getCreated() );
        calendar.add( Calendar.DATE, -1 );
        Duration duration = DurationWorkedUtil.lastEstimateForDay( issue, calendar.getTime() );
        assertTrue( duration != null );
        assertEquals( new Integer( 0 ), duration.getTime() );

        // issue creation date
        calendar.add( Calendar.DATE, 1 );
        duration = DurationWorkedUtil.lastEstimateForDay( issue, calendar.getTime() );
        assertTrue( duration != null );
        assertEquals( new Integer( 5 ), duration.getTime() );

        // issue creation date +1
        calendar.add( Calendar.DATE, 1 );
        duration = DurationWorkedUtil.lastEstimateForDay( issue, calendar.getTime() );
        assertTrue( duration != null );
        assertEquals( new Integer( 5 ), duration.getTime() );

        // issue creation date +2
        calendar.add( Calendar.DATE, 1 );
        duration = DurationWorkedUtil.lastEstimateForDay( issue, calendar.getTime() );
        assertTrue( duration != null );
        assertEquals( new Integer( 3 ), duration.getTime() );

        // issue creation date +3
        calendar.add( Calendar.DATE, 1 );
        duration = DurationWorkedUtil.lastEstimateForDay( issue, calendar.getTime() );
        assertTrue( duration != null );
        assertEquals( new Integer( 3 ), duration.getTime() );
    }

    private Project getProject( final String ignoreWeekend )
    {
        return new Project()
        {
            public String getId()
            {
                return null;
            }

            public String getName()
            {
                return null;
            }

            public String getAlias()
            {
                return null;
            }

            public void setAlias( String alias )
            {
            }

            public String getScm()
            {
                return null;
            }

            public String getScmUsername()
            {
                return null;
            }

            public String getScmPassword()
            {
                return null;
            }

            public Set<Project> getChildProjects()
            {
                return null;
            }

            public Set<Project> getChildProjects( boolean withDisabled )
            {
                return null;
            }

            public Project getParent()
            {
                return null;
            }

            public String getRevision()
            {
                return null;
            }

            public void setRevision( String revision )
            {
            }

            public Date getImported()
            {
                return null;
            }

            public Date getUpdated()
            {
                return null;
            }

            public Set<User> getUsers()
            {
                return null;
            }

            public String getTypeName()
            {
                return null;
            }

            public void fileModified( String path, File file )
            {
            }

            public boolean foundMetadata( File directory )
            {
                return false;
            }

            public PropertyTree getConfiguration()
            {
                return null;
            }

            public String getConfigurationValue( ConfigurationItem item )
            {
                return ignoreWeekend;
            }

            public int compareTo( Project project )
            {
                return 0;
            }
        };
    }
}
