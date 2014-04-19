package org.headsupdev.agile.app.ci.builders;

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
import org.headsupdev.agile.api.PropertyTree;
import org.headsupdev.agile.app.ci.CIApplication;

import java.util.ArrayList;

/**
 * Tests for the XCode build handler
 * <p/>
 * Created: 19/04/2014
 *
 * @author Andrew Williams
 * @since 2.0.1
 */
public class XCodeBuildHandlerTest extends TestCase
{
    private XCodeBuildHandler handler;

    protected void setUp()
    {
        handler = new XCodeBuildHandler();
    }

    public void testDestinationParameterMatchesSimulatorVersion()
    {
        PropertyTree config = new PropertyTree();
        config.setProperty( CIApplication.CONFIGURATION_XCODE_SDK.getKey(), "iphonesimulator6.1" );
        ArrayList<String> commands = new ArrayList<String>();

        handler.appendTestDestinationCommand( config, commands );
        assertTrue( commands.contains( "-destination" ) );
        assertTrue( commands.contains( "'platform=iOS Simulator,name=iPhone Retina (4-inch),OS=6.1'" ) );
    }
}
