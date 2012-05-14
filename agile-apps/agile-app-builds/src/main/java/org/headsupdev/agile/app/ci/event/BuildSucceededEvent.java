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

package org.headsupdev.agile.app.ci.event;

import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.api.User;
import org.headsupdev.support.java.IOUtil;
import org.headsupdev.agile.web.AbstractEvent;
import org.headsupdev.agile.storage.ci.Build;
import org.headsupdev.agile.web.components.FormattedDurationModel;
import org.headsupdev.agile.web.BookmarkableMenuLink;
import org.headsupdev.agile.web.RenderUtil;
import org.headsupdev.agile.app.ci.*;

import javax.persistence.Entity;
import javax.persistence.DiscriminatorValue;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.panel.Panel;

/**
 * Event added when a CI build is successful
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@Entity
@DiscriminatorValue( "buildsucceeded" )
public class BuildSucceededEvent
    extends AbstractEvent
{
    BuildSucceededEvent()
    {
    }

    public BuildSucceededEvent( Build build )
    {
        this( build, "succeeded" );
    }

    protected BuildSucceededEvent( Build build, String result )
    {
        super( "Build " + build.getId() + " " + result, "Completed in " +
                FormattedDurationModel.parseDuration( build.getStartTime(), build.getEndTime() ), build.getEndTime() );

        setApplicationId( CIApplication.ID );
        setProject( build.getProject() );
        setObjectId( String.valueOf( build.getId() ) );
    }

    public String getBody()
    {
        int id;
        try
        {
            id = Integer.parseInt( getObjectId() );
        }
        catch ( NumberFormatException e )
        {
            return "<p>Invalid build ID " + getObjectId() + "</p>";
        }

        final Build build = CIApplication.getBuild( id, getProject() );

        PageParameters params = new PageParameters();
        params.add( "project", getProject().getId() );
        addLink( new BookmarkableMenuLink( CI.class, params, "history" ) );
        if ( build.getTestResults().size() > 0 )
        {
            params.add( "id", String.valueOf( id ) );
            addLink( new BookmarkableMenuLink( Tests.class, params, "tests" ) );
        }

        String ret = new RenderUtil()
        {
            public Panel getPanel()
            {
                return  new BuildPanel( RenderUtil.PANEL_ID, build );
            }
        }.getRenderedContent();

        File outputFile = new File( CIApplication.getProjectDir( getProject() ), getObjectId() + ".txt" );
        try
        {
            String content = IOUtil.toString( new FileInputStream( outputFile ) );

            ret += "<div class=\"build\"><pre class=\"content\">" + content + "</pre></div>";
        }
        catch ( IOException e )
        {
            ret += "<p>Unable to load results file - reason: " + e.getMessage() + "</p>";
        }

        return ret;
    }

    public List<AbstractEvent.CssReference> getBodyCssReferences()
    {
        List<CssReference> ret = new LinkedList<CssReference>();
        ret.add( referenceForCss( CIApplication.class, "ci.css" ) );

        return ret;
    }

    @Override
    public boolean shouldNotify( User user )
    {
        if ( !user.isSubscribedTo( getProject() ) )
        {
            return false;
        }

        Build build = CIApplication.getBuild( Long.parseLong( getObjectId() ), getProject() );
        Build previousBuild = null;
        if ( build.getId() > 0 )
        {
            previousBuild = CIApplication.getBuild( build.getId() - 1, getProject() );
        }

        boolean repeatNotify = (Boolean) CIApplication.CONFIGURATION_NOTIFY_REPEAT_PASS.getDefault();
        String repeatNotifyStr = Manager.getStorageInstance().getGlobalConfiguration().getApplicationConfiguration( CIApplication.ID )
                .getProperty( CIApplication.CONFIGURATION_NOTIFY_REPEAT_PASS.getKey() );
        if ( repeatNotifyStr != null )
        {
            repeatNotify = Boolean.parseBoolean( repeatNotifyStr );
        }
        return previousBuild == null || build.getStatus() != previousBuild.getStatus() ||
                build.getStatus() != Build.BUILD_SUCCEEDED || repeatNotify;
    }
}