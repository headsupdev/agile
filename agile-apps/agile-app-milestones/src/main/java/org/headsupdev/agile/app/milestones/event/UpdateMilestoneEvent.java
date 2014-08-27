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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.api.User;
import org.headsupdev.agile.app.milestones.MilestonesApplication;
import org.headsupdev.agile.app.milestones.ViewMilestone;
import org.headsupdev.agile.app.milestones.permission.MilestoneEditPermission;
import org.headsupdev.agile.storage.Comment;
import org.headsupdev.agile.storage.dao.MilestonesDAO;
import org.headsupdev.agile.storage.issues.Milestone;
import org.headsupdev.agile.web.AbstractEvent;
import org.headsupdev.agile.web.RenderUtil;
import org.headsupdev.agile.web.components.CommentPanel;
import org.headsupdev.agile.web.components.PercentagePanel;
import org.headsupdev.agile.web.dialogs.ConfirmDialog;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import java.util.LinkedList;
import java.util.List;

/**
 * Event added when a milestone is updated
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@Entity
@DiscriminatorValue("updatemilestone")
public class UpdateMilestoneEvent
        extends AbstractEvent
{
    @Transient
    private MilestonesDAO dao = new MilestonesDAO();

    UpdateMilestoneEvent()
    {
    }

    public UpdateMilestoneEvent( Milestone milestone, Project project, User user )
    {
        super( "Milestone " + milestone.getName() + " updated by " + user.getFullnameOrUsername(),
                milestone.getDescription(), milestone.getUpdated() );

        setApplicationId( MilestonesApplication.ID );
        setProject( project );
        setUser( user );
        setObjectId( milestone.getName() );
    }

    public UpdateMilestoneEvent( Milestone milestone, Project project, User user, Comment comment, String action )
    {
        super( user.getFullnameOrUsername() + " " + action + " milestone " + milestone.getName(),
                comment.getComment(), milestone.getUpdated() );
        setApplicationId( MilestonesApplication.ID );
        setProject( project );
        setUser( user );
        setObjectId( milestone.getName() );
        setSubObjectId( String.valueOf( comment.getId() ) );
    }

    public String getBody()
    {
        String name = getObjectId();
        Milestone milestone = dao.find( name, getProject() );
        if ( milestone == null )
        {
            return "<p>Milestone " + getObjectId() + " does not exist for project " + getProject().getAlias() + "</p>";
        }

        addLinks( ViewMilestone.getLinks( milestone ) );
        if ( getSubObjectId() == null || "0".equals( getSubObjectId() ) )
        {
            return CreateMilestoneEvent.renderMilestone( milestone );
        }
        else
        {
            Comment comment = MilestonesApplication.getComment( Long.parseLong( getSubObjectId() ) );
            if ( comment == null )
            {
                return "<p>Comment " + getSubObjectId() + " does not exist</p>";
            }

            return renderComment( comment, milestone );
        }
    }

    public List<CssReference> getBodyCssReferences()
    {
        List<CssReference> ret = new LinkedList<CssReference>();
        ret.add( referenceForCss( MilestonesApplication.class, "milestone.css" ) );
        ret.add( referenceForCss( PercentagePanel.class, "percent.css" ) );

        return ret;
    }

    private String renderComment( final Comment comment, final Milestone milestone )
    {
        if ( comment == null )
        {
            return "";
        }

        String content = new RenderUtil()
        {
            public Panel getPanel()
            {
                return new CommentPanel<Milestone>( RenderUtil.PANEL_ID, new Model( comment ), getProject(), null, milestone, new MilestoneEditPermission() );
            }
        }.getRenderedContent();

        return "<table class=\"comments vertical\">" + content + "</table>";
    }
}