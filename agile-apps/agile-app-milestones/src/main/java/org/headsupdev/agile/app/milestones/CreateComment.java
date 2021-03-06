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

package org.headsupdev.agile.app.milestones;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.form.Form;
import org.headsupdev.agile.api.Event;
import org.headsupdev.agile.api.MenuLink;
import org.headsupdev.agile.api.Page;
import org.headsupdev.agile.api.Permission;
import org.headsupdev.agile.app.milestones.event.UpdateMilestoneEvent;
import org.headsupdev.agile.app.milestones.permission.MilestoneEditPermission;
import org.headsupdev.agile.storage.Comment;
import org.headsupdev.agile.storage.dao.MilestonesDAO;
import org.headsupdev.agile.storage.issues.Milestone;
import org.headsupdev.agile.web.BookmarkableMenuLink;
import org.headsupdev.agile.web.MountPoint;
import org.headsupdev.agile.web.SubmitChildException;
import org.headsupdev.agile.web.components.AbstractCreateComment;
import org.headsupdev.agile.web.components.Subheader;

/**
 * Add a comment for a milestone
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@MountPoint("comment")
public class CreateComment
        extends AbstractCreateComment<Milestone>
{
    @Override
    protected Subheader<Milestone> getSubheader()
    {
        String preamble;
        if ( submitLabel.toLowerCase().contains( "milestone" ) ) {
            preamble = submitLabel + " ";
        }
        else
        {
            preamble = submitLabel + " for Milestone ";
        }
        preamble += commentable.getName();
        return new Subheader<Milestone>( "subHeader", preamble, commentable );
    }

    @Override
    public Permission getRequiredPermission()
    {
        return new MilestoneEditPermission();
    }

    @Override
    protected Milestone getObject()
    {
        MilestonesDAO dao = new MilestonesDAO();
        String name = getPageParameters().getString( "id" );
        return dao.find( name, getProject() );
    }

    @Override
    protected Event getUpdateEvent( Comment comment )
    {
        return new UpdateMilestoneEvent( commentable, commentable.getProject(), getSession().getUser(), comment, "commented on" );
    }

    @Override
    protected MenuLink getViewLink()
    {
        return new BookmarkableMenuLink( getPageClass( "milestones/view" ), getPageParameters(), "view" );
    }

    @Override
    protected void layoutChild( Form form )
    {
    }

    @Override
    protected void submitChild( Comment comment )
            throws SubmitChildException
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
        return getPageClass( "milestones/view" );
    }
}