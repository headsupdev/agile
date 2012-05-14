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

package org.headsupdev.agile.api.service;

import org.headsupdev.agile.api.Project;

import java.util.Date;
import java.util.Set;

/**
 * A base class representing a set of changes to, for example, a source repository
 * <p/>
 * Created: 27/01/2012
 *
 * @author Andrew Williams
 * @since 1.0
 */
public interface ChangeSet
{
    String getId();
    
    Project getProject();

    String getAuthor();

    String getComment();

    Date getDate();

    Set<Change> getChanges();

    ChangeSet getPrevious();

    ChangeSet getNext();
}
