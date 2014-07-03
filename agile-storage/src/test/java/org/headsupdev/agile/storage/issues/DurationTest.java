/*
 * HeadsUp Agile
 * Copyright 2009-2014 Heads Up Development Ltd.
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

/**
 * Lots of tests for the Duration class
 *
 * @author Andrew Williams
 * @since 1.0
 */
public class DurationTest
        extends TestCase
{
    public void testHoursToString()
    {
        Duration hours = new Duration( 5 );
        TestCase.assertEquals( "Wrong rendering for hours duration", "5h", hours.toString() );

        hours = new Duration( 5, Duration.UNIT_HOURS );
        TestCase.assertEquals( "Wrong rendering for hours duration", "5h", hours.toString() );
    }

    public void testDaysToString()
    {
        Duration hours = new Duration( 8 );
        TestCase.assertEquals( "Wrong rendering for days duration", "1d", hours.toString() );

        hours = new Duration( 1, Duration.UNIT_DAYS );
        TestCase.assertEquals( "Wrong rendering for days duration", "1d", hours.toString() );
    }

    public void testDaysHoursToString()
    {
        Duration hours = new Duration( 12 );
        TestCase.assertEquals( "Wrong rendering for days + hours duration", "1d 4h", hours.toString() );

        hours = new Duration( 12, Duration.UNIT_HOURS );
        TestCase.assertEquals( "Wrong rendering for days + hours duration", "1d 4h", hours.toString() );
    }

    public void testWeeksDaysHoursToString()
    {
        Duration hours = new Duration( 51 );
        TestCase.assertEquals( "Wrong rendering for weeks + days + hours duration", "1w 1d 3h", hours.toString() );

        hours = new Duration( 51, Duration.UNIT_HOURS );
        TestCase.assertEquals( "Wrong rendering for weeks + days + hours duration", "1w 1d 3h", hours.toString() );
    }

    public void testWeeksDaysToString()
    {
        Duration hours = new Duration( 48 );
        TestCase.assertEquals( "Wrong rendering for weeks + days duration", "1w 1d", hours.toString() );

        hours = new Duration( 6, Duration.UNIT_DAYS );
        TestCase.assertEquals( "Wrong rendering for weeks + days duration", "1w 1d", hours.toString() );
    }

    public void testWeeksHoursToString()
    {
        Duration hours = new Duration( 47 );
        TestCase.assertEquals( "Wrong rendering for weeks + hours duration", "1w 7h", hours.toString() );

        hours = new Duration( 47, Duration.UNIT_HOURS );
        TestCase.assertEquals( "Wrong rendering for weeks + hours duration", "1w 7h", hours.toString() );
    }

    public void testToStringWithZero()
    {
        Duration hours = new Duration( 0 );
        TestCase.assertEquals( "Should return 0h for 0", "0h", hours.toString() );

        hours = new Duration( -0 );
        TestCase.assertEquals( "Should return 0h for -0", "0h", hours.toString() );
    }

    public void testToFractionStringWithZero()
    {
        Duration hours = new Duration( 0.75 );
        TestCase.assertEquals( "Should not have a 0h prefix to a fraction", "¾h", hours.toHoursWithFractionString() );

        hours = new Duration( -0.75 );
        TestCase.assertEquals( "Should not have a 0h prefix to a fraction", "-¾h", hours.toHoursWithFractionString() );
    }

    public void testIsValidDurationFromStringWithSpaces()
    {
        String input = "   3d  53h    9m  ";
        input = input.replaceAll( "\\s", "" );
        TestCase.assertTrue( Duration.isValidDurationFromString( input ) );
    }

    public void testIsValidDurationFromStringEmptyString()
    {
        String input = "";
        input = input.replaceAll( "\\s", "" );
        TestCase.assertFalse( Duration.isValidDurationFromString( input ) );
    }

    public void testIsValidDurationFromStringInvalidCharacters()
    {
        String input = "   3d  53k  9m  ";
        input = input.replaceAll( "\\s", "" );
        TestCase.assertFalse( Duration.isValidDurationFromString( input ) );
    }

    public void testIsValidDurationFromStringDoubleHours()
    {
        String input = "3d53h9h9m";
        input = input.replaceAll( "\\s", "" );
        TestCase.assertFalse( Duration.isValidDurationFromString( input ) );
    }

    public void testDurationFromStringWithSpaces()
    {
        String input = "2d  67h 180m";
        Duration testDuration = null;
        try
        {
            testDuration = Duration.fromString( input );
        }
        catch ( IllegalArgumentException e )
        {
            fail();
        }
        Duration actualDuration = new Duration( 86 );
        TestCase.assertTrue( testDuration.equals( actualDuration ) );
    }

    public void testDurationFromString()
    {
        String input = "10w4h9m";
        Duration testDuration = null;
        try
        {
            testDuration = Duration.fromString( input );
        }
        catch ( IllegalArgumentException e )
        {
            fail();
        }
        Duration actualDuration = new Duration( 404.15 );
        TestCase.assertTrue( testDuration.equals( actualDuration ) );
    }

    public void testDurationFromStringInvalidCharacters()
    {
        String input = "2d  67k 180m";
        try
        {
            Duration testDuration = Duration.fromString( input );
            fail();
        }
        catch ( IllegalArgumentException e )
        {
        }
    }

    public void testDurationFromStringInvalidDoubleHours()
    {
        String input = "3d53h9h9m";
        try
        {
            Duration testDuration = Duration.fromString( input );
            fail();
        }
        catch ( IllegalArgumentException e )
        {
        }
    }

    public void testDurationToString()
    {
        Duration duration = new Duration( 24.0166666667 );
        TestCase.assertTrue( duration.toString().equals( "3d 1m" ) );
    }
}
