/*
 * HeadsUp Agile
 * Copyright 2014 Heads Up Development.
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

package org.headsupdev.agile.storage.resource;

/**
 * A DurationWorked object that always has a unique id;
 * <p/>
 * Created: 11/08/14
 *
 * @author Andrew Williams
 * @since 2.1
 */
public class UniqueDurationWorked
    extends DurationWorked
{
    private static int workedCount;

    private long id;

    public UniqueDurationWorked()
    {
        id = ++workedCount;
    }

    @Override
    public long getId()
    {
        return id;
    }
}
