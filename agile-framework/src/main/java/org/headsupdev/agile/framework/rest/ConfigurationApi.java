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
