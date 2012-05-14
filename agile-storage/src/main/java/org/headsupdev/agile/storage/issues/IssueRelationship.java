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

package org.headsupdev.agile.storage.issues;

import javax.persistence.*;
import java.io.Serializable;

/**
 * TODO document me
 *
 * @author Andrew Williams
 * @since 1.0
 */
@Entity
@Table( name = "IssueRelationships" )
public class IssueRelationship
    implements Serializable
{
    public static final int TYPE_BLOCKS = 1;
    public static final int TYPE_REQUIRES = 2;
    public static final int TYPE_DUPLICATE = 3;
    public static final int TYPE_LINKED = 4;

    public static final int REVERSE_RELATIONSHIP = 100;

    @Id
    @GeneratedValue
    private long id;

    @ManyToOne
    private Issue owner;

    @ManyToOne
    private Issue related;

    private int type = TYPE_LINKED;

    public IssueRelationship()
    {
    }

    public IssueRelationship( Issue owner, Issue related, int type )
    {
        this.owner = owner;
        this.related = related;
        this.type = type;
    }

    public Issue getOwner()
    {
        return owner;
    }

    public void setOwner( Issue owner )
    {
        this.owner = owner;
    }

    public Issue getRelated()
    {
        return related;
    }

    public void setRelated( Issue related )
    {
        this.related = related;
    }

    public int getType()
    {
        return type;
    }

    public void setType( int type )
    {
        this.type = type;
    }
}
