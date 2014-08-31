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

package org.headsupdev.agile.app.search;

import org.headsupdev.support.java.CollectionUtil;

import org.apache.wicket.model.Model;
import org.apache.wicket.util.string.Strings;

import java.util.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * A renderer for a search result. Given an object and a list of fields matching terms we can render a google
 * style highlighted matches result.
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class SearchRenderModel
    extends Model<String>
{
    private static final int BUFFER_CHARS = 50;
    private Object object;
    private Map<String, List<String>> fields;
    private Map<String, Integer> colors;

    String output;

    public SearchRenderModel( Object object, Map<String, List<String>> fields, Map<String, Integer> colors )
    {
        this.object = object;
        this.fields = fields;
        this.colors = colors;
    }

    public String getObject()
    {
        if ( output == null )
        {
            renderOutput();
        }

        return output;
    }

    private void renderOutput()
    {
        StringBuffer summary = new StringBuffer();
        Iterator<String> fieldIter = fields.keySet().iterator();
        while ( fieldIter.hasNext() )
        {
            String field = fieldIter.next();

            summary.append( "<span class=\"search-field\">" );
            summary.append( field );
            summary.append( "</span> " );

            List<String> matches = CollectionUtil.uniqueList( fields.get( field ) );
            String content = getFieldContent( object, field, matches );
            if ( content == null )
            {
                summary.append( "&lt;content unavailable&gt;" );
            }
            else
            {
                renderField( field, content, matches, summary );
            }

            if ( fieldIter.hasNext() )
            {
                summary.append( "<br />" );
            }
        }
        output = summary.toString();
    }

    private String getFieldContent( Object o, String fieldName, List<String> matches )
    {
        int split = fieldName.indexOf( '.' );
        if ( split > 0 )
        {
            String pre = fieldName.substring( 0, split );
            String post = fieldName.substring( split + 1 );

            Object first = getField( o, pre );
            if ( first != null )
            {
                if ( Collection.class.isAssignableFrom( first.getClass() ) )
                {
                    for ( Object child : (Collection) first )
                    {
                        String got = getFieldContent( child, post, matches );

                        if ( got != null )
                        {
                            for ( String match : matches )
                            {
                                if ( got.toLowerCase().contains( match ) )
                                {
                                    return got;
                                }
                            }
                        }
                    }
                }
                return getFieldContent( first, post, matches );
            }
        }

        Object ret = getField( o, fieldName );
        return ( ret == null ) ? null : ret.toString();
    }

    private Object getField( Object o, String fieldName )
    {
        try
        {
            String methodName = "get" + fieldName.substring( 0, 1 ).toUpperCase() + fieldName.substring( 1 );
            Method method = o.getClass().getMethod( methodName );

            return method.invoke( o );
        }
        catch ( Exception e )
        {
            // TODO tidy this up, no debug just better coded :-p
            System.err.println("method fail = " + e.getMessage() );
            // ignore, just fall to next method

            try
            {
                Field field = o.getClass().getDeclaredField( fieldName );
                field.setAccessible( true );
                return field.get( o );
            }
            catch ( Exception e2 )
            {
                System.err.println("field fail = " + e2.getMessage() );
                // ignore, just fall to next method
            }
        }

        return null;
    }

    void renderField( String field, String content, List<String> matches, StringBuffer out )
    {
        List<RenderedMatch> renderedMatches = new LinkedList<RenderedMatch>();

        for ( String match : matches )
        {
            renderedMatches.addAll( renderMatch( field, content, match ) );
        }
        if ( renderedMatches.size() == 0 )
        {
            out.append( "..." );
            return;
        }

        renderedMatches = findNonSubstringMatches( renderedMatches );
        Collections.sort( renderedMatches );

        int renderStart = 0;
        int renderEnd = content.length();
        if ( renderedMatches.size() > 0 )
        {
            renderStart = renderedMatches.get( 0 ).getStart() - BUFFER_CHARS;
            if ( renderStart <= 0 )
            {
                renderStart = 0;
            }
            renderEnd = renderedMatches.get( renderedMatches.size() - 1 ).getEnd() + BUFFER_CHARS;
            if ( renderEnd >= content.length() )
            {
                renderEnd = content.length();
            }
        }

        if ( renderStart > 0 )
        {
            out.append( "..." );
        }

        int curr = renderStart;
        // TODO some clever appending of matches
        for ( RenderedMatch render : renderedMatches )
        {
            int myStart = render.getStart();
            if ( myStart - curr < ( BUFFER_CHARS * 2 ) )
            {
                out.append( escape( content.substring( curr, myStart ) ) );
            }
            else
            {
                out.append( escape( content.substring( curr, curr + BUFFER_CHARS ) ) );
                out.append( "... ..." );
                out.append( escape( content.substring( myStart - BUFFER_CHARS, myStart ) ) );
            }
            out.append( render.getRender() );

            curr = render.getEnd();
        }
        out.append( escape( content.substring( curr, renderEnd ) ) );

        if ( renderEnd < content.length() )
        {
            out.append( "..." );
        }
    }

    private List<RenderedMatch> findNonSubstringMatches( List<RenderedMatch> renderedMatches )
    {
        Collections.sort( renderedMatches, new Comparator<RenderedMatch>()
                {
                    @Override
                    public int compare( RenderedMatch renderedMatch, RenderedMatch renderedMatch2 )
                    {
                        int length1 = renderedMatch.getEnd() - renderedMatch.getStart();
                        int length2 = renderedMatch2.getEnd() - renderedMatch.getEnd();
                        return length2 - length1;
                    }
                } );
        ArrayList<RenderedMatch> out = new ArrayList<RenderedMatch>( renderedMatches.size() );

        for ( RenderedMatch match : renderedMatches )
        {
            boolean addMatch = true;

            Iterator<RenderedMatch> superIter = out.iterator();
            while ( superIter.hasNext() )
            {
                RenderedMatch supermatch = superIter.next();

                if ( match.getStart() >= supermatch.getStart() && match.getEnd() <= supermatch.getEnd() )
                {
                    addMatch = false;
                    break;
                }
                else if ( ( match.getStart() > supermatch.getStart() && match.getStart() < supermatch.getEnd() ) ||
                        ( match.getEnd() > supermatch.getStart() && match.getEnd() < supermatch.getEnd() ) )
                {
                    addMatch = false;
                    superIter.remove();
                    break;
                }
            }

            if ( addMatch )
            {
                out.add( match );
            }
        }

        return out;
    }

    private List<RenderedMatch> renderMatch( String field, String content, String match )
    {
        List<RenderedMatch> matches = new LinkedList<RenderedMatch>();
        String lowerContent = content.toLowerCase();

        int start = lowerContent.indexOf( match );
        while ( start > -1 )
        {
            int end = start + match.length();

            StringBuilder render = new StringBuilder();
            render.append( "<span class=\"search-match search-match-" );
            render.append( colors.get( match ) );
            render.append( "\">" );
            render.append( escape( content.substring( start, end ) ) );
            render.append( "</span>" );

            matches.add( new RenderedMatch( render.toString(), start, end ) );

            start = lowerContent.indexOf( match, start + 1 );
        }

        return matches;
    }

    private CharSequence escape( String in )
    {
        return Strings.escapeMarkup( in, false, true );
    }
}

