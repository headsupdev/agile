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

package org.headsupdev.agile.app.milestones;

import org.headsupdev.agile.storage.dao.MilestonesDAO;
import org.headsupdev.agile.web.HeadsUpPage;
import org.headsupdev.agile.web.BookmarkableMenuLink;
import org.headsupdev.agile.web.MountPoint;
import org.headsupdev.agile.app.milestones.event.UpdateMilestoneEvent;
import org.headsupdev.agile.app.milestones.permission.MilestoneEditPermission;
import org.headsupdev.agile.api.Permission;
import org.headsupdev.agile.storage.issues.Milestone;
import org.apache.wicket.markup.html.CSSPackageResource;

/**
 * Milestone edit page
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@MountPoint( "edit" )
public class EditMilestone
    extends HeadsUpPage
{
    private MilestonesDAO dao = new MilestonesDAO();

    private String name;

    public Permission getRequiredPermission()
    {
        return new MilestoneEditPermission();
    }

    public void layout()
    {
        super.layout();
        add( CSSPackageResource.getHeaderContribution( getClass(), "milestone.css" ) );

        name = getPageParameters().getString( "id" );

        Milestone milestone = dao.find(name, getProject());
        if ( milestone == null )
        {
            notFoundError();
            return;
        }

        addLink( new BookmarkableMenuLink( getPageClass( "milestones/view" ), getPageParameters(), "view" ) );

        final Milestone finalMilestone = milestone;
        add( new EditMilestoneForm( "edit", finalMilestone, false, this )
        {
            public void submitParent()
            {
                getHeadsUpApplication().addEvent( new UpdateMilestoneEvent( finalMilestone, finalMilestone.getProject(),
                                                                        EditMilestone.this.getSession().getUser() ) );
            }
        } );
    }

    @Override
    public String getTitle()
    {
        return "Edit Milestone " + name;
    }
}