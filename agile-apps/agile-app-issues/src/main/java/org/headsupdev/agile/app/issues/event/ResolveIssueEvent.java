/*
 * HeadsUp Agile
 * Copyright 2014 Heads Up Development.
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

package org.headsupdev.agile.app.issues.event;

import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.api.User;
import org.headsupdev.agile.storage.Comment;
import org.headsupdev.agile.storage.issues.Issue;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Event added when an issue is resolved
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 2.1
 */
@Entity
@DiscriminatorValue("resolveissue")
public class ResolveIssueEvent
        extends UpdateIssueEvent
{
    ResolveIssueEvent()
    {
    }

    public ResolveIssueEvent( Issue issue, Project project, User user )
    {
        super( issue, project, user, "resolved" );
    }

    public ResolveIssueEvent( Issue issue, Project project, User user, Comment comment )
    {
        super( issue, project, user, comment, "resolved" );
    }
}