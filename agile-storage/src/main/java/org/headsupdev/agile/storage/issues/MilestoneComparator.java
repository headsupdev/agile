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
public class MilestoneComparator implements Comparator<Milestone>
{
    private boolean ascending;

    public MilestoneComparator()
    {
        this( true );
    }

    public MilestoneComparator( boolean ascending )
    {
        this.ascending = ascending;
    }

    public int compare( Milestone milestone1, Milestone milestone2 )
    {
        if ( milestone1.getDueDate() == null || milestone2.getDueDate() == null )
        {
            if ( milestone1.getDueDate() == null && milestone2.getDueDate() == null )
            {
                if ( ascending )
                {
                    return milestone1.getName().compareToIgnoreCase( milestone2.getName() );
                }
                else
                {
                    return milestone2.getName().compareToIgnoreCase( milestone1.getName() );
                }
            }

            if ( milestone2.getDueDate() == null )
            {
                return ascending ? -1 : 1;
            }

            return ascending ? 1 : -1;
        }

        if ( milestone1.getDueDate().equals( milestone2.getDueDate() ) )
        {
            if ( ascending )
            {
                return milestone1.getName().compareToIgnoreCase( milestone2.getName() );
            }
            else
            {
                return milestone2.getName().compareToIgnoreCase( milestone1.getName() );
            }
        }

        if ( ascending )
        {
            return milestone1.getDueDate().compareTo( milestone2.getDueDate() );
        }
        else
        {
            return milestone2.getDueDate().compareTo( milestone1.getDueDate() );
        }
    }
}
