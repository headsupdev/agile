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

package org.headsupdev.agile.storage.dao;

import org.headsupdev.agile.storage.issues.Milestone;
import org.hibernate.Query;

import java.util.Date;
import java.util.List;

/**
 * A basic DAO for handling common milestone tasks
 * <p/>
 * Created: 14/05/2013
 *
 * @author Andrew Williams
 * @since 2.0
 */
public class MilestonesDAO
    extends HibernateDAO<Milestone, String>
{
    @Override
    protected Class<Milestone> getPersistentClass()
    {
        return Milestone.class;
    }

    public List<Milestone> findAllActiveDuring( Date startDate, Date endDate )
    {
        Query q = getSession().createQuery( "from " + persistentClass.getSimpleName() + " o where o.start <= :endDate " +
                "and o.due >= :startDate" );
        q.setTimestamp( "startDate", startDate );
        q.setTimestamp( "endDate", endDate );

        return (List<Milestone>) q.list();
    }

}
