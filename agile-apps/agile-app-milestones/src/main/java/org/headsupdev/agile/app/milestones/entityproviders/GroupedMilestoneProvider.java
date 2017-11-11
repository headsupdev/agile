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

import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.app.milestones.MilestoneFilter;
import org.headsupdev.agile.storage.issues.MilestoneGroup;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

/**
 * A provider of milestones to data tables. Returns content based on the filter applied, the group the milestones are
 * in and, if applicable, project.
 * If a null group is passed this provider will return only milestones with no group.
 * <p/>
 * Created: 12/12/2012
 *
 * @author Andrew Williams
 * @since 2.0
 */
public class GroupedMilestoneProvider
    extends MilestoneProvider
{
    private MilestoneGroup group;

    public GroupedMilestoneProvider( MilestoneGroup group, MilestoneFilter filter )
    {
        super( filter );

        this.group = group;
    }

    public GroupedMilestoneProvider( MilestoneGroup group, Project project, MilestoneFilter filter )
    {
        super( project, filter );

        this.group = group;
    }

    @Override
    protected Criteria createCriteria()
    {
        Criteria filterCriteria = super.createCriteria();

        if ( group == null )
        {
            return filterCriteria.add( Restrictions.isNull( "group" ) );
        }

        return filterCriteria.add( Restrictions.eq( "group", group ) );
    }
}
