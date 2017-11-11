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

import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.model.Model;

/**
 * A simple percent bar
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class PercentagePanel
    extends Panel
{
    public PercentagePanel( final String id, final int percent )
    {
        super( id );
        add( CSSPackageResource.getHeaderContribution( PercentagePanel.class, "percent.css" ) );

        WebMarkupContainer todo = new WebMarkupContainer( "todo" );
        add( todo.add( new AttributeModifier( "title", true, new Model<String>()
        {
            public String getObject() {
                return ( 100 - percent ) + "% incomplete";
            }
        } ) ) );

        todo.add( new WebMarkupContainer( "done" ).add( new AttributeModifier( "style", true, new Model<String>()
        {
            public String getObject() {
                return "width:" + percent + "%";
            }
        } ) ).add( new AttributeModifier( "title", true, new Model<String>()
        {
            public String getObject() {
                return percent + "% completed";
            }
        } ) ) );

        add( new Label( "percent", String.valueOf( percent ) + "%" ) );
    }
}
