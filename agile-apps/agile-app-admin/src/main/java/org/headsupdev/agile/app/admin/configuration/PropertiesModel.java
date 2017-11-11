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

package org.headsupdev.agile.app.admin.configuration;

import java.util.Properties;

import org.apache.wicket.model.Model;

/**
 * A simple wicket model to get/set a property in a given PropertyTree.
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class PropertiesModel
    extends Model<String>
{
    private String key;
    private Properties properties;

    public PropertiesModel( String key, Properties props )
    {
        this.key = key;
        this.properties = props;
    }

    public String getObject()
    {
        return properties.getProperty( key );
    }

    public void setObject( String s )
    {
        properties.setProperty( key, s );
    }
}