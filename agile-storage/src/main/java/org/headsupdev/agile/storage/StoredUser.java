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

import org.headsupdev.agile.api.rest.Publish;
import org.headsupdev.agile.api.util.HashUtil;
import org.headsupdev.support.java.StringUtil;
import org.headsupdev.agile.api.*;
import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;

import javax.persistence.*;
import java.util.*;

/**
 * The basic implementation of a user class
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@Entity
@Table( name = "Users" )
@Indexed( index = "Users" )
public class StoredUser
    implements User, SearchResult
{
    @Id
    @DocumentId
    @Field
    @Publish
    private String username;

    private String password;

    @Field(index = Index.TOKENIZED)
    @Publish
    private String firstname, lastname, email;

    @Type( type = "text" )
    @Field(index = Index.TOKENIZED)
    @Publish
    private String description;

    @Temporal( TemporalType.TIMESTAMP )
    @Publish
    private Date created = new Date();

    @Temporal( TemporalType.TIMESTAMP )
    @Publish
    private Date lastLogin = null;

    @Publish
    private String timeZoneId;

    @Publish
    private Boolean disabled = Boolean.FALSE;

    private Boolean hiddenInTimeTracking = Boolean.FALSE;

    @ManyToMany( targetEntity = StoredRole.class, fetch = FetchType.LAZY )
    private Set<Role> roles = new HashSet<Role>();

    @ManyToMany( targetEntity = StoredProject.class, fetch = FetchType.LAZY )
    private Set<Project> projects = new HashSet<Project>();

    @ManyToMany( targetEntity = StoredProject.class, fetch = FetchType.LAZY )
    @JoinTable( name = "Users_Subscriptions" )
    private Set<Project> subscriptions = new HashSet<Project>();

    private transient Map<String, String> preferences = null;

    StoredUser()
    {
    }

    public StoredUser( String username )
    {
        this.username = username;
    }

    public String getUsername()
    {
        return username;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword( String password )
    {
        if ( password == null )
        {
            throw new IllegalArgumentException( "password cannot be null" );
        }

        this.password = HashUtil.getMD5Hex( password );
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail( String email )
    {
        this.email = email;
    }

    public String getFirstname()
    {
        return firstname;
    }

    public void setFirstname( String firstname )
    {
        this.firstname = firstname;
    }

    public String getLastname()
    {
        return lastname;
    }

    public void setLastname( String lastname )
    {
        this.lastname = lastname;
    }

    public String getFullname()
    {
        if ( StringUtil.isEmpty( firstname ) && StringUtil.isEmpty( lastname ) )
        {
            return username;
        }

        return ( ( ( firstname == null ) ? "" : firstname ) + " " + ( ( lastname == null ) ? "" : lastname ) ).trim();
    }

    public String getFullnameOrUsername()
    {
        if ( Manager.getStorageInstance().getGlobalConfiguration().getUseFullnamesForUsers() )
        {
            return getFullname();
        }

        return getUsername();
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription( String description )
    {
        this.description = description;
    }

    public Date getCreated()
    {
        return created;
    }

    public void setCreated( Date created )
    {
        this.created = created;
    }

    public Date getLastLogin()
    {
        return lastLogin;
    }

    public void setLastLogin( Date last )
    {
        this.lastLogin = last;
    }

    public boolean canLogin()
    {
        return disabled == null || disabled == Boolean.FALSE;
    }

    public boolean isDisabled()
    {
        return disabled == Boolean.TRUE;
    }

    public void setDisabled( boolean disabled )
    {
        this.disabled = disabled;
    }

    public Set<Role> getRoles()
    {
        return roles;
    }

    public void addRole( Role role )
    {
        roles.add( role );
    }

    public Set<Project> getProjects()
    {
        return projects;
    }

    public Set<Project> getSubscriptions()
    {
        return subscriptions;
    }

    public boolean isSubscribedTo( Project project )
    {
        return ( getSubscriptions() != null && getSubscriptions().contains( project ) ) ||
            ( ( project == null || project.equals( StoredProject.getDefault() ) ) &&
                StoredProject.getDefaultProjectSubscribers().contains( this ) );
    }

    public void setProjects( Set<Project> projects )
    {
        this.projects.clear();
        this.projects.addAll( projects );
    }

    public String getIconPath() {
        return null;
    }

    public String getLink() {
        return "/account/username/" + getUsername();
    }

    public String getTimeZoneId()
    {
        return timeZoneId;
    }

    public void setTimeZoneId( String timeZoneId )
    {
        this.timeZoneId = timeZoneId;
    }

    public TimeZone getTimeZone()
    {
        if ( StringUtil.isEmpty( getTimeZoneId() ) )
        {
            return Manager.getStorageInstance().getGlobalConfiguration().getDefaultTimeZone();
        }

        return TimeZone.getTimeZone( getTimeZoneId() );
    }

    public boolean isHiddenInTimeTracking()
    {
        return hiddenInTimeTracking != null && hiddenInTimeTracking.equals( Boolean.TRUE );
    }

    public void setHiddenInTimeTracking( boolean hidden )
    {
        this.hiddenInTimeTracking = hidden;
    }
    
    public void loadPreferences()
    {
        preferences = new Hashtable<String,String>();
        preferences.putAll( Manager.getStorageInstance().getConfigurationItems( "user." + username + "." ) );
    }

    public Set<String> getPreferenceKeys()
    {
        if ( preferences == null )
        {
            loadPreferences();
        }

        Set<String> ret = new HashSet<String>();
        for ( String key : preferences.keySet() )
        {
            ret.add( key.substring( 6 + username.length() ) );
        }
        return ret;
    }

    public String getPreference( String key )
    {
        if ( preferences == null )
        {
            loadPreferences();
        }

        return preferences.get( "user." + username + "." + key );
    }

    public String getPreference( String key, String fallback )
    {
        String ret = getPreference( key );

        if ( ret == null )
        {
            return fallback;
        }

        return ret;
    }

    public boolean getPreference( String key, boolean fallback )
    {
        String value = getPreference( key );

        if ( value == null )
        {
            return fallback;
        }

        return Boolean.parseBoolean( value );
    }

    public int getPreference( String key, int fallback )
    {
        String value = getPreference( key );

        if ( value == null )
        {
            return fallback;
        }

        try
        {
            return Integer.parseInt( value );
        }
        catch ( NumberFormatException e )
        {
            return fallback;
        }
    }

    public void savePreferences()
    {
        for ( String key : preferences.keySet() )
        {
            Manager.getStorageInstance().setConfigurationItem( key, preferences.get( key ) );
        }
    }

    public void setPreference( String key, String value )
    {
        String oldValue = getPreference( key );
        if ( key == null || ( value == null && oldValue == null ) )
        {
            return;
        }

        key = "user." + username + "." + key;
        if ( value == null )
        {
            preferences.remove( key );
            Manager.getStorageInstance().removeConfigurationItem(  key );
        }
        else if ( oldValue == null || !value.equals( oldValue ) )
        {
            preferences.put( key, value );
            Manager.getStorageInstance().setConfigurationItem( key, value );
        }
    }

    public void setPreference( String key, int value )
    {
        setPreference(key, String.valueOf(value));
    }

    public void setPreference( String key, boolean value )
    {
        setPreference(key, String.valueOf(value));
    }

    public boolean equals( Object user )
    {
        return user instanceof User && equals( (User) user );
    }

    public boolean equals( User user )
    {
        if ( user == null )
        {
            return false;
        }

        // TODO wtf?
        if ( username == null )
        {
            return user.getUsername() == null;
        }

        return username.equals( user.getUsername() );
    }

    public int hashCode()
    {
        // TODO wtf?
        if ( username == null ) {
            return 0;
        }

        return username.hashCode();
    }

    public String toString()
    {
        return getFullnameOrUsername();
    }

    public int compareTo( User u )
    {
        if ( u == null )
        {
            return 1;
        }

        if ( getLastname() == null || u.getLastname() == null )
        {
            return getUsername().compareToIgnoreCase( u.getUsername() );
        }

        if ( Manager.getStorageInstance().getGlobalConfiguration().getUseFullnamesForUsers() )
        {
            return getFullname().compareToIgnoreCase( u.getFullname() );
        }

        return u.getUsername().compareToIgnoreCase( u.getUsername() );
    }
}