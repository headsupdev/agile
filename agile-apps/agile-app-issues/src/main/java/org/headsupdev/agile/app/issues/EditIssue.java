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

import org.headsupdev.agile.web.HeadsUpPage;
import org.headsupdev.agile.web.BookmarkableMenuLink;
import org.headsupdev.agile.web.MountPoint;
import org.headsupdev.agile.app.issues.event.UpdateIssueEvent;
import org.headsupdev.agile.app.issues.permission.IssueEditPermission;
import org.headsupdev.agile.api.Permission;
import org.headsupdev.agile.storage.issues.Issue;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.headsupdev.agile.web.components.issues.IssueListPanel;

/**
 * Issue edit page
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@MountPoint( "edit" )
public class EditIssue
    extends HeadsUpPage
{
    private long issueId;
    private Issue issue;

    public Permission getRequiredPermission()
    {
        return new IssueEditPermission();
    }

    public void layout()
    {
        super.layout();
        add( CSSPackageResource.getHeaderContribution( getClass(), "issue.css" ) );
        add( CSSPackageResource.getHeaderContribution( IssueListPanel.class, "issue.css" ) );

        try
        {
            issueId = getPageParameters().getInt( "id" );
        }
        catch ( NumberFormatException e )
        {
            notFoundError();
            return;
        }

        issue = IssuesApplication.getIssue( issueId, getProject() );
        if ( issue == null )
        {
            notFoundError();
            return;
        }

        addLink( new BookmarkableMenuLink( getPageClass( "issues/view" ), getPageParameters(), "view" ) );

        add( new EditIssueForm( "edit", issue, false, this )
        {
            public void onSubmit( Issue issue )
            {
                getHeadsUpApplication().addEvent( new UpdateIssueEvent( issue, issue.getProject(),
                                                                        EditIssue.this.getSession().getUser(), "edited" ) );
            }
        });
    }

//    @Override
//    public String getTitle()
//    {
//        return "Edit Issue:" + issueId + " " + issue.getSummary();
//    }
}