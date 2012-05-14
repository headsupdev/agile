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

package org.headsupdev.agile.app.docs;

import org.headsupdev.agile.web.HeadsUpPage;
import org.headsupdev.agile.web.MountPoint;
import org.headsupdev.agile.web.components.StripedListView;
import org.headsupdev.agile.app.docs.permission.DocListPermission;
import org.headsupdev.agile.api.Permission;
import org.headsupdev.agile.storage.docs.Document;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.PageParameters;

import java.util.Collections;
import java.util.List;

/**
 * Documents home page
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@MountPoint( "contents" )
public class Index
    extends HeadsUpPage
{
    public Permission getRequiredPermission() {
        return new DocListPermission();
    }

    public void layout() {
        super.layout();
        add( CSSPackageResource.getHeaderContribution( getClass(), "doc.css" ));

        // TODO add the maven site to the doc contents page too...
        View.layoutMenuItems( this );

        List<Document> docs = ( (DocsApplication) getHeadsUpApplication() ).getDocuments( getProject() );
        Collections.sort( docs );
        add( new StripedListView<Document>( "index", docs )
        {
            protected void populateItem( ListItem<Document> listItem)
            {
                super.populateItem( listItem );

                Document doc = listItem.getModelObject();

                PageParameters params = getProjectPageParameters();
                params.add( "page", doc.getName() );
                BookmarkablePageLink link = new BookmarkablePageLink( "link", View.class, params );
                link.add( new Label( "label", doc.getName() ) );
                listItem.add( link );
            }
        });
    }

    @Override
    public String getTitle()
    {
        return "Contents";
    }
}