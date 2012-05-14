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
import java.io.IOException;

import com.sun.syndication.io.SyndFeedOutput;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.feed.synd.*;
import org.headsupdev.agile.api.*;
import org.headsupdev.agile.storage.StoredProject;
import org.headsupdev.agile.web.WebUtil;

/**
 * An abstract backing for Rome based RSS feeds in agile
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public abstract class AbstractFeed
   extends WebPage
{
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
        SyndFeedOutput output = new SyndFeedOutput();
        try
        {
            SyndFeed feed = new SyndFeedImpl();
            feed.setFeedType( "rss_2.0" );
            feed.setTitle( getTitle() );
            feed.setDescription( getDescription() );
            feed.setLink( getLink() );

            populateFeed( feed );
            output.output( feed, writer );
        }
        catch ( IOException e )
        {
            throw new RuntimeException( "Error streaming feed.", e );
        }
        catch ( FeedException e )
        {
            throw new RuntimeException( "Error streaming feed.", e );
        }
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

    public abstract String getTitle();

    public abstract String getDescription();

    public String getLink()
    {
        return Manager.getStorageInstance().getGlobalConfiguration().getFullUrl( getRequest().getURL() );
    }

    protected abstract void populateFeed( SyndFeed feed );
}