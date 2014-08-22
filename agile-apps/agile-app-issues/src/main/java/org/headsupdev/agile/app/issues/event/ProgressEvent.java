/*
 * HeadsUp Agile
 * Copyright 2009-2014 Heads Up Development Ltd.
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

package org.headsupdev.agile.app.issues.event;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.api.User;
import org.headsupdev.agile.app.issues.CommentPanel;
import org.headsupdev.agile.app.issues.IssuesApplication;
import org.headsupdev.agile.storage.issues.Issue;
import org.headsupdev.agile.storage.resource.DurationWorked;
import org.headsupdev.agile.web.AbstractEvent;
import org.headsupdev.agile.web.RenderUtil;
import org.headsupdev.agile.web.components.issues.IssueListPanel;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.LinkedList;
import java.util.List;

/**
 * Event added when an issue is updated
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@Entity
@DiscriminatorValue("progress")
public class ProgressEvent
        extends AbstractEvent
{
    ProgressEvent()
    {
    }

    public ProgressEvent( Issue issue, Project project, User user, String type )
    {
        super( user.getFullnameOrUsername() + " " + type + " issue " + issue.getId() + " \"" + issue.getSummary() + "\"",
                CreateIssueEvent.getBodySummary( issue.getBody() ), issue.getUpdated() );

        setApplicationId( IssuesApplication.ID );
        setProject( project );
        setUser( user );
        setObjectId( String.valueOf( issue.getId() ) );
    }

    public ProgressEvent( Issue issue, Project project, User user, DurationWorked duration, String type )
    {
        super( user.getFullnameOrUsername() + " " + type + " issue:" + issue.getId() + " \"" + issue.getSummary() + "\"",
                getDurationComment( duration ), issue.getUpdated() );

        setApplicationId( IssuesApplication.ID );
        setProject( project );
        setUser( user );
        setObjectId( String.valueOf( issue.getId() ) );
        setSubObjectId( String.valueOf( duration.getId() ) );
    }

    public String getBody()
    {
        long id;
        try
        {
            id = Long.parseLong( getObjectId() );
        }
        catch ( NumberFormatException e )
        {
            return "<p>Invalid issue ID " + getObjectId() + "</p>";
        }

        Issue issue = IssuesApplication.getIssue( id, getProject() );
        if ( issue == null )
        {
            return "<p>Issue " + getObjectId() + " does not exist for project " + getProject().getAlias() + "</p>";
        }

        DurationWorked duration = IssuesApplication.getDurationWorked( Long.parseLong( getSubObjectId() ) );
        if ( duration == null )
        {
            return "<p>Progress " + getSubObjectId() + " does not exist for Issue:" + issue.getId() + "</p>";
        }

        return renderDuration( duration, issue );
    }

    public List<CssReference> getBodyCssReferences()
    {
        List<CssReference> ret = new LinkedList<CssReference>();
        ret.add( referenceForCss( IssuesApplication.class, "issue.css" ) );
        ret.add( referenceForCss( IssueListPanel.class, "issue.css" ) );

        return ret;
    }

    private String renderDuration( final DurationWorked duration, final Issue issue )
    {
        if ( duration == null )
        {
            return "";
        }

        String content = new RenderUtil()
        {
            public Panel getPanel()
            {
                return new CommentPanel( RenderUtil.PANEL_ID, new Model( duration ), getProject(), null, issue );
            }
        }.getRenderedContent();

        return "<table class=\"comments vertical\">" + content + "</table>";
    }


    public static String getDurationComment( DurationWorked duration )
    {
        if ( duration.getComment() != null )
        {
            return duration.getComment().getComment();
        }
        else
        {
            return null;
        }
    }

    @Override
    public boolean shouldNotify( User user )
    {
        int id = Integer.parseInt( getObjectId() );
        Issue issue = IssuesApplication.getIssue( id, getProject() );

        return notifyUserForIssue( user, issue );
    }

    static boolean notifyUserForIssue( User user, Issue issue )
    {
        return issue.getWatchers().contains( user );
    }
}