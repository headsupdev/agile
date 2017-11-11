/*
 * HeadsUp Agile
 * Copyright 2009-2012 Heads Up Development.
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

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.Model;

/**
 * TODO document me
 *
 * @author Andrew Williams
 * @since 1.0
 */
public class StripedDataView<T> extends DataView<T>
{
    private boolean inverted = false;

    public StripedDataView( String s, IDataProvider<T> data )
    {
        super( s, data );
    }

    public StripedDataView( String s, IDataProvider<T> data, int pageSize )
    {
        super( s, data, pageSize );
    }

    @Override
    protected void populateItem( final Item<T> item )
    {
        item.add( new AttributeModifier( "class", true, new Model<String>()
        {
            public String getObject()
            {
                if ( !inverted && item.getIndex() % 2 == 1 )
                {
                    return "odd";
                }
                else if ( inverted && item.getIndex() % 2 == 0 )
                {
                    return "odd";
                }

                return "even";
            }
        } ) );
    }
}
