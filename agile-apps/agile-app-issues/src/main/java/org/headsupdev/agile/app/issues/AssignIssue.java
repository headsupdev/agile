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

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.lang.Bytes;
import org.headsupdev.agile.api.Event;
import org.headsupdev.agile.api.User;
import org.headsupdev.agile.app.issues.event.UpdateIssueEvent;
import org.headsupdev.agile.storage.Comment;
import org.headsupdev.agile.web.HeadsUpPage;
import org.headsupdev.agile.web.MountPoint;
import org.headsupdev.agile.api.Permission;
import org.headsupdev.agile.app.issues.permission.IssueEditPermission;
import org.headsupdev.agile.storage.issues.Issue;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.headsupdev.agile.web.components.AttachmentPanel;
import org.headsupdev.agile.web.components.UserDropDownChoice;

import java.util.*;

/**
 * Issue assign page - set the issue's assigned user to any active user in the system
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 2.0
 */
@MountPoint( "assign" )
public class AssignIssue
    extends CreateComment
{
    private UserDropDownChoice userChoice;

    protected void layoutChild( Form form )
    {
        form.add( userChoice = new UserDropDownChoice( "userChoice", getIssue().getAssignee() ) );
        userChoice.setModel( new PropertyModel<User>( getIssue(),  "assignee" ) );
        userChoice.setNullValid( true );
        userChoice.setRequired( false );

        setSubmitLabel( "Assign Issue" );
    }

    protected void submitChild()
    {
        // if we have an assignee that is not watching then add them to the watchers
        if ( getIssue().getAssignee() != null && !getIssue().getWatchers().contains( getIssue().getAssignee() ) )
        {
            getIssue().getWatchers().add( getIssue().getAssignee() );
        }

        getIssue().setStatus( Issue.STATUS_ASSIGNED );
    }

    protected Event getUpdateEvent( Comment comment )
    {
        return new UpdateIssueEvent( getIssue(), getIssue().getProject(), getSession().getUser(), comment,
                "assigned" );
    }

}