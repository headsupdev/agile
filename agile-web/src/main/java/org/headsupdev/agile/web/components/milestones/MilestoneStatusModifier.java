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

package org.headsupdev.agile.web.components.milestones;

import org.headsupdev.agile.storage.issues.Milestone;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.model.Model;
import org.headsupdev.agile.storage.issues.MilestoneGroup;

import java.util.Date;

/**
 * A modifier for highlighting table cells based on the milestone status
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class MilestoneStatusModifier
    extends AttributeModifier
{
    public MilestoneStatusModifier( final String className, final Milestone milestone )
    {
        this( className, milestone.getDueDate(), milestone.getCompletedDate() );
    }

    public MilestoneStatusModifier( final String className, final MilestoneGroup group )
    {
        this( className, group.getDueDate(), group.getCompletedDate() );
    }

    public MilestoneStatusModifier(final String className, final Date due, final Date completed )
    {
        super ( "class", true, new Model<String>()
        {
            public String getObject()
            {
                if ( completed != null )
                {
                    return className + " statuscomplete";
                }

                if ( due != null )
                {
                    if ( due.before( new Date() ) )
                    {
                        return className + " statusoverdue";
                    }

                    if ( due.before( getDueSoonDate() ) )
                    {
                        return className + " statusduesoon";
                    }
                }

                return className + " statusnotdue";
            }
        } );
    }

    public static Date getDueSoonDate()
    {
        return new Date( System.currentTimeMillis() + (1000L * 60 * 60 * 24 * 14 ) );
    }
}
