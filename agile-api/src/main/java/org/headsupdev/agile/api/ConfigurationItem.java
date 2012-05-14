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

import java.io.File;
import java.io.Serializable;
import java.util.List;
import java.util.LinkedList;

/**
 * A class representing a single configuration item for system or application configuration.
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class ConfigurationItem
    implements Serializable
{
    public static final int TYPE_STRING = 1;
    public static final int TYPE_INT = 2;
    public static final int TYPE_BOOL = 3;
    public static final int TYPE_CRON = 4;
    public static final int TYPE_DOUBLE = 5;

    public static final int TYPE_SET = 10;
    public static final int TYPE_LIST = 20;

    private int type;
    private String key, title, description;
    private Object fallback;

    private List<ConfigurationItem> setItems = new LinkedList<ConfigurationItem>();
    private ConfigurationItem listItem;

    public ConfigurationItem( String key, String title, String description )
    {
        this( TYPE_STRING, key, title, description );
    }

    public ConfigurationItem( int type, String key, String title, String description )
    {
        this( type, key, null, title, description, null, null );
    }

    public ConfigurationItem( String key, String fallback, String title, String description )
    {
        this( TYPE_STRING, key, fallback, title, description, null, null );
    }

    public ConfigurationItem( String key, File fallback, String title, String description )
    {
        this( TYPE_STRING, key, fallback.getAbsolutePath(), title, description, null, null );
    }

    public ConfigurationItem( String key, int fallback, String title, String description )
    {
        this( TYPE_INT, key, fallback, title, description, null, null );
    }

    public ConfigurationItem( String key, double fallback, String title, String description )
    {
        this( TYPE_DOUBLE, key, fallback, title, description, null, null );
    }

    public ConfigurationItem( String key, boolean fallback, String title, String description )
    {
        this( TYPE_BOOL, key, fallback, title, description, null, null );
    }

    public ConfigurationItem( String key, String title, List<ConfigurationItem> children )
    {
        this( TYPE_SET, key, null, title, "", children, null );
    }

    public ConfigurationItem( String key, String title, ConfigurationItem child )
    {
        this( TYPE_LIST, key, null, title, "", null, child );
    }

    public ConfigurationItem( int type, String key, Object fallback, String title, String description )
    {
        this( type, key, fallback, title, description, null, null );
    }

    protected ConfigurationItem( int type, String key, Object fallback, String title, String description,
                                 List<ConfigurationItem> children, ConfigurationItem child )
    {
        this.type = type;
        this.key = key;
        this.fallback = fallback;
        this.title = title;
        this.description = description;
        this.setItems = children;
        this.listItem = child;
    }

    public int getType()
    {
        return type;
    }

    public String getKey()
    {
        return key;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle( String title )
    {
        this.title = title;
    }

    public String getDescription()
    {
        return description;
    }

    public void SetDescription( String description )
    {
        this.description = description;
    }

    public Object getDefault()
    {
        return fallback;
    }

    public void setDefault( Object fallback )
    {
        this.fallback = fallback;
    }

    public List<ConfigurationItem> getSetItems()
    {
        return setItems;
    }

    public ConfigurationItem getListItem()
    {
        return listItem;
    }
}
