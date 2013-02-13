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

package org.headsupdev.agile.app.search.feed;

import org.headsupdev.agile.web.WebUtil;
import org.headsupdev.agile.web.MountPoint;
import org.headsupdev.agile.security.permission.ProjectListPermission;
import org.headsupdev.agile.api.Permission;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.PageParameters;
import org.headsupdev.agile.web.feed.AbstractFeed;

import java.io.PrintWriter;

/**
 * A simple xml page that reports search results in xml form
 * This has been replaced by the new JSON API
 *
 * @deprecated
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@MountPoint( "search.xml" )
public class SearchFeed
   extends WebPage
{
    String DEPRECATED_ACTIVITY = "<searchResults>\n" +
            "<result>\n" +
            "<title>This app is no longer supported</title>\n" +
            "<relevance>0%</relevance>\n" +
            "<link>" + AbstractFeed.APP_URL + "</link>\n" +
            "<icon>http://headsupdev.org/resources/org.headsupdev.agile.HeadsUpResourceMarker/images/type/System.png</icon>\n" +
            "</result>\n" +
            "</searchResults>";

    public Permission getRequiredPermission() {
        return new ProjectListPermission();
    }

    public SearchFeed( PageParameters params )
    {
        WebUtil.authenticate( (WebRequest) getRequest(), (WebResponse) getResponse(), getRequiredPermission(), null );
    }

    @Override
    public String getMarkupType()
    {
        return "xml";
    }

    @Override
    protected final void onRender( MarkupStream markupStream )
    {
        PrintWriter writer = new PrintWriter(getResponse().getOutputStream());
        writer.write( DEPRECATED_ACTIVITY );
        writer.close();
    }
}
