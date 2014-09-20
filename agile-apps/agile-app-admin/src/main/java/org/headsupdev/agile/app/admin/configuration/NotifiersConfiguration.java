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

import org.apache.maven.model.Developer;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import java.util.*;

import org.apache.wicket.model.util.CollectionModel;
import org.headsupdev.agile.app.admin.AdminApplication;
import org.headsupdev.agile.core.DefaultManager;
import org.headsupdev.agile.core.PrivateConfiguration;
import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.api.Notifier;
import org.headsupdev.agile.api.PropertyTree;
import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.storage.StoredProject;
import org.headsupdev.agile.web.ApplicationPageMapper;
import org.headsupdev.agile.web.MountPoint;
import org.headsupdev.agile.web.components.OnePressSubmitButton;
import org.headsupdev.support.java.StringUtil;

/**
 * Admin of the notifiers used for HeadsUp events
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@MountPoint( "configuration/notifiers" )
public class NotifiersConfiguration
    extends ConfigurationPage
{
    public void layout()
    {
        super.layout();

        add( CSSPackageResource.getHeaderContribution( AdminApplication.class, "admin.css" ));

        List<String> inherit = getInheritedNotifiers( getProject() );
        add( new ListView<String>( "inherit", inherit ) {
            protected void populateItem( ListItem<String> listItem ) {
                String id = listItem.getModelObject();
                listItem.add( new Label( "id", id ) );
            }
        }.setVisible( !inherit.isEmpty() ) );

        add( new ListView<Notifier>( "notifiers", ( (DefaultManager) Manager.getInstance() ).getNotifiers( getProject() ) )
        {
            protected void populateItem( ListItem<Notifier> listItem )
            {
                Notifier notifier = listItem.getModelObject();

                listItem.add( new Label( "id", notifier.getId() ) );
                listItem.add( new NotifierEditForm( "edit", notifier ) );
            }
        } );

        add( new Label( "project", getProject().getName() ) );
        add( new NotifierAddForm( "add" ) );
    }

    class NotifierEditForm extends Form
    {
        Notifier notifier;
        PropertyTree config;
        public NotifierEditForm( String id, Notifier n )
        {
            super( id );
            this.notifier = n;
            config = notifier.getConfiguration();

            final CheckGroup<String> ignoreGroup = new CheckGroup<String>( "ignoregroup", new CollectionModel<String>()
            {
                @Override
                public Collection<String> getObject()
                {
                    return notifier.getIgnoredEvents();
                }

                @Override
                public void setObject( Collection<String> object )
                {
                    notifier.setIgnoredEvents( object );
                }
            }){
                @Override
                public void updateModel()
                {
                    notifier.setIgnoredEvents( getConvertedInput() );
                }
            };
            add( ignoreGroup );

            List<String> fields = notifier.getConfigurationKeys();
            ignoreGroup.add( new ListView<String>( "fields", fields )
            {
                protected void populateItem( ListItem<String> listItem )
                {
                    final String fieldName = listItem.getModelObject();
                    if ( fieldName.equals( "<smtp>" ) )
                    {
                        listItem.add( new WebMarkupContainer( "requiressmtp" ) );
                        listItem.add( new Label( "field", "smtp" ) );
                        listItem.add( new WebMarkupContainer( "value" ).setVisible( false ) );
                        listItem.add( new WebMarkupContainer( "password" ).setVisible( false ) );

                        listItem.setVisible( Manager.getStorageInstance().getGlobalConfiguration().getSmtpHost() == null ||
                                Manager.getStorageInstance().getGlobalConfiguration().getSmtpFrom() == null );
                        return;
                    }
                    listItem.add( new WebMarkupContainer( "requiressmtp" ).setVisible( false ) );

                    listItem.add( new Label( "field", fieldName ) );
                    if ( fieldName.equals( "password" ) )
                    {
                        listItem.add( new WebMarkupContainer( "value" ).setVisible( false ) );
                        listItem.add( new PasswordTextField( "password",
                                new PropertyTreePasswordModel( fieldName, config ) ).setRequired( false ) );
                    }
                    else
                    {
                        listItem.add( new TextField<String>( "value", new PropertyTreeModel( fieldName, config ) ) );
                        listItem.add( new WebMarkupContainer( "password" ).setVisible( false ) );
                    }
                }
            } );

            ignoreGroup.add( new ListView<String>( "ignores", getAllEventTypes() )
            {
                @Override
                protected void populateItem( final ListItem<String> listItem )
                {
                    listItem.add( new Check<String>( "eventTypeCheck", listItem.getModel() ) );
                    listItem.add( new Label( "eventType", listItem.getModelObject() ) );
                }
            } );

            add( new OnePressSubmitButton( "submitEdit" ) );
            add( new Button( "remove" )
            {
                public void onSubmit()
                {
                    ( (DefaultManager) Manager.getInstance() ).removeNotifier( notifier, getProject() );
                }
            }.setDefaultFormProcessing( false ) );
        }

        public void onSubmit()
        {
            PrivateConfiguration.setNotifierConfiguration( notifier.getId(), config, getProject() );

            // this just tells the notifier we updated it's config
            notifier.setConfiguration( config );
        }
    }

    class NotifierAddForm extends Form
    {
        private DropDownChoice<String> create;
        private String adding;

        public NotifierAddForm( String id )
        {
            super( id );

            create = new DropDownChoice<String>( "id" );
            add( create.setRequired( true ) );
            add( new OnePressSubmitButton( "submitNotifier" ) );
        }

        public void onSubmit()
        {
            if ( adding != null )
            {
                ( (DefaultManager) Manager.getInstance() ).addNotifier( adding, getProject() );
            }
            adding = null;
        }

        protected void onBeforeRender() {
            super.onBeforeRender();

            create.setChoices( getOtherNotifiers() );
            // strange, the model is reset
            create.setModel( new PropertyModel<String>( this, "adding" ) );
        }
    }

    private List<? extends String> getAllEventTypes()
    {
        List<String> eventTypes = new ArrayList<String>();

        List<String> allApps = ApplicationPageMapper.get().getApplicationIds();
        for ( String appId : allApps )
        {
            eventTypes.addAll( ApplicationPageMapper.get().getApplication( appId ).getEventTypes() );
        }

        Collections.sort( eventTypes );
        return eventTypes;
    }

    public List<String> getOtherNotifiers()
    {
        List<String> ret = new LinkedList<String>( ( (DefaultManager) Manager.getInstance() ).getAvailableNotifiers() );
        List<Notifier> configured = ( (DefaultManager) Manager.getInstance() ).getNotifiers( getProject() );

        if ( configured == null )
        {
            return ret;
        }

        for ( Notifier notifier : configured )
        {
            if ( ret.contains( notifier.getId() ) )
            {
                ret.remove( notifier.getId() );
            }
        }

        return ret;
    }

    public List<String> getInheritedNotifiers( Project project ) {
        List<String> inherit = new LinkedList<String>();
        if ( project.equals( StoredProject.getDefault() ) ) {
            return inherit;
        }

        Project inherits = project.getParent();
        while ( inherits != null ) {
            List<Notifier> notifiers = ( (DefaultManager) Manager.getInstance() ).getNotifiers( project );
            for ( Notifier notifier : notifiers ) {
                if ( !inherit.contains( notifier.getId() ) ) {
                    inherit.add( notifier.getId() );
                }
            }

            inherits = inherits.getParent();
        }
        List<Notifier> notifiers = ( (DefaultManager) Manager.getInstance() ).getNotifiers( StoredProject.getDefault() );
        for ( Notifier notifier : notifiers ) {
            if ( !inherit.contains( notifier.getId() ) ) {
                inherit.add( notifier.getId() );
            }
        }

        return inherit;
    }
}