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

package org.headsupdev.agile.framework;

import org.headsupdev.support.java.FileUtil;
import org.headsupdev.support.java.IOUtil;
import org.headsupdev.support.java.compression.GZipFile;
import org.headsupdev.support.java.compression.TarFile;
import org.headsupdev.agile.api.*;
import org.headsupdev.agile.api.logging.Logger;
import org.headsupdev.agile.security.permission.AdminPermission;
import org.headsupdev.agile.core.UpdateDetails;
import org.headsupdev.agile.core.DefaultManager;
import org.headsupdev.agile.web.SystemEvent;
import org.headsupdev.agile.web.HeadsUpSession;
import org.headsupdev.agile.storage.HibernateUtil;
import org.headsupdev.agile.runtime.HeadsUpRuntime;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.Session;
import org.apache.wicket.model.Model;
import org.wicketstuff.progressbar.ProgressBar;
import org.wicketstuff.progressbar.ProgressionModel;
import org.wicketstuff.progressbar.Progression;

import java.net.URL;
import java.net.URLConnection;
import java.io.*;

/**
 * The main updating panel that displays progress as it performs the update.
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class UpdatingPanel extends Panel
{
    private int progress = 0;
    File outFile;
    private HeadsUpRuntime runtime;

    private Logger log = Manager.getLogger( getClass().getName() );

    public UpdatingPanel( String id, HeadsUpRuntime runtime )
    {
        super( id );
        this.runtime = runtime;

        // declare this object early so we do not look it up while restarting
        final Model restartingModel = new Model<String>() {
            public String getObject() {
                return "Restarting...";
            }
        };

        final WebMarkupContainer run = new WebMarkupContainer( "runupdate" );
        run.setOutputMarkupId( true );
        final Label done = new Label( "doneupdate", "Updating will restart the software. " +
            "Please note that after it restarts there may be additional setup steps." );
        done.setOutputMarkupId( true );
        final ProgressBar bar = new ProgressBar( "bar", new ProgressionModel()
        {
            protected Progression getProgression()
            {
                return new Progression( progress );
            }
        } )
        {
            protected void onFinished( AjaxRequestTarget target )
            {
                target.addComponent( run.setVisible( false ) );
                target.addComponent( this.setVisible( false ) );

                target.addComponent( done.setVisible( true ) );
                done.setDefaultModel( restartingModel );
            }
        };

        final Form updateForm = new Form( "updatenow" );
        updateForm.add( new AjaxButton( "submit", updateForm )
        {
            public void onSubmit( AjaxRequestTarget target, Form form )
            {
                target.addComponent( done.setDefaultModel( new Model<String>()
                {
                    public String getObject()
                    {
                        return "Please Wait...";
                    }
                } ) );

                try {
                    final UpdateDetails update = ( (DefaultManager) Manager.getInstance() ).getAvailableUpdates().get( 0 );
                    URL download = new URL( update.getFile() );
                    final long length = update.getLength();
                    final URLConnection conn = download.openConnection();
                    conn.connect();

                    if ( length > -1 ) {
                        target.addComponent( this.setVisible( false ) );
                        target.addComponent( run.setVisible( true ) );
                        target.addComponent( bar.setVisible( true ) );
                        bar.start( target );
                    }

                    // declare the thread now so we do not try to look it up after our resources have been moved
                    final Thread restartThread = new Thread() {
                        public void run() {
                            try
                            {
                                sleep( 1000 );
                            } catch (InterruptedException e) {
                                // just restart anyway
                            }

                            try
                            {
                                UpdatingPanel.this.runtime.restart();
                            }
                            catch ( Exception e )
                            {
                                log.error( "Failed to restart container", e );
                            }
                        }
                    };

                    new Thread() { public void run() {
                        try {
                            byte[] buffer = new byte[4096];
                            long downloaded = 0;
                            InputStream in = conn.getInputStream();

                            // little hack to get the temp dir, better or worse than reading the property?
                            outFile = File.createTempFile( "blah", "" );
                            outFile.delete();
                            outFile = new File( outFile.getParentFile(), new File( update.getFile() ).getName() );
                            OutputStream out = new FileOutputStream( outFile );

                            int chunk;
                            while ( ( chunk = in.read( buffer ) ) > -1 ) {
                                downloaded += chunk;
                                out.write( buffer, 0, chunk );

                                if ( length > 0 )
                                {
                                    int prog = (int) ( ( (double) downloaded / length ) * 100 );
                                    if ( prog > 99 ) {
                                        prog = 99;
                                    }
                                    progress = prog;
                                }
                            }

                            in.close();
                            out.close();

                            outFile = new TarFile( new GZipFile( outFile ).expand( true ) ).expand();

                            // pull it out of the dir created
                            File tmpDir = new File( outFile.getAbsolutePath() + "_dir" );
                            outFile.renameTo( tmpDir );
                            File[] children = tmpDir.listFiles();

                            // push new to a direct child of our root for later
                            outFile = new File( outFile.getParentFile(), outFile.getName() );
                            children[0].renameTo( outFile );
                            tmpDir.delete();

                            // add a system event before we start the moving
                            String version = new File( update.getFile() ).getName();
                            version = version.substring( 0, version.indexOf( ".tar.gz" ) );
                            Event event = new SystemEvent( Manager.getStorageInstance().getGlobalConfiguration().getProductName() + " upgraded to " +
                                version, Manager.getStorageInstance().getGlobalConfiguration().getProductName() + " has been upgraded to " + version +
                                " - congratulations", "<h2>" + Manager.getStorageInstance().getGlobalConfiguration().getProductName() + " version " +
                                version + "</h2>" + update.getDetails() );
                            Manager.getStorageInstance().addEvent( event );
                            Manager.getInstance().fireEventAdded(event);

                            // override some permissions as they are lost when we unpack in java
                            Process chmodProcess = null;
                            try
                            {
                                chmodProcess = Runtime.getRuntime().exec( "chmod -R +x " + new File( outFile, "bin" ).getAbsolutePath() );
                                chmodProcess.waitFor();
                            }
                            catch ( InterruptedException e )
                            {
                                // never mind
                            }
                            finally
                            {
                                if ( chmodProcess != null )
                                {
                                    IOUtil.close( chmodProcess.getOutputStream() );
                                    IOUtil.close( chmodProcess.getErrorStream() );
                                    IOUtil.close( chmodProcess.getInputStream() );
                                    chmodProcess.destroy();
                                }
                            }
                            updateProperties( new File( new File( outFile, "conf" ), "config.properties" ) );

                            File mainDir = new File( "." );
                            File backupDir = new File( mainDir, "backup" );
                            if ( backupDir.exists() ) {
                                for ( File child : backupDir.listFiles() ) {
                                    FileUtil.delete( child );
                                }
                            } else {
                                backupDir.mkdir();
                            }

                            for ( File child : mainDir.listFiles() ) {
                                if ( child.equals( outFile ) ) {
                                    continue;
                                }

                                child.renameTo( new File( backupDir, child.getName() ) );
                            }
                            for ( File child : outFile.listFiles() ) {
                                child.renameTo( new File( mainDir, child.getName() ) );
                            }
                            outFile.delete();

                            progress = 100;

                            restartThread.start();
                        }
                        catch ( IOException e )
                        {
                            log.error( "Failed to unpack the downloaded update", e );
                        }
                    } }.start();
                }
                catch ( IOException e )
                {
                    log.error( "Exception trying to upgrade system", e );
                }
            }
        } );
        updateForm.add( bar );
        bar.setVisible( false );
        updateForm.add( run );
        run.setVisible( false );
        updateForm.add( done );

        add( updateForm );
        setVisible( ( (DefaultManager) Manager.getInstance() ).getAvailableUpdates().size() > 0 &&
            Manager.getSecurityInstance().userHasPermission( ( (HeadsUpSession) Session.get() ).getUser(),
                new AdminPermission(), null ) );
    }

    private void updateProperties( File file )
    {
        java.util.Properties properties = new java.util.Properties();

        // here we check that the file exists, we are preparing for a new config system so the
        // first upgrade will not have the file present
        if ( file.exists() )
        {
            InputStream in = null;
            try
            {
                in = new FileInputStream( file );
                properties.load( in );
            }
            catch ( IOException e )
            {
                log.error( "Failed reading configuration for upgrade", e );
                return;
            }
            finally
            {
                if ( in != null )
                {
                    try
                    {
                        in.close();
                    }
                    catch ( IOException e )
                    {
                        // ignore
                    }
                }
            }
        }

        java.util.Properties current = HibernateUtil.properties;
        for ( Object o : current.keySet() ) {
            String key = o.toString();
            properties.setProperty( key, current.getProperty( key ) );
        }

        OutputStream out = null;
        try
        {
            out = new FileOutputStream( file );
            properties.store( out, "Installed current properties" );
        }
        catch ( IOException e )
        {
            log.error( "Failed writing configuration for upgrade", e );
        }
        finally
        {
            if ( out != null )
            {
                try
                {
                    out.close();
                }
                catch ( IOException e )
                {
                    // ignore
                }
            }
        }
    }
}
