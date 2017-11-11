/*
 * HeadsUp Agile
 * Copyright 2014 Heads Up Development.
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

package org.headsupdev.agile.storage.issues;

import junit.framework.TestCase;

import java.util.Date;

/**
 * Tests for Milestone
 *
 * @author gordonedwards
 * @since 2.1
 */
public class MilestoneTest
        extends TestCase
{
    public void testStartDateNull()
    {
        Milestone milestone = new Milestone();
        assertNull( milestone.getStartDate() );
    }

    public void testValidStartDate()
    {
        Milestone milestone = new Milestone();
        Date date = new Date();
        milestone.setStartDate( date );
        assertEquals( date, milestone.getStartDate() );
    }

    public void testDueDateNull()
    {
        Milestone milestone = new Milestone();
        assertNull( milestone.getDueDate() );
    }

    public void testValidDueDate()
    {
        Milestone milestone = new Milestone();
        Date date = new Date();
        milestone.setDueDate( date );
        assertEquals( date, milestone.getDueDate() );
    }

    public void testInvalidTimePeriod()
    {
        Milestone milestone = new Milestone();
        Date date = new Date();
        milestone.setStartDate( date );
        milestone.setDueDate( null );
        assertFalse( milestone.hasValidTimePeriod() );
    }

    public void testValidTimePeriod()
    {
        Milestone milestone = new Milestone();
        Date startDate = new Date();
        milestone.setStartDate( startDate );
        Date dueDate = new Date( startDate.getTime() + 1000 );
        milestone.setDueDate( dueDate );
        assertTrue( milestone.hasValidTimePeriod() );
    }

    public void testInvertedTimePeriod()
    {
        Milestone milestone = new Milestone();
        Date startDate = new Date();
        milestone.setStartDate( startDate );
        Date dueDate = new Date( startDate.getTime() - 1000 );
        milestone.setDueDate( dueDate );
        assertFalse( milestone.hasValidTimePeriod() );
    }

}
