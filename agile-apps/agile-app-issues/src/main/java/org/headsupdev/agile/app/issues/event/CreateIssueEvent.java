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

package org.headsupdev.agile.app.issues.event;

import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.api.User;
import org.headsupdev.agile.web.AbstractEvent;
import org.headsupdev.agile.web.HeadsUpPage;
import org.headsupdev.agile.web.RenderUtil;
import org.headsupdev.agile.web.components.issues.IssueListPanel;
import org.headsupdev.agile.storage.issues.Issue;
import org.headsupdev.agile.app.issues.IssuesApplication;
import org.headsupdev.agile.app.issues.IssuePanel;
import org.headsupdev.agile.app.issues.ViewIssue;

import javax.persistence.Entity;
import javax.persistence.DiscriminatorValue;

import org.apache.wicket.markup.html.panel.Panel;

import java.util.List;
import java.util.LinkedList;

/**
 * Event added when an issue is created
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@Entity
@DiscriminatorValue( "createissue" )
public class CreateIssueEvent
    extends AbstractEvent
{
    CreateIssueEvent()
    {
    }

    public CreateIssueEvent( Issue issue, Project project )
    {
        super( issue.getReporter().getFullnameOrUsername() + " created issue " + issue.getId() + " \"" + issue.getSummary() + "\"",
               getBodySummary( issue.getBody() ), issue.getCreated() );

        setApplicationId( IssuesApplication.ID );
        setProject( project );
        setUser( issue.getReporter() );
        setObjectId( String.valueOf( issue.getId() ) );
    }

    public String getBody()
    {
        int id;
        try
        {
            id = Integer.parseInt( getObjectId() );
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

        addLinks( ViewIssue.getLinks( issue ) );

        return renderIssue( issue );
    }

    public static String getBodySummary( String body )
    {
        if ( body == null )
        {
            return null;
        }
        if ( body.length() <= 250 )
        {
            return body;
        }

        return body.substring( 0, 250 );
    }

    public List<CssReference> getBodyCssReferences()
    {
        List<CssReference> ret = new LinkedList<CssReference>();
        ret.add( referenceForCss( IssuesApplication.class, "issue.css" ) );
        ret.add( referenceForCss( IssueListPanel.class, "issue.css" ) );

        return ret;
    }

    public static String renderIssue( final Issue issue )
    {
        return new RenderUtil()
        {
            public Panel getPanel()
            {
                return new IssuePanel( RenderUtil.PANEL_ID, issue, (HeadsUpPage) getPanel().getPage());
            }
        }.getRenderedContent();
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
