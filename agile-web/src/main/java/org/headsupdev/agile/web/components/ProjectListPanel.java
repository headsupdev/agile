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

import org.headsupdev.agile.storage.HibernateUtil;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.PageParameters;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.model.Model;
import org.headsupdev.agile.api.Project;

import java.util.List;
import java.util.LinkedList;
import java.util.Collections;

/**
 * Panel to display a tree of the projects loaded
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class ProjectListPanel
    extends Panel
{
    public ProjectListPanel( String id, final List<Project> projects, final Class pageClass, final Project current )
    {
        super( id );
        Collections.sort( projects );

        add( new ListView<Project>( "projectlinks", projects ) {
            protected void populateItem( ListItem<Project> listItem )
            {
                final Project project = (Project) HibernateUtil.getCurrentSession().load(
                        listItem.getModelObject().getClass(), listItem.getModelObject().getId() );

                PageParameters params = new PageParameters();
                params.add( "project", project.getId() );

                BookmarkablePageLink projectlink = new BookmarkablePageLink( "project-link", pageClass, params );
                projectlink.add( new Label( "project-label", project.getAlias() ) );
                projectlink.add( new AttributeModifier( "class", new Model<String>() {
                    public String getObject()
                    {
                        if ( project.equals( current ) )
                        {
                            return "selected";
                        }

                        return "";
                    }
                } ) );
                listItem.add( projectlink );

                if ( project.getChildProjects().isEmpty() )
                {
                    listItem.add( new WebMarkupContainer( "subprojects" ).setVisible( false ) );
                }
                else
                {
                    boolean show = false;
                    Project traverse = current;
                    while ( traverse != null )
                    {
                        if ( traverse.equals( project ) )
                        {
                            show = true;
                            break;
                        }

                        traverse = traverse.getParent();
                    }

                    if ( show )
                    {
                        List<Project> children = new LinkedList<Project>( project.getChildProjects() );
                        listItem.add( new ProjectListPanel( "subprojects", children, pageClass, current ) );
                    }
                    else
                    {
                        listItem.add( new WebMarkupContainer( "subprojects" ).setVisible( false ) );
                    }
                }
            }
        } );

    }
}
