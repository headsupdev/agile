/*
 * HeadsUp Agile
 * Copyright 2012-2013 Heads Up Development Ltd.
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

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.Model;
import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.api.Permission;
import org.headsupdev.agile.api.logging.Logger;
import org.headsupdev.agile.app.docs.permission.DocViewPermission;
import org.headsupdev.agile.web.HeadsUpPage;
import org.headsupdev.agile.web.MountPoint;

import java.io.File;

/**
 * Renders results of an analyzer pass
 *
 * @author Adam Boardman
 * @since 2.0
 */
@MountPoint("analyze")
public class AnalyzeResults
        extends HeadsUpPage
{
    private static Logger log = Manager.getLogger("analyze");

    public Permission getRequiredPermission()
    {
        return new DocViewPermission();
    }

    public void layout()
    {
        super.layout();
        add( CSSPackageResource.getHeaderContribution( getClass(), "doc.css" ) );

        View.layoutMenuItems( this );
        String page = getPageParameters().getString( "page" );
        if ( page == null || page.length() == 0 )
        {
            page = "index.html";
        }

        boolean exists = false;
        File index = null;

        File dataDir = Manager.getStorageInstance().getDataDirectory();
        File analyzeDir = new File( new File( new File( new File( dataDir, "repository" ), "site" ), getProject().getId() ), "analyze" );
        log.debug("dataDir: " + dataDir.getPath() + ", analyzeDir:" + analyzeDir.getPath());

        String buildId = getPageParameters().getString( "id" );
        if ( buildId != null && buildId.length() > 0 )
        {
            analyzeDir = new File( analyzeDir, buildId );
            index = new File( analyzeDir, page );
            exists = index.exists();
            log.debug("build id: " + buildId + ", index:" + index + ", exists: " + exists);
        }
        else
        {
            File dirList[] = analyzeDir.listFiles();
            if ( dirList != null && dirList.length > 0 )
            {
                analyzeDir = dirList[dirList.length - 1];
                index = new File( analyzeDir, page );
                exists = index.exists();
            }
            log.debug("index:" + index + ", exists: " + exists);
        }

        WebMarkupContainer frame = new WebMarkupContainer( "frame" );
        if ( exists )
        {
            final String lastAnalysis = analyzeDir.getName();
            final String finalPage = page;
            frame.add( new AttributeModifier( "src", new Model<String>()
            {
                public String getObject()
                {
                    return "/repository/site/" + getProject().getId() + "/analyze/" + lastAnalysis + "/" + finalPage;
                }
            } ) );
        }
        add( frame.setVisible( exists ) );
        WebMarkupContainer nosite = new WebMarkupContainer( "nosite" );
        add( nosite.setVisible( !exists ) );
    }

    @Override
    public String getPageTitle()
    {
        return "Analysis Results" + PAGE_TITLE_SEPARATOR + super.getPageTitle();
    }
}