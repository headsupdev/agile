package org.headsupdev.agile.storage;

import junit.framework.TestCase;
import org.headsupdev.agile.api.*;
import org.headsupdev.agile.api.logging.Logger;
import org.headsupdev.agile.api.service.ScmService;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * TODO: Document me
 * <p/>
 * Created: 05/01/2014
 *
 * @author Andrew Williams
 * @since 1.2
 */
public class StoredXCodeProjectTest extends TestCase
{
    private StoredXCodeProject project;

    protected void setUp()
            throws URISyntaxException
    {
        Manager.setInstance( new Manager() {
            @Override
            public void addProjectListener(ProjectListener listener) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void removeProjectListener(ProjectListener listener) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void fireProjectAdded(Project project) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void fireProjectModified(Project project) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void fireProjectFileModified(Project project, String path, File file) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void fireEventAdded(Event event) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public Map<String, LinkProvider> getLinkProviders() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public List<Task> getTasks() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void addTask(Task task) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void removeTask(Task task) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public Date getInstallDate() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public double getInstallVersion() {
                return 0;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void setupCompleted() {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public boolean isUpdateAvailable() {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            protected Logger getLoggerForComponent(String component) {
                return new Logger(){
                    @Override
                    public void debug(String error) {
                        //To change body of implemented methods use File | Settings | File Templates.
                    }

                    @Override
                    public void info(String error) {
                        //To change body of implemented methods use File | Settings | File Templates.
                    }

                    @Override
                    public void warn(String error) {
                        //To change body of implemented methods use File | Settings | File Templates.
                    }

                    @Override
                    public void error(String error) {
                        //To change body of implemented methods use File | Settings | File Templates.
                    }

                    @Override
                    public void error(String error, Throwable t) {
                        //To change body of implemented methods use File | Settings | File Templates.
                    }

                    @Override
                    public void fatalError(String fatal) {
                        //To change body of implemented methods use File | Settings | File Templates.
                    }

                    @Override
                    public void fatalError(String fatal, Throwable t) {
                        //To change body of implemented methods use File | Settings | File Templates.
                    }
                };
            }

            @Override
            public ScmService getScmService() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }
        });
        URL url = Thread.currentThread().getContextClassLoader().getResource( "demo/project.pbxproj" );
        project = new StoredXCodeProject( new File( url.toURI() ) );
    }

    public void testTargetNameFound()
    {
        assertEquals( "iPhone Demo", project.getName() );
        assertEquals( "com.yourcompany.iPhone_Demo", project.getBundleId() );
    }

    public void testEncoding()
    {
        assertEquals( "iPhone_Demo", project.getRFC1034( "iPhone Demo" ) );
    }
}
