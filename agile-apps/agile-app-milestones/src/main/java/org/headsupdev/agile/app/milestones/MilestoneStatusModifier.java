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

package org.headsupdev.agile.app.milestones;

import org.headsupdev.agile.storage.issues.Milestone;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.model.Model;

import java.util.Date;

/**
 * A modifier for highlighting table cells based on the milestone status
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
class MilestoneStatusModifier
    extends AttributeModifier
{
    public MilestoneStatusModifier(final String className, final Milestone milestone)
    {
        super ( "class", true, new Model<String>() {
            public String getObject()
            {
                if ( milestone.getCompletedDate() != null )
                {
                    return className + " statuscomplete";
                }

                if ( milestone.getDueDate() != null )
                {
                    if ( milestone.getDueDate().before( new Date() ) )
                    {
                        return className + " statusoverdue";
                    }

                    if ( milestone.getDueDate().before( MilestonesApplication.getDueSoonDate() ) )
                    {
                        return className + " statusduesoon";
                    }
                }

                return className + " statusnotdue";
            }
        } );
    }

}
