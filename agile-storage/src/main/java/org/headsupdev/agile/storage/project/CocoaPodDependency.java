/*
 * HeadsUp Agile
 * Copyright 2013-2014 Heads Up Development Ltd.
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

package org.headsupdev.agile.storage.project;

import org.headsupdev.agile.api.XCodeDependency;

import java.io.Serializable;

/**
 * A simple dependency for XCode projects using CocoaPods
 * <p/>
 * Created: 07/01/2014
 *
 * @author Andrew Williams
 * @since 2.0
 */
public class CocoaPodDependency
    implements XCodeDependency, Serializable
{
    private String name, version;

    public CocoaPodDependency( String name )
    {
        this( name, UNVERSIONED );
    }

    public CocoaPodDependency( String name, String version )
    {
        this.name = name;
        this.version = version;
    }

    public String getName()
    {
        return name;
    }

    public String getVersion()
    {
        return version;
    }
}
