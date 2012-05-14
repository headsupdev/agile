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

import org.apache.wicket.model.Model;
import org.headsupdev.agile.api.PropertyTree;

/**
 * A simple wicket model to get/set a boolean property in a given PropertyTree.
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class BooleanPropertyTreeModel
    extends Model<Boolean>
{
    private String key;
    private PropertyTree tree;
    private String fallback;

    public BooleanPropertyTreeModel( String key, PropertyTree tree )
    {
        this( key, tree, null );
    }

    public BooleanPropertyTreeModel( String key, PropertyTree tree, String fallback )
    {
        this.key = key;
        this.tree = tree;
        this.fallback = fallback;
    }

    public Boolean getObject()
    {
        return Boolean.parseBoolean( tree.getProperty( key, fallback ) );
    }

    public void setObject( Boolean b )
    {
        tree.setProperty( key, b.toString() );
    }
}
