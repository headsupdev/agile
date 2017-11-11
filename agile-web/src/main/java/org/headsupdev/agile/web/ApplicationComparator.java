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

package org.headsupdev.agile.web;

import org.headsupdev.agile.api.Application;

import java.util.Comparator;

/**
 * A (TODO: configurable) comparator that sorts the applications to the main menu order
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class ApplicationComparator implements Comparator<Application>
{
    public int compare( Application app1, Application app2 )
    {
        return ApplicationIdComparator.compareIds( app1.getApplicationId(), app2.getApplicationId() );
    }
}