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

import org.headsupdev.agile.api.Permission;
import org.headsupdev.agile.storage.issues.Milestone;
import org.headsupdev.agile.app.milestones.permission.MilestoneEditPermission;
import org.headsupdev.agile.app.milestones.event.CreateMilestoneEvent;
import org.headsupdev.agile.web.HeadsUpPage;
import org.headsupdev.agile.web.MountPoint;
import org.apache.wicket.markup.html.CSSPackageResource;

import java.util.Date;

/**
 * Create a milestone page
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@MountPoint( "create" )
public class CreateMilestone
    extends HeadsUpPage
{
    public Permission getRequiredPermission() {
        return new MilestoneEditPermission();
    }

    public void layout()
    {
        super.layout();
        add( CSSPackageResource.getHeaderContribution( getClass(), "milestone.css" ) );

        final Milestone create = new Milestone( "Milestone1", getProject() );

        add( new EditMilestoneForm( "create", create, true, this )
        {
            public void onSubmit() {
                create.setCreated( new Date() );
                ( (MilestonesApplication) getHeadsUpApplication() ).addMilestone( create );

                getHeadsUpApplication().addEvent( new CreateMilestoneEvent( create, create.getProject(),
                    CreateMilestone.this.getSession().getUser() ) );
            }
        }  );
    }

    @Override
    public String getTitle()
    {
        return "Create Milestone";
    }
}