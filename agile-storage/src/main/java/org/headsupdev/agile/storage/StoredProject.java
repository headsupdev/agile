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
import org.headsupdev.support.java.StringUtil;
import org.headsupdev.support.java.Base64;
import org.hibernate.search.annotations.*;
import org.hibernate.annotations.Proxy;

import javax.persistence.*;
import java.util.*;
import java.io.File;

/**
 * Basic interface for an imported project
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@Entity
@Table( name = "Projects" )
@Inheritance( strategy = InheritanceType.SINGLE_TABLE )
@DiscriminatorColumn( name = "type", discriminatorType = DiscriminatorType.STRING )
@Indexed( index = "Projects" )
@Proxy( lazy = false )
public class StoredProject
    implements Project, SearchResult
{
    private static Project defaultProject;
    public static Project getDefault()
    {
        if ( defaultProject == null )
        {
            defaultProject = new StoredProject( ALL_PROJECT_ID, "All Projects" );

            defaultProject.getUsers().addAll( getDefaultProjectMembers() );
        }
        return defaultProject;
    }

    public static final ConfigurationItem CONFIGURATION_TIMETRACKING_ENABLED = new ConfigurationItem(
            "timetracking.enabled", true, "Should this project enable time tracking?",
            "If set then time tracking features will be available for issues and milestones" );
    public static final ConfigurationItem CONFIGURATION_TIMETRACKING_REQUIRED = new ConfigurationItem(
            "timetracking.required", false, "Should this project enforce time tracking?",
            "If set then time tracking features information will be required for issues and milestones" );
    public static final ConfigurationItem CONFIGURATION_TIMETRACKING_BURNDOWN = new ConfigurationItem(
            "timetracking.burndown", true, "Use project burndown for tracking a milestone's progress?",
            "When tracking progress and projecting milestone completion should we use the sprint burndown methodology?" );
    public static final ConfigurationItem CONFIGURATION_TIMETRACKING_IGNOREWEEKEND = new ConfigurationItem(
            "timetracking.ignoreweekend", true, "Ignore weekends in time tracking?",
            "Hide weekend days from graphs and do not include when calculating time requirements" );

    private static final String KEY_DEFAULTPROJECT_MEMBERS = "defaultprojectmembers";
    private static final String KEY_DEFAULTPROJECT_SUBSCRIBERS = "defaultprojectsubscribers";

    public static Set<User> getDefaultProjectMembers()
    {
        Set<User> ret = new HashSet<User>();
        String memberList = Manager.getStorageInstance().getGlobalConfiguration()
                .getProperty( KEY_DEFAULTPROJECT_MEMBERS );

        if ( memberList != null && memberList.length() > 0 )
        {
            String[] memberNames = memberList.split( "," );

            for ( String name : memberNames )
            {
                ret.add( Manager.getSecurityInstance().getUserByUsername( name ) );
            }
        }

        return ret;
    }

    public static void setDefaultProjectMembers( Set<User> users )
    {
        StringBuffer list = new StringBuffer();

        Iterator<User> userIter = users.iterator();
        while ( userIter.hasNext() )
        {
            User user = userIter.next();

            list.append( user.getUsername() );
            if ( userIter.hasNext() )
            {
                list.append( "," );
            }
        }

        Manager.getStorageInstance().getGlobalConfiguration()
                .setProperty( KEY_DEFAULTPROJECT_MEMBERS, list.toString() );
    }

    public static Set<User> getDefaultProjectSubscribers()
    {
        Set<User> ret = new HashSet<User>();
        String subscriberList = Manager.getStorageInstance().getGlobalConfiguration()
                .getProperty( KEY_DEFAULTPROJECT_SUBSCRIBERS );

        if ( subscriberList != null && subscriberList.length() > 0 )
        {
            String[] subscriberNames = subscriberList.split( "," );

            for ( String name : subscriberNames )
            {
                ret.add( Manager.getSecurityInstance().getUserByUsername( name ) );
            }
        }

        return ret;
    }

    public static void setDefaultProjectSubscribers( Set<User> users )
    {
        StringBuffer list = new StringBuffer();

        Iterator<User> userIter = users.iterator();
        while ( userIter.hasNext() )
        {
            User user = userIter.next();

            list.append( user.getUsername() );
            if ( userIter.hasNext() )
            {
                list.append( "," );
            }
        }

        Manager.getStorageInstance().getGlobalConfiguration()
                .setProperty( KEY_DEFAULTPROJECT_SUBSCRIBERS, list.toString() );
    }

    @Id
    @DocumentId
    protected String id;

    @Field(index = Index.TOKENIZED)
    protected String name, alias, scm;

    protected String scmUsername, scmPassword;

    @Field
    protected String revision;

    @Temporal( TemporalType.TIMESTAMP )
    private Date imported = new Date(), updated;

    protected Boolean disabled;

    @ManyToOne( targetEntity = StoredProject.class, fetch = FetchType.LAZY )
    protected Project parent;

    @OneToMany( targetEntity = StoredProject.class, fetch = FetchType.LAZY, mappedBy = "parent" )
    protected Set<Project> children = new HashSet<Project>();

    @ManyToMany( targetEntity = StoredUser.class, fetch = FetchType.LAZY, mappedBy = "projects" )
    private Set<User> users = new HashSet<User>();

    protected StoredProject()
    {
    }

    public StoredProject( String id, String name )
    {
        this.id = id;
        this.name = name;
    }

    public static String encodeId( String name )
    {
        if ( name == null )
        {
            return null;
        }

        String newId = "";
        String lowerName = name.toLowerCase();
        for ( int i = 0; i < lowerName.length(); i++ )
        {
            char chr = lowerName.charAt( i );
            if ( ( chr >= 'a' && chr <= 'z' ) || ( chr >= 'A' && chr <= 'Z' ) || chr == '_' || chr == '-' || chr == ':' )
            {
                newId += chr;
            }
        }

        return newId;
    }

    /**
     * Never call this on a persisted entity, it is the primary key
     * @param id
     */
    public void setId( String id )
    {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setAlias( String alias )
    {
        this.alias = alias;
    }

    public String getAlias() {
        if ( alias == null )
        {
            return name;
        }

        return alias;
    }

    public void setScm( String scm )
    {
        this.scm = scm;
    }

    public String getScm() {
        return scm;
    }

    public void setScmUsername( String scmUsername )
    {
        this.scmUsername = scmUsername;
    }

    public String getScmUsername() {
        return scmUsername;
    }

    public void setScmPassword( String scmPassword )
    {
        if ( scmPassword == null )
        {
            this.scmPassword = null;
        }
        else
        {
            this.scmPassword = new String( Base64.encodeBase64( scmPassword.getBytes() ) );
        }
    }

    public String getScmPassword() {
        if ( scmPassword == null )
        {
            return null;
        }

        return new String( Base64.decodeBase64( scmPassword.getBytes() ) );
    }

    public String getRevision()
    {
        return revision;
    }

    public void setRevision( String revision )
    {
        this.revision = revision;
    }

    public Date getImported()
    {
        return imported;
    }

    public void setImported( Date imported )
    {
        this.imported = imported;
    }

    public Date getUpdated()
    {
        return updated;
    }

    public void setUpdated( Date updated )
    {
        this.updated = updated;
    }

    public boolean isDisabled()
    {
        return disabled != null && disabled.equals( Boolean.TRUE );
    }

    public void setDisabled( boolean disabled )
    {
        this.disabled = disabled;
    }

    public String getTypeName()
    {
        return "Files";
    }

    public void addChildProject( Project child )
    {
        if ( child instanceof StoredProject )
        {
            ( (StoredProject) child ).setParent( this );
        }

        children.add( child );
    }

    public Set<Project> getChildProjects()
    {
        return getChildProjects( false );
    }

    public Set<Project> getChildProjects( boolean withDisabled )
    {
        if ( withDisabled )
        {
            return children;
        }

        Set<Project> toRemove = new HashSet<Project>();

        for ( Project project : children )
        {
            if ( ( (StoredProject) project ).isDisabled() )
            {
                toRemove.add( project );
            }
        }

        if ( toRemove.isEmpty() )
        {
            return children;
        }

        Set<Project> notDisabled = new HashSet<Project>( children.size() );
        notDisabled.addAll( children );
        notDisabled.removeAll( toRemove );

        return notDisabled;
    }

    public void setParent( Project parent )
    {
        this.parent = parent;
    }

    public Project getParent() {
        return parent;
    }

    public Set<User> getUsers()
    {
        return users;
    }

    public void fileModified( String path, File file )
    {
    }

    public boolean foundMetadata( File directory )
    {
        return false;
    }

    public String getIconPath() {
        return null;
    }

    public String getLink() {
        return "/" + getId() + "/show";
    }

    public PropertyTree getConfiguration()
    {
        return Manager.getStorageInstance().getGlobalConfiguration().getProjectConfiguration( this );
    }

    public String getConfigurationValue( ConfigurationItem item )
    {
        String ret = getConfiguration().getProperty( item.getKey() );
        if ( ret == null )
        {
            if ( getParent() != null )
            {
                return parent.getConfigurationValue( item );
            }
            else if ( !equals( StoredProject.getDefault() ) )
            {
                return StoredProject.getDefault().getConfigurationValue( item );
            }
            return String.valueOf( item.getDefault() );
        }

        return ret;
    }

    public String toString()
    {
        if ( StringUtil.isEmpty( getAlias() ) )
        {
            return getId();
        }

        return getAlias();
    }

    public boolean equals( Object o )
    {
         return o instanceof Project && equals( (Project) o );
    }

    public boolean equals( Project p )
    {
        return p != null && getId().equals( p.getId() );
    }

    public int hashCode()
    {
        // TODO WTF?
        if ( getId() == null )
        {
            return 0;
        }

        return getId().hashCode();
    }

    public int compareTo( Project p )
    {
        if ( getAlias() == null )
        {
            return -1;
        }
        if ( p == null )
        {
            return 1;
        }

        return getAlias().compareToIgnoreCase( p.getAlias() );
    }
}
