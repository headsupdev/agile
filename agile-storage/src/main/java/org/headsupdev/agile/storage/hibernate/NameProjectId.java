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

package org.headsupdev.agile.storage.hibernate;

import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.storage.StoredProject;

import javax.persistence.Embeddable;
import javax.persistence.ManyToOne;
import javax.persistence.Column;
import java.io.Serializable;

import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Field;

/**
 * An embeddable entity that uses a name and a project as the id.
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@Embeddable
public class NameProjectId
    implements Serializable
{
    @ManyToOne( targetEntity = StoredProject.class, optional = false )
    Project project;

    @Column( nullable = false )
    @Field( index = Index.TOKENIZED )
    String name;

    public NameProjectId()
    {
    }

    public NameProjectId( String name )
    {
        this( name, StoredProject.getDefault() );
    }

    public NameProjectId( String name, Project project )
    {
        this.project = project;
        this.name = name;
    }

    public Project getProject()
    {
        return project;
    }

    public String getName()
    {
        return name;
    }

    /**
     * This should not be used once the object has been persisted!....
     * @param name The new name to set
     */
    public void setName( String name )
    {
        this.name = name;
    }

    public int hashCode()
    {
        return ( project.getId() + name ).hashCode();
    }

    public boolean equals( Object o )
    {
        return o instanceof NameProjectId && equals( (NameProjectId) o );
    }

    public boolean equals( NameProjectId id )
    {
        return project.equals( id.getProject() ) && name.equals( id.getName() );
    }
}
