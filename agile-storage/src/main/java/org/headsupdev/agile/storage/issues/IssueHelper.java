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

package org.headsupdev.agile.storage.issues;

import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.storage.HibernateStorage;
import org.headsupdev.agile.storage.resource.ResourceManagerImpl;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import java.util.ArrayList;
import java.util.List;

/**
* TODO Add documentation
* <p/>
* Created: 27/04/2012
*
* @author roberthewitt
* @since 2.0-alpha-2
*/
public class IssueHelper
{
    public static int getIssueCountForProject( Project project )
    {
        if ( project == null )
        {
            return 0;
        }
        Session session = ( (HibernateStorage) Manager.getStorageInstance() ).getHibernateSession();
        Criteria criteria = session.createCriteria( Issue.class );
        criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );
        criteria.add( Restrictions.eq( "id.project", project ) );
        criteria.setProjection( Projections.count( "id.project" ) );
        return ( (Number) criteria.uniqueResult() ).intValue();
    }

    public static int getIssueOpenCountForProject( Project project )
    {
        if ( project == null )
        {
            return 0;
        }
        Session session = ( (HibernateStorage) Manager.getStorageInstance() ).getHibernateSession();
        Criteria criteria = session.createCriteria( Issue.class );
        criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );
        criteria.add( Restrictions.lt( "status", Issue.STATUS_RESOLVED ) );
        criteria.add( Restrictions.eq( "id.project", project ) );
        criteria.setProjection( Projections.count( "id.project" ) );
        return ( (Number) criteria.uniqueResult() ).intValue();
    }

    public static int getIssueReOpenedCountForProject( Project project )
    {
        if ( project == null )
        {
            return 0;
        }
        Session session = ( (HibernateStorage) Manager.getStorageInstance() ).getHibernateSession();
        Criteria criteria = session.createCriteria( Issue.class );
        criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );
        criteria.add( Restrictions.ge( "reopened", 1 ) );
        criteria.add( Restrictions.eq( "id.project", project ) );
        criteria.setProjection( Projections.count( "id.project" ) );
        return ( (Number) criteria.uniqueResult() ).intValue();
    }

    public static List<Issue> getOverworkedIssuesForMilestone( Milestone milestone )
    {
        if ( milestone == null )
        {
            return new ArrayList<Issue>();
        }

        Session session = ( (HibernateStorage) Manager.getStorageInstance() ).getHibernateSession();
        Criteria criteria = session.createCriteria( Issue.class );
        criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );
        criteria.add( Restrictions.lt( "status", Issue.STATUS_RESOLVED ) );
        criteria.add( Restrictions.eq( "milestone", milestone ) );

        return findOverworkedIssues( criteria.list() );
    }


    private static List<Issue> findOverworkedIssues( List<Issue> issues )
    {
        ResourceManagerImpl resourceManager = ( (HibernateStorage) Manager.getStorageInstance() ).getResourceManager();

        List<Issue> overworkedIssues = new ArrayList<Issue>();
        for ( Issue issue : issues )
        {
            if ( resourceManager.isIssueMissingEstimate( issue ) )
            {
                overworkedIssues.add( issue );
            }
        }

        return overworkedIssues;
    }
}
