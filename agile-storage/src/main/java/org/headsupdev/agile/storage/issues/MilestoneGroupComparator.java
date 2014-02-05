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

package org.headsupdev.agile.storage.issues;

import java.util.Comparator;

/**
 * Comparator for milestones to get round issues with sql null issues on due date sorting
 * <p/>
 * Created: 04/03/2012
 *
 * @author Andrew Williams
 * @since 2.0
 */
public class MilestoneGroupComparator implements Comparator<MilestoneGroup>
{
    private boolean ascending;

    public MilestoneGroupComparator()
    {
        this( true );
    }

    public MilestoneGroupComparator( boolean ascending )
    {
        this.ascending = ascending;
    }

    public int compare( MilestoneGroup group1, MilestoneGroup group2 )
    {
        if ( group1.getDueDate() == null || group2.getDueDate() == null )
        {
            if ( group1.getDueDate() == null && group2.getDueDate() == null )
            {
                if ( ascending )
                {
                    return group1.getName().compareToIgnoreCase( group2.getName() );
                }
                else
                {
                    return group2.getName().compareToIgnoreCase( group1.getName() );
                }
            }

            if ( group2.getDueDate() == null )
            {
                return ascending ? -1 : 1;
            }

            return ascending ? 1 : -1;
        }

        if ( group1.getDueDate().equals(group2.getDueDate()) )
        {
            if ( ascending )
            {
                return group1.getName().compareToIgnoreCase( group2.getName() );
            }
            else
            {
                return group2.getName().compareToIgnoreCase( group1.getName() );
            }
        }

        if ( ascending )
        {
            return group1.getDueDate().compareTo(group2.getDueDate());
        }
        else
        {
            return group2.getDueDate().compareTo(group1.getDueDate());
        }
    }
}
