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

package org.headsupdev.agile.app.admin.configuration;

import org.apache.wicket.model.Model;
import org.headsupdev.agile.api.HeadsUpConfiguration;
import org.headsupdev.agile.api.ConfigurationItem;
import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.app.admin.AdminApplication;
import org.headsupdev.agile.api.logging.Logger;
import org.headsupdev.agile.storage.DatabaseRegistry;
import org.headsupdev.agile.web.components.BooleanImage;
import org.headsupdev.agile.web.components.OnePressButton;
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

import java.util.*;
import java.io.*;

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
            SystemConfigurationSource configSource =
                    ( (AdminApplication) getHeadsUpApplication() ).getConfigurationSource();

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

            add( new CheckBox( "debug", new Model<Boolean>()
            {
                @Override
                public Boolean getObject()
                {
                    return HeadsUpConfiguration.isDebug();
                }

                @Override
                public void setObject( Boolean debug )
                {
                    HeadsUpConfiguration.setDebug( debug );
                }
            } ) );
            final HeadsUpConfiguration global = getStorage().getGlobalConfiguration();
            add( new ListView<ConfigurationItem>( "item", configSource.getConfigurationItems() )
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

            add( new ListView<ConfigurationItem>( "restartitem", configSource.getConfigurationItemsRequiringRestart() )
            {
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
            add( new OnePressButton( "submitConfig" ) );
        }

        private BooleanImage sqlTestImage;
        private SQLURLField sqlUrlField;
        private TextField<String> sqlUsernameField, sqlPasswordField;
        private void setupSQLItems( final String key, ListItem listItem, final SystemConfigurationItem item,
                                    final BooleanImage test )
        {
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

                        String type = DatabaseRegistry.getTypeForUrl( s );
                        if ( !DatabaseRegistry.getTypes().contains( type ) )
                        {
                            // TODO report error
                            return;
                        }

                        String dialect = DatabaseRegistry.getDialect( type );
                        String driver = DatabaseRegistry.getDriver( type );
                        restartItems.setProperty( "headsup.db.dialect", dialect );
                        restartItems.setProperty( "headsup.db.driver", driver );

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
            String username = sqlUsernameField.getModelObject();
            String password = sqlPasswordField.getModelObject();

            boolean result = DatabaseRegistry.canConnect( url, username, password );
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
