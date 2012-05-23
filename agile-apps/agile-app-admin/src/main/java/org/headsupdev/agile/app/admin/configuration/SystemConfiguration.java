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

package org.headsupdev.agile.app.admin.configuration;

import org.headsupdev.agile.api.HeadsUpConfiguration;
import org.headsupdev.agile.api.ConfigurationItem;
import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.app.admin.AdminApplication;
import org.headsupdev.agile.api.logging.Logger;
import org.headsupdev.agile.web.components.BooleanImage;
import org.headsupdev.agile.web.components.configuration.SQLURLField;
import org.headsupdev.agile.web.MountPoint;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.headsupdev.support.java.IOUtil;
import org.headsupdev.support.java.StringUtil;

import java.net.InetAddress;
import java.util.*;
import java.io.*;
import java.net.URL;
import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.DriverManager;

/**
 * A panel for editing the base system configuration.
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@MountPoint( "configuration/system" )
public class SystemConfiguration
    extends ConfigurationPage
{
    private List<ConfigurationItem> config = new LinkedList<ConfigurationItem>();
    private List<ConfigurationItem> restartConfig = new LinkedList<ConfigurationItem>();

    private boolean showRestart = false;

    private Logger log = Manager.getLogger( getClass().getName() );

    public void layout()
    {
        super.layout();
        add( CSSPackageResource.getHeaderContribution( AdminApplication.class, "admin.css" ) );
        if ( getPageParameters().getString( "restart" ) != null )
        {
            showRestart = true;
        }

        config.add( new SystemConfigurationItem( "baseUrl", "Public URL for this system",
            "Use this field to set the published URL for this system - used for notifications and feeds" )
        {
            public boolean test( String value )
            {
                try
                {
                    URL url = new URL( value + "project-feed.xml" );
                    // todo can we test the project-feed.xml - might be password protected
                    return true;
                }
                catch ( MalformedURLException e )
                {
                    return false;
                }
            }
        });
        config.add( new SystemConfigurationItem( "timezone.id", "UTC",  "Default Timezone",
                "The name of the default timezone - this will be used for anonymous users and as a default for all accounts" )
        {
            @Override
            public boolean test( String value )
            {
                try
                {
                    return TimeZone.getTimeZone( value ).getID().equals( value );
                }
                catch ( Exception e ) // catching errors that occur when looking up null or invalid timezone
                {
                    return false;
                }
            }
        });

        config.add( new SystemConfigurationItem( "log.errors", true, "Keep a log of any errors encountered",
            "Switching off this parameter can be useful if you encounter lots of problems with an external component " +
            "and wish to save database space" ) );
        config.add( new SystemConfigurationItem( "userList.useNames", true, "Show real names in user lists",
            "Check this if you prefer real names to usernames in user lists" ) );

        config.add( new SystemConfigurationItem( "smtp.host", "Hostname for outgoing email",
            "This should be set to the host used to send notification emails" )
        {
            public boolean test( String value )
            {
                if ( StringUtil.isEmpty( value ) )
                {
                    return false;
                }

                try
                {
                    InetAddress address = InetAddress.getByName( value );
                    return address != null && address.isReachable( 10000 );
                }
                catch ( IOException e )
                {
                    return false;
                }
            }
        });
        config.add( new SystemConfigurationItem( "smtp.from", "Senders email address for outgoing email",
            "This should be set to the email address to send notification emails" )
        {
            public boolean test( String value )
            {
                return !StringUtil.isEmpty( value );
            }
        });
        config.add( new SystemConfigurationItem( "smtp.username", "Username for outgoing email account",
            "This should be set to the username used to send notification emails" ) );
        config.add( new SystemConfigurationItem( "smtp.password", "Password for outgoing email account",
            "This should be set to the password used to send notification emails" ) );

        restartConfig.add( new SystemConfigurationItem( "dataDir", "Data Directory",
            "This is the location that " + getStorage().getGlobalConfiguration().getProductName() +
                " stores files and runs it's builds. " )
        {
            public boolean test( String value )
            {
                File dir = new File( value );
                return dir.isDirectory() && dir.canWrite();
            }
        } );
        restartConfig.add( new SystemConfigurationItem( "org.osgi.service.http.port", 8069, "Server Port",
            "The port that the system should run on - used either in the URL or for mapping through a web server" )
        {
            public boolean test( String value )
            {
                try
                {
                    int port = Integer.parseInt( value );
                    return port > 0 && port <= 65535;
                }
                catch ( NumberFormatException e )
                {
                    return false;
                }
            }
        } );
        restartConfig.add( new SystemConfigurationItem( "heasup.db.url", "Database URL",
            "The URL of the SQL \"agile\" database " ) );
        restartConfig.add( new SystemConfigurationItem( "headsup.db.username", "Database Username",
            "The username for the SQL \"agile\" database ") );
        restartConfig.add( new SystemConfigurationItem( "headsup.db.password", "Database Password",
            "The password for the SQL \"agile\" database " ) );

        // little hack here to make sure the defaults are copied in...
        HeadsUpConfiguration global = getStorage().getGlobalConfiguration();
        global.setBaseUrl( global.getBaseUrl() );
        global.setDataDir( global.getDataDir() );
        global.setLogErrors( global.getLogErrors() );

        add( new Form( "restart" )
        {
            public void onSubmit()
            {
                new Thread() {
                    public void run() {
                        try {
                            sleep( 1000 );
                        } catch (InterruptedException e) {
                            // just restart anyway
                        }

                        try
                        {
                            ( (AdminApplication) getHeadsUpApplication() ).getHeadsUpRuntime().restart();
                        }
                        catch ( Exception e )
                        {
                            log.error( "Failed to restart container", e );
                        }
                    }
                }.start();
            }
        }.setVisible( showRestart ) );

        add( new ConfigurationForm( "config" ) );
    }

    class ConfigurationForm
        extends Form
    {
        private Properties restartItems = new Properties();

        public ConfigurationForm( String id )
        {
            super( id );
            File bootstrap = getBootstrapPropertiesFile();
            InputStream in = null;
            try
            {
                restartItems.load( in = new FileInputStream( bootstrap ) );
            }
            catch ( IOException e )
            {
                log.error(  "Unable to load bootstrap properties", e );
            }
            finally
            {
                if ( in != null )
                {
                    IOUtil.close( in );
                }
            }

            final HeadsUpConfiguration global = getStorage().getGlobalConfiguration();
            add( new ListView<ConfigurationItem>( "item", config )
            {
                protected void populateItem( ListItem<ConfigurationItem> listItem )
                {
                    final SystemConfigurationItem item = (SystemConfigurationItem) listItem.getModelObject();
                    final BooleanImage test = new BooleanImage( "test", item.test( global.getProperty( item.getKey() ) ) );
                    listItem.add( test.setOutputMarkupId( true ) );

                    listItem.add( new Label( "title", item.getTitle() ) );
                    listItem.add( new Label( "description", item.getDescription() ) );

                    if ( item.getType() == ConfigurationItem.TYPE_BOOL )
                    {
                        final CheckBox field = new CheckBox( "check", new BooleanPropertyTreeModel( item.getKey(), global ) );
                        listItem.add( field );

                        listItem.add( new WebMarkupContainer( "field" ).setVisible( false ) );
                        listItem.add( new WebMarkupContainer( "passwordfield" ).setVisible( false ) );
                    }
                    else
                    {
                        TextField<String> f;
                        if ( item.getKey().toLowerCase().contains( "password" ) )
                        {
                            f = new PasswordTextField( "passwordfield", new PropertyTreePasswordModel( item.getKey(), global ) );
                            f.setRequired( false );
                            listItem.add( new WebMarkupContainer( "field" ).setVisible( false ) );
                        }
                        else
                        {
                            f = new TextField<String>( "field", new PropertyTreeModel( item.getKey(), global ) );
                            listItem.add( new WebMarkupContainer( "passwordfield" ).setVisible( false ) );
                        }
                        final TextField<String> field = f;

                        field.add( new AjaxFormComponentUpdatingBehavior( "onblur" )
                        {
                            protected void onUpdate( AjaxRequestTarget target )
                            {
                                Object obj = field.getModelObject();
                                test.setBoolean( item.test( obj == null ? null : obj.toString() ) );

                                target.addComponent( test );
                            }
                        } );
                        if ( item.getType() == ConfigurationItem.TYPE_INT )
                        {
                            field.add( new IntegerValidator() );
                        }
                        else if ( item.getType() == ConfigurationItem.TYPE_DOUBLE )
                        {
                            field.add( new DoubleValidator() );
                        }
                        listItem.add( field );

                        listItem.add( new WebMarkupContainer( "check" ).setVisible( false ) );
                    }
                }
            } );

            add( new ListView<ConfigurationItem>( "restartitem", restartConfig ) {
                protected void populateItem( ListItem<ConfigurationItem> listItem )
                {
                    final SystemConfigurationItem item = (SystemConfigurationItem) listItem.getModelObject();
                    final String key = item.getKey();
                    final BooleanImage test = new BooleanImage( "test" );
                    listItem.add( test.setOutputMarkupId( true ) );

                    listItem.add( new Label( "title", item.getTitle() ) );
                    listItem.add( new Label( "description", item.getDescription() ) );
                    if ( key.equals( "dataDir" ) )
                    {
                        test.setBoolean( item.test( global.getProperty( key ) ) );
                        final TextField<String> field = new TextField<String>( "field", new PropertyTreeModel( key, global )
                        {
                            public void setObject( String s )
                            {
                                if ( s == null )
                                {
                                    s = "";
                                }
                                if ( !s.equals( getObject() ) )
                                {
                                    showRestart = true;
                                }

                                super.setObject( s );
                            }
                        } );
                        field.add( new AjaxFormComponentUpdatingBehavior( "onblur" )
                        {
                            protected void onUpdate( AjaxRequestTarget target )
                            {
                                test.setBoolean( item.test( field.getModelObject() ) );

                                target.addComponent( test );
                            }
                        } );
                        if ( item.getType() == ConfigurationItem.TYPE_INT ) {
                            field.add( new IntegerValidator() );
                        }
                        listItem.add( field );
                        listItem.add( new WebMarkupContainer( "customfield" ).setVisible( false ) );
                    }
                    else if ( key.startsWith( "headsup.db." ) )
                    {
                        setupSQLItems( key, listItem, item, test );
                    }
                    else
                    {
                        test.setBoolean( item.test( restartItems.getProperty( key ) ) );
                        final TextField<String> field = new TextField<String>( "field", new PropertiesModel( key, restartItems )
                        {
                            public void setObject( String s )
                            {
                                if ( s == null )
                                {
                                    s = "";
                                }
                                if ( !s.equals( getObject() ) )
                                {
                                    showRestart = true;
                                }

                                super.setObject( s );
                            }
                        } );
                        field.add( new AjaxFormComponentUpdatingBehavior( "onblur" )
                        {
                            protected void onUpdate( AjaxRequestTarget target )
                            {
                                test.setBoolean( item.test( field.getModelObject() ) );

                                target.addComponent( test );
                            }
                        } );
                        if ( item.getType() == ConfigurationItem.TYPE_INT ) {
                            field.add( new IntegerValidator() );
                        }
                        listItem.add( field );
                        listItem.add( new WebMarkupContainer( "customfield" ).setVisible( false ) );
                    }
                }
            } );
        }

        private BooleanImage sqlTestImage;
        private SQLURLField sqlUrlField;
        private TextField sqlUsernameField, sqlPasswordField;
        private String sqlDriver;
        private void setupSQLItems( final String key, ListItem listItem, final SystemConfigurationItem item,
                                    final BooleanImage test )
        {
            sqlDriver = restartItems.getProperty( "headsup.db.driver" );

            if ( key.equals( "headsup.db.url" ) )
            {
                sqlTestImage = test;
                test.setBoolean( item.test( restartItems.getProperty( key ) ) );

                listItem.add( sqlUrlField = new SQLURLField( "customfield", new PropertiesModel( key, restartItems )
                {
                    public void setObject( String s )
                    {
                        if ( !s.equals( getObject() ) )
                        {
                            showRestart = true;
                        }

                        String dialect = "org.hibernate.dialect.H2Dialect";
                        String driver = "org.h2.Driver";
                        if ( s.startsWith( "jdbc:mysql:" ) )
                        {
                            dialect = "org.hibernate.dialect.MySQLDialect";
                            driver = "com.mysql.jdbc.Driver";
                        }
                        restartItems.setProperty( "headsup.db.dialect", dialect );
                        restartItems.setProperty( "headsup.db.driver", driver );

                        sqlDriver = driver;
                        super.setObject( s );
                    }


                } )
                {
                    protected void onUpdate( AjaxRequestTarget target )
                    {
                        testSQL( target );
                    }
                } );
                listItem.add( new WebMarkupContainer( "field" ).setVisible( false ) );
            }
            else
            {
                test.setVisible( false );
                final TextField<String> field = new TextField<String>( "field", new PropertiesModel( key, restartItems )
                {
                    public void setObject( String s )
                    {
                        if ( s == null )
                        {
                            s = "";
                        }
                        if ( !s.equals( getObject() ) )
                        {
                            showRestart = true;
                        }

                        super.setObject( s );
                    }
                } );
                field.add( new AjaxFormComponentUpdatingBehavior( "onblur" )
                {
                    protected void onUpdate( AjaxRequestTarget target )
                    {
                        testSQL( target );
                    }
                } );
                if ( item.getType() == ConfigurationItem.TYPE_INT ) {
                    field.add( new IntegerValidator() );
                }
                listItem.add( field );
                listItem.add( new WebMarkupContainer( "customfield" ).setVisible( false ) );

                if ( key.equals( "headsup.db.username" ) )
                {
                    sqlUsernameField = field;
                }
                else
                {
                    sqlPasswordField = field;
                }
            }
        }

        private void testSQL( AjaxRequestTarget target )
        {
            String url = sqlUrlField.getDefaultModelObject().toString();
            String username = sqlUsernameField.getModelObject().toString();
            String password = sqlPasswordField.getModelObject().toString();

            boolean result = false;

            try
            {
                Class.forName( sqlDriver );
                Connection conn = DriverManager.getConnection( url, username, password );

                result = conn != null;
            }
            catch( Exception e )
            {
                log.error( "Failed SQL test", e );
            }
            sqlTestImage.setBoolean( result );

            target.addComponent( sqlTestImage );
        }

        protected void onSubmit()
        {
            super.onSubmit();

            OutputStream out = null;
            try
            {
                restartItems.store( out = new FileOutputStream( getBootstrapPropertiesFile() ), null );
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

            PageParameters params = new PageParameters();
            if ( showRestart )
            {
                params.add( "restart", "true" );
            }
            setResponsePage( SystemConfiguration.class, params );
        }

        private File getBootstrapPropertiesFile()
        {
            File confDir = new File( getStorage().getGlobalConfiguration().getInstallDir(), "conf" );
            File ret = new File( confDir, "config.properties" );

            // could be pax:runner which uses a different config file
            if ( !ret.exists() )
            {
                File felixDir = new File( getStorage().getGlobalConfiguration().getInstallDir(), "felix" );
                ret = new File( felixDir, "config.ini" );
            }

            return ret;
        }
    }
}
