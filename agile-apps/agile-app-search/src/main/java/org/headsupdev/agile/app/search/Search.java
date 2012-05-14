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
import org.headsupdev.agile.app.search.permission.SearchPermission;
import org.headsupdev.agile.storage.StoredProject;
import org.headsupdev.agile.web.HeadsUpPage;
import org.headsupdev.agile.web.components.HeadsUpResourceReference;
import org.headsupdev.agile.api.*;
import org.headsupdev.agile.storage.HibernateStorage;
import org.headsupdev.agile.storage.SessionProxy;
import org.headsupdev.agile.storage.HibernateUtil;

import java.util.*;
import java.lang.reflect.Method;

import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.PackageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ResourceReference;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.hibernate.Session;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.FullTextQuery;

/**
 * Search home page
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class Search
    extends HeadsUpPage
{
    private static final int PAGE_SIZE = 25;
    private String query = null;
    private int from = 0;
    private WebMarkupContainer noresults;
    private BookmarkablePageLink moreresultsLink, notallprojectsLink;

    public Permission getRequiredPermission() {
        return new SearchPermission();
    }

    public void layout()
    {
        super.layout();
        add( CSSPackageResource.getHeaderContribution( getClass(), "search.css" ) );
        query = getPageParameters().getString( "query" );
        try
        {
            from = getPageParameters().getInt( "from" );
        }
        catch ( Exception e ) // NumberFormatException or a wicket wrapped NumberFormatException
        {
            from = 0;
        }
        noresults = new WebMarkupContainer( "noresults" );
        noresults.setOutputMarkupPlaceholderTag( true );
        add( noresults.setVisible( false ) );

        PageParameters params = getPageParameters();
        params.remove( "project" );
        params.add( "project", StoredProject.ALL_PROJECT_ID );
        notallprojectsLink = new BookmarkablePageLink( "notallprojectsLink", Search.class, params );
        notallprojectsLink.setOutputMarkupPlaceholderTag( true );
        noresults.add(notallprojectsLink.setVisible(false));

        params.remove( "project" );
        params.add( "project", getProject().getId() );
        params.add( "from", String.valueOf( from + PAGE_SIZE ) );
        moreresultsLink = new BookmarkablePageLink( "morelink", Search.class, params );
        moreresultsLink.setOutputMarkupPlaceholderTag( true );
        add( moreresultsLink.setVisible( false ) );

        Form form = new Form( "search" ) {

            protected void onSubmit() {
                super.onSubmit();

                PageParameters params = getProjectPageParameters();
                params.add( "query", query );
                setResponsePage( Search.class, params );
            }
        };
        add( form );
        form.add( new TextField<String>( "query", new PropertyModel<String>( this, "query" ) ) );

        final Map<String,Integer> colors = new HashMap<String,Integer>();
        add( new ListView<Object[]>( "result", new SearchModel() )
        {
            protected void populateItem( ListItem<Object[]> listItem ) {
                Object[] o = listItem.getModelObject();

                if ( o[1] == null )
                {
                    listItem.setVisible( false );
                    return;
                }

                ResourceReference icon = new HeadsUpResourceReference( getClassImageName( o[1] ) );
                listItem.add( new Image( "icon", icon ) );

                int relevance = (int) ( ( (Float) o[0] ) * 100 );
                listItem.add(new Label("relevance", relevance + "%"));

                WebMarkupContainer container;
                if ( o[1] instanceof SearchResult)
                {
                    container = new ExternalLink( "link", ( (SearchResult) o[1] ).getLink() );
                    listItem.add( new WebMarkupContainer( "nolink" ).setVisible( false ) );
                }
                else
                {
                    container = new WebMarkupContainer( "nolink" );
                    listItem.add( new WebMarkupContainer( "link" ).setVisible( false ) );
                }
                container.add( new Label( "title", o[1].toString() ) );
                listItem.add( container );

                listItem.add( new Label( "project", getProjectFromResult( o ).getAlias() ) );

                Map<String,List<String>> fields = new HashMap<String,List<String>>();
                parseMatches( (Explanation) o[2], fields, colors );

                listItem.add( new Label( "summary", new SearchRenderModel( o[1], fields, colors ) )
                    .setEscapeModelStrings( false ) );
            }
        });
    }

    @Override
    public String getTitle()
    {
        return "Search";
    }

    private Project getProjectFromResult( Object[] result )
    {
        if ( result[1] instanceof Project) {
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

    private void parseMatches( Explanation e, Map<String,List<String>> fields, Map<String,Integer> colors )
    {
        if ( e.getDescription().contains( "fieldWeight" ) )
        {
            String desc = e.getDescription();
            String sub = desc.substring(desc.indexOf('(') + 1, desc.indexOf(')') );

            int colon = sub.indexOf( ':' );
            String field = sub.substring( 0, colon );

            int inPlace = sub.length();
            if ( sub.lastIndexOf( " in " ) > 0 )
            {
                inPlace = sub.lastIndexOf( " in " );
            }
            String match = sub.substring( colon + 1, inPlace );
            List<String> matches;
            if ( fields.containsKey( field ) )
            {
                matches = fields.get( field );
            }
            else
            {
                matches = new LinkedList<String>();
                fields.put( field, matches );
            }

            matches.add( match );
            if ( !colors.containsKey( match ) )
            {
                colors.put( match, colors.size() + 1 ); 
            }
        }

        if ( e.getDetails() != null )
        {
            for ( Explanation e2 : e.getDetails() )
            {
                parseMatches( e2, fields, colors );
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

        return getClassImageName( o.getClass() );
    }

    public static String getClassImageName( Class type )
    {
        if ( type.equals( Object.class ) )
        {
            return "images/type/System.png";
        }

        int start = type.getPackage().getName().length() + 1;
        int end = type.getName().length();
        if ( type.getName().contains( "$" ) )
        {
            end = type.getName().indexOf( '$' );
        }

        String image = "images/type/" + type.getName().substring( start, end ) + ".png";
        if ( PackageResource.exists( HeadsUpResourceMarker.class, image, null, null ) )
        {
            return image;
        }

        return getClassImageName( type.getSuperclass() );
    }

    class SearchModel
        extends AbstractReadOnlyModel<List<Object[]>>
    {
        List<Object[]> results = new LinkedList<Object[]>();
        int newFrom;

        public SearchModel()
        {
            if ( query != null )
            {
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

                noresults.setVisible( results.size() == 0 );
                notallprojectsLink.setVisible( results.size() == 0 && !getProject().equals( StoredProject.getDefault() ) );
                moreresultsLink.setVisible( results.size() == PAGE_SIZE );
                moreresultsLink.setParameter( "from", newFrom );
            }
        }

        public List<Object[]> getObject()
        {
            return results;
        }

        private boolean addResults( Query q, FullTextSession fullTextSession, int from )
        {
            FullTextQuery textQuery = fullTextSession.createFullTextQuery( q );
            textQuery.setProjection( FullTextQuery.SCORE, FullTextQuery.THIS, FullTextQuery.EXPLANATION );
            textQuery.setMaxResults(PAGE_SIZE);
            textQuery.setFirstResult( from );

            Iterator i = textQuery.iterate();
            while ( i.hasNext() ) {
                try
                {
                    Object[] result = (Object[]) i.next();

                    if ( getProject().equals( StoredProject.getDefault() ) || getProject().equals(
                            getProjectFromResult( result ) ) )
                    {
                        results.add( result );
                    }
                }
                catch ( Exception e )
                {
                    Manager.getLogger( "Search" ).error( "Error extracting search results", e );
                }
                newFrom++;
            }
            
            return textQuery.getResultSize() == PAGE_SIZE;
        }
    }
}
