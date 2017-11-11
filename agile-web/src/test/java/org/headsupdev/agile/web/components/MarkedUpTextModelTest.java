/*
 * HeadsUp Agile
 * Copyright 2013 Heads Up Development.
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

package org.headsupdev.agile.web.components;

import junit.framework.TestCase;

/**
 * Testing various text markup functions
 *
 * @author Andrew Williams
 * @since 2.0
 */
public class MarkedUpTextModelTest
    extends TestCase
{
    public void testIsValidLink()
    {
        assertTrue( MarkedUpTextModel.isValidLink( "file:test.txt" ) );
        assertTrue( MarkedUpTextModel.isValidLink( "file:all:test.txt" ) );
        assertTrue( MarkedUpTextModel.isValidLink( "account:admin" ) );

        assertFalse( MarkedUpTextModel.isValidLink( "file::test.txt" ) );
        assertFalse( MarkedUpTextModel.isValidLink( "change:" ) );
        assertFalse( MarkedUpTextModel.isValidLink( "change::" ) );
        assertFalse( MarkedUpTextModel.isValidLink( ":243" ) );
        assertFalse( MarkedUpTextModel.isValidLink( "::243" ) );
        assertFalse( MarkedUpTextModel.isValidLink( "" ) );
        assertFalse( MarkedUpTextModel.isValidLink( null ) );
    }
}
