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

package org.headsupdev.agile.app.search.feed;

import org.headsupdev.agile.web.WebUtil;
import org.headsupdev.agile.web.MountPoint;
import org.headsupdev.agile.app.search.Search;
import org.headsupdev.agile.security.permission.ProjectListPermission;
import org.headsupdev.agile.api.Storage;
import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.api.Permission;
import org.headsupdev.agile.api.SearchResult;
import org.headsupdev.agile.storage.HibernateUtil;
import org.headsupdev.agile.storage.SessionProxy;
import org.headsupdev.agile.storage.HibernateStorage;
import org.hibernate.Session;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.FullTextQuery;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.search.Query;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.PageParameters;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;

import java.util.*;
import java.io.PrintWriter;
import java.io.IOException;

/**
 * A simple xml page that reports search results in xml form
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@MountPoint( "search.xml" )
public class SearchFeed
   extends WebPage
{
    private PageParameters parameters;
    private Storage storage = Manager.getStorageInstance();

    public Permission getRequiredPermission() {
        return new ProjectListPermission();
    }

    public SearchFeed( PageParameters params )
    {
        this.parameters = params;
        WebUtil.authenticate( (WebRequest) getRequest(), (WebResponse) getResponse(), getRequiredPermission(), null );
    }

    @Override
    public String getMarkupType()
    {
        return "xml";
    }

    @Override
    protected final void onRender( MarkupStream markupStream )
    {
        PrintWriter writer = new PrintWriter(getResponse().getOutputStream());
        try
        {
            Document doc = new Document();
            Element root = new Element( "searchResults" );
            doc.setRootElement( root );

            populateFeed( root );
            XMLOutputter out = new XMLOutputter();
            out.output( doc, writer );
        }
        catch ( IOException e )
        {
            throw new RuntimeException( "Error streaming feed.", e );
        }
    }

    protected void populateFeed( Element root )
    {
        String query = parameters.getString( "query" );
        List<Object[]> results = new LinkedList<Object[]>();

        Session session = ( (HibernateStorage) Manager.getStorageInstance() ).getHibernateSession();
        FullTextSession fullTextSession = org.hibernate.search.Search.createFullTextSession(
            ( (SessionProxy) session ).getRealSession() );

        MultiFieldQueryParser parser = new MultiFieldQueryParser(
            new ArrayList<String>( HibernateUtil.getSearchFields() ).toArray( new String[ 0 ] ),
            new StandardAnalyzer());
        try
        {
            // TODO can we limit by project somehow? I think not :(
            Query q = parser.parse( query );

            FullTextQuery textQuery = fullTextSession.createFullTextQuery( q );
            textQuery.setProjection( FullTextQuery.SCORE, FullTextQuery.THIS );//, FullTextQuery.EXPLANATION );
            textQuery.setMaxResults( 25 );
            results = textQuery.list();
        }
        catch ( Exception e )
        {
            Manager.getLogger( getClass().getName() ).error( "Failed to run search", e );
        }

        for ( Object[] o : results )
        {
            Element node = new Element( "result" );
            int relevance = (int) ( ( (Float) o[0] ) * 100 );
            String title = o[1].toString();

            String link = "";
            if ( o[1] instanceof SearchResult)
            {
                link = ( (SearchResult) o[1] ).getLink();
            }

            node.addContent( new Element( "title" ).addContent( title ) );
            node.addContent( new Element( "relevance" ).addContent( relevance + "%" ) );
            node.addContent( new Element( "link" ).addContent( storage.getGlobalConfiguration().getFullUrl( link ) ) );

            String image = Search.getClassImageName( o[1] );
            node.addContent( new Element( "icon" ).addContent( storage.getGlobalConfiguration().getFullUrl(
                "resources/org.headsupdev.agile.HeadsUpResourceMarker/" + image ) ) );

            root.addContent( node );
        }
    }
}
