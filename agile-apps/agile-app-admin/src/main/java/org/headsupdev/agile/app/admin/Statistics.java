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

package org.headsupdev.agile.app.admin;

import org.headsupdev.agile.api.HeadsUpConfiguration;
import org.headsupdev.agile.api.Permission;
import org.headsupdev.agile.storage.HibernateUtil;
import org.headsupdev.agile.web.components.FormattedDurationModel;
import org.headsupdev.agile.web.components.FormattedSizeModel;
import org.headsupdev.agile.web.HeadsUpPage;
import org.headsupdev.agile.web.MountPoint;
import org.headsupdev.agile.security.permission.AdminPermission;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.hibernate.Session;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.util.Map;

/**
 * A simple page for various statistics
 *
 * @author Andrew Williams
 * @since 1.0
 */
@MountPoint( "stats" )
public class Statistics
    extends HeadsUpPage
{
    public Permission getRequiredPermission()
    {
        return new AdminPermission();
    }

    public void layout()
    {
        super.layout();
        add( CSSPackageResource.getHeaderContribution( getClass(), "admin.css" ) );
        add( new Label( "productname", getStorage().getGlobalConfiguration().getProductName() ) );

        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
        long uptime = runtimeBean.getUptime();

        add( new Label( "name", osBean.getName() ) );
        add( new Label( "version", osBean.getVersion() ) );
        add( new Label( "arch", osBean.getArch() ) );
        add( new Label( "processors", String.valueOf( osBean.getAvailableProcessors() ) ) );
        add( new Label( "uptime", new FormattedDurationModel( uptime ) ) );

        add( new Label( "vmname", runtimeBean.getVmName() ) );
        add( new Label( "vmversion", runtimeBean.getVmVersion() ) );
        add( new Label( "vmvendor", runtimeBean.getVmVendor() ) );

        boolean sunJVM = true;
        try
        {
            if ( osBean instanceof com.sun.management.OperatingSystemMXBean )
            {
                com.sun.management.OperatingSystemMXBean sunBean = ( (com.sun.management.OperatingSystemMXBean) osBean );
                long cputime = sunBean.getProcessCpuTime() / 1000000;

                WebMarkupContainer time = new WebMarkupContainer( "times" );
                add( time );
                add( new WebMarkupContainer( "notimes" ).setVisible( false ) );

                time.add( new Label( "uptime2", new FormattedDurationModel( uptime ) ) );
                time.add( new Label( "cputime", new FormattedDurationModel( cputime ) ).setVisible( cputime != -1 ) );
                time.add( new Label( "percenttime", (int) ( (float) cputime / (float) uptime * 100f ) + "%" ) );

                WebMarkupContainer mem = new WebMarkupContainer( "mem" );
                add( mem );
                add( new WebMarkupContainer( "nomem" ).setVisible( false ) );

                mem.add( new Label( "totalram", new FormattedSizeModel( sunBean.getTotalPhysicalMemorySize() ) ) );
                mem.add( new Label( "freeram", new FormattedSizeModel( sunBean.getFreePhysicalMemorySize() ) ) );
                mem.add( new Label( "totalswap", new FormattedSizeModel( sunBean.getTotalSwapSpaceSize() ) ) );
                mem.add( new Label( "freeswap", new FormattedSizeModel( sunBean.getFreeSwapSpaceSize() ) ) );
            }
        }
        catch ( NoClassDefFoundError e )
        {
            e.printStackTrace();
            sunJVM = false;
        }

        if ( !sunJVM )
        {
            add( new WebMarkupContainer( "times" ).setVisible( false ) );
            add( new WebMarkupContainer( "notimes" ).setVisible( true ) );

            add( new WebMarkupContainer( "mem" ).setVisible( false ) );
            add( new WebMarkupContainer( "nomem" ).setVisible( true ) );
        }

        // database stats
        add( new Label( "active", String.valueOf( HibernateUtil.getStatistics().getActiveConnections() ) ) );
        add( new Label( "maxActive", String.valueOf( HibernateUtil.getStatistics().getMaximumActiveConnections() ) ) );
        add( new Label( "idle", String.valueOf( HibernateUtil.getStatistics().getIdleConnections() ) ) );
        add( new Label( "maxIdle", String.valueOf( HibernateUtil.getStatistics().getMaximumIdleConnections() ) ) );

        add( new Label( "sessions", getSessionStacks() ).setEscapeModelStrings( false ).setVisible(
                HeadsUpConfiguration.isDebug()
        ) );
    }

    @Override
    public String getTitle()
    {
        return "Statistics";
    }

    protected String getSessionStacks()
    {
        StringBuilder out = new StringBuilder();
        out.append( "<table class=\"listing\">\n");
        out.append( "  <tr>\n");
        out.append( "    <th>ID</th>\n");
        out.append( "    <th>Allocating Stack</th>\n");
        out.append( "  </tr>\n");

        int i = 1;
        boolean odd = true;
        final Map<Session,Exception> sessions = HibernateUtil.getOpenSessions();
        for ( Session session : sessions.keySet() )
        {
            if ( odd )
            {
                out.append( "  <tr class=\"odd\">\n" );
            }
            else
            {
                out.append( "  <tr class=\"even\">\n" );
            }
            odd = !odd;
            out.append( "    <td style=\"vertical-align: top\">" );
            out.append( i++ );
            out.append( "</td>\n" );

            StringWriter stackTrace = new StringWriter();
            //noinspection ThrowableResultOfMethodCallIgnored
            sessions.get( session ).printStackTrace( new PrintWriter( stackTrace ) {
                boolean removed = false;

                @Override
                public void println() {
                    if ( removed )
                    {
                        super.println();
                    }

                    removed = true;
                }

                @Override
                public void write( String s )
                {
                    if ( !removed )
                    {
                        return;
                    }

                    if ( s.startsWith( "\t" ) )
                    {
                        s = s.substring( 1 );
                    }
                    super.write( s );
                }
            } );

            out.append( "    <td><pre style=\"margin:0\">" );
            out.append( stackTrace.toString() );
            out.append( "</pre></td>" );
            out.append( "  </tr>\n" );
        }

        out.append( "</table>" );
        return out.toString();
    }
}
