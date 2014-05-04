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

import org.apache.wicket.markup.html.CSSPackageResource;
import org.headsupdev.agile.api.Permission;
import org.headsupdev.agile.storage.dao.MilestoneGroupsDAO;
import org.headsupdev.agile.app.milestones.event.UpdateMilestoneGroupEvent;
import org.headsupdev.agile.app.milestones.permission.MilestoneEditPermission;
import org.headsupdev.agile.storage.issues.MilestoneGroup;
import org.headsupdev.agile.web.BookmarkableMenuLink;
import org.headsupdev.agile.web.HeadsUpPage;
import org.headsupdev.agile.web.MountPoint;

/**
 * Milestone group edit page
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 2.0
 */
@MountPoint( "editgroup" )
public class EditMilestoneGroup
    extends HeadsUpPage
{
    private MilestoneGroupsDAO dao = new MilestoneGroupsDAO();

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

        MilestoneGroup group = dao.find(name, getProject());
        if ( group == null )
        {
            notFoundError();
            return;
        }

        addLink( new BookmarkableMenuLink( getPageClass( "milestones/viewgroup" ), getPageParameters(), "view" ) );

        final MilestoneGroup finalGroup = group;
        add( new EditMilestoneGroupForm( "edit", finalGroup, false, this )
        {
            public void submitParent()
            {
                super.submitParent();

                getHeadsUpApplication().addEvent( new UpdateMilestoneGroupEvent( finalGroup, finalGroup.getProject(),
                                                                        EditMilestoneGroup.this.getSession().getUser() ) );
            }
        } );
    }

    @Override
    public String getTitle()
    {
        return "Edit Milestone " + name;
    }
}