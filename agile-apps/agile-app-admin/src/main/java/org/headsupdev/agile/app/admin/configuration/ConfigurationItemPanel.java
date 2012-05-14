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

import org.headsupdev.agile.api.*;
import org.headsupdev.agile.storage.StoredProject;
import org.headsupdev.agile.web.HeadsUpPage;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

import java.util.LinkedList;
import java.util.List;

/**
 * Render a single configuration item or group.
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class ConfigurationItemPanel
        extends Panel
{
    public ConfigurationItemPanel( String id, ConfigurationItem config, final PropertyTree tree, Application app,
                                   Project project, final boolean useRowId, final int rowId )
    {
        super( id );

        String key = config.getKey();
        if ( useRowId )
        {
            key = String.valueOf( rowId );
        }

        AttributeModifier rowColor = new AttributeModifier( "class", true, new Model<String>()
        {
            public String getObject()
            {
                if ( rowId % 2 == 1 )
                {
                    return "odd";
                }

                return "even";
            }
        } );

        if ( config.getType() == ConfigurationItem.TYPE_SET )
        {
            WebMarkupContainer set = new WebMarkupContainer( "set" );
            add( set );
            set.add( new Label( "title", config.getTitle() ) );

            WebMarkupContainer child = new WebMarkupContainer( "subitems" );
            set.add( child.setVisible( config.getSetItems() != null && config.getSetItems().size() > 0 ) );
            child.add( new ConfigurationPanel( "item", config.getSetItems(), tree.getSubTree( key ),
                    app, project ) );

            Link delete = new Link( "delete" ) {
                public void onClick() {
                    removeRow( tree, rowId );
                }
            };
            delete.add( new Image( "delete-icon", new ResourceReference( HeadsUpPage.class, "images/delete.png" ) ) );
            set.add( delete.setVisible( useRowId ) );

            add( new WebMarkupContainer( "list" ).setVisible( false ) );
            add( new WebMarkupContainer( "entry" ).setVisible( false ) );
        }
        else if ( config.getType() == ConfigurationItem.TYPE_LIST )
        {
            WebMarkupContainer list = new WebMarkupContainer( "list" );
            add( list );
            final PropertyTree listTree = tree.getSubTree( key );

            String countStr = listTree.getProperty( "count" );
            int count = 0;
            if ( countStr != null && countStr.length() > 0 )
            {
                count = Integer.parseInt( countStr );
            }

            List<ConfigurationItem> items = new LinkedList<ConfigurationItem>();
            for ( int i = 0; i < count; i++ )
            {
                ConfigurationItem confChild = new ListConfigurationItem( config.getListItem(), i );
                items.add( confChild );
            }
            WebMarkupContainer child = new WebMarkupContainer( "subitems" );
            list.add( child.setVisible( count > 0 ) );
            child.add( new ConfigurationPanel( "item", items, listTree, app, project, true ) );

            Link add = new Link( "add" ) {
                public void onClick() {
                    addRow( listTree );
                }
            };
            add.add( new Image( "add-icon", new ResourceReference( HeadsUpPage.class, "images/add.png" ) ) );
            add.add( new Label( "add-label", "Add a new " + config.getListItem().getTitle() ) );
            list.add( add );

            Link delete = new Link( "delete" ) {
                public void onClick() {
                    removeRow( tree, rowId );
                }
            };
            delete.add( new Image( "delete-icon", new ResourceReference( HeadsUpPage.class, "images/delete.png" ) ) );
            list.add( delete.setVisible( useRowId ) );

            add( new WebMarkupContainer( "set" ).setVisible( false ) );
            add( new WebMarkupContainer( "entry" ).setVisible( false ) );
        }
        else
        {
            WebMarkupContainer entry = new WebMarkupContainer( "entry" );
            add( entry );
            WebMarkupContainer row1 = new WebMarkupContainer( "row1" );
            row1.add( rowColor );
            entry.add( row1 );
            WebMarkupContainer row2 = new WebMarkupContainer( "row2" );
            row2.add( rowColor );
            entry.add( row2 );

            String fallback = String.valueOf( config.getDefault() );
            if ( project != null )
            {
                fallback = getDefaultConfigurationValue( config, project, app );
            }
            row1.add( new Label( "title", config.getTitle() ) );
            row1.add( new Label( "default", fallback ).setVisible( config.getDefault() != null ) );
            row2.add( new Label( "description", config.getDescription() ) );
            if ( config.getType() == ConfigurationItem.TYPE_BOOL )
            {
                row1.add( new WebMarkupContainer( "field" ).setVisible( false ) );
                row1.add( new CheckBox( "check", new BooleanPropertyTreeModel( key, tree, fallback ) ) );
            }
            else
            {
                TextField<String> field = new TextField<String>( "field", new PropertyTreeModel( key, tree, fallback ) );
                if ( config.getType() == ConfigurationItem.TYPE_INT )
                {
                    field.add( new IntegerValidator() );
                }
                else if ( config.getType() == ConfigurationItem.TYPE_CRON )
                {
                    field.add( new CronValidator() );
                }

                row1.add( field );
                row1.add( new WebMarkupContainer( "check" ).setVisible( false ) );
            }

            Link delete = new Link( "delete" ) {
                public void onClick() {
                    removeRow( tree, rowId );
                }
            };
            delete.add( new Image( "delete-icon", new ResourceReference( HeadsUpPage.class, "images/delete.png" ) ) );
            row1.add( delete.setVisible( useRowId ) );

            add( new WebMarkupContainer( "set" ).setVisible( false ) );
            add( new WebMarkupContainer( "list" ).setVisible( false ) );
        }
    }


    public String getDefaultConfigurationValue( ConfigurationItem item, Project project, Application app )
    {
        if ( project.equals( StoredProject.getDefault() ) ) {
            return String.valueOf( item.getDefault() );
        }

        HeadsUpConfiguration mainConfig = Manager.getStorageInstance().getGlobalConfiguration();
        project = project.getParent();

        PropertyTree props;
        while ( project != null )
        {
            if ( app == null )
            {
                props = mainConfig.getProjectConfiguration( project );
            }
            else
            {
                props = mainConfig.getApplicationConfigurationForProject( app, project );
            }

            String ret = props.getProperty( item.getKey() );
            if ( ret != null ) {
                return ret;
            }

            project = project.getParent();
        }

        if ( app == null )
        {
            props = mainConfig.getProjectConfiguration( StoredProject.getDefault() );
        }
        else
        {
            props = mainConfig.getApplicationConfigurationForProject( app, StoredProject.getDefault() );
        }
        String ret = props.getProperty( item.getKey() );
        if ( ret != null )
        {
            return ret;
        }

        return String.valueOf( item.getDefault() );
    }

    public void addRow( PropertyTree tree )
    {
        String countStr = tree.getProperty( "count", "0" );
        int count = Integer.parseInt( countStr );

        tree.setProperty( "count", String.valueOf( ++count ) );
    }

    public void removeRow( PropertyTree tree, int id )
    {
        String countStr = tree.getProperty( "count", "0" );
        int count = Integer.parseInt( countStr );

        tree.removeSubTree( String.valueOf( id ) );
        for ( int i = id; i < ( count - 1 ); i++ )
        {
            PropertyTree next = tree.removeSubTree( String.valueOf( i + 1 ) );
            tree.removeSubTree( String.valueOf( i ) );
            tree.addSubTree( String.valueOf( i ), next );
        }
        tree.removeSubTree( String.valueOf( count - 1 ) );

        tree.setProperty( "count", String.valueOf( --count ) );
    }
}
