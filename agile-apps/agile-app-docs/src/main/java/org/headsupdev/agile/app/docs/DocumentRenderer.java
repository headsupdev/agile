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

package org.headsupdev.agile.app.docs;

import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.api.LinkProvider;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;
import java.io.*;
import java.util.Enumeration;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * This is a helper for rendering document objects.
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class DocumentRenderer
{
    public static String markupLinks( String in, final Project project,
                                      final Map<String,LinkProvider> linkProviders )
    {
        try
        {
            final StringWriter text = new StringWriter();

            HTMLEditorKit.ParserCallback callback = new HTMLEditorKit.ParserCallback ()
            {
                boolean inlink = false;

                @Override
                public void handleStartTag( HTML.Tag t, MutableAttributeSet a, int pos )
                {
                    if ( t.equals( HTML.Tag.A ) )
                    {
                        inlink = true;
                    }

                    text.append( "<" );
                    text.append( t.toString() );

                    Enumeration attEnum = a.getAttributeNames();
                    while ( attEnum.hasMoreElements() )
                    {
                        Object att = attEnum.nextElement();
                        if ( !( att instanceof HTML.Attribute ) )
                        {
                            continue;
                        }

                        text.append( ' ' );
                        text.append( att.toString() );
                        text.append( "=\"" );
                        if ( a.getAttribute( att ) != null )
                        {
                            text.append( a.getAttribute( att ).toString() );
                        }
                        text.append( "\"" );
                    }

                    text.append( ">" );
                }

                @Override
                public void handleEndTag( HTML.Tag t, int pos )
                {
                    if ( t.equals( HTML.Tag.A ) )
                    {
                        inlink = false;
                    }

                    text.append( "</" );
                    text.append( t.toString() );
                    text.append( ">" );
                }

                @Override
                public void handleSimpleTag( HTML.Tag t, MutableAttributeSet a, int pos )
                {
                    text.append( "<" );
                    text.append( t.toString() );

                    Enumeration attEnum = a.getAttributeNames();
                    while ( attEnum.hasMoreElements() )
                    {
                        Object att = attEnum.nextElement();
                        if ( !( att instanceof HTML.Attribute ) )
                        {
                            continue;
                        }

                        text.append( ' ' );
                        text.append( att.toString() );
                        text.append( "=\"" );
                        if ( a.getAttribute( att ) != null )
                        {
                            text.append( a.getAttribute( att ).toString() );
                        }
                        text.append( "\"" );
                    }

                    text.append( " />" );
                }

                @Override
                public void handleText( char[] data, int pos )
                {
                    StringTokenizer tokens = new StringTokenizer( new String( data ), " ", true );
                    while ( tokens.hasMoreElements() )
                    {

                        String token = (String) tokens.nextElement();
                        /* weird that the parser should return these brackets... */
                        if ( token.startsWith( ">" ) )
                        {
                            if ( token.length() == 1 )
                            {
                                continue;
                            }
                            else
                            {
                                token = token.substring( 1 );
                            }
                        }

                        if ( token.indexOf( ':' ) > -1 )
                        {
                            if ( !inlink )
                            {
                                text.write( markupLink( token, project, linkProviders ) );
                            }
                            else
                            {
                                text.write( escapeString( token ) );
                            }
                        }
                        else
                        {
                            text.write( escapeString( token ) );
                        }
                    }
                }

                @Override
                public void handleEndOfLineString( String eol )
                {
                    text.append( eol );
                }
            };

            new ParserDelegator().parse( new StringReader( in ), callback, false );

            return text.toString();
        }
        catch ( IOException e )
        {
            return "(unable to parse document)";
        }
    }

    protected static String markupLink( String text, Project project, Map<String, LinkProvider> linkProviders )
    {
        String name = text;

        String module = "doc";
        int pos = name.indexOf( ':' );
        if ( pos != -1 )
        {
            module = name.substring( 0, pos ).toLowerCase();
            name = name.substring( pos + 1 );

            if ( module.equals( "wiki" ) )
            {
                module = "doc";
            }
        }

        if ( linkProviders.containsKey( module ) )
        {
            Project linkProject = project;
            pos = name.indexOf( ":" );
            if ( pos != -1 )
            {
                String projectId = name.substring( 0, pos );
                Project p2 = Manager.getStorageInstance().getProject( projectId );
                if ( p2 != null )
                {
                    linkProject = p2;
                }
                name = name.substring( pos + 1 );
            }

            String link = linkProviders.get( module ).getLink( name, linkProject );

            return "<a href=\"" + link + "\">" + escapeString( text ) + "</a>";
        }
        else
        {
            return escapeString( text );
        }
    }

    protected static String escapeString( String in )
    {
        String out = in.replaceAll( "&",  "&amp;" ).replaceAll( "<", "&lt;" ).replaceAll( ">", "&gt;" );
        return out.replaceAll( "\"", "&quot;" );
    }
}
