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

import org.headsupdev.agile.api.service.Change;
import org.headsupdev.agile.api.service.ChangeSet;
import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;

import javax.persistence.*;
import java.io.Serializable;

/**
 * TODO add a description
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@Entity
@Table( name = "Changes" )
@Inheritance( strategy = InheritanceType.SINGLE_TABLE )
@DiscriminatorColumn( name = "changeType", discriminatorType = DiscriminatorType.STRING )
@DiscriminatorValue( "file" )
public class ScmChange
    implements Change, Serializable
{
    public static final int TYPE_ADDED = 1;
    public static final int TYPE_CHANGED = 2;
    public static final int TYPE_REMOVED = 3;

    @Id
    @GeneratedValue
    private long id;

    @Field
    private String name, revision;
    private int type;

    @Type( type = "text" )
    @Column( length = 2147483647 )
    @Field(index = Index.TOKENIZED)
    private String diff;

    @ManyToOne( fetch = FetchType.LAZY, targetEntity = ScmChangeSet.class )
    private ChangeSet set;

    public ScmChange()
    {
    }

    public ScmChange(String name, int type, String diff, ChangeSet set)
    {
        this( name, null, type, diff, set );
    }

    public ScmChange(String name, String revision, int type, String diff, ChangeSet set)
    {
        this.name = name;
        this.revision = revision;
        this.type = type;
        this.diff = diff;
        this.set = set;
    }

    public long getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public String getRevision()
    {
        return revision;
    }

    public int getType()
    {
        return type;
    }

    public String getDiff()
    {
        return diff;
    }

    public ChangeSet getSet()
    {
        return set;
    }

    public boolean equals( Object o )
    {
        return o instanceof ScmChange && equals( (ScmChange) o );
    }

    public boolean equals( ScmChange change )
    {
        return change != null && id == change.getId();
    }

    public int hashCode() {
        return ( (Long) id ).hashCode();
    }
}
