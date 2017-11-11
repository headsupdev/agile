/*
 * HeadsUp Agile
 * Copyright 2009-2012 Heads Up Development.
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

import org.headsupdev.agile.api.LinkProvider;
import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.api.service.ChangeSet;
import org.headsupdev.agile.storage.ScmChangeSet;
import org.headsupdev.agile.storage.HibernateStorage;
import org.headsupdev.agile.storage.issues.Issue;
import org.hibernate.*;

import java.util.Map;
import java.util.StringTokenizer;

/**
 * Parser for scm comments that set up relationships and actions based on commit comments
 * <p/>
 * Created: 10/08/2011
 *
 * @author Andrew Williams
 * @since 1.0
 */
public class ScmCommentParser
{
    // TODO share a lot of this logic with the MarkedUpTextModel
    public static void parseComment( String comment, ChangeSet set )
    {
        if ( comment == null )
        {
            return;
        }

        StringTokenizer tokenizer = new StringTokenizer( comment, " \t\n\r\f<>(){}&.,!?;", true );

        Map<String, LinkProvider> providers = Manager.getInstance().getLinkProviders();
        String previous = "";
        while ( tokenizer.hasMoreTokens() )
        {
            String next = tokenizer.nextToken();

            if ( next.indexOf( ':' ) != -1 )
            {
                parseLink( next, previous, set, providers );
            }

            previous = next;
        }

    }

    public static String parseLink( String text, String prepend, ChangeSet set, Map<String, LinkProvider> providers )
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

        text = text.toLowerCase();
        String module = text.substring( 0, pos );
        String name = text.substring( pos + 1 );

        if ( module.equals( "wiki" ) )
        {
            module = "doc";
        }

        Project fallback = set.getProject();
//        if ( providers.containsKey( module ) )
//        {
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

            handleLink( module, name, prepend, set, fallback );
//        }

        return null;
    }

    public static void handleLink( String type, String parameter, String prepend, ChangeSet set, Project project )
    {
        if ( type.equals( "issue" ) && set instanceof ScmChangeSet )
        {
            Issue issue = getIssue( parameter, project );

            if ( issue != null )
            {
                ( (ScmChangeSet) set ).getIssues().add( issue );
                issue.getChangeSets().add( set );
            }

            // TODO check what prepend was - i.e. "fixes", "resolves", "closes"
        }

        // TODO handle other things like "milestone" and "doc"
    }

    public static Issue getIssue( String idStr, Project project )
    {
        long id;
        try
        {
            id = Long.parseLong( idStr );
        }
        catch ( NumberFormatException e )
        {
            return null;
        }

        Session session = ( (HibernateStorage) Manager.getStorageInstance() ).getHibernateSession();

// already in a transaction here, perhaps we should pass that tx through? or just the session?
//        Transaction tx = session.beginTransaction();
        Query q = session.createQuery( "from Issue i where id.id = :id and id.project.id = :pid" );
        q.setLong( "id", id );
        q.setString( "pid", project.getId() );
        Issue ret = (Issue) q.uniqueResult();
//        tx.commit();

        return ret;
    }
}
