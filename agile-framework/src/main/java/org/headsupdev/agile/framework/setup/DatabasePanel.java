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

package org.headsupdev.agile.framework.setup;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.api.logging.Logger;
import org.headsupdev.agile.core.PrivateConfiguration;
import org.headsupdev.agile.runtime.HeadsUpRuntime;
import org.headsupdev.agile.storage.DatabaseRegistry;
import org.headsupdev.agile.web.components.BooleanImage;
import org.headsupdev.agile.web.components.OnePressButton;
import org.headsupdev.agile.web.components.configuration.SQLURLField;
import org.headsupdev.support.java.IOUtil;

import java.io.*;
import java.util.Properties;

/**
 * A panel for the setup to connect to the required database.
 * Currently requires a restart after anything is changed - that sucks :(
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class DatabasePanel
        extends Panel
{
    private Class setupPage;
    private WebMarkupContainer restartMessage;

    private Logger log = Manager.getLogger( getClass().getName() );
    private HeadsUpRuntime runtime;

    public DatabasePanel( String id, Class setupPage, HeadsUpRuntime runtime )
    {
        super( id );
        this.setupPage = setupPage;
        this.runtime = runtime;

        add( restartMessage = new WebMarkupContainer( "restart" ) );
        restartMessage.setVisible( false );
        add( new DatabaseForm( "database" ) );
    }

    class DatabaseForm
            extends Form
    {
        private String url;
        private String username;
        private String password;
        private BooleanImage testImage;

        public DatabaseForm( String id )
        {
            super( id );
            testImage = new BooleanImage( "test", true );
            add( testImage.setOutputMarkupId( true ) );

            setModel( new CompoundPropertyModel( this ) );
            Properties bootstrap = getBootstrapProperties();
            url = bootstrap.getProperty( "headsup.db.url" );
            username = bootstrap.getProperty( "headsup.db.username" );
            password = bootstrap.getProperty( "headsup.db.password" );

            add( new SQLURLField( "url" )
            {
                protected void onUpdate( AjaxRequestTarget target )
                {
                    testSQL( target );
                }
            } );
            add( new TextField( "username" ).setRequired( false ).add( new AjaxFormComponentUpdatingBehavior( "onblur" )
            {
                protected void onUpdate( AjaxRequestTarget target )
                {
                    testSQL( target );
                }
            } ) );
            add( new TextField( "password" ).setRequired( false ).add( new AjaxFormComponentUpdatingBehavior( "onblur" )
            {
                protected void onUpdate( AjaxRequestTarget target )
                {
                    testSQL( target );
                }
            } ) );
            add( new OnePressButton( "submitDatabase" ) );
        }

        void testSQL( AjaxRequestTarget target )
        {
            boolean result = DatabaseRegistry.canConnect( url, username, password );
            testImage.setBoolean( result );

            target.addComponent( testImage );
        }

        protected void onSubmit()
        {
            String type = DatabaseRegistry.getTypeForUrl( url );
            String dialect = DatabaseRegistry.getDialect( type );
            String driver = DatabaseRegistry.getDriver( type );

            Properties bootstrap = getBootstrapProperties();
            String currentType = DatabaseRegistry.getTypeForUrl( bootstrap.getProperty( "headsup.db.url" ) );
            bootstrap.setProperty( "headsup.db.dialect", dialect );
            bootstrap.setProperty( "headsup.db.driver", driver );
            bootstrap.setProperty( "headsup.db.url", url );

            if ( username == null )
            {
                username = "";
            }
            bootstrap.setProperty( "headsup.db.username", username );
            if ( password == null )
            {
                password = "";
            }
            bootstrap.setProperty( "headsup.db.password", password );

            OutputStream out = null;
            try
            {
                bootstrap.store( out = new FileOutputStream( getBootstrapPropertiesFile() ), null );
            }
            catch ( IOException e )
            {
                log.error( "Unable to store bootstrap properties", e );
            }
            finally
            {
                if ( out != null )
                {
                    IOUtil.close( out );
                }
            }

            if ( !currentType.equals( type ) )
            {
                restartMessage.setVisible( true );

                // here we will start config from scratch with a new DB...
                new Thread()
                {
                    public void run()
                    {
                        try
                        {
                            sleep( 1000 );
                        }
                        catch ( InterruptedException e )
                        {
                            // just restart anyway
                        }

                        try
                        {
                            runtime.restart();
                        }
                        catch ( Exception e )
                        {
                            log.error( "Error restarting container", e );
                        }
                    }
                }.start();
            }
            else
            {
                PrivateConfiguration.setSetupStep( PrivateConfiguration.STEP_DATABASE );
                setResponsePage( setupPage );
            }
        }

        private File getBootstrapPropertiesFile()
        {
            File confDir = new File( Manager.getStorageInstance().getGlobalConfiguration().getInstallDir(), "conf" );
            File ret = new File( confDir, "config.properties" );

            // could be pax:runner which uses a different config file
            if ( !ret.exists() )
            {
                File felixDir = new File( Manager.getStorageInstance().getGlobalConfiguration().getInstallDir(), "felix" );
                ret = new File( felixDir, "config.ini" );
            }

            return ret;
        }

        private Properties getBootstrapProperties()
        {
            Properties bootstrap = new Properties();
            InputStream in = null;
            try
            {
                bootstrap.load( in = new FileInputStream( getBootstrapPropertiesFile() ) );
            }
            catch ( IOException e )
            {
                log.error( "Unable to load bootstrap properties", e );
            }
            finally
            {
                if ( in != null )
                {
                    IOUtil.close( in );
                }
            }

            return bootstrap;
        }
    }
}
