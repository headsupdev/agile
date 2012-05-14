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

import org.headsupdev.agile.api.*;
import org.headsupdev.agile.web.WebApplication;
import org.headsupdev.agile.app.search.feed.SearchFeed;
import org.headsupdev.agile.app.search.permission.SearchPermission;

import java.util.List;
import java.util.LinkedList;

/**
 * The search application for the system
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class SearchApplication
    extends WebApplication
{
    List<MenuLink> links;

    public SearchApplication()
    {
        links = new LinkedList<MenuLink>();
    }

    public String getName()
    {
        return "Search";
    }

    public String getApplicationId()
    {
        return "search";
    }

    public String getDescription()
    {
        return "The " + HeadsUpConfiguration.getProductName() + " search application";
    }

    public List<MenuLink> getLinks()
    {
        return links;
    }

    @Override
    public Class<? extends Page>[] getPages() {
        return new Class[] { SearchFeed.class, Reindex.class, Search.class };
    }

    @Override
    public Class<? extends Page> getHomePage() {
        return Search.class;
    }

    @Override
    public Permission[] getPermissions()
    {
        return new Permission[] { new SearchPermission() };
    }
}