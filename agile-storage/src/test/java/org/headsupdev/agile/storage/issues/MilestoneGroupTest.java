package org.headsupdev.agile.storage.issues;
/*
 * HeadsUp Agile
 * Copyright 2014 Heads Up Development Ltd.
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


import junit.framework.TestCase;
import org.headsupdev.agile.storage.StoredCommandLineProject;

import java.util.Date;

/**
 * Tests for milestone groups
 *
 * @author gordonedwards
 * @since 2.1
 */
public class MilestoneGroupTest
        extends TestCase
{
    private MilestoneGroup milestoneGroup;
    private Milestone[] milestones;

    public void setUp()
            throws Exception
    {
        super.setUp();
        StoredCommandLineProject proj = new StoredCommandLineProject( "test", "Test" );
        milestoneGroup = new MilestoneGroup( "group", proj );
        milestones = new Milestone[3];

        for ( int i = 0; i < milestones.length; i++ )
        {
            milestones[i] = new Milestone( "milestone" + i, proj );
            Date milestoneStartDate = new Date( System.currentTimeMillis() + ( i * 100 ) );
            Date milestoneDueDate = new Date( milestoneStartDate.getTime() + 200 );

            milestones[i].setGroup( milestoneGroup );
            milestoneGroup.addMilestone( milestones[i] );

            milestones[i].setStartDate( milestoneStartDate );
            milestones[i].setDueDate( milestoneDueDate );
        }
    }

    public void testStartDateNotNull()
    {
        assertNotNull( milestoneGroup.getStartDate() );
    }

    public void testValidStartDate()
    {
        Date milestoneGroupStartDate = milestoneGroup.getStartDate();
        for ( Milestone milestone : milestones )
        {
            Date milestoneStartDate = milestone.getStartDate();
            if ( milestoneStartDate.before( milestoneGroupStartDate ) )
            {
                fail();
            }
        }
    }

    public void testDueDateNotNull()
    {
        assertNotNull( milestoneGroup.getDueDate() );
    }

    public void testValidDueDate()
    {
        Date milestoneGroupDueDate = milestoneGroup.getDueDate();
        for ( Milestone milestone : milestones )
        {
            Date milestoneDueDate = milestone.getDueDate();
            if ( milestoneDueDate.after( milestoneGroupDueDate ) )
            {
                fail();
            }
        }
    }

    public void testInvalidTimePeriod()
    {
        for ( Milestone milestone : milestones )
        {
            Date milestoneStartDate = new Date();
            milestone.setStartDate( milestoneStartDate );
            milestone.setDueDate( null );
        }

        assertFalse( milestoneGroup.hasValidTimePeriod() );
    }

    public void testValidTimePeriod()
    {
        Date milestoneStartDate = new Date();
        Date milestoneDueDate = new Date( milestoneStartDate.getTime() + 100 );

        milestones[0].setStartDate( milestoneStartDate );
        milestones[0].setDueDate( milestoneDueDate );

        assertTrue( milestoneGroup.hasValidTimePeriod() );
    }

    public void testInvertedTimePeriod()
    {
        Date groupDueDate = milestoneGroup.getDueDate();

        for ( Milestone milestone : milestones )
        {
            Date milestoneStartDate = new Date( groupDueDate.getTime() + 100 );
            milestone.setStartDate( milestoneStartDate );
            milestone.setDueDate( groupDueDate );
        }
        assertFalse( milestoneGroup.hasValidTimePeriod() );
    }
}
