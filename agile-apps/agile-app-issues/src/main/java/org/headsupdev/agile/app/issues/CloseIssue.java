/*
 * HeadsUp Agile
 * Copyright 2009-2012 Heads Up Development.
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

package org.headsupdev.agile.app.issues;

import org.headsupdev.agile.app.issues.event.CloseIssueEvent;
import org.headsupdev.agile.storage.Comment;
import org.headsupdev.agile.storage.issues.Issue;
import org.headsupdev.agile.api.Event;
import org.headsupdev.agile.web.MountPoint;
import org.apache.wicket.markup.html.form.Form;

/**
 * Close an issue
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@MountPoint( "close" )
public class CloseIssue
    extends CreateComment
{
    protected void layoutChild( Form form )
    {
        setSubmitLabel( "Close Issue" );
    }

    protected void submitChild( Comment comment )
    {
        getIssue().setStatus( Issue.STATUS_CLOSED );
    }

    protected Event getUpdateEvent( Comment comment )
    {
        return new CloseIssueEvent( getIssue(), getIssue().getProject(), getSession().getUser(), comment );
    }
}