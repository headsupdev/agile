/*
 * HeadsUp Agile
 * Copyright 2009-2015 Heads Up Development.
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

import java.util.Set;
import java.util.Date;
import java.io.Serializable;
import java.io.File;

/**
 * Basic interface for an imported project
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public interface Project
    extends Serializable, Comparable<Project>
{
    public static final String ALL_PROJECT_ID = "all";

    String getId();

    String getName();

    String getAlias();

    void setAlias( String alias );

    String getScm();

    String getScmUsername();

    String getScmPassword();

    Set<Project> getChildProjects();
    Set<Project> getChildProjects( boolean withDisabled );

    Project getParent();

    String getRevision();

    void setRevision( String revision );

    Date getImported();

    Date getUpdated();

    boolean isDisabled();

    Set<User> getUsers();

    String getTypeName();

    void fileModified( String path, File file );

    boolean foundMetadata( File directory );

    PropertyTree getConfiguration();
    String getConfigurationValue( ConfigurationItem item );
}
