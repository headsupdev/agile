/*
 * HeadsUp Agile
 * Copyright 2009-2013 Heads Up Development Ltd.
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

package org.headsupdev.agile.framework;

import org.headsupdev.agile.api.*;
import org.headsupdev.agile.storage.StoredProject;
import org.headsupdev.agile.web.components.FormattedDurationModel;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.util.time.Duration;
import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.headsupdev.agile.security.permission.TaskListPermission;
import org.headsupdev.agile.web.HeadsUpPage;
import org.headsupdev.agile.web.MountPoint;

import java.util.Date;
import java.util.List;

/**
 * A page to display the tasks that are currently running in the background.
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@MountPoint( "tasks" )
public class Tasks
    extends HeadsUpPage
{
    public Permission getRequiredPermission()
    {
        return new TaskListPermission();
    }

    public void layout()
    {
        super.layout();
        add( CSSPackageResource.getHeaderContribution( getClass(), "tasks.css" ) );

        final WebMarkupContainer taskLists = new WebMarkupContainer( "taskLists" );
        ListView<Task> projectTasks = new ListView<Task>( "projecttasks", new AbstractReadOnlyModel<List<? extends Task>>()
        {
            public List<? extends Task> getObject() {
                return getManager().getTasks();
            }
        } )
        {
            protected void populateItem( ListItem<Task> listItem )
            {
                Task task = listItem.getModelObject();
                if ( task == null || task.getProject() == null || !task.getProject().equals( getProject() ) )
                {
                    listItem.setVisible( false );
                    return;
                }

                setVisible( true );
                listItem.add( new Label( "time", new FormattedDurationModel( task.getStartTime(), new Date() ) ) );
                listItem.add( new Label( "title", task.getTitle() ) );
                listItem.add( new Label( "description", task.getDescription() ) );
            }
        };
        taskLists.add( projectTasks );

        ListView<Task> otherTasks = new ListView<Task>( "othertasks", new AbstractReadOnlyModel<List<? extends Task>>()
        {
            public List<? extends Task> getObject() {
                return getManager().getTasks();
            }
        } )
        {
            protected void populateItem( ListItem<Task> listItem )
            {
                Task task = listItem.getModelObject();
                if ( task == null || ( task.getProject() != null && task.getProject().equals( getProject() ) ) )
                {
                    listItem.setVisible( false );
                    return;
                }

                setVisible( true );
                listItem.add( new Label( "time", new FormattedDurationModel( task.getStartTime(), new Date() ) ) );
                listItem.add( new Label( "title", task.getTitle() ) );

                if ( task.getProject() != null )
                {
                    ExternalLink projectLink = new ExternalLink( "project-link", "/" + task.getProject().getId() + "/tasks/" );
                    projectLink.add( new Label( "project", task.getProject().getAlias() ) );
                    listItem.add( projectLink );
                }
                else
                {
                    listItem.add( new WebMarkupContainer( "project-link" ).setVisible( false ) );
                }

                listItem.add( new Label( "description", task.getDescription() ) );
            }
        };
        taskLists.add( otherTasks );

        taskLists.setOutputMarkupId( true );
        taskLists.add( new AbstractAjaxTimerBehavior( Duration.seconds( 10 ) )
        {
            protected void onTimer( AjaxRequestTarget target )
            {
                target.addComponent( taskLists );
            }
        } );


        add( taskLists );
    }

    @Override
    public String getTitle()
    {
        return "Running Tasks";
    }
}