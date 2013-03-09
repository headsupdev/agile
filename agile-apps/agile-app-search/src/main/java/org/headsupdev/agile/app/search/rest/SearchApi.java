/*
 * HeadsUp Agile
 * Copyright 2013 Heads Up Development Ltd.
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

package org.headsupdev.agile.app.search.rest;

import org.headsupdev.agile.api.Permission;
import org.headsupdev.agile.app.search.Searcher;
import org.headsupdev.agile.app.search.permission.SearchPermission;
import org.headsupdev.agile.web.MountPoint;
import org.headsupdev.agile.web.rest.HeadsUpApi;

import org.apache.wicket.PageParameters;
import org.apache.wicket.model.Model;
import org.headsupdev.support.java.StringUtil;

import java.util.ArrayList;

/**
 * A search API that provides a simple list of details for any results matching the given query.
 * <p/>
 * Created: 16/02/2013
 *
 * @author Andrew Williams
 * @since 2.0
 */
@MountPoint( "search" )
public class SearchApi
        extends HeadsUpApi
{
    public SearchApi( PageParameters params )
    {
        super( params );
    }

    @Override
    public Permission getRequiredPermission()
    {
        return new SearchPermission();
    }

    @Override
    public void doGet( PageParameters params )
    {
        String query = params.getString( "query" );
        if ( StringUtil.isEmpty( query ) )
        {
            setModel( new Model<ArrayList<Searcher.Result>>( new ArrayList<Searcher.Result>() ) );
        }
        else
        {
            setModel( new Model<ArrayList<Searcher.Result>>( getSearchResults( query ) ) );
        }
    }

    protected ArrayList<Searcher.Result> getSearchResults( String query )
    {
        Searcher searcher = new Searcher( query, 0 );
        searcher.setProject( getProject() );

        return searcher.getResults();
    }
}