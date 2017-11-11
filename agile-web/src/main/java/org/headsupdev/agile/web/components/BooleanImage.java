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

package org.headsupdev.agile.web.components;

import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.model.IModel;
import org.headsupdev.agile.web.HeadsUpPage;

/**
 * A component that shows a pass or fail icon depending on the boolean value
 *
 * @author Andrew Williams
 * @since 1.0
 */
public class BooleanImage extends Image
{
    public BooleanImage( String s )
    {
        super( s, new ResourceReference( HeadsUpPage.class, "images/blank.gif" ) );
    }

    public BooleanImage( String s, boolean state )
    {
        super( s, getImageReference( state ) );
    }

    public BooleanImage( String s, IModel model )
    {
        this( s, (Boolean) model.getObject() );
    }

    public void setBoolean( boolean state )
    {
        setImageResourceReference( getImageReference( state ) );
    }

    private static ResourceReference getImageReference( boolean pass )
    {
        if ( pass ) {
            return new ResourceReference( HeadsUpPage.class, "images/pass.png" );
        }

        return new ResourceReference( HeadsUpPage.class, "images/fail.png" );
    }
}
