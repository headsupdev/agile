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

package org.headsupdev.agile.api;

import java.util.Date;
import java.util.List;
import java.io.Serializable;

/**
 * The Event definition - all events must implement these methods
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public interface Event
    extends Comparable<Event>, Serializable
{
    long getId();

    Date getTime();

    String getTitle();

    String getSummary();
    boolean isSummaryHTML();

    String getUsername();

    Project getProject();

    String getApplicationId();

    String getObjectId();

    String getBody();

    String getBodyHeader();

    List<MenuLink> getLinks();

    boolean shouldNotify( User user );
}
