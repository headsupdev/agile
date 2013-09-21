/*
 * HeadsUp Agile
 * Copyright 2013 Heads Up Development Ltd.
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

import org.apache.wicket.PageParameters;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.html.PackageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.panel.Panel;
import org.headsupdev.agile.api.*;
import org.headsupdev.agile.web.HeadsUpPage;
import org.headsupdev.agile.web.RenderUtil;

import java.io.File;

/**
 * A panel to display the maven 2 specific project information. Link to dependencies and developers if they
 * can be found in the system.
 *
 * @author Andrew Williams
 * @since 2.0
 */
public class XCodeProjectDetailsPanel
    extends Panel
{
    public XCodeProjectDetailsPanel( String id, final XCodeProject project )
    {
        super( id );

        add( new StripedListView<XCodeDependency>( "dependencies", project.getDependencies() )
        {
            protected void populateItem( ListItem<XCodeDependency> listItem )
            {
                super.populateItem( listItem );
                XCodeDependency dependency = listItem.getModelObject();

                listItem.add( new Label( "dependency-name", dependency.getName() ) );
                listItem.add( new Label( "dependency-version", dependency.getVersion() ) );
            }
        } );
    }
}
