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

package org.headsupdev.agile.app.admin.project;

import org.headsupdev.agile.web.components.StripedListView;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.web.components.IdPatternValidator;
import org.apache.wicket.util.string.Strings;

import java.util.LinkedList;
import java.util.List;

/**
 * Listview showing projects about to be imported, handles the setting of the project IDs
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class ProjectImportListView
    extends Panel
{
    private static final String INDENT = "&nbsp;&nbsp;&nbsp;";
    private int myRow = 0;

    public ProjectImportListView( String id, List<? extends Project> projects )
    {
        this( id, projects, "", 0 );
    }

    public ProjectImportListView( String id, List<? extends Project> projects, final String indent, int row )
    {
        super( id );
        myRow = row;

        add( new StripedListView<Project>( "project", projects ) {
            protected void populateItem( ListItem<Project> listItem )
            {
                super.populateItem( listItem );

                final Project project = listItem.getModelObject();
                listItem.setModel( new CompoundPropertyModel<Project>( project ) );

                listItem.add( new Label( "name", new Model<String>() {
                    public String getObject() {
                        return indent + Strings.escapeMarkup( project.getName() );
                    }
                } ).setEscapeModelStrings( false ) );

                // TODO add a nice error message if the pattern does not match
                listItem.add( new TextField<String>( "id" ).add( new IdPatternValidator() ).setRequired( true ) );

                List<Project> children = new LinkedList<Project>( project.getChildProjects() );
                listItem.add( new ProjectImportListView( "childProjects", children, INDENT + indent, myRow + 1 ) );

                // increment row by number that will appear
                myRow += countProjects( project );
            }
        } );
    }

    private int countProjects( Project project )
    {
        int ret = 1;
        if ( project.getChildProjects() != null )
        {
            for ( Project child : project.getChildProjects() )
            {
                ret += countProjects( child );
            }
        }

        return ret;
    }
}
