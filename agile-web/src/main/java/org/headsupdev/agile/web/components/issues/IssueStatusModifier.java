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

package org.headsupdev.agile.web.components.issues;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.model.Model;
import org.headsupdev.agile.storage.issues.Issue;

/**
 * A behaviour to set the class of an element depending on the issue status.
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class IssueStatusModifier
    extends AttributeModifier
{
    public IssueStatusModifier(final String className, final Issue issue)
    {
        super ( "class", true, new Model<String>() {
            public String getObject()
            {
                return className + " status" + String.valueOf( issue.getStatus() - 200 );
            }
        } );
    }
}
