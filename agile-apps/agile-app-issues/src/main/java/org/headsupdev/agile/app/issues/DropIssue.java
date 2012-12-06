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

import org.apache.wicket.markup.html.CSSPackageResource;
import org.headsupdev.agile.api.Permission;
import org.headsupdev.agile.app.issues.event.UpdateIssueEvent;
import org.headsupdev.agile.app.issues.permission.IssueEditPermission;
import org.headsupdev.agile.storage.issues.Issue;
import org.headsupdev.agile.web.HeadsUpPage;
import org.headsupdev.agile.web.MountPoint;

import java.util.Date;

/**
 * Issue drop page - clears the issue's assigned user, removes the user as a watcher and marks the issue as new.
 *
 * @author Charles Harley
 * @version $Id$
 * @since 2.0
 */
@MountPoint( "drop" )
public class DropIssue
    extends HeadsUpPage
{
    public Permission getRequiredPermission()
    {
        return new IssueEditPermission();
    }

    public void layout()
    {
        super.layout();
        add( CSSPackageResource.getHeaderContribution( getClass(), "issue.css" ) );

        long id;
        try
        {
            id = getPageParameters().getLong("id");
        }
        catch ( NumberFormatException e )
        {
            notFoundError();
            return;
        }

        Issue issue = IssuesApplication.getIssue( id, getProject() );
        if ( issue == null )
        {
            notFoundError();
            return;
        }

        issue.setAssignee( null );
        issue.getWatchers().remove( getSession().getUser() );
        issue.setStatus( Issue.STATUS_NEW );
        issue.setUpdated( new Date() );

        getHeadsUpApplication().addEvent( new UpdateIssueEvent( issue, issue.getProject(), getSession().getUser(),
            "dropped" ) );

        setResponsePage( getPageClass( "issues/view" ), getPageParameters() );
    }
}