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

package org.headsupdev.agile.api;

/**
 * A simple menu link where we provide the target path and label as parameters and let the framework do the rest
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class SimpleMenuLink implements MenuLink
{
    private String target, label;

    public SimpleMenuLink( String target )
    {
        this( target, target );
    }

    public SimpleMenuLink( String target, String label )
    {
        this.target = target;
        this.label = label;
    }

    public String getTarget()
    {
        return target;
    }

    public String getLabel()
    {
        return label;
    }

    public void onClick()
    {
        // never called, we are special
    }

    @Override
    public boolean equals( Object o )
    {
        if ( this == o ) return true;
        if ( o == null || getClass() != o.getClass() ) return false;

        SimpleMenuLink that = (SimpleMenuLink) o;

        if ( label != null ? !label.equals( that.label ) : that.label != null ) return false;
        if ( target != null ? !target.equals( that.target ) : that.target != null ) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = target != null ? target.hashCode() : 0;
        result = 31 * result + ( label != null ? label.hashCode() : 0 );
        return result;
    }
}