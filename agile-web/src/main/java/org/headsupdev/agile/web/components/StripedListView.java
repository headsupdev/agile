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

package org.headsupdev.agile.web.components;

import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.AttributeModifier;

import java.util.List;

/**
 * TODO document me
 *
 * @author Andrew Williams
 * @since 1.0
 */
public class StripedListView<T> extends ListView<T>
{
    private boolean inverted = false;

    public StripedListView( String s )
    {
        super( s );
        this.setStartIndex( 0 );
    }

    public StripedListView( String s, IModel<? extends java.util.List<? extends T>> iModel )
    {
        super( s, iModel );
        this.setStartIndex( 0 );
    }

    public StripedListView( String s, List<? extends T> list )
    {
        super( s, list );
        this.setStartIndex( 0 );
    }

    protected void populateItem( final ListItem<T> listItem )
    {
        listItem.add( new AttributeModifier( "class", true, new Model<String>()
        {
            public String getObject()
            {
                if ( !inverted && listItem.getIndex() % 2 == 1 )
                {
                    return "odd";
                }
                else if ( inverted && listItem.getIndex() % 2 == 0 )
                {
                    return "odd";
                }

                return "even";
            }
        } ) );
    }

    public boolean isInverted()
    {
        return inverted;
    }

    public StripedListView setInverted( boolean inverted )
    {
        this.inverted = inverted;

        return this;
    }
}
