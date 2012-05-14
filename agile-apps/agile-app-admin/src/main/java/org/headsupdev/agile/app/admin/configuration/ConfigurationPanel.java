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

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.list.ListItem;

import java.util.List;

/**
 * A configuration panel that shows configuration items for the user to edit the values.
 *
 * @author Andrew Williams
 * @since 1.0
 */
public class ConfigurationPanel
    extends Panel
{
    public ConfigurationPanel( String id, List<ConfigurationItem> items, final PropertyTree tree, final Application app,
                               final Project project )
    {
        this( id, items, tree, app, project, false );
    }

    public ConfigurationPanel( String id, List<ConfigurationItem> items, final PropertyTree tree, final Application app,
                               final Project project, final boolean useRowId )
    {
        super( id );

        add( new ListView<ConfigurationItem>( "item", items ) {
            protected void populateItem( final ListItem<ConfigurationItem> listItem )
            {
                ConfigurationItem config = listItem.getModelObject();

                listItem.add( new ConfigurationItemPanel( "itempanel", config, tree, app, project, useRowId,
                        listItem.getIndex() ) );
            }
        } );

    }
}

class ListConfigurationItem
    extends ConfigurationItem
{
    private int id;

    ListConfigurationItem( ConfigurationItem config, int id ) {
        super( config.getType(), config.getKey(), config.getDefault(), config.getTitle(), config.getDescription(),
                config.getSetItems(), config.getListItem() );
        this.id = id;
    }

    @Override
    public String getTitle() {
        return super.getTitle() + " " + ( id + 1 );
    }
}