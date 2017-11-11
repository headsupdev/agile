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

package org.headsupdev.agile.web.components;

import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.basic.Label;

import java.util.List;
import java.util.LinkedList;
import java.util.Collections;
import java.io.Serializable;

import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.storage.StoredProject;

/**
 * TODO document me
 *
 * @author Andrew Williams
 * @since 1.0
 */
public abstract class ProjectTreeListView extends StripedListView<ProjectTreeNode>
{
    public ProjectTreeListView( String s )
    {
        this( s, StoredProject.getDefault() );
    }

    public ProjectTreeListView( String s, Project root )
    {
        super( s, getTreeModel( root ) );
    }

    protected abstract void populateProjectItem( ListItem listItem, Project project );

    protected final void populateItem( ListItem<ProjectTreeNode> listItem )
    {
        super.populateItem( listItem );

        listItem.add( new Label( "indent", getIndentString(
            listItem.getModelObject().getIndent() ) ).setEscapeModelStrings( false ) );

        populateProjectItem( listItem, listItem.getModelObject().getProject() );
    }

    private String getIndentString( int indent )
    {
        StringBuilder ret = new StringBuilder();
        while ( indent > 0 )
        {
            ret.append( "&nbsp;&nbsp;&nbsp;&nbsp;" );
            indent--;
        }

        return ret.toString();
    }

    private static List<ProjectTreeNode> getTreeModel( Project project )
    {
        List<ProjectTreeNode> list = new LinkedList<ProjectTreeNode>();

        List<Project> childProjects;
        if ( project.equals( StoredProject.getDefault() ) )
        {
            childProjects = Manager.getStorageInstance().getRootProjects();
        }
        else
        {
            childProjects = new LinkedList<Project>( project.getChildProjects() );
        }
        Collections.sort( childProjects );

        for ( Project child : childProjects )
        {
            addProject( child, list, 0 );
        }

        return list;
    }

    private static void addProject( Project project, List<ProjectTreeNode> list, int indent )
    {
        list.add( new ProjectTreeNode( project, indent ) );

        List<Project> childProjects = new LinkedList<Project>( project.getChildProjects() );
        Collections.sort( childProjects );
        for ( Project child : childProjects )
        {
            addProject( child, list, indent + 1 );
        }
    }
}

class ProjectTreeNode implements Serializable
{
    private Project project;
    private int indent;

    ProjectTreeNode( Project project, int indent )
    {
        this.project = project;
        this.indent = indent;
    }

    public Project getProject()
    {
        return project;
    }

    public int getIndent()
    {
        return indent;
    }
}
