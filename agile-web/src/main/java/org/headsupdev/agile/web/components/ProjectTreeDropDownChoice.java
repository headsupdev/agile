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

package org.headsupdev.agile.web.components;

import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.IModel;
import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.storage.StoredProject;
import org.apache.wicket.util.string.Strings;

import java.util.LinkedList;
import java.util.List;

/**
 * Simple dropdown that lists projects but uses projectIds as the value.
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class ProjectTreeDropDownChoice
    extends DropDownChoice<Project>
{
    public ProjectTreeDropDownChoice( String id )
    {
        this( id, null );
    }

    public ProjectTreeDropDownChoice( String id, IModel<Project> model )
    {
        this( id, model, true, false );
    }

    public ProjectTreeDropDownChoice( String id, IModel<Project> model, boolean showDefault, boolean withDisabled )
    {
        super( id );
        setEscapeModelStrings( false );

        List<Project> tree = new LinkedList<Project>();
        for ( Project project : Manager.getStorageInstance().getRootProjects( withDisabled ) )
        {
            tree.add( project );
            addChildIds( tree, project );
        }
        if ( showDefault )
        {
            tree.add( StoredProject.getDefault() );
        }

        setChoices( tree );
        setChoiceRenderer( new ChoiceRenderer<Project>()
        {
            public Object getDisplayValue( Project project )
            {
                StringBuilder name = new StringBuilder();
                Project parent = project.getParent();
                while ( parent != null )
                {
                    name.append( "&#160;&#160;&#160;" );
                    parent = parent.getParent();
                }
                name.append(Strings.escapeMarkup( project.getAlias() ) );

                return name.toString();
            }
        } );
        setModel( model );
    }

    private void addChildIds( List<Project> tree, Project parent )
    {
        for ( Project project : parent.getChildProjects() )
        {
            tree.add( project );

            addChildIds( tree, project );
        }
    }
}
