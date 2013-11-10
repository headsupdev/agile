/*
 * HeadsUp Agile
 * Copyright 2009-2013 Heads Up Development Ltd.
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

package org.headsupdev.agile.app.ci;

import org.apache.wicket.markup.html.CSSPackageResource;
import org.headsupdev.support.java.IOUtil;
import org.headsupdev.agile.web.HeadsUpPage;
import org.headsupdev.agile.web.BookmarkableMenuLink;
import org.headsupdev.agile.web.MountPoint;
import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.api.Permission;
import org.headsupdev.agile.app.ci.permission.BuildViewPermission;
import org.headsupdev.agile.storage.ci.Build;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Page that when loaded suggests to the CI Builder that it should run again.
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@MountPoint( "view" )
public class View
    extends HeadsUpPage
{
    private long buildId;

    public Permission getRequiredPermission()
    {
        return new BuildViewPermission();
    }

    public void layout()
    {
        super.layout();
        add( CSSPackageResource.getHeaderContribution( getClass(), "ci.css" ) );

        Project project = getProject();
        long id = getPageParameters().getLong("id");
        if ( project == null || id < 0 )
        {
            notFoundError();
            return;
        }

        Build build = CIApplication.getBuild( id, getProject() );
        buildId = build.getId();
        addLink( new BookmarkableMenuLink( getPageClass( "builds/" ), getProjectPageParameters(), "history" ) );
        if ( build.getTestResults().size() > 0 )
        {
            addLink( new BookmarkableMenuLink( getPageClass( "builds/tests" ), getPageParameters(), "tests" ) );
        }
        add( new BuildPanel( "details", build ) );

        add( new BookmarkablePageLink( "test-link", getPageClass( "builds/tests" ), getPageParameters() )
            .setVisible( build.getTestResults().size() > 0 ) );

        File outputFile = new File( CIApplication.getProjectDir( project ), id + ".txt" );

        try
        {
            add( new Label( "result", IOUtil.toString( new FileInputStream( outputFile ) ) ) );
        }
        catch ( IOException e )
        {
            add( new Label( "result", "Unable to load results file - reason: " + e.getMessage() ) );
        }
    }

    @Override
    public String getPageTitle()
    {
        return "Build:" + buildId + PAGE_TITLE_SEPARATOR + super.getPageTitle();
    }
}