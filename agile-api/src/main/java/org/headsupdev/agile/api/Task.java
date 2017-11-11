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

package org.headsupdev.agile.api;

import java.io.Serializable;
import java.util.Date;

/**
 * A task is a unit of work that runs in the background.
 * Tasks are things such as updating the local source or building projects etc.
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public interface Task
        extends Serializable
{
    String getTitle();

    String getDescription();

    Project getProject();

    Date getStartTime();
}
