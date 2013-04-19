/*
 * HeadsUp Agile
 * Copyright 2009-2013 Heads Up Development Ltd.
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


import org.headsupdev.agile.app.search.permission.SearchPermission;
import org.headsupdev.agile.storage.StoredProject;
import org.headsupdev.agile.web.HeadsUpPage;
import org.headsupdev.agile.web.components.HeadsUpResourceReference;
import org.headsupdev.agile.api.*;

import java.util.*;

import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ResourceReference;
import org.apache.lucene.search.Explanation;
import org.headsupdev.agile.web.components.filters.ApplicationFilterPanel;

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
    private String query = null;
    private int from = 0;
    private WebMarkupContainer noresults;
    private BookmarkablePageLink moreresultsLink, notallprojectsLink;

    private ApplicationFilterPanel filter;
    private SearchModel searchModel;

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

        add( filter = new ApplicationFilterPanel( "filter", "search" )
        {
            @Override
            public void onFilterUpdated()
            {
                searchModel.filterUpdated();
            }
        });

        PageParameters params = getPageParameters();
        params.remove( "project" );
        params.add( "project", StoredProject.ALL_PROJECT_ID );
        notallprojectsLink = new BookmarkablePageLink( "notallprojectsLink", Search.class, params );
        notallprojectsLink.setOutputMarkupPlaceholderTag( true );
        noresults.add(notallprojectsLink.setVisible(false));

        params.remove( "project" );
        params.add( "project", getProject().getId() );
        params.add( "from", String.valueOf( from + Searcher.PAGE_SIZE ) );
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
        searchModel = new SearchModel();
        add( new ListView<Searcher.Result>( "result", searchModel )
        {
            protected void populateItem( ListItem<Searcher.Result> listItem )
            {
                Searcher.Result result = listItem.getModelObject();

                ResourceReference icon = new HeadsUpResourceReference( Searcher.getClassImageName( result.match ) );
                listItem.add( new Image( "icon", icon ) );

                listItem.add( new Label( "relevance", (int) ( result.relevance * 100 ) + "%" ) );

                WebMarkupContainer container;
                if ( result.link != null )
                {
                    container = new ExternalLink( "link", result.link );
                    listItem.add( new WebMarkupContainer( "nolink" ).setVisible( false ) );
                }
                else
                {
                    container = new WebMarkupContainer( "nolink" );
                    listItem.add( new WebMarkupContainer( "link" ).setVisible( false ) );
                }
                container.add( new Label( "title", result.title ) );
                listItem.add( container );

                listItem.add( new Label( "project", result.project.getAlias() ) );

                Map<String,List<String>> fields = new HashMap<String,List<String>>();
                parseMatches( result.explanation, fields, colors );

                listItem.add( new Label( "summary", new SearchRenderModel( result.match, fields, colors ) )
                    .setEscapeModelStrings( false ) );
            }
        });
    }

    @Override
    public String getTitle()
    {
        return "Search";
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

    class SearchModel
        extends AbstractReadOnlyModel<List<Searcher.Result>>
    {
        Searcher searcher = new Searcher( query, from );

        public SearchModel()
        {
            if ( query != null )
            {
                searcher.setProject( getProject() );
                searcher.setAppFilter( filter.getApplications() );
                List<Searcher.Result> results = searcher.getResults();

                noresults.setVisible( results.size() == 0 );
                notallprojectsLink.setVisible( results.size() == 0 && !getProject().equals( StoredProject.getDefault() ) );
                moreresultsLink.setVisible( results.size() == Searcher.PAGE_SIZE );
                moreresultsLink.setParameter( "from", from + Searcher.PAGE_SIZE );
            }
        }

        public List<Searcher.Result> getObject()
        {
            return searcher.getResults();
        }

        public void filterUpdated()
        {
            searcher.setAppFilter( filter.getApplications() );
        }
    }
}
