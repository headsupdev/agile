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

package org.headsupdev.agile.api.rest;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.GsonBuilder;
import org.apache.wicket.PageParameters;
import org.apache.wicket.model.IModel;

/**
 * The base class for HeadsUp Agile REST API classes. This allows for simple API creation by exposing objects
 * through wicket models.
 * <p/>
 * Created: 09/02/2013
 *
 * @author Andrew Williams
 * @since 2.0
 */
public abstract class Api
    extends org.innobuilt.wicket.rest.JsonWebServicePage
{
    // TODO add basic auth to this, require if anon user has no permissions - allow hooking in for required role
    protected Api( PageParameters params )
    {
        super( params );

        setupBuilder();
    }

    protected Api( PageParameters params, Class clazz )
    {
        super( params, clazz );

        setupBuilder();
    }

    private void setupBuilder()
    {
        GsonBuilder builder = getBuilder();
        if ( respectPublishAnnotation() )
        {
            // Prefer to only expose the fields I have annotated
            builder.setExclusionStrategies( new MissingPublishExclusionStrategy() );
        }

        setupJson( builder );
    }

    public boolean respectPublishAnnotation()
    {
        return true;
    }

    public void setModel( IModel model )
    {
        super.setDefaultModel( model );
    }

    public void setupJson( GsonBuilder builder )
    {
        // for extension purposes
    }

    public abstract void doGet( PageParameters pageParameters );

    public void doPost( PageParameters pageParameters )
    {
        // override this to handle POST method
    }

    public void doPut( PageParameters pageParameters )
    {
        // override this to handle PUT method
    }

    public void doDelete( PageParameters pageParameters )
    {
        // override this to handle DELETE method
    }

    private static class MissingPublishExclusionStrategy
            implements ExclusionStrategy
    {
        public boolean shouldSkipField( FieldAttributes fieldAttributes )
        {
            return fieldAttributes.getAnnotation( Publish.class ) == null;

        }

        public boolean shouldSkipClass( Class<?> aClass )
        {
            return false;
        }
    }
}
