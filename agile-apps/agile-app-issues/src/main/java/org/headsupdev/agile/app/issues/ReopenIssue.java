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

package org.headsupdev.agile.app.issues;

import org.headsupdev.agile.api.Event;
import org.headsupdev.agile.app.issues.event.UpdateIssueEvent;
import org.headsupdev.agile.storage.Comment;
import org.apache.wicket.markup.html.form.Form;
import org.headsupdev.agile.storage.issues.Issue;
import org.headsupdev.agile.web.MountPoint;

/**
 * Add a comment for an issue
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@MountPoint( "reopen" )
public class ReopenIssue
    extends CreateComment
{
    protected void layoutChild( Form form )
    {
        setSubmitLabel( "Reopen Issue" );
    }

    protected void submitChild( Comment comment )
    {
        getIssue().setResolution( 0 );
        getIssue().setReopened( getIssue().getReopened() + 1 );
        getIssue().setStatus( Issue.STATUS_REOPENED );
    }

    protected Event getUpdateEvent( Comment comment )
    {
        return new UpdateIssueEvent( getIssue(), getIssue().getProject(), getSession().getUser(), comment,
                "reopened" );
    }
}