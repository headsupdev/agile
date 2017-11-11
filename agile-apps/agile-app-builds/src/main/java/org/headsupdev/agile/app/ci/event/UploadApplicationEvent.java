/*
 * HeadsUp Agile
 * Copyright 2009-2013 Heads Up Development.
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

import org.headsupdev.agile.storage.ci.Build;
import org.headsupdev.agile.web.AbstractEvent;
import org.headsupdev.support.java.StringUtil;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.Date;

/**
 * An event fired when an application is deployed to the repository
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@Entity
@DiscriminatorValue( "uploadapplication" )
public class UploadApplicationEvent
    extends AbstractEvent
{
    public UploadApplicationEvent()
    {
    }

    public UploadApplicationEvent( Build build, String version, String type, String repoName, String path )
    {
        this( build.getProject().getAlias(), version, repoName, path, build, type + " Application" );
    }

    protected UploadApplicationEvent( String name, String version, String repoName, String path, Build build,
                                   String type )
    {
        super( getTitleForBuildEvent( name, version, build, type ),
            getVersionTitle( version ) + type + " " + name + " was deployed to the " + repoName +
                    " repository<br /><a href=\"" + path + "\">Install</a>" +
                    "<img src=\"/qrcode.png?path=" + path + "\" style=\"margin-top: -108px;height: 150px;float:right;\">", new Date() );

        setApplicationId( "artifacts" );
        setProject( build.getProject() );
        setObjectId( repoName + ',' + path );
    }

    protected static String getTitleForBuildEvent( String name, String version, Build build, String type )
    {

        String title = type + " " + name + getVersionString( version ) + " (build " + build.getId();
        if ( !StringUtil.isEmpty( build.getConfigName() ) )
        {
            title += " \"" + build.getConfigName() + "\"";
        }

        return title + ") deployed";
    }

    public String getLink()
    {
        int start = getBody().indexOf( "href=\"" ) + 6;
        int end = getBody().indexOf( "\"", start );

        if ( start == -1 || end < start )
        {
            return null;
        }

        return getBody().substring( start, end );
    }

    protected String getBuildTitle()
    {
        int start = getTitle().indexOf( "(build" ) + 6;
        int end = getTitle().indexOf( ")", start );
        return getTitle().substring( start, end ).trim();
    }

    public long getBuildNumber()
    {
        String buildString = getBuildTitle();
        if ( buildString.contains( "\"" ) )
        {
            int pos = buildString.indexOf( "\"" );
            return Long.parseLong( buildString.substring( 0, pos ).trim() );
        }

        return Long.parseLong( buildString );
    }

    public String getBuildConfigName()
    {
        String buildString = getBuildTitle();
        if ( !buildString.contains( "\"" ) )
        {
            return null;
        }

        int start = buildString.indexOf( "\"" ) + 1;
        int end = buildString.lastIndexOf( "\"" );
        if ( end == -1 || end <= start )
        {
            return null;
        }

        return buildString.substring( start, end ).trim();
    }

    public String getVersion()
    {
        if ( !getTitle().contains( ":" ) )
        {
            return null;
        }

        int start = getTitle().indexOf( ":" ) + 1;
        int end = getTitle().indexOf( " ", start );

        return getTitle().substring(start, end).trim();
    }

    private static String getVersionString( String version )
    {
        if ( version == null )
        {
            return "";
        }

        return ":" + version;
    }

    private static String getVersionTitle( String version )
    {
        if ( version == null )
        {
            return "The ";
        }

        return "Version " + version + " of the ";
    }

    @Override
    public boolean isSummaryHTML()
    {
        return true;
    }

    public String getBody()
    {
        // todo add some info, as can be seen on "/artifacts/<oid[..,]>path/<oid[,...]>"
        return super.getBody();
    }
}
