/*
 * HeadsUp Agile
 * Copyright 2009-2013 Heads Up Development.
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

import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.storage.hibernate.NameProjectId;

import javax.persistence.*;
import java.util.Date;

import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Field;

/**
 * TODO add a description
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@Entity
@DiscriminatorValue( "scm-transaction" )
@Indexed( index = "ChangeSets" )
public class TransactionalScmChangeSet
    extends ScmChangeSet
{
    @Column
    @Field
    private String revision;

    public TransactionalScmChangeSet()
    {
    }

    public TransactionalScmChangeSet( String revision, String author, String comment, Date date, Project project )
    {
        super( author, comment, date, project );

        this.revision = revision;
        this.id = new NameProjectId( revision, project );
    }

    public String getRevision()
    {
        return revision;
    }

    public String toString()
    {
        return "ScmChangeSet " + revision + " by " + getAuthor() + " \"" + getComment() + "\"";
    }

    public boolean equals( Object o )
    {
        return o instanceof TransactionalScmChangeSet && equals( (TransactionalScmChangeSet) o );
    }

    public boolean equals( TransactionalScmChangeSet set )
    {
        return set != null && getRevision().equals( set.getRevision() );
    }

    public int hashCode()
    {
        return getRevision().hashCode();
    }
}