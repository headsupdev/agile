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

package org.headsupdev.agile.app.milestones.event;

import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.api.User;
import org.headsupdev.agile.app.milestones.MilestonesApplication;
import org.headsupdev.agile.app.milestones.ViewMilestoneGroup;
import org.headsupdev.agile.storage.dao.MilestoneGroupsDAO;
import org.headsupdev.agile.storage.Comment;
import org.headsupdev.agile.storage.issues.MilestoneGroup;
import org.headsupdev.agile.web.AbstractEvent;
import org.headsupdev.agile.web.components.PercentagePanel;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import java.util.LinkedList;
import java.util.List;

/**
 * Event added when a milestone group is updated
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 2.0
 */
@Entity
@DiscriminatorValue( "updatemilestonegroup" )
public class UpdateMilestoneGroupEvent
    extends AbstractEvent
{
    @Transient
    private MilestoneGroupsDAO dao = new MilestoneGroupsDAO();

    UpdateMilestoneGroupEvent()
    {
    }

    public UpdateMilestoneGroupEvent( MilestoneGroup group, Project project, User user )
    {
        super( "Milestone " + group.getName() + " updated by " + user.getFullnameOrUsername(),
                group.getDescription(), group.getUpdated() );

        setApplicationId( MilestonesApplication.ID );
        setProject( project );
        setUser( user );
        setObjectId( group.getName() );
    }

    public UpdateMilestoneGroupEvent( MilestoneGroup group, Project project, User user, Comment comment, String type )
    {
        super( type + " added to milestone " + group.getName() + " by " + user.getFullnameOrUsername(),
                comment.getComment(), group.getUpdated() );

        setApplicationId( MilestonesApplication.ID );
        setProject( project );
        setUser( user );
        setObjectId( group.getName() );
    }

    public String getBody() {
        String name = getObjectId();
        MilestoneGroup group = dao.find(name, getProject());
        if ( group == null )
        {
            return "<p>Milestone group " + getObjectId() + " does not exist for project " + getProject().getAlias() + "</p>";
        }

        addLinks( ViewMilestoneGroup.getLinks( group ) );

        return CreateMilestoneGroupEvent.renderMilestoneGroup( group );
    }

    public List<CssReference> getBodyCssReferences()
    {
        List<CssReference> ret = new LinkedList<CssReference>();
        ret.add( referenceForCss( MilestonesApplication.class, "milestone.css" ) );
        ret.add( referenceForCss( PercentagePanel.class, "percent.css" ) );

        return ret;
    }
}