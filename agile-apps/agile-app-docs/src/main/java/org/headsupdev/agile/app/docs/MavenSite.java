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

package org.headsupdev.agile.app.docs;

import org.headsupdev.agile.web.HeadsUpPage;
import org.headsupdev.agile.web.MountPoint;
import org.headsupdev.agile.api.Permission;
import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.app.docs.permission.DocViewPermission;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.model.Model;

import java.io.File;

/**
 * Documents page which renders the maven generated content
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@MountPoint( "maven-site" )
public class MavenSite
    extends HeadsUpPage
{
    public Permission getRequiredPermission() {
        return new DocViewPermission();
    }

    public void layout() {
        super.layout();
        add( CSSPackageResource.getHeaderContribution( getClass(), "doc.css" ));

        View.layoutMenuItems( this );
        String page = getPageParameters().getString( "page" );
        if ( page == null || page.length() == 0 )
        {
            page = "index.html";
        }

        File siteRepo = new File( new File( Manager.getStorageInstance().getDataDirectory(), "repository" ), "site" );
        String sitePath = getSitePath( getProject() );
        File siteFile = new File( new File( siteRepo, sitePath.replace( "/", File.separator ) ), page );
        boolean exists = siteFile.exists();

        add( new WebMarkupContainer( "nosite" ).setVisible( !exists ) );
        final String finalPage = sitePath + "/" + page;
        WebMarkupContainer frame = new WebMarkupContainer( "frame" );
        frame.add( new AttributeModifier( "src", new Model<String>()
        {
            public String getObject()
            {
                return "/repository/site/" + finalPage;
            }
        } ) );
        add( frame.setVisible( exists ) );
    }

    @Override
    public String getPageTitle()
    {
        return "Maven Site" + PAGE_TITLE_SEPARATOR + super.getPageTitle();
    }

    protected String getSitePath( Project project )
    {
        String sitePath = project.getId();
        Project parent = project;
        while ( parent.getParent() != null )
        {
            parent = parent.getParent();

            // these separators are for a URL but we convert it where needed for a file
            sitePath = parent.getId() + "/" + sitePath;
        }

        return sitePath;
    }
}
