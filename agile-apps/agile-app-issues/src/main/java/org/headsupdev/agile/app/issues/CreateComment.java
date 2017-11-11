/*
 * HeadsUp Agile
 * Copyright 2009-2014 Heads Up Development.
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

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.form.Form;
import org.headsupdev.agile.api.Event;
import org.headsupdev.agile.api.MenuLink;
import org.headsupdev.agile.api.Page;
import org.headsupdev.agile.api.Permission;
import org.headsupdev.agile.app.issues.event.CommentEvent;
import org.headsupdev.agile.app.issues.permission.IssueEditPermission;
import org.headsupdev.agile.storage.Comment;
import org.headsupdev.agile.storage.issues.Issue;
import org.headsupdev.agile.web.BookmarkableMenuLink;
import org.headsupdev.agile.web.MountPoint;
import org.headsupdev.agile.web.SubmitChildException;
import org.headsupdev.agile.web.components.AbstractCreateComment;
import org.headsupdev.agile.web.components.Subheader;

import java.util.IllegalFormatException;

/**
 * Add a comment for an issue
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@MountPoint("comment")
public class CreateComment
        extends AbstractCreateComment<Issue>
{
    @Override
    public Permission getRequiredPermission()
    {
        return new IssueEditPermission();
    }

    @Override
    protected Issue getObject()
    {
        long id;
        try
        {
            id = getPageParameters().getLong( "id" );
        }
        catch ( NumberFormatException e )
        {
            return null;
        }

        return IssuesApplication.getIssue( id, getProject() );
    }

    @Override
    protected Event getUpdateEvent( Comment comment )
    {
        return new CommentEvent( commentable, commentable.getProject(), getSession().getUser(), comment, "commented on" );
    }

    @Override
    protected MenuLink getViewLink()
    {
        return new BookmarkableMenuLink( getPageClass( "issues/view" ), getPageParameters(), "view" ) ;
    }

    @Override
    protected void layoutChild( Form form )
    {
    }

    @Override
    protected void submitChild( Comment comment ) throws SubmitChildException
    {
    }

    @Override
    protected PageParameters getSubmitPageParameters()
    {
        return getPageParameters();
    }

    @Override
    protected Class<? extends Page> getSubmitPageClass()
    {
        return getPageClass( "issues/view" );
    }

    @Override
    protected Subheader<Issue> getSubheader()
    {
        String preamble;
        if ( submitLabel.toLowerCase().contains( "issue" ) )
        {
            preamble = submitLabel.replace( "Issue", "" );
        }
        else
        {
            preamble = submitLabel + " for ";
        }
        return new IssueSubheader( "subHeader", preamble, commentable );
    }

    public Issue getIssue()
    {
        return commentable;
    }
}