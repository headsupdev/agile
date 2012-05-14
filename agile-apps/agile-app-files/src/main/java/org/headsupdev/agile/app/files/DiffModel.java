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

package org.headsupdev.agile.app.files;

import org.headsupdev.agile.api.service.Change;
import org.headsupdev.agile.api.service.ChangeSet;
import org.apache.wicket.model.Model;
import org.headsupdev.agile.api.Manager;

import java.io.BufferedReader;
import java.io.StringReader;
import java.io.IOException;
import java.io.File;

/**
 * A model that renders the diffs for a given changeset
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class DiffModel extends Model<String>
{
    private ChangeSet changes;
    private String match;

    public DiffModel( ChangeSet changes )
    {
        this( changes, "" );
    }

    public DiffModel( ChangeSet changes, String match )
    {
        this.changes = changes;
        this.match = match;
    }

    public int getHiddenCount()
    {
        int others = 0;
        for ( Change change : changes.getChanges() )
        {
            String fileName = change.getName();
            if ( !fileName.startsWith( match ) )
            {
                others++;
            }
        }

        return others;
    }

    public String getObject()
    {
        StringBuffer diffOut = new StringBuffer();
        for ( Change change : changes.getChanges() )
        {
            String fileName = change.getName();
            if ( !fileName.startsWith( match ) )
            {
                continue;
            }

            String rawDiff = change.getDiff();
            if ( rawDiff == null || rawDiff.length() == 0 )
            {
                continue;
            }

            String path = fileName.substring( match.length() );
            diffOut.append( "<h3><a name=\"" );
            diffOut.append( path.replace( File.separatorChar, ':' ) );
            diffOut.append( "\"></a>" );
            diffOut.append( path );
            if ( change.getRevision() != null )
            {
                diffOut.append( "<span wicket:id=\"revision\">" );
                diffOut.append( change.getRevision() );
                diffOut.append( "</span>" );
            }
            diffOut.append( "</h3>");

            markupDiff( rawDiff, diffOut );
        }

        return diffOut.toString();
    }

    public void markupDiff( String in, StringBuffer out )
    {
        if ( in == null )
        {
            return;
        }
        out.append( "<table class=\"diff\">" );

        BufferedReader reader = new BufferedReader( new StringReader( in ) ) ;
        try
        {
            String line;
            int newLine = 0, oldLine = 0;
            boolean displayLines = false;
            boolean firstCommand = true;
            while ( ( line = reader.readLine() ) != null )
            {
                boolean displaySpacer = false;
                char first = '\0';
                if ( line.length() > 0)
                {
                    first = line.charAt( 0 );
                }

                String type;
                if ( first == '\\' )
                {
                    continue;
                }
                else if ( first == '@' )
                {
                    if ( firstCommand )
                    {
                        firstCommand = false;
                    }
                    else
                    {
                        displaySpacer = true;
                    }
                    try {
                        String numbers = line.replaceAll( "@", "" ).trim();
                        String oldRange = numbers.substring( 1, numbers.indexOf( " " ) );
                        String newRange = numbers.substring( numbers.indexOf( "+" ) + 1 );

                        if ( oldRange.indexOf( ',' ) != -1 )
                        {
                            oldLine = Integer.parseInt( oldRange.substring( 0, oldRange.indexOf( ',' ) ) );
                        }
                        else
                        {
                            oldLine = Integer.parseInt( oldRange );
                        }
                        if ( newRange.indexOf( ',' ) != -1 )
                        {
                            newLine = Integer.parseInt( newRange.substring( 0, newRange.indexOf( ',' ) ) );
                        }
                        else
                        {
                            newLine = Integer.parseInt( newRange );
                        }

                        if ( !displayLines )
                        {
                            displayLines = true;
                            continue;
                        }
                    }
                    catch ( NumberFormatException e )
                    {
                        Manager.getLogger( getClass().getName() ).error( "Failed to parse line numbers from \"" + line + "\"", e );
                    }
                }

                out.append( "<tr>" );
                if ( first == '+' )
                {
                    if ( displayLines )
                    {
                        out.append( "<td class=\"linenum-old\">&nbsp</td><td class=\"linenum-new\">" );
                        out.append( newLine++ );
                        out.append( "</td>" );
                    }

                    type = "diff-add";
                    out.append( "<td class=\"sign-add\">+</td>" );
                }
                else if ( first == '-' )
                {
                    if ( displayLines )
                    {
                        out.append( "<td class=\"linenum-old\">" );
                        out.append( oldLine++ );
                        out.append( "</td><td class=\"linenum-new\">&nbsp</td>" );
                    }

                    type = "diff-rem";
                    out.append( "<td class=\"sign-rem\">-</td>" );
                }
                else if ( displaySpacer )
                {
                    if ( displayLines )
                    {
                        out.append( "<td class=\"linenum-old\">&nbsp</td><td class=\"linenum-new\">&nbsp;</td>" );
                    }

                    type = "diff-skip";
                    line = "....";
                    out.append( "<td class=\"sign-none\">&nbsp;</td>" );

                }
                else
                {
                    if ( displayLines )
                    {
                        out.append( "<td class=\"linenum-old\">" );
                        out.append( oldLine++ );
                        out.append( "</td><td class=\"linenum-new\">" );
                        out.append( newLine++ );
                        out.append( "</td>" );
                    }

                    type = "diff-line";
                    out.append( "<td class=\"sign-none\">&nbsp;</td>" );
                }
                out.append( "<td class=\"" );
                out.append( type );
                out.append( "\">" );

                out.append( encode( line.substring( 1 ) ) );
                out.append( "</td></tr>" );
            }
        }
        catch ( IOException e )
        {
            /* really not gonna happen */
        }

        out.append( "</table>" );
    }

    public String encode( String in )
    {
        if ( in.length() == 0 )
        {
            return "&nbsp;";
        }

        StringBuilder ret = new StringBuilder( in.length() );
        for ( int i = 0; i < in.length(); i++ )
        {
            char chr = in.charAt( i );

            switch (chr)
            {
                case '>':
                    ret.append( "&gt;" );
                    break;
                case '<':
                    ret.append( "&lt;" );
                    break;
                case '\"':
                    ret.append( "&quot;" );
                    break;
                case ' ':
                    ret.append( "&nbsp;" );
                    break;
                case '&':
                    ret.append( "&amp;" );
                    break;
                case '\t':
                    ret.append( "&nbsp;&nbsp;&nbsp;&nbsp;" );
                    break;
                default:
                    ret.append( chr );
            }
        }
        return ret.toString();
    }
}
