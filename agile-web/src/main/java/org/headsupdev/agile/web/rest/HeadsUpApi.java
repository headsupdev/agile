package org.headsupdev.agile.web.rest;

import org.apache.wicket.PageParameters;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebResponse;
import org.headsupdev.agile.api.*;
import org.headsupdev.agile.api.rest.Api;
import org.headsupdev.agile.storage.StoredProject;
import org.headsupdev.agile.web.auth.AuthenticationHelper;
import org.headsupdev.agile.web.auth.WebLoginManager;

import java.io.IOException;

/**
 * An extension of the Api definition that handles authentication and other web related issues. 
 * <p/>
 * Created: 10/02/2013
 *
 * @author Andrew Williams
 * @since 2.0
 */
public abstract class HeadsUpApi
    extends Api
{
    private Project project;

    public HeadsUpApi( PageParameters params )
    {
        super( params );
    }

    public HeadsUpApi( PageParameters params, Class clazz )
    {
        super( params, clazz );
    }

    @Override
    public boolean isRequestAuthorized()
    {
        if ( isAnonymousAllowed() )
        {
            return true;
        }

        try
        {
            boolean authenticated = AuthenticationHelper.requestAuthentication(
                    ( (WebRequest) getRequest() ).getHttpServletRequest(),
                    ( (WebResponse) getResponse() ).getHttpServletResponse() );

            if ( authenticated )
            {
                User user = WebLoginManager.getInstance().getLoggedInUser( ( (WebRequest) getRequest() ).getHttpServletRequest() );

                return Manager.getSecurityInstance().userHasPermission( user, getRequiredPermission(), getProject() );
            }
        }
        catch ( IOException e )
        {
            // simply return in the unauthorised state
        }

        return false;
    }

    protected boolean isAnonymousAllowed()
    {
        // if anon access allowed then grant access to other areas
        Role anon = Manager.getSecurityInstance().getRoleById( "anonymous" );
        if ( getRequiredPermission() == null || anon.getPermissions().contains( getRequiredPermission().getId() ) )
        {
            return true;
        }

        return false;
    }

    public abstract Permission getRequiredPermission();

    public Project getProject()
    {
        if ( project != null )
        {
            return project;
        }

        String projectId = getPageParameters().getString( "project" );

        if ( projectId != null && projectId.length() > 0 && !projectId.equals( StoredProject.ALL_PROJECT_ID ) )
        {
            project = Manager.getStorageInstance().getProject( projectId );
        }

        if ( project == null )
        {
            project = StoredProject.getDefault();
        }
        return project;
    }
}
