/*
 * HeadsUp Agile
 * Copyright 2013-2014 Heads Up Development Ltd.
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

import org.headsupdev.agile.api.Event;
import org.headsupdev.agile.app.milestones.event.UpdateMilestoneEvent;
import org.headsupdev.agile.storage.Comment;
import org.apache.wicket.markup.html.form.Form;
import org.headsupdev.agile.web.MountPoint;

/**
 * Add a comment for an issue
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@MountPoint( "reopen" )
public class ReopenMilestone
        extends CreateComment
{
    protected void layoutChild( Form form )
    {
        setSubmitLabel( "Reopen Milestone" );
    }

    protected void submitChild()
    {
        getMilestone().setCompletedDate( null );

        if ( getMilestone().getGroup() != null )
        {
            getMilestone().getGroup().setCompletedDate( null );
        }
    }

    @Override
    public String getTitle()
    {
        return "Reopen milestone " + getMilestone().getName();
    }

    protected Event getUpdateEvent( Comment comment )
    {
        return new UpdateMilestoneEvent( getMilestone(), getMilestone().getProject(), getSession().getUser(), comment,
                "reopened" );
    }
}