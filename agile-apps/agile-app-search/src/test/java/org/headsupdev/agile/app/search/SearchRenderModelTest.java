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

package org.headsupdev.agile.app.search;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Simple tests to check some complex search rendering cases are correctly handled.
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 2.1
 */
public class SearchRenderModelTest
        extends TestCase
{
    public void testSubstringMatches()
            throws Exception
    {
        SearchRenderModel model = new SearchRenderModel( "", new HashMap<String, List<String>>(), new HashMap<String, Integer>() );

        StringBuffer out = new StringBuffer();
        model.renderField( "summary", "A substring string test", Arrays.asList( "string", "substring" ), out );
        // if this test completes we are happy - it has a habit of crashing on render
    }

    public void testOverlappingStringMatches()
            throws Exception
    {
        SearchRenderModel model = new SearchRenderModel( "", new HashMap<String, List<String>>(), new HashMap<String, Integer>() );

        StringBuffer out = new StringBuffer();
        model.renderField( "summary", "A stringer test", Arrays.asList( "string", "ringer" ), out );
        // if this test completes we are happy - it has a habit of crashing on render
    }
}
