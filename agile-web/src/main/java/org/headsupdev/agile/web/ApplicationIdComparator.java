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

package org.headsupdev.agile.web;

import java.util.Comparator;
import java.util.List;
import java.util.Arrays;

/**
 * A (TODO: configurable) comparator that sorts the application ids to the main menu order
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class ApplicationIdComparator implements Comparator<String>
{
    private static List idOrder = Arrays.asList( "dashboard", "activity", "docs", "issues", "milestones", "files", "builds",
        "artifacts", "search", "admin" );

    public int compare( String app1, String app2 )
    {
        return ApplicationIdComparator.compareIds( app1, app2 );
    }

    public static int compareIds( String app1, String app2 )
    {
        int id1 = 99;
        int id2 = 99;

        if ( idOrder.contains( app1 ) )
        {
            id1 = idOrder.indexOf( app1 );
        }
        if ( idOrder.contains( app2 ) )
        {
            id2 = idOrder.indexOf( app2 );
        }

        return id1 - id2;
    }
}
