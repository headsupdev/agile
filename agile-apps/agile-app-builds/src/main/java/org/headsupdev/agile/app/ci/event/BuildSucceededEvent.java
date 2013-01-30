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
import java.io.InputStream;
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
    private static final long BUILD_NOTIFICATION_LIMIT = 100 * 1024;

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

        StringBuilder ret = new StringBuilder( new RenderUtil()
        {
            public Panel getPanel()
            {
                return  new BuildPanel( RenderUtil.PANEL_ID, build );
            }
        }.getRenderedContent() );

        File outputFile = new File( CIApplication.getProjectDir( getProject() ), getObjectId() + ".txt" );
        getBuildContent(outputFile, ret);

        return ret.toString();
    }

    private void getBuildContent( File buildFile, StringBuilder out )
    {
        try
        {
            String content;

            if ( buildFile.length() > BUILD_NOTIFICATION_LIMIT )
            {
                content = getTruncatedBuildContent( buildFile );
            }
            else
            {
                content = IOUtil.toString( new FileInputStream( buildFile ), BUILD_NOTIFICATION_LIMIT );
            }

            out.append( "<div class=\"build\"><pre class=\"content\">" );
            out.append( content );
            out.append( "</pre></div>" );
        }
        catch ( IOException e )
        {
            out.append( "<p>Unable to load results file - reason: " );
            out.append( e.getMessage() );
            out.append( "</p>" );
        }
    }

    private String getTruncatedBuildContent( File buildFile )
        throws IOException
    {
        StringBuilder ret = new StringBuilder();
        InputStream input = new FileInputStream( buildFile );

        ret.append( IOUtil.toString( input, BUILD_NOTIFICATION_LIMIT / 2 ) );
        ret.append( "\n...\n\n" );

        input = new FileInputStream( buildFile );
        if ( input.skip( buildFile.length() - BUILD_NOTIFICATION_LIMIT / 2 ) > 0 )
        {
            seekNextLine( input );
            ret.append( IOUtil.toString( input ) );
            return ret.toString();
        }
        else
        {
            return IOUtil.toString( input, BUILD_NOTIFICATION_LIMIT );
        }
    }

    private void seekNextLine( InputStream input )
        throws IOException
    {
        int in = input.read();
        while ( in != '\n' && in != -1 )
        {
            in = input.read();
        }
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