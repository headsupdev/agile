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

package org.headsupdev.agile.app.milestones.entityproviders;

import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.app.milestones.MilestoneFilter;
import org.headsupdev.agile.storage.HibernateStorage;
import org.headsupdev.agile.storage.issues.MilestoneGroup;
import org.headsupdev.agile.storage.issues.MilestoneGroupComparator;
import org.headsupdev.agile.web.wicket.SortableEntityProvider;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import java.util.*;

/**
 * A provider of MilestoneGroups to data tables. Returns content based on the filter applied and, if applicable, project.
 * <p/>
 * Created: 12/12/2012
 *
 * @author Andrew Williams
 * @since 2.0
 */
public class MilestoneGroupProvider
    extends SortableEntityProvider<MilestoneGroup>
{
    private MilestoneFilter filter;
    private Project project;

    public MilestoneGroupProvider(MilestoneFilter filter)
    {
        this.filter = filter;
    }

    public MilestoneGroupProvider(Project project, MilestoneFilter filter)
    {
        this( filter );

        this.project = project;
    }

    @Override
    protected Criteria createCriteria()
    {
        Session session = ( (HibernateStorage) Manager.getStorageInstance() ).getHibernateSession();

        Criteria c = session.createCriteria( MilestoneGroup.class );
        c.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );

        Criterion completed = filter.getCompletedCriterion();
        if ( completed != null )
        {
            c.add( completed );
        }

        Criterion dueQuery = filter.getDueCriterion();
        if ( dueQuery != null )
        {
            c.add( dueQuery );
        }

        if ( project != null )
        {
            c.add( Restrictions.eq( "name.project", project ) );
        }

        return c;
    }

    @Override
    protected List<Order> getDefaultOrder()
    {
        return Arrays.asList( Order.asc( "due" ), Order.desc( "name.name" ) );
    }

    @Override
    public String getCountProperty()
    {
        return "name.name";
    }
}
