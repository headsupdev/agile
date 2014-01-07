package org.headsupdev.agile.storage;

import junit.framework.TestCase;
import org.headsupdev.agile.api.*;
import org.headsupdev.agile.storage.project.CocoaPodDependency;

import java.util.LinkedList;
import java.util.List;

/**
 * Testing some xcode project serialisation
 * <p/>
 * Created: 05/01/2014
 *
 * @author Andrew Williams
 * @since 2.0
 */
public class StoredXCodeProjectTest
        extends TestCase
{
    public void testDependencyLoading()
    {
        final String depString = "name1:version1,name2:version2,name3";
        StoredXCodeProject project = new StoredXCodeProject();
        project.dependencies = depString;

        List<XCodeDependency> deps = project.getDependencies();
        assertEquals( 3, deps.size() );
        assertEquals( "name1", deps.get( 0 ).getName() );
        assertEquals( "version2", deps.get( 1 ).getVersion() );
        assertEquals( "name3", deps.get( 2 ).getName() );
        assertEquals( XCodeDependency.UNVERSIONED, deps.get( 2 ).getVersion() );
    }

    public void testDependencyLoadingLegacy()
    {
        // note the trailing colon here which older versions of the serialisation would leave...
        final String depString = "name1:version1,name2:version2,name3:";
        StoredXCodeProject project = new StoredXCodeProject();
        project.dependencies = depString;

        List<XCodeDependency> deps = project.getDependencies();
        assertEquals( 3, deps.size() );
        assertEquals( XCodeDependency.UNVERSIONED, deps.get( 2 ).getVersion() );
    }

    public void testDependencyStoring()
    {
        List<XCodeDependency> deps = new LinkedList<XCodeDependency>();
        deps.add( new CocoaPodDependency( "name1", "version1" ) );
        deps.add( new CocoaPodDependency( "name2", "version2" ) );
        deps.add( new CocoaPodDependency( "name3" ) );

        StoredXCodeProject project = new StoredXCodeProject();
        project.setDependencies( deps );
        assertEquals( "name1:version1,name2:version2,name3", project.dependencies );
    }
}
