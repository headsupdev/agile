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

package org.headsupdev.agile.api;

import junit.framework.TestCase;

import java.util.Map;
import java.util.HashMap;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Simple tests to ensure the validity of our PropertyTree storage class.
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class PropertyTreeTest
    extends TestCase
{
    private PropertyTree test;

    protected void setUp() throws Exception {
        Map<String, String> properties = new HashMap<String, String>();

        properties.put( "test", "value" );
        properties.put( "test2", "value2" );
        properties.put( "mynode.test", "leafValue" );
        properties.put( "mynode.test2", "leafValue2" );

        test = new PropertyTree( properties );
    }

    public void testLoad()
    {
        assertNotNull( test );
        assertNotNull( test.getSubTree( "mynode" ) );

        assertEquals( "value", test.getProperty( "test" ) );
        assertEquals( "leafValue", test.getSubTree( "mynode" ).getProperty( "test" ) );
    }

    public void testPrint()
    {
        StringWriter string = new StringWriter();
        test.list( new PrintWriter( string ) );

        System.out.println( string.toString() );
    }

    public void testSet()
    {
        test.setProperty( "myname", "myval" );
        assertEquals( "myval", test.getProperty( "myname" ) );
    }

    public void testAddSub()
    {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put( "def", "123" );

        assertEquals( 1, test.getSubTreeIds().size() );
        PropertyTree sub = new PropertyTree( properties );
        test.addSubTree( "abc", sub );
        assertEquals( 2, test.getSubTreeIds().size() );

        assertEquals( "123", test.getSubTree( "abc" ).getProperty( "def" ) );
    }

    public void testReplaceSub()
    {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put( "def", "123" );
        assertEquals( 1, test.getSubTreeIds().size() );
        PropertyTree oldSub = new PropertyTree( properties );

        properties = new HashMap<String, String>();
        properties.put( "def", "456" );
        assertEquals( 1, test.getSubTreeIds().size() );
        PropertyTree sub = new PropertyTree( properties );

        test.addSubTree( "abc", oldSub );
        assertEquals( 2, test.getSubTreeIds().size() );
        assertEquals( "123", test.getSubTree( "abc" ).getProperty( "def" ) );

        test.removeSubTree( "abc" );
        test.addSubTree( "abc", sub );
        assertEquals( "456", test.getSubTree( "abc" ).getProperty( "def" ) );
    }

    public void testRemoveSub()
    {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put( "def", "123" );

        assertEquals( 1, test.getSubTreeIds().size() );
        PropertyTree sub = new PropertyTree( properties );
        test.addSubTree( "abc", sub );
        assertEquals( 2, test.getSubTreeIds().size() );

        assertEquals( "123", test.removeSubTree( "abc" ).getProperty( "def" ) );
    }

    public void testRemoveSubWithSub()
    {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put( "def", "123" );

        assertEquals( 1, test.getSubTreeIds().size() );
        PropertyTree sub = new PropertyTree( properties );
        test.addSubTree( "abc", sub );
        assertEquals( 2, test.getSubTreeIds().size() );

        PropertyTree subsub = new PropertyTree( properties );
        sub.addSubTree( "ghi", subsub );
        assertEquals( 1, sub.getSubTreeIds().size() );

        assertEquals( "123", test.getSubTree( "abc" ).getSubTree( "ghi" ).getProperty( "def" ) );
        assertEquals( "123", test.removeSubTree( "abc" ).getSubTree( "ghi" ).getProperty( "def" ) );
    }

    public void testSetSub()
    {
        test.getSubTree( "mynode" ).setProperty( "myname", "myval" );
        assertEquals( "myval", test.getSubTree( "mynode" ).getProperty( "myname" ) );
    }

    public void testGetRecurse()
    {
        assertEquals( "leafValue", test.getProperty( "mynode.test" ) );
    }

    public void testSetRecurse()
    {
        test.setProperty( "abc.123", "456" );
        assertEquals( "456", test.getProperty( "abc.123" ) );
    }

    public void testGetMissingSub()
    {
        test.getSubTree( "missing" ).getSubTree( "tree" ).getProperty( "child" );

        PropertyTree aMissing = test.getSubTree( "somethingmissing" );

        aMissing.setProperty( "key", "value" );
        assertEquals( aMissing.getProperty( "key" ), "value" );
    }

    public void testBadKeys()
    {
        test.setProperty( "A.key:that+always.breaks", "result" );
        assertEquals( "result", test.getProperty( "A.key:that+always.breaks" ) );
        assertEquals( "result", test.getSubTree( "A" ).getSubTree( "key:that+always" ).getProperty( "breaks" ) );
    }
}
