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

import org.headsupdev.agile.api.*;
import org.headsupdev.agile.web.RenderUtil;
import org.headsupdev.agile.web.AbstractEvent;
import org.headsupdev.agile.web.components.PercentagePanel;
import org.headsupdev.agile.app.milestones.MilestonesApplication;
import org.headsupdev.agile.app.milestones.ViewMilestone;
import org.headsupdev.agile.app.milestones.MilestonePanel;
import org.headsupdev.agile.storage.issues.Milestone;

import javax.persistence.Entity;
import javax.persistence.DiscriminatorValue;

import org.apache.wicket.markup.html.panel.Panel;

import java.util.List;
import java.util.LinkedList;

/**
 * Event added when a milestone is created
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@Entity
@DiscriminatorValue( "createmilestone" )
public class CreateMilestoneEvent
    extends AbstractEvent
{
    CreateMilestoneEvent()
    {
    }

    public CreateMilestoneEvent( Milestone milestone, Project project, User user )
    {
        super( "Milestone " + milestone.getName() + " created by " + user.getFullnameOrUsername() ,
                milestone.getDescription(), milestone.getCreated() );

        setApplicationId( MilestonesApplication.ID );
        setProject( project );
        setUser( user );
        setObjectId( milestone.getName() );
    }

    public String getBody() {
        String name = getObjectId();
        Milestone milestone = MilestonesApplication.getMilestone( name, getProject() );
        if ( milestone == null )
        {
            return "<p>Milestone " + getObjectId() + " does not exist for project " + getProject().getAlias() + "</p>";
        }

        addLinks( ViewMilestone.getLinks( milestone ) );

        return renderMilestone( milestone );
    }

    public List<CssReference> getBodyCssReferences()
    {
        List<CssReference> ret = new LinkedList<CssReference>();
        ret.add( referenceForCss( MilestonesApplication.class, "milestone.css" ) );
        ret.add( referenceForCss( PercentagePanel.class, "percent.css" ) );

        return ret;
    }

    public static String renderMilestone( final Milestone milestone )
    {
        return new RenderUtil()
        {
            public Panel getPanel()
            {
                return new MilestonePanel( RenderUtil.PANEL_ID, milestone );
            }
        }.getRenderedContent();
    }
}
