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

package org.headsupdev.agile.storage;

import org.headsupdev.agile.api.*;
import org.headsupdev.agile.api.rest.Publish;
import org.headsupdev.support.java.IOUtil;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.Model;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Developer;
import org.hibernate.*;
import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;

import javax.persistence.*;
import java.util.*;
import java.io.File;
import java.io.Reader;
import java.io.FileReader;
import java.io.Serializable;

/**
 * A project representing a maven2 pom. This type of project has many extra fields for maven metadata.
 *
 * @author Andrew Williams
 * @version $Id: MavenTwoProject.java 138 2008-01-05 15:30:58Z handyande $
 * @since 1.0
 */
@Entity
@DiscriminatorValue( "m2" )
@Indexed( index = "MavenTwoProjects" )
public class StoredMavenTwoProject
    extends StoredProject
    implements MavenTwoProject
{
    @Field(index = Index.TOKENIZED)
    @Publish
    protected String groupId, artifactId, version, packaging;

    @Type( type = "text" )
    @Field(index = Index.TOKENIZED)
    @Publish
    protected String developers;

    @Type( type = "text" )
    @Publish
    protected String modules;

    @Type( type = "text" )
    @Publish
    protected String dependencies;

    protected StoredMavenTwoProject()
    {
    }

    public StoredMavenTwoProject( File pom )
    {
        this( pom, null );
    }

    public StoredMavenTwoProject( File pom, String id )
    {
        this.id = id;

        loadFromPom( pom );
    }

    protected void loadFromPom( File pom )
    {
        Reader reader = null;
        try
        {
            reader = new FileReader( pom );
            Model model = new MavenXpp3Reader().read( reader );

            this.groupId = ( model.getGroupId() == null )?model.getParent().getGroupId():model.getGroupId();
            this.artifactId = model.getArtifactId();

            if ( id == null )
            {
                this.id = artifactId; // removed the old (groupId + ":" + artifactId) shorter ids unlikely to clash
            }

            this.packaging = model.getPackaging();
            this.version = model.getVersion();
            this.name = model.getName();

            StringBuffer developers = new StringBuffer();
            boolean first = true;
            for ( Developer developer : (List<Developer>) model.getDevelopers() )
            {
                if ( !first )
                {
                    developers.append( ',' );
                }

                developers.append( developer.getId() );
                first = false;
            }
            this.developers = developers.toString();

            StringBuffer modules = new StringBuffer();
            first = true;
            for ( String module : (List<String>) model.getModules() )
            {
                if ( !first )
                {
                    modules.append( ',' );
                }

                modules.append( module );
                first = false;
            }
            this.modules = modules.toString();

            StringBuffer dependencies = new StringBuffer();
            first = true;
            for ( Dependency dependency : (List<Dependency>) model.getDependencies() )
            {
                String depStr = dependency.getGroupId() + ":" + dependency.getArtifactId() + ":" +
                    dependency.getVersion() + ":" + dependency.getType();
                if ( !first )
                {
                    dependencies.append( ',' );
                }

                dependencies.append( depStr );
                first = false;
            }
            this.dependencies = dependencies.toString();
        }
        catch ( Exception e )
        {
            Manager.getLogger( getClass().getName() ).error( "Error reading pom file " + pom, e );
        }
        finally
        {
            if ( reader != null )
            {
                IOUtil.close( reader );
            }
        }
    }

    public void setGroupId( String groupId ) {
        this.groupId = groupId;
    }

    public String getGroupId()
    {
        return groupId;
    }

    public void setArtifactId( String artifactId ) {
        this.artifactId = artifactId;
    }

    public String getArtifactId()
    {
        return artifactId;
    }

    public void setVersion( String version ) {
        this.version = version;
    }

    public String getVersion()
    {
        if ( version != null && version.length() > 0 && !version.equals( "${project.version}" ) &&
            !version.equals( "${pom.version}" ) )
        {
            return version;
        }

        if ( parent != null && parent instanceof MavenTwoProject )
        {
            return ( (MavenTwoProject) parent ).getVersion();
        }

        return version == null ? "" : version;
    }

    public void setPackaging( String packaging ) {
        this.packaging = packaging;
    }

    public String getPackaging()
    {
        return packaging;
    }

    public List<String> getDevelopers()
    {
        if ( developers == null || developers.length() == 0 )
        {
            return new LinkedList<String>();
        }

        return Arrays.asList( developers.split( "," ) );
    }

    public List<String> getModules()
    {
        if ( modules == null || modules.length() == 0 )
        {
            return new LinkedList<String>();
        }

        return Arrays.asList( modules.split( "," ) );
    }

    public List<Project> getOrderedChildProjects()
    {
        LinkedList<Project> projects = new LinkedList<Project>();
        Set<Project> adding = getChildProjects();
        for ( String module : getModules() ) {
            String childScm;
            if ( scm.endsWith( "/" ) )
            {
                childScm = scm + module + "/";
            }
            else
            {
                childScm = scm + "/" + module;
            }

            Iterator<Project> iter = adding.iterator();
            while ( iter.hasNext() )
            {
                Project project = iter.next();
                if ( !project.getScm().equals( childScm ) )
                {
                    continue;
                }

                projects.add( project );
                iter.remove();
                break;
            }
        }

        // anything not matched by the module list
        for ( Project remaining : adding )
        {
            projects.add( remaining );
        }
        return projects;
    }

    public List<MavenDependency> getDependencies()
    {
        List<MavenDependency> ret = new LinkedList<MavenDependency>();
        if ( dependencies == null || dependencies.length() == 0 )
        {
            return ret;
        }

        String[] dependencyList = dependencies.split( "," );
        for ( String dependency : dependencyList )
        {
            final String[] gav = dependency.split( ":" );
            ret.add( new MavenTwoDependency( gav[0], gav[1], gav[2], gav[3] ) );
        }

        return ret;
    }

    public String getTypeName()
    {
        return "Maven2";
    }

    public void fileModified( String path, File file )
    {
        if ( path.equals( "pom.xml" ) )
        {
            loadFromPom( file );
            setUpdated( new Date() );

            ( (HibernateStorage) Manager.getStorageInstance() ).merge( this );

            // TODO handle the addition or removal of modules... (reusing import code)
            Manager.getInstance().fireProjectModified( this );
        }

    }

    public boolean foundMetadata( File directory )
    {
        return foundMaven2Metadata( directory );
    }

    public static boolean foundMaven2Metadata( File directory )
    {
        return ( new File( directory, "pom.xml" ) ).exists();
    }
}

class MavenTwoDependency
    implements MavenDependency, Serializable
{
    private String group, artifact, version, type;

    public MavenTwoDependency( String group, String artifact, String version, String type )
    {
        this.group = group;
        this.artifact = artifact;
        this.version = version;
        this.type = type;
    }

    public String getGroupId()
    {
        return group;
    }

    public String getArtifactId()
    {
        return artifact;
    }

    public String getVersion()
    {
        return version;
    }

    public String getType()
    {
        return type;
    }

    public MavenTwoProject getProject()
    {
        Session session = HibernateStorage.getCurrentSession();
        Transaction tx = session.beginTransaction();

        org.hibernate.Query q = session.createQuery( "from StoredProject p where groupId = :group and artifactId = :artifact" );
        q.setString( "group", group );
        q.setString( "artifact", artifact );

        // no particular guarantee that there will only be one project with these identifiers loaded!...
        MavenTwoProject project = null;
        List<MavenTwoProject> projects = (List<MavenTwoProject>) q.list();
        if ( projects.size() > 0 )
        {
            project = projects.get( 0 );
        }
        tx.commit();

        return project;
    }
}
