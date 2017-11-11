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

package org.headsupdev.agile.framework.rest;

import org.apache.wicket.protocol.http.WebRequest;
import org.headsupdev.agile.api.*;
import org.headsupdev.agile.api.rest.Publish;
import org.headsupdev.agile.security.permission.ProjectListPermission;
import org.headsupdev.agile.storage.*;
import org.headsupdev.agile.web.MountPoint;
import org.headsupdev.agile.web.auth.WebLoginManager;
import org.headsupdev.agile.web.rest.HeadsUpApi;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import org.apache.wicket.PageParameters;
import org.apache.wicket.model.Model;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

/**
 * This project API lists all active projects in a tree heirarchy along with a type identifier for each project.
 * <p/>
 * Created: 09/02/2013
 *
 * @author Andrew Williams
 * @since 2.0
 */
@MountPoint("projects")
public class ProjectApi
        extends HeadsUpApi
{
    public ProjectApi( PageParameters params )
    {
        super( params );
    }

    @Override
    protected boolean shouldCollapseProjects()
    {
        return false;
    }

    @Override
    public Permission getRequiredPermission()
    {
        return new ProjectListPermission();
    }

    @Override
    public void setupJson( GsonBuilder builder )
    {
        registerProjectType( "ant", StoredAntProject.class );
        registerProjectType( "cmdline", StoredCommandLineProject.class );
        registerProjectType( "eclipse", StoredEclipseProject.class );
        registerProjectType( "file", StoredFileProject.class );
        registerProjectType( "maven", StoredMavenTwoProject.class );
        registerProjectType( "xcode", StoredXCodeProject.class );
    }

    protected void registerProjectType( String type, Class storageClass )
    {
        getBuilder().registerTypeAdapterFactory( new ProjectTypeAdapterFactory( type, storageClass ) );
    }

    @Override
    public void doGet( PageParameters params )
    {
        if ( getProject() == null || getProject().equals( StoredProject.getDefault() ) )
        {
            User user = WebLoginManager.getInstance().getLoggedInUser( ( (WebRequest) getRequest() ).getHttpServletRequest() );

            if ( returnInactiveProjects( params ) )
            {
                setModel( new Model<GroupedProjects>( new GroupedProjectsWithInactive( user ) ) );
            }
            else
            {
                setModel( new Model<GroupedProjects>( new GroupedProjects( user ) ) );
            }
        }
        else
        {
            setModel( new Model( getProjectEntry() ) );
        }
    }

    protected boolean returnInactiveProjects( PageParameters params )
    {
        return params.getAsBoolean( "withInactive", true );
    }

    protected HashMap<String, Project> getProjectEntry()
    {
        HashMap<String, Project> ret = new HashMap<String, Project>();

        ret.put( "project", getProject() );
        return ret;
    }

    static class GroupedProjects
            implements Serializable
    {
        @Publish
        List<Project> recent, active;

        public GroupedProjects( User user )
        {
            Storage storage = Manager.getStorageInstance();

            recent = storage.getRecentRootProjects( user );
            Collections.sort( recent );

            active = new ArrayList<Project>( storage.getActiveRootProjects() );
            active.removeAll( recent );
            Collections.sort( active );
        }
    }

    static class GroupedProjectsWithInactive
            extends GroupedProjects
    {
        @Publish
        List<Project> inactive;

        public GroupedProjectsWithInactive( User user )
        {
            super( user );
            Storage storage = Manager.getStorageInstance();

            inactive = new ArrayList<Project>( storage.getRootProjects() );
            inactive.removeAll( recent );
            inactive.removeAll( active );
            Collections.sort( inactive );
        }
    }

    static class ProjectTypeAdapterFactory
            implements TypeAdapterFactory
    {
        private String typeName;
        private Class myType;

        public ProjectTypeAdapterFactory( String typeName, Class myType )
        {
            this.typeName = typeName;
            this.myType = myType;
        }

        public <T> TypeAdapter<T> create( final Gson gson, TypeToken<T> type )
        {
            if ( !myType.isAssignableFrom( type.getRawType() ) )
            {
                return null; // this class only serializes specific Project subtypes
            }

            final TypeAdapter<Project> projectAdapter = gson.getDelegateAdapter( this, TypeToken.get( myType ) );

            return new TypeAdapter<T>()
            {
                @Override
                public void write( JsonWriter out, T value )
                        throws IOException
                {
                    JsonObject object = projectAdapter.toJsonTree( (Project) value ).getAsJsonObject();
                    object.addProperty( "type", typeName );

                    gson.getAdapter( JsonElement.class ).write( out, object );
                }

                @Override
                public T read( JsonReader in )
                        throws IOException
                {
                    return null;
                }
            };
        }
    }
}
