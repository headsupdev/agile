/*
 * HeadsUp Agile
 * Copyright 2009-2015 Heads Up Development Ltd.
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

package org.headsupdev.agile.web.wicket;

import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * A data provider that supports sorting of entities retrieved from hibernate.
 * <p/>
 * Created: 22/01/2012
 *
 * @author Andrew Williams
 * @since 2.0
 */
public abstract class SortableEntityProvider<T> extends SortableDataProvider<T>
{
    protected abstract Criteria createCriteria();

    protected abstract List<Order> getDefaultOrder();

    protected List<Order> getOrder( SortParam sort )
    {
        if ( sort.isAscending() )
        {
            return Arrays.asList( Order.asc( sort.getProperty() ).ignoreCase() );
        }
        else
        {
            return Arrays.asList( Order.desc(sort.getProperty()).ignoreCase() );
        }
    }

    public Iterator<T> iterator( int start, int limit )
    {
        Criteria criteria = createCriteria();
        if ( getSort() != null )
        {
            for ( Order order : getOrder( getSort() ) )
            {
                criteria.addOrder( order );
            }
        }
        else
        {
            for ( Order order : getDefaultOrder() )
            {
                criteria.addOrder( order );
            }
        }
        criteria.setFirstResult( start ).setMaxResults( limit );
        return (Iterator<T>) criteria.list().iterator();
    }

    public int size()
    {
        Criteria criteria = createCriteria();
        criteria.setProjection( Projections.rowCount() );
        return ((Number) criteria.uniqueResult() ).intValue();
    }

    public IModel<T> model( Object o )
    {
        return new CompoundPropertyModel<T>( o );
    }
}
