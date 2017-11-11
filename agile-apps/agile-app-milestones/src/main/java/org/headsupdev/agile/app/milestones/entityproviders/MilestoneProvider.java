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

package org.headsupdev.agile.app.milestones.entityproviders;

import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.app.milestones.MilestoneFilter;
import org.headsupdev.agile.storage.HibernateStorage;
import org.headsupdev.agile.storage.StoredProject;
import org.headsupdev.agile.storage.issues.Milestone;
import org.headsupdev.agile.storage.issues.MilestoneComparator;
import org.headsupdev.agile.web.wicket.SortableEntityProvider;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import java.util.*;

/**
 * A provider of milestones to data tables. Returns content based on the filter applied and, if applicable, project.
 * <p/>
 * Created: 12/12/2012
 *
 * @author Andrew Williams
 * @since 2.0
 */
public class MilestoneProvider
    extends SortableEntityProvider<Milestone>
{
    private MilestoneFilter filter;
    private Project project;

    public MilestoneProvider( MilestoneFilter filter )
    {
        this.filter = filter;
    }

    public MilestoneProvider( Project project, MilestoneFilter filter )
    {
        this( filter );

        this.project = project;
    }

    @Override
    protected Criteria createCriteria() {
        Session session = ( (HibernateStorage) Manager.getStorageInstance() ).getHibernateSession();
        Criteria c = session.createCriteria( Milestone.class );
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

        Criterion updatedDateQuery = filter.getDateCriterionUpdated();
        if ( updatedDateQuery != null )
        {
            c.add( updatedDateQuery );
        }

        Criterion createdDateQuery = filter.getDateCriterionCreated();
        if ( createdDateQuery != null )
        {
            c.add( createdDateQuery );
        }

        Criterion completedDateQuery = filter.getDateCriterionCompleted();
        if ( completedDateQuery != null )
        {
            c.add( completedDateQuery );
        }

        if ( project != null )
        {
            c.add( Restrictions.eq( "name.project", project ) );
        }
        else
        {
            List<Project> allWithAll = Manager.getStorageInstance().getProjects( false );
            allWithAll.add( StoredProject.getDefault() );
            c.add( Restrictions.in( "name.project", allWithAll ) );
        }
        return c;
    }

    @Override
    protected List<Order> getDefaultOrder() {
        return Arrays.asList( Order.asc( "due" ), Order.desc( "name.name" ) );
    }

    @Override
    public String getCountProperty() {
        return "name.name";
    }

    @Override
    public Iterator<Milestone> iterator( int start, int limit )
    {
        if ( getSort() != null && !getSort().getProperty().equals( "due" ) )
        {
            return super.iterator( start, limit );
        }

        Iterator<Milestone> iter = super.iterator( 0, size() );
        List<Milestone> all = new ArrayList<Milestone>();

        while ( iter.hasNext() )
        {
            all.add( iter.next() );
        }

        Collections.sort( all, new MilestoneComparator( getSort() == null || getSort().isAscending() ) );
        return all.subList( start, start + limit ).iterator();
    }
}
