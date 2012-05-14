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

package org.headsupdev.agile.app.milestones;

import org.headsupdev.agile.api.Event;
import org.headsupdev.agile.storage.Comment;
import org.headsupdev.agile.app.milestones.event.CompleteMilestoneEvent;
import org.headsupdev.agile.web.MountPoint;

import java.util.Date;

import org.apache.wicket.markup.html.form.Form;

/**
 * Milestone complete page - mark it as completed
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@MountPoint( "complete" )
public class CompleteMilestone
    extends CreateComment
{
    protected void layoutChild( Form form )
    {
        setSubmitLabel( "Complete Milestone" );
    }

    protected void submitChild()
    {
        getMilestone().setCompletedDate( new Date() );
    }

    protected Event getUpdateEvent( Comment comment )
    {
        return new CompleteMilestoneEvent( getMilestone(), getMilestone().getProject(), getSession().getUser(), comment );
    }
}
