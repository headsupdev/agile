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
import org.headsupdev.support.java.Base64;
import org.headsupdev.support.java.StringUtil;

/**
 * A simple wicket model to get/set a property encoded using Base64 in a given PropertyTree.
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class PropertyTreePasswordModel
    extends Model<String>
{
    private String key;
    private PropertyTree tree;

    public PropertyTreePasswordModel( String key, PropertyTree tree )
    {
        this.key = key;
        this.tree = tree;
    }

    public String getObject()
    {
        String ret = tree.getProperty( key );
        if ( ret == null )
        {
            return null;
        }

        return new String( Base64.decodeBase64( ret.getBytes() ) );
    }

    public void setObject( String s )
    {
        if ( !StringUtil.isEmpty( s ) )
        {
            tree.setProperty( key, new String( Base64.encodeBase64( ( s ).getBytes() ) ) );
        }
    }
}
