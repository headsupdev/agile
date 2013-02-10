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

package org.headsupdev.agile.framework.rest;

import org.apache.wicket.PageParameters;
import org.apache.wicket.model.Model;
import org.headsupdev.agile.api.HeadsUpConfiguration;
import org.headsupdev.agile.api.Permission;
import org.headsupdev.agile.web.rest.HeadsUpApi;
import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.web.ApplicationPageMapper;
import org.headsupdev.agile.web.MountPoint;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple API class that exposes a list of configuration properties - along with a list of loaded app IDs.
 * <p/>
 * Created: 09/02/2013
 *
 * @author Andrew Williams
 * @since 2.0
 */
@MountPoint( "configuration" )
public class ConfigurationApi
    extends HeadsUpApi
{
    public ConfigurationApi( PageParameters params )
    {
        super( params );
    }

    @Override
    public boolean respectPublishAnnotation()
    {
        return false;
    }

    @Override
    public Permission getRequiredPermission()
    {
        return null;
    }

    @Override
    public void doGet( PageParameters params )
    {
        setModel( new Model<Configurations>( new Configurations() ) );
    }

    private static class Configurations
        implements Serializable
    {
        private String productName, productUrl, url, version, timeZone;
        private boolean useFullNames;

        private List<String> appIds;

        public Configurations()
        {
            HeadsUpConfiguration global = Manager.getStorageInstance().getGlobalConfiguration();
            productName = global.getProductName();
            productUrl = global.getProductUrl();

            url = global.getBaseUrl();
            version = global.getBuildVersion();
            timeZone = global.getDefaultTimeZone().getID();

            useFullNames = global.getUseFullnamesForUsers();

            appIds = new ArrayList<String>( ApplicationPageMapper.get().getApplicationIds() );
            appIds.remove( "home" );
        }
    }
}
