/*
 * HeadsUp Agile
 * Copyright 2013 Heads Up Development.
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

import org.headsupdev.agile.storage.issues.MilestoneGroup;

/**
 * A basic DAO for handling common milestone group tasks
 * <p/>
 * Created: 4/05/2014
 *
 * @author Andrew Williams
 * @since 2.1
 */
public class MilestoneGroupsDAO
    extends HibernateDAO<MilestoneGroup, String>
{
    @Override
    protected Class<MilestoneGroup> getPersistentClass()
    {
        return MilestoneGroup.class;
    }
}
