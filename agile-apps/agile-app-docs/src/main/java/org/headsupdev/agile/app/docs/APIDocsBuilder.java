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

package org.headsupdev.agile.app.docs;

import org.headsupdev.agile.api.*;
import org.headsupdev.agile.api.logging.Logger;
import org.headsupdev.support.java.FileUtil;
import org.headsupdev.support.java.IOUtil;
import org.headsupdev.support.java.StringUtil;

import java.io.*;
import java.util.*;

/**
 * A document builder that takes source files and creates API documentation
 * <p/>
 * Created: 31/05/2012
 *
 * @author Andrew Williams
 * @since 2.0
 */
public class APIDocsBuilder
    implements ProjectListener
{

    private static final List<Project> pendingBuilds = new LinkedList<Project>();

    private static boolean building = false;

    private Logger log = Manager.getLogger(getClass().getName());

    public void queueProject( Project project )
    {
        if ( !isProjectQueued( project ) )
        {
            synchronized ( pendingBuilds )
            {
                pendingBuilds.add( project );
            }

            buildProjects();
        }
    }


    public static boolean isProjectQueued( Project project )
    {
        synchronized( pendingBuilds )
        {
            return pendingBuilds.contains( project );
        }
    }

    protected void buildProjects()
    {
        if ( building )
        {
            return;
        }

        building = true;
        new Thread()
        {
            public void run()
            {
                try
                {
                    while ( pendingBuilds.size() > 0 )
                    {
                        Project project;
                        synchronized( pendingBuilds )
                        {
                            try
                            {
                                project = pendingBuilds.get( 0 );
                            }
                            catch ( NoSuchElementException e )
                            {
                                // changed whilst building - just ignore and try later
                                break;
                            }
                        }

                        buildProject( project );

                        synchronized( pendingBuilds )
                        {
                            try
                            {
                                pendingBuilds.remove( project );
                            }
                            catch ( NoSuchElementException e )
                            {
                                // changed whilst building - just ignore and try later
                                break;
                            }
                        }
                    }
                }
                catch ( Exception e )
                {
                    e.printStackTrace();
                }
                finally
                {
                    building = false;
                }
            }
        }.start();
    }

    private void buildProject( Project project )
    {
        Task apiTask = new APIDocBuildTask( project );
        Manager.getInstance().addTask( apiTask );

        Writer out = null;
        File doxyFile = null, header = null, footer = null;
        File output = null;
        try
        {
            doxyFile = org.headsupdev.agile.api.util.FileUtil.createTempFile( "doxyFile", "" );
            out = new BufferedWriter( new OutputStreamWriter( new FileOutputStream( doxyFile ) ) );

            Map<String, Object> params = new HashMap<String, Object>();
            params.put( "projectName", project.getName() );
            if ( project instanceof MavenTwoProject )
            {
                params.put( "projectVersion", ( (MavenTwoProject) project ).getVersion() );
            }
            else if ( project instanceof XCodeProject )
            {
                params.put( "projectVersion", ( (XCodeProject) project ).getVersion() );
            }
            else
            {
                params.put( "projectVersion", "" );
            }
            params.put( "projectSummary", "" );
            params.put( "inputDir", Manager.getStorageInstance().getWorkingDirectory( project ).getAbsolutePath() );
            params.put( "date", new Date().toString() );

            output = org.headsupdev.agile.api.util.FileUtil.createTempDir( "docs", "" );
            params.put( "outputDir", output.getAbsolutePath() );
            params.put( "currentDir", doxyFile.getParentFile().getAbsolutePath() );

            out.write( StringUtil.format( DocsApplication.class.getResource( "Doxyfile" ).openStream(),
                    "{", "}", params ) );

            header = new File( doxyFile.getParentFile(), "doxyheader.html" );
            String headerContent = StringUtil.format( DocsApplication.class.getResource( "doxyheader.html" ).openStream(),
                    "{", "}", params );
            FileUtil.writeToFile( headerContent, header );

            footer = new File( doxyFile.getParentFile(), "doxyfooter.html" );
            String footerContent = StringUtil.format( DocsApplication.class.getResource( "doxyfooter.html" ).openStream(),
                    "{", "}", params );
            FileUtil.writeToFile( footerContent, footer );

            runDoxygen( doxyFile, output, project );
        }
        catch ( IOException e )
        {
            log.error( "Failed to set up doxygen input", e );
        }
        finally
        {
            IOUtil.close( out );

            if ( doxyFile != null )
            {
                doxyFile.delete();
                footer.delete();
                header.delete();
            }

            Manager.getInstance().removeTask( apiTask );
        }
    }

    private void runDoxygen( File doxyFile, File outDir, Project project )
    {
        Process p = null;
         try {
             log.debug( "Running doxygen with file: " + doxyFile.getAbsolutePath() );
             String[] args = { "doxygen", doxyFile.getAbsolutePath() };
             p = Runtime.getRuntime().exec( args );

             int ret = p.waitFor();
             if ( ret != 0 )
             {
                 log.warn( "Doxygen build failed" );
             }
         }
         catch ( IOException e )
         {
             if ( e.getMessage() != null && e.getMessage().contains( "No such file or directory" ) )
             {
                 log.warn( "Doxygen missing" );
             }
             else
             {
                e.printStackTrace();
             }
         }
         catch ( InterruptedException e )
         {
             // ignore
         }
         finally
         {
             if ( p != null )
             {
                 IOUtil.close( p.getErrorStream() );
                 IOUtil.close( p.getOutputStream() );
                 IOUtil.close( p.getInputStream() );
             }
         }

         File apiDocs = DocsApplication.getApiDir( project );
         if ( apiDocs.exists() )
         {
             File apiDocsOld = new File( apiDocs.getParentFile(), "api-old" );
             apiDocs.renameTo( apiDocsOld );
             new File( outDir, "html" ).renameTo( apiDocs );
             try
             {
                 FileUtil.delete( apiDocsOld, true );
             }
             catch ( IOException e )
             {
                 log.error( "Error cleaning old api docs", e );
             }
         }
         else
         {
             apiDocs.getParentFile().mkdirs();
             log.debug( "installed to " + apiDocs.getAbsolutePath() );
             new File( outDir, "html" ).renameTo( apiDocs );
         }
             
         try
         {
             FileUtil.delete( outDir, true );
         }
         catch ( IOException e )
         {
             log.error( "Error moving generated docs", e );
         }
    }

    public void projectAdded( Project project )
    {
    }

    public void projectModified( Project project )
    {
    }

    public void projectFileModified( Project project, String path, File file )
    {
        queueProject( project );
    }

    public void projectRemoved( Project project )
    {
    }
}
