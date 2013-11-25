/*
 * HeadsUp Agile
 * Copyright 2013 Heads Up Development Ltd.
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

package org.headsupdev.agile.app.issues.dao;

import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.storage.HibernateStorage;
import org.headsupdev.agile.storage.issues.Issue;
import org.headsupdev.agile.web.components.issues.IssueFilterPanel;
import org.headsupdev.agile.web.wicket.SortableEntityProvider;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;

import java.util.Arrays;
import java.util.List;

/**
 * An issue data provider that provides some issue specific setup and overrides the "priority" sort to sort
 * by "priority" and then "type".
 *
 * @author Andrew Williams
 * @since 2.0
 */
public class SortableIssuesProvider
    extends SortableEntityProvider<Issue>
{
    private IssueFilterPanel filter;

    public SortableIssuesProvider( IssueFilterPanel filter )
    {
        this.filter = filter;
    }

    @Override
    protected Criteria createCriteria() {
        Session session = ( (HibernateStorage) Manager.getStorageInstance() ).getHibernateSession();

        Criteria c = session.createCriteria( Issue.class );
        c.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );
        c.add( filter.getStatusCriterion() );
        Criterion assignmentRestriction = filter.getAssignmentCriterion();
        if ( assignmentRestriction != null )
        {
            c.add( assignmentRestriction );
        }

        return c;
    }

    @Override
    protected List<Order> getDefaultOrder()
    {
        return Arrays.asList( Order.asc( "priority" ), Order.asc( "rank" ), Order.asc( "status" ),
                Order.asc( "id.id" ) );
    }

    @Override
    protected List<Order> getOrder( SortParam sort )
    {
        if ( sort.getProperty() == null || !sort.getProperty().equals( "priority" ) )
        {
            return super.getOrder( sort );
        }

        if ( sort.isAscending() )
        {
            return Arrays.asList( Order.asc( "priority" ), Order.asc( "type" ) );
        }
        else
        {
            return Arrays.asList( Order.desc( "priority" ), Order.desc( "type" ) );
        }
    }
}
