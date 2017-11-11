/*
 * HeadsUp Agile
 * Copyright 2013 Heads Up Development.
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

package org.headsupdev.agile.web.rest;

import com.google.gson.*;
import org.apache.wicket.PageParameters;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebResponse;
import org.headsupdev.agile.api.*;
import org.headsupdev.agile.api.rest.Api;
import org.headsupdev.agile.storage.StoredProject;
import org.headsupdev.agile.web.auth.AuthenticationHelper;
import org.headsupdev.agile.web.auth.WebLoginManager;

import java.io.IOException;
import java.lang.reflect.Type;

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
    public void setupJson( GsonBuilder builder )
    {
        super.setupJson( builder );

        if ( shouldCollapseProjects() )
        {
            getBuilder().registerTypeHierarchyAdapter( Project.class, new ProjectAdapter() );
        }

        if ( shouldCollapseUsers() )
        {
            getBuilder().registerTypeHierarchyAdapter( User.class, new UserAdapter() );
        }
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
        if ( getRequiredPermission() == null || anonUserHasRequiredPermission() )
        {
            return true;
        }

        return false;
    }

    private boolean anonUserHasRequiredPermission() {
        Role anon = Manager.getSecurityInstance().getRoleById( "anonymous" );
        if ( anon == null )
        {
            return false;
        }

        return anon.getPermissions().contains( getRequiredPermission().getId() );
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

    protected boolean shouldCollapseProjects()
    {
        return true;
    }

    private class ProjectAdapter implements JsonSerializer<Project>
    {
        @Override
        public JsonElement serialize( Project project, Type type, JsonSerializationContext jsonSerializationContext )
        {
            return new JsonPrimitive( project.getId() );
        }
    }

    protected boolean shouldCollapseUsers()
    {
        return true;
    }

    private class UserAdapter implements JsonSerializer<User>
    {
        @Override
        public JsonElement serialize( User user, Type type, JsonSerializationContext jsonSerializationContext )
        {
            return new JsonPrimitive( user.getUsername() );
        }
    }
}
