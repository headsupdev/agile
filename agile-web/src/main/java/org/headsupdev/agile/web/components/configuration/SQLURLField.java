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

package org.headsupdev.agile.web.components.configuration;

import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.headsupdev.agile.storage.DatabaseRegistry;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;

/**
 * A field used to choose a valid SQL url from the supported database types.
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class SQLURLField
    extends Panel
{
    private String type, url;

    public SQLURLField( String id )
    {
        super( id );

        layout();
    }

    public SQLURLField( String id, IModel<String> model )
    {
        super( id, model );

        layout();
    }

    protected void onBeforeRender() {
        super.onBeforeRender();

        String o = getDefaultModelObjectAsString();
        int colon1 = o.indexOf( ':' );
        int colon2 = o.indexOf( ':', colon1 + 1 );

        type = o.substring( colon1 + 1, colon2 );
        url = o.substring( colon2 + 1 );
    }

    private void layout()
    {
        final SQLURLField self = this;
        final TextField urlField = new TextField<String>( "url", new PropertyModel<String>( this, "url" ) {
            public void setObject( String o )
            {
                super.setObject( o );
                updateModelObject();
            }
        });
        urlField.setMarkupId( "sqlurl" ).setOutputMarkupId( true );
        final DropDownChoice types = new DropDownChoice<String>( "type", new PropertyModel<String>( this, "type" ),
            DatabaseRegistry.getTypes() );
        types.add( new OnChangeAjaxBehavior()
        {
            protected void onUpdate( final AjaxRequestTarget target )
            {
                String newType = (String) types.getModelObject();

                url = DatabaseRegistry.getDefaultUrl( newType );
                target.addComponent( urlField );
                updateModelObject();

                target.appendJavascript( "document.getElementById('sqlurl').onblur()" );
            }
        } );
        add( types );

        urlField.add( new AjaxFormComponentUpdatingBehavior( "onblur" )
        {
            protected void onUpdate( AjaxRequestTarget target )
            {
                self.onUpdate( target );
            }
        } );
        add( urlField );
    }

    private void updateModelObject()
    {
        ( (IModel<String>) getDefaultModel() ).setObject( "jdbc:" + type + ":" + url );
    }

    protected void onUpdate( AjaxRequestTarget target )
    {
    }
}
