package org.headsupdev.agile.framework.rest;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.apache.wicket.model.Model;
import org.headsupdev.agile.api.rest.Api;
import org.headsupdev.agile.api.Manager;

import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.storage.*;
import org.apache.wicket.PageParameters;
import org.headsupdev.agile.web.MountPoint;

import java.io.IOException;
import java.util.ArrayList;

/**
 * This project API lists all active projects in a tree heirarchy along with a type identifier for each project.
 * <p/>
 * Created: 09/02/2013
 *
 * @author Andrew Williams
 * @since 2.0
 */
@MountPoint( "projects" )
public class ProjectApi
    extends Api
{
    public ProjectApi( PageParameters params )
    {
        super(params);
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
        getBuilder().registerTypeAdapterFactory(new ProjectTypeAdapterFactory( type, storageClass ) );
    }

    @Override
    public void doGet( PageParameters params )
    {
        setModel( new Model( new ArrayList( Manager.getStorageInstance().getRootProjects( true ) ) ) );
    }

    static class ProjectTypeAdapterFactory implements TypeAdapterFactory {
        private String typeName;
        private Class myType;

        public ProjectTypeAdapterFactory( String typeName, Class myType )
        {
            this.typeName = typeName;
            this.myType = myType;
        }

        public <T> TypeAdapter<T> create(final Gson gson, TypeToken<T> type) {
            if (!myType.isAssignableFrom(type.getRawType())) {
                return null; // this class only serializes specific Project subtypes
            }

            final TypeAdapter<Project> projectAdapter
                    = gson.getDelegateAdapter(this, TypeToken.get(myType));

            return new TypeAdapter<T>() {
                @Override public void write(JsonWriter out, T value) throws IOException {
                    JsonObject object = projectAdapter.toJsonTree((Project) value).getAsJsonObject();
                    object.addProperty("type", typeName);

                    gson.getAdapter(JsonElement.class).write(out, object);
                }

                @Override public T read(JsonReader in) throws IOException {return null;}
            };
        }
    }
}
