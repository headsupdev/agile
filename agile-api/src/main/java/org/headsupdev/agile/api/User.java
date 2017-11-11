/*
 * HeadsUp Agile
 * Copyright 2009-2014 Heads Up Development.
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
import java.util.Set;
import java.io.Serializable;
import java.util.TimeZone;

/**
 * Basic user object.
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public interface User
    extends Serializable, Comparable<User>
{
    public String getUsername();

    public String getPassword();

    public String getEmail();

    public String getTelephone();

    public String getFirstname();

    public String getLastname();

    public String getFullname();

    public String getFullnameOrUsername();

    public String getInitials();

    public Date getCreated();

    public Date getLastLogin();

    public boolean canLogin();
    public boolean isDisabled();

    public String getDescription();

    public Set<Role> getRoles();

    public Set<Project> getProjects();

    public Set<Project> getSubscriptions();
    public boolean isSubscribedTo( Project project );

    public TimeZone getTimeZone();

    public boolean isHiddenInTimeTracking();

    public String getPreference( String key, String fallback );

    public int getPreference( String key, int fallback );

    public boolean getPreference( String key, boolean fallback );

    public Date getPreference( String key, Date fallback );

    public void setPreference( String key, String value );

    public void setPreference( String key, int value );

    public void setPreference( String key, boolean value );

    public void setPreference( String key, Date date );
}
