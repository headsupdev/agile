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

package org.headsupdev.agile.app.dashboard;

import org.headsupdev.agile.api.*;
import org.headsupdev.agile.api.rest.Api;
import org.headsupdev.agile.app.dashboard.rest.AccountApi;
import org.headsupdev.agile.web.WebApplication;
import org.headsupdev.agile.app.dashboard.permission.ProjectViewPermission;
import org.headsupdev.agile.app.dashboard.permission.MemberViewPermission;
import org.headsupdev.agile.app.dashboard.permission.MemberListPermission;
import org.headsupdev.agile.app.dashboard.permission.MemberEditPermission;
import org.headsupdev.agile.app.dashboard.feed.MemberFeed;
import org.headsupdev.agile.app.dashboard.feed.ProjectFeed;

import java.util.List;
import java.util.LinkedList;

/**
 * The main application manages the landing pages and dashboard graphs etc
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class DashboardApplication
    extends WebApplication
{
    public static final ConfigurationItem DEFAULT_TIMEFRAME = new ConfigurationItem( "default-timeframe",
        ActivityGraph.TIME_MONTH, "The default time frame for project graphs",
        "The time frame that is shown as an overview or project activity, either " + ActivityGraph.TIME_MONTH +
        " or " + ActivityGraph.TIME_YEAR );
    List<MenuLink> links;

    public DashboardApplication()
    {
        addConfigurationItem( DEFAULT_TIMEFRAME );
        links = new LinkedList<MenuLink>();
        links.add( new SimpleMenuLink( "accounts" ) );
        links.add( new SimpleMenuLink( "about" ) );
        links.add( new SimpleMenuLink( "updates" ) );
    }

    public String getName()
    {
        return "Dashboard";
    }

    public String getApplicationId()
    {
        return "dashboard";
    }

    public String getDescription()
    {
        return "The " + Manager.getStorageInstance().getGlobalConfiguration().getProductName() + " dashboard application";
    }

    public List<MenuLink> getLinks( Project project )
    {
        return links;
    }

    @Override
    public Class<? extends Page>[] getPages() {
        return (Class<? extends Page>[]) new Class[]{ ChangePassword.class, EditAccount.class, Subscriptions.class,
            Accounts.class, Show.class, Account.class, Welcome.class, ProjectFeed.class, MemberFeed.class };
    }

    @Override
    public Class<? extends Api>[] getApis()
    {
        return (Class<? extends Api>[]) new Class[]{ AccountApi.class };
    }

    @Override
    public Class<? extends Page> getHomePage() {
        return Welcome.class;
    }

    @Override
    public Class[] getResources() {
        return new Class[]{ ActivityGraph.class, AccountGraph.class };
    }

    @Override
    public Permission[] getPermissions() {
        return new Permission[] { new MemberEditPermission(), new MemberListPermission(), new MemberViewPermission(), new ProjectViewPermission() };
    }

    @Override
    public LinkProvider[] getLinkProviders()
    {
        return new LinkProvider[]{ new UserLinkProvider() };
    }
}
