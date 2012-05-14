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

package org.headsupdev.agile.app.admin.configuration;

import org.headsupdev.agile.api.ConfigurationItem;

/**
 * An extension of ConfigurationItem that allows us to define a test for validating a configuration value.
 *
 * @author Andrew Williams
 * @since 1.0
 */
public class SystemConfigurationItem extends ConfigurationItem
{
    public SystemConfigurationItem( String key, String title, String description )
    {
        super( key, title, description );
    }

    public SystemConfigurationItem( int type, String key, String title, String description )
    {
        super( type, key, title, description );
    }

    public SystemConfigurationItem( String key, String fallback, String title, String description )
    {
        super( key, fallback, title, description );
    }

    public SystemConfigurationItem( String key, int fallback, String title, String description )
    {
        super( key, fallback, title, description );
    }

    public SystemConfigurationItem( String key, double fallback, String title, String description )
    {
        super( key, fallback, title, description );
    }

    public SystemConfigurationItem( String key, boolean fallback, String title, String description )
    {
        super( key, fallback, title, description );
    }

    public boolean test( String value )
    {
        return true;
    }
}
