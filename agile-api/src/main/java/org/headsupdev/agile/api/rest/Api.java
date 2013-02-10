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
