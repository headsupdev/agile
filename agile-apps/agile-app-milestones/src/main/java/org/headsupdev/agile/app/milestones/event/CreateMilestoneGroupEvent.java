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

package org.headsupdev.agile.app.milestones.event;

import org.apache.wicket.markup.html.panel.Panel;
import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.api.User;
import org.headsupdev.agile.app.milestones.MilestoneGroupPanel;
import org.headsupdev.agile.app.milestones.MilestonesApplication;
import org.headsupdev.agile.app.milestones.ViewMilestoneGroup;
import org.headsupdev.agile.storage.dao.MilestoneGroupsDAO;
import org.headsupdev.agile.storage.issues.MilestoneGroup;
import org.headsupdev.agile.web.AbstractEvent;
import org.headsupdev.agile.web.RenderUtil;
import org.headsupdev.agile.web.components.PercentagePanel;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import java.util.LinkedList;
import java.util.List;

/**
 * Event added when a milestone group is created
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 2.0
 */
@Entity
@DiscriminatorValue( "createmilestonegroup" )
public class CreateMilestoneGroupEvent
    extends AbstractEvent
{
    @Transient
    private MilestoneGroupsDAO dao = new MilestoneGroupsDAO();

    CreateMilestoneGroupEvent()
    {
    }

    public CreateMilestoneGroupEvent( MilestoneGroup group, Project project, User user )
    {
        super( "Milestone group " + group.getName() + " created by " + user.getFullnameOrUsername() ,
                group.getDescription(), group.getCreated() );

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

        return renderMilestoneGroup( group );
    }

    public List<CssReference> getBodyCssReferences()
    {
        List<CssReference> ret = new LinkedList<CssReference>();
        ret.add( referenceForCss( MilestonesApplication.class, "milestone.css" ) );
        ret.add( referenceForCss( PercentagePanel.class, "percent.css" ) );

        return ret;
    }

    public static String renderMilestoneGroup( final MilestoneGroup group )
    {
        return new RenderUtil()
        {
            public Panel getPanel()
            {
                return new MilestoneGroupPanel( RenderUtil.PANEL_ID, group );
            }
        }.getRenderedContent();
    }
}
