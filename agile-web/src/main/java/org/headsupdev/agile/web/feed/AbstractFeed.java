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

package org.headsupdev.agile.web.feed;

import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.PageParameters;

import java.io.PrintWriter;

import org.headsupdev.agile.api.*;
import org.headsupdev.agile.storage.StoredProject;
import org.headsupdev.agile.web.WebUtil;

/**
 * An abstract backing for Rome based RSS feeds in agile
 * This has been replaced by the new JSON API
 *
 * @deprecated
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public abstract class AbstractFeed
   extends WebPage
{
    public static final String APP_URL = "https://itunes.apple.com/us/app/headsup-agile/id602356809?ls=1&amp;mt=8";

    String EMPTY_RESPONSE = "<rss xmlns:content=\"http://purl.org/rss/1.0/modules/content/\" xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:hud=\"http://headsupdev.com/ns/agile\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:maven=\"http://headsupdev.com/ns/maven\" xmlns:taxo=\"http://purl.org/rss/1.0/modules/taxonomy/\" version=\"2.0\">\n" +
            "</rss>";

    String DEPRECATED_RESPONSE = "<rss xmlns:content=\"http://purl.org/rss/1.0/modules/content/\" xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:hud=\"http://headsupdev.com/ns/agile\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:maven=\"http://headsupdev.com/ns/maven\" xmlns:taxo=\"http://purl.org/rss/1.0/modules/taxonomy/\" version=\"2.0\">\n" +
            "<channel>\n" +
            "<title>HeadsUp Agile Feeds Deprecated</title>\n" +
            "<link>" + APP_URL + "</link>\n" +
            "<description>This app is no longer supported</description>\n" +
            "<item>\n" +
            "<title>This app is no longer supported</title>\n" +
            "<link>" + APP_URL + "</link>\n" +
            "<description>Please download the new HeadsUp Agile app for iOS</description>\n" +
            "<pubDate>Mon, 11 Feb 2013 21:54:10 GMT</pubDate>\n" +
            "<guid>" + APP_URL + "</guid>\n" +
            "<dc:date>2013-02-11T21:54:10Z</dc:date>\n" +
            "<hud:id>1</hud:id>\n" +
            "<hud:type>SystemEvent</hud:type>\n" +
            "<hud:time>1360590850000</hud:time>\n" +
            "</item>\n" +
            "</channel>\n" +
            "</rss>";

    private Project project;
    private PageParameters parameters = new PageParameters();

    @Override
    protected void onBeforeRender() {
        super.onBeforeRender();    //To change body of overridden methods use File | Settings | File Templates.

        WebUtil.authenticate( (WebRequest) getRequest(), (WebResponse) getResponse(), getRequiredPermission(),
                getProject() );
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
        if ( parameters.get( "before" ) == null )
        {
            writer.write( DEPRECATED_RESPONSE );
        }
        else
        {
            writer.write( EMPTY_RESPONSE );
        }
        writer.close();
    }

    public Project getProject()
    {
        if ( project != null )
        {
            return project;
        }

        String projectId = getPageParameters().getString( "project" );
        if ( projectId != null && projectId.length() > 0 && !projectId.equals( StoredProject.ALL_PROJECT_ID ) )
        {
            project = Manager.getStorageInstance().getProject( projectId );
        }

        if ( project == null )
        {
            project = StoredProject.getDefault();
        }
        return project;
    }

    public PageParameters getPageParameters()
    {
        return parameters;
    }

    public void setPageParameters( PageParameters parameters )
    {
        this.parameters = parameters;
    }

    public abstract Permission getRequiredPermission();
}