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

import org.headsupdev.agile.HeadsUpResourceMarker;
import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.api.SearchResult;
import org.headsupdev.agile.api.rest.Api;
import org.headsupdev.agile.api.rest.Publish;
import org.headsupdev.agile.storage.HibernateStorage;
import org.headsupdev.agile.storage.HibernateUtil;
import org.headsupdev.agile.storage.SessionProxy;
import org.headsupdev.agile.storage.StoredProject;
import org.headsupdev.support.java.StringUtil;

import org.hibernate.Session;
import org.hibernate.proxy.HibernateProxyHelper;
import org.hibernate.search.FullTextQuery;
import org.hibernate.search.FullTextSession;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.Query;
import org.apache.wicket.markup.html.PackageResource;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

/**
 * A class to handle the search logic for search page and API
 * <p/>
 * Created: 16/02/2013
 *
 * @author Andrew Williams
 * @since 2.0
 */
public class Searcher
    implements Serializable
{
    public static final int PAGE_SIZE = 25;

    private String query;
    private Map<String, Boolean> appFilter;

    private int from = 0;
    private int newFrom;

    private Project project;
    private ArrayList<Result> results;

    public Searcher( String query )
    {
        this.query = query;
    }

    public Searcher( String query, int from )
    {
        this( query );

        this.from = from;
    }

    public Project getProject()
    {
        return project;
    }

    public void setProject( Project project )
    {
        this.project = project;
    }

    public ArrayList<Result> getResults()
    {
        if ( results != null )
        {
            return results;
        }

        results = calculateResults();
        return results;
    }

    public ArrayList<Result> calculateResults()
    {
        results = new ArrayList<Result>();
        if ( StringUtil.isEmpty( query ) || ( appFilter != null && appFilter.size() == 0 ) )
        {
            return results;
        }

        Session session = ( (HibernateStorage) Manager.getStorageInstance() ).getHibernateSession();
        FullTextSession fullTextSession = org.hibernate.search.Search.createFullTextSession(
                ( (SessionProxy) session ).getRealSession() );

        MultiFieldQueryParser parser = new MultiFieldQueryParser(
                new ArrayList<String>( HibernateUtil.getSearchFields() ).toArray(
                        new String[ HibernateUtil.getSearchFields().size() ] ),
                new StandardAnalyzer());
        try
        {
            Query q = parser.parse( query );

            newFrom = from;
            boolean more = true;
            while ( more && results.size() < PAGE_SIZE )
            {
                more = addResults( q, fullTextSession, newFrom );
            }
        }
        catch ( Exception e )
        {
            Manager.getLogger( "Search" ).error( "Failed to run search", e );
        }

        return results;
    }

    private boolean addResults( Query q, FullTextSession fullTextSession, int from )
    {
        FullTextQuery textQuery = fullTextSession.createFullTextQuery( q );
        textQuery.setProjection( FullTextQuery.SCORE, FullTextQuery.THIS, FullTextQuery.EXPLANATION );
        textQuery.setMaxResults( PAGE_SIZE );
        textQuery.setFirstResult( from );

        Iterator i = textQuery.iterate();
        while ( i.hasNext() ) {
            try
            {
                Object[] result = (Object[]) i.next();
                if ( result[1] == null )
                {
                    continue;
                }

                if ( !resultMatchesAppFilter( result[1] ) )
                {
                    continue;
                }


                if ( getProject().equals( StoredProject.getDefault() ) || getProject().equals(
                        getProjectFromResult( result ) ) )
                {
                    results.add( new Result( result ) );
                }
            }
            catch ( Exception e )
            {
                e.printStackTrace();
                Manager.getLogger( "Search" ).error( "Error extracting search results", e );
            }
            newFrom++;
        }

        return textQuery.getResultSize() == PAGE_SIZE;
    }

    private boolean resultMatchesAppFilter( Object object )
    {
        if ( appFilter == null )
        {
            return true;
        }

        if ( object instanceof SearchResult )
        {
            String id = ( (SearchResult) object ).getAppId();
            if ( id .equals( "home" ) )
            {
                id = "system";
            }
            return appFilter.get( id );
        }
        else
        {
            return appFilter.get( "system" );
        }
    }

    private Project getProjectFromResult( Object[] result )
    {
        if ( result[1] instanceof Project)
        {
            return (Project) result[1];
        }
        else
        {
            try
            {
                Method getProject = result[1].getClass().getMethod( "getProject" );
                return (Project) getProject.invoke( result[1] );
            }
            catch ( Exception e )
            {
                return StoredProject.getDefault();
            }
        }
    }

    public static String getClassImageName( Object o )
    {
        if ( o instanceof SearchResult )
        {
            String path = ( (SearchResult) o ).getIconPath();
            if ( path != null )
            {
                return path;
            }
        }

        return getClassImageName( HibernateProxyHelper.getClassWithoutInitializingProxy( o ) );
    }


    public static String getClassImageName( Class type )
    {
        if ( type.equals( Object.class ) )
        {
            return "images/type/System.png";
        }

        String image = "images/type/" + type.getSimpleName() + ".png";
        if ( PackageResource.exists( HeadsUpResourceMarker.class, image, null, null ) )
        {
            return image;
        }

        return getClassImageName( type.getSuperclass() );
    }

    public void setAppFilter( Map<String, Boolean> appFilter )
    {
        this.appFilter = appFilter;
        results = null;
    }

    public class Result
            implements Serializable
    {
        public Object match;

        @Publish
        private String matchType;

        public Project project;
        @Publish
        private String projectId;

        @Publish
        public String title, link, icon;

        @Publish
        public float relevance;

        public Explanation explanation;

        public Result( Object[] object )
        {
            match = object[1];
            matchType = Api.getClassName( match );
            title = object[1].toString();

            project = getProjectFromResult( object );
            projectId = project.getId();

            relevance = (Float) object[0];
            // avoid over-ranking results (based on many field match?)
            if ( relevance > 1 )
            {
                relevance = 1;
            }
            explanation = (Explanation) object[2];

            if ( match instanceof SearchResult )
            {
                link = Api.getURLForPath( ( (SearchResult) match ).getLink() );

                String image = getClassImageName( match );
                icon = Api.getURLForPath( "/resources/" + HeadsUpResourceMarker.class.getCanonicalName() + "/" + image );
            }
        }
    }
}
