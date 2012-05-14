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

package org.headsupdev.agile.app.admin.event;

import org.headsupdev.agile.api.*;
import org.headsupdev.agile.web.RenderUtil;
import org.headsupdev.agile.web.AbstractEvent;
import org.headsupdev.agile.web.components.ProjectDetailsPanel;
import org.headsupdev.agile.web.components.MavenTwoProjectDetailsPanel;
import org.headsupdev.agile.storage.StoredProject;

import javax.persistence.Entity;
import javax.persistence.DiscriminatorValue;
import java.util.Date;

import org.apache.wicket.markup.html.panel.Panel;

/**
 * Event representing the addition of a project to the system.
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@Entity
@DiscriminatorValue( "updateproject" )
public class UpdateProjectEvent
    extends AbstractEvent
{
    UpdateProjectEvent()
    {
    }

    // TODO move this event to the admin app and any code related to updating the projects...
    public UpdateProjectEvent( Project project )
    {
        super( "Project " + project.getName() + " was updated",
               "The project \"" + project.getName() + "\" was updated from scm", new Date() );

        setApplicationId( "admin" );
        setProject( project );
    }

    private String renderProject( final Project project )
    {
        return new RenderUtil()
        {
            public Panel getPanel()
            {
                return new ProjectDetailsPanel( RenderUtil.PANEL_ID, project );
            }
        }.getRenderedContent();
    }

    private String renderMavenTwoProjectDetails( final MavenTwoProject project )
    {
        return new RenderUtil()
        {
            public Panel getPanel()
            {
                return new MavenTwoProjectDetailsPanel( RenderUtil.PANEL_ID, project, true );
            }
        }.getRenderedContent();
    }

    public String getBodyHeader() {
        return "<link rel=\"stylesheet\" type=\"text/css\" href=\"/resources/org.headsupdev.agile.app.dashboard.Welcome/welcome.css\" />";
    }

    public String getBody()
    {
        Project project = getProject();
        if ( project == null || project.equals( StoredProject.getDefault() ) )
        {
            return "<p>Project could not be found</p>";
        }

        project = Manager.getStorageInstance().getProject( project.getId() );

        StringBuffer ret = new StringBuffer();
        ret.append( renderProject( project ) );
        if ( project instanceof MavenTwoProject )
        {
            ret.append( "<br />" );
            ret.append( renderMavenTwoProjectDetails( (MavenTwoProject) project ) );
        }
        return ret.toString();
    }
}