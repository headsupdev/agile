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

import org.headsupdev.agile.api.User;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.LinkedList;

import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.api.Event;
import org.headsupdev.agile.api.MenuLink;

/**
 * The basic implementation of an event class
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@Entity
@Table( name = "Events" )
@Inheritance( strategy = InheritanceType.SINGLE_TABLE )
@DiscriminatorColumn( name = "type", discriminatorType = DiscriminatorType.STRING )
@DiscriminatorValue( "system" )
public class StoredEvent
    implements Event
{
    @Id
    @GeneratedValue
    private long id;

    @Temporal( TemporalType.TIMESTAMP )
    @Index( name="timeIndex" )
    private Date time;

    @Type( type = "text" )
    private String title;

    private String username, applicationId;

    private String objectId;

    private String subObjectId;

    @Type( type = "text" )
    private String summary;

    @ManyToOne( targetEntity = StoredProject.class, fetch = FetchType.LAZY )
    private Project project;

    protected StoredEvent()
    {
    }

    public StoredEvent( String title, Date time )
    {
        this( title, null, time );
    }

    public StoredEvent( String title, String summary, Date time )
    {
        if ( time == null || title == null )
        {
            throw new IllegalArgumentException( "time and title cannot be null" );
        }

        this.title = title;
        this.summary = summary;
        this.time = time;
    }

    public String getType()
    {
        DiscriminatorValue val = getClass().getAnnotation( DiscriminatorValue.class );

        return val == null ? null : val.value();
    }

    public long getId() {
        return id;
    }

    public Date getTime()
    {
        return time;
    }

    public void setTime( Date time )
    {
        this.time = time;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle( String title )
    {
        this.title = title;
    }

    public String getSummary()
    {
        return summary;
    }
    
    public boolean isSummaryHTML()
    {
        return false;
    }

    public void setSummary( String summary )
    {
        this.summary = summary;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername( String username )
    {
        this.username = username;
    }

    public Project getProject()
    {
        return project;
    }

    public void setProject( Project project )
    {
        this.project = project;
    }

    public String getApplicationId()
    {
        return applicationId;
    }

    public void setApplicationId( String applicationId )
    {
        this.applicationId = applicationId;
    }

    public String getObjectId()
    {
        return objectId;
    }

    public void setObjectId( String objectId )
    {
        this.objectId = objectId;
    }

    public String getSubObjectId()
    {
        return subObjectId;
    }

    public void setSubObjectId( String subObjectId )
    {
        this.subObjectId = subObjectId;
    }

    public String getBody()
    {
        return "<p>This event has no further description</p>";
    }

    public String getBodyHeader()
    {
        return "";
    }

    public List<MenuLink> getLinks()
    {
        return new LinkedList<MenuLink>();
    }

    public boolean shouldNotify( User user )
    {
        return false;
    }

    public int hashCode() {
        return ( (Long) getId() ).hashCode();
    }

    public boolean equals( Object o ) {
        return o instanceof Event && equals( (Event) o );
    }

    public boolean equals( Event e )
    {
        return getId() == e.getId();
    }

    public int compareTo( Event event )
    {
        return -1 * getTime().compareTo( event.getTime() );
    }
}