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

package org.headsupdev.agile.app.files;

import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.api.service.ChangeSet;
import org.headsupdev.agile.api.service.ScmService;
import org.headsupdev.agile.storage.HibernateStorage;
import org.headsupdev.agile.storage.ScmChangeSet;
import org.hibernate.Query;
import org.hibernate.Session;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * An SCM service that provides access to data about file changes
 * <p/>
 * Created: 27/01/2012
 *
 * @author Andrew Williams
 * @since 1.0
 */
public class BrowseScmService implements ScmService
{
    public ChangeSet getLastChange( Project project )
    {
        return getChangeSet( project, project.getRevision() );
    }

    public ChangeSet getChangeSet( Project project, String revision )
    {
        Session session = ( (HibernateStorage) Manager.getStorageInstance() ).getHibernateSession();

        Query q = session.createQuery( "from ScmChangeSet c where id.project = :project and id.name = :revision" );
        q.setEntity( "project", project );
        q.setString( "revision", revision );

        return (ScmChangeSet) q.uniqueResult();
    }

    public List<ChangeSet> getChangesSinceRevision( String fromRevision, Project project )
    {
        return getChangesBetweenRevisions( fromRevision, null, project );
    }

    public List<ChangeSet> getChangesBetweenRevisions( String fromRevision, String toRevision, Project project )
    {
        ChangeSet from = getChangeSet( project, fromRevision );
        ChangeSet to = null;
        if ( toRevision != null )
        {
            to = getChangeSet( project, toRevision );
        }

        List<ChangeSet> ret = new LinkedList<ChangeSet>();
        if ( from == null || from.equals( to ) )
        {
            return ret;
        }

        ChangeSet set = from.getNext();
        while ( set != null )
        {
            ret.add( set );

            if ( to != null && set.equals( to ) )
            {
                break;
            }

            set = set.getNext();
        }

        // TODO issue:36 - the above list may not be accurate if to cannot be traced to from simply

        Collections.reverse( ret );
        return ret;
    }

    public void updateProject(Project project)
    {
        BrowseApplication.getUpdater().updateProject( project );
    }
}
