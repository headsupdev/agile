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

import junit.framework.TestCase;
import org.headsupdev.agile.api.MavenDependency;

import java.util.List;

/**
 * Testing some maven project serialisation
 * <p/>
 * Created: 09/02/2014
 *
 * @author Andrew Williams
 * @since 2.0
 */
public class StoredMavenTwoProjectTest
        extends TestCase
{
    public void testDependencyLoading()
    {
        final String depString = "commons-codec:commons-codec:null:jar|com.google.android:android:null:jar";
        StoredMavenTwoProject project = new StoredMavenTwoProject();
        project.dependencies = depString;

        List<MavenDependency> deps = project.getDependencies();
        assertEquals( 2, deps.size() );
        assertEquals( "commons-codec", deps.get( 0 ).getGroupId() );
        assertEquals( "commons-codec", deps.get( 0 ).getArtifactId() );
        assertEquals( "jar", deps.get( 0 ).getType() );

        assertEquals( "com.google.android", deps.get( 1 ).getGroupId() );
        assertEquals( "android", deps.get( 1 ).getArtifactId() );
        assertEquals( "jar", deps.get( 1 ).getType() );
    }

    public void testDependencyLoadingLegacy()
    {
        // Checking no crashes as we transition
        final String depString = "commons-codec:commons-codec:null:jar,com.google.android:android:null:jar";
        StoredMavenTwoProject project = new StoredMavenTwoProject();
        project.dependencies = depString;

        List<MavenDependency> deps = project.getDependencies();
        assertTrue( deps.size() > 0 );
    }

    public void testModuleParse()
    {
        final String moduleString = "module1|module2";
        StoredMavenTwoProject project = new StoredMavenTwoProject();
        project.modules = moduleString;

        List<String> modules = project.getModules();
        assertEquals( 2, modules.size() );
        assertEquals( "module1", modules.get( 0 ) );
    }
}
