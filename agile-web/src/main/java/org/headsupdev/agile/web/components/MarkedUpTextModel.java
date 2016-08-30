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

package org.headsupdev.agile.web.components;

import org.headsupdev.agile.HeadsUpResourceMarker;
import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.api.LinkProvider;
import org.headsupdev.agile.api.Manager;
import org.apache.wicket.markup.html.PackageResource;
import org.apache.wicket.model.Model;

import java.util.Map;
import java.util.StringTokenizer;

/**
 * A text based model that marksup wiki style links in the text, such as doc:MyPage and change:25.
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class MarkedUpTextModel extends Model<String> {
    private static final String BROKEN_LINK_HINT = "Item could not be found";
    private String in, out = null;
    private Project project;

    public MarkedUpTextModel( String in, Project project )
    {
        this.in = in;
        this.project = project;
    }

    public String getObject()
    {
        if ( out == null )
        {
            out = markUp( in, project );
        }

        return out;
    }

    private static String getAppIdForPageName( String pageName )
    {
        if ( !pageName.contains( "/" ) )
            return pageName;

        return pageName.substring( 0, pageName.indexOf( "/" ) );
    }

    public static String markUp( String in, Project project )
    {
        if ( in == null )
        {
            return null;
        }

        StringBuilder ret = new StringBuilder( in.length() );
        StringTokenizer tokenizer = new StringTokenizer( in, " \t\n\r\f<>(){}&,!?;'\"", true );

        Map<String, LinkProvider> providers = Manager.getInstance().getLinkProviders();
        while ( tokenizer.hasMoreTokens() )
        {
            String next = tokenizer.nextToken();

            if ( isValidLink( next ) )
            {
                // compensate from not splitting on "." we still want to split ". "
                if ( next.endsWith( "." ) )
                {
                    next = next.substring( 0, next.length() - 1 );
                }

                String link = getLink( next, project, providers );
                boolean broken = isLinkBroken( next, project, providers );
                if ( link == null )
                {
                    ret.append( encode( next ) );
                }
                else
                {
                    ret.append( "<a href=\"" );
                    ret.append( link );
                    ret.append( "\" class=\"" );
                    if ( broken )
                    {
                        ret.append( "brokenlink\" title=\"" );
                        ret.append( BROKEN_LINK_HINT );
                    }
                    else if ( getProviderForLink( next, providers ) != null )
                    {
                        ret.append( "applink" );

                        String pageName = getProviderForLink( next, providers ).getPageName();
                        String appId = getAppIdForPageName( pageName );
                        String icon = "images/app/" + appId + "_icon.png";
                        if ( PackageResource.exists( HeadsUpResourceMarker.class, icon, null, null ) )
                        {
                            ret.append( "\"><img src=\"/resources/org.headsupdev.agile.HeadsUpResourceMarker/" + icon );
                        }
                    }
                    ret.append( "\">" );
                    ret.append( encode( next ) );
                    ret.append( "</a>" );
                }
                if ( broken )
                {
                    ret.append( "<span class=\"brokenlinkhint\" title=\"" );
                    ret.append( BROKEN_LINK_HINT );
                    ret.append( "\">[?]</span>" );
                }
            }
            else
            {
                ret.append( encode( next ) );
            }
        }

        return ret.toString();
    }

    public static boolean isValidLink( String text )
    {
        if ( text == null || text.indexOf( ':' ) == -1 )
        {
            return false;
        }


        if ( text.charAt( 0 ) == ':' || text.charAt( text.length() - 1 ) == ':' )
        {
            return false;
        }

        if ( text.charAt( text.indexOf( ':' ) + 1 ) == ':' )
        {
            return false;
        }

        return true;
    }

    public static LinkProvider getProviderForLink( String text, Map<String, LinkProvider> providers )
    {
        int pos = text.indexOf( ':' );
        if ( pos == -1 )
        {
            return null;
        }

        String module = text.substring( 0, pos ).toLowerCase();
        if ( module.equals( "wiki" ) )
        {
            module = "doc";
        }

        if ( providers.containsKey( module ) )
            return providers.get( module );
        return null;
    }

    public static String getLink( String text, Project fallback, Map<String, LinkProvider> providers )
    {
        if ( text == null )
        {
            return null;
        }

        int pos = text.indexOf( ':' );
        if ( pos == -1 )
        {
            return null;
        }

        String name = text.substring( pos + 1 );
        // simple URLs should be marked up too!
        if ( name.startsWith( "//" ) ) {
            return text;
        }

        LinkProvider provider = getProviderForLink( text, providers );
        if ( provider == null )
        {
            return null;
        }

        pos = name.indexOf( ":" );
        if ( pos != -1 )
        {
            String projectId = name.substring( 0, pos );
            Project project = Manager.getStorageInstance().getProject( projectId );
            if ( project != null )
            {
                fallback = project;
            }

            name = name.substring( pos + 1 );
        }
        return provider.getLink( name, fallback );
    }

    public static boolean isLinkBroken( String text, Project fallback, Map<String, LinkProvider> providers )
    {
        if ( text == null )
        {
            return false;
        }

        int pos = text.indexOf( ':' );
        if ( pos == -1 )
        {
            return false;
        }

        String name = text.substring( pos + 1 );
        // simple URLs should never be broken
        if ( name.startsWith( "//" ) ) {
            return false;
        }


        LinkProvider provider = getProviderForLink( text, providers );
        if ( provider == null )
        {
            return false;
        }

        pos = name.indexOf( ":" );
        if ( pos != -1 )
        {
            String projectId = name.substring( 0, pos );
            Project project = Manager.getStorageInstance().getProject( projectId );
            if ( project != null )
            {
                fallback = project;
            }

            name = name.substring( pos + 1 );
        }
        return provider.isLinkBroken( name, fallback );
    }

    // TODO find the wicket encoding and use that instead - else centralise this...
    private static String encode( String in )
    {
        String out = in.replace( "&",  "&amp;" ).replace( "<", "&lt;" ).replace( ">", "&gt;" );
        return out.replace( "\"", "&quot;" ).replace( "\n", "<br />" );
    }
}
