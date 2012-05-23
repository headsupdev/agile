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

package org.headsupdev.agile.app.ci;

import org.headsupdev.support.java.FileUtil;
import org.headsupdev.agile.app.ci.irc.BuildCommand;
import org.headsupdev.agile.storage.StoredProject;
import org.headsupdev.agile.storage.hibernate.IdProjectId;
import org.headsupdev.agile.storage.ci.Build;
import org.headsupdev.agile.storage.HibernateStorage;
import org.headsupdev.agile.storage.HibernateUtil;
import org.headsupdev.agile.app.ci.event.BuildFailedEvent;
import org.headsupdev.agile.app.ci.event.BuildSucceededEvent;
import org.headsupdev.agile.app.ci.event.UploadApplicationEvent;
import org.headsupdev.agile.app.ci.permission.BuildViewPermission;
import org.headsupdev.agile.app.ci.permission.BuildListPermission;
import org.headsupdev.agile.app.ci.permission.BuildForcePermission;
import org.headsupdev.agile.web.WebApplication;
import org.headsupdev.agile.api.*;
import org.headsupdev.irc.IRCCommand;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.Query;
import org.osgi.framework.BundleContext;

import java.util.List;
import java.util.LinkedList;
import java.io.File;

/**
 * The application descritor for continuous integration
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class CIApplication
    extends WebApplication
{
    public static final String ID = "builds";

    public static final ConfigurationItem CONFIGURATION_NOTIFY_REPEAT_PASS = new ConfigurationItem(
            "notify.pass.repeat", false, "Send notifications for continual success",
            "Set this to true if you wish every build to cause a notification, otherwise only failed " +
            "or newly passed builds will send notifications" );

    public static final ConfigurationItem CONFIGURATION_MAVEN_HOME = new ConfigurationItem( "maven.home",
        "", "Maven Home (not including /bin/mvn)", "Change this parameter if you wish to use a particular version of maven " +
        "or if your maven installation is not in the system path" );
//    {
//        public boolean test( String value )
//        {
//            File mvn = new File( new File( value, "bin" ), "mvn" );
//            return mvn.exists();
//        }
//    } );
    public static final ConfigurationItem CONFIGURATION_ANT_HOME = new ConfigurationItem( "ant.home",
        "", "Ant Home Directory", "Change this parameter if you wish to use a particular version of ant " +
        "or if your ant installation is not in the system path" );
    public static final ConfigurationItem CONFIGURATION_ECLIPSE_HOME = new ConfigurationItem( "eclipse.home",
        "", "Eclipse home directory", "The directory where Eclipse is installed" );

    public static final ConfigurationItem CONFIGURATION_MAVEN_GOALS = new ConfigurationItem( "maven.goals",
        "clean install", "Maven build goals", "A space separated list of maven goals and phases to run when building" );
    public static final ConfigurationItem CONFIGURATION_MAVEN_PROFILES = new ConfigurationItem( "maven.profiles",
        "", "Maven build profiles", "A space separated list of maven profiles to activate when building" );
    public final ConfigurationItem CONFIGURATION_MAVEN_HOME_OVERRIDE = new ConfigurationItem( "maven.home",
        "", "Maven Home (not including /bin/mvn)", "Use this option to specify a different maven installation for " +
        "this build schedule" )
    {
        @Override
        public Object getDefault()
        {
            PropertyTree appConfig = CIApplication.this.getConfiguration();

            return appConfig.getProperty( CONFIGURATION_MAVEN_HOME.getKey(), (String) CONFIGURATION_MAVEN_HOME.getDefault() );
        }
    };

    public final ConfigurationItem CONFIGURATION_ANT_HOME_OVERRIDE = new ConfigurationItem( "ant.home",
        "", "Ant Home Directory", "Use this option to specify a different ant installation for " +
        "this build schedule" )
    {
        @Override
        public Object getDefault()
        {
            PropertyTree appConfig = CIApplication.this.getConfiguration();

            return appConfig.getProperty( CONFIGURATION_ANT_HOME.getKey(), (String) CONFIGURATION_ANT_HOME.getDefault() );
        }
    };

    public final ConfigurationItem CONFIGURATION_ECLIPSE_HOME_OVERRIDE = new ConfigurationItem( "eclipse.home",
        "", "Eclipse home directory", "Use this option to specify a different eclipse installation for " +
        "this build schedule" )
    {
        @Override
        public Object getDefault()
        {
            PropertyTree appConfig = CIApplication.this.getConfiguration();

            return appConfig.getProperty( CONFIGURATION_ECLIPSE_HOME.getKey(), (String) CONFIGURATION_ECLIPSE_HOME.getDefault() );
        }
    };

    public static final ConfigurationItem CONFIGURATION_COMMAND_LINE = new ConfigurationItem( "commandline",
        "make", "Command line", "The command used to start the build process for this project" );

    public static final ConfigurationItem CONFIGURATION_XCODE_CONFIG = new ConfigurationItem( "xcode.config",
        "Release", "Project Configuration", "The xcode configuation to build this project" );
    public static final ConfigurationItem CONFIGURATION_XCODE_TARGET = new ConfigurationItem( "xcode.target",
        "", "Project Target", "The xcode target to build this project (optional)" );
    public static final ConfigurationItem CONFIGURATION_XCODE_SDK = new ConfigurationItem( "xcode.sdk",
        "", "Project SDK", "The SDK to build this project against - mainly used for testing (optional)" );
    public static final ConfigurationItem CONFIGURATION_XCODE_COPYSYMBOLS = new ConfigurationItem( "xcode.symbols",
        false, "Copy debugging symbols", "Make a copy of the debugging symbols for later use" );

    public static final ConfigurationItem CONFIGURATION_CRON_EXPRESSION = new ConfigurationItem(
        ConfigurationItem.TYPE_CRON, "cron", "0 15 23 * * ?", "Cron Expression",
        "A cron expression to specify when the schedule should run (use format s m h DoM M DoW)" );

    private List<MenuLink> links;
    private List<String> eventTypes;

    private List<ConfigurationItem> globalItems = new LinkedList<ConfigurationItem>();

    private List<ConfigurationItem> mavenProjectItems = new LinkedList<ConfigurationItem>();
    private List<ConfigurationItem> antProjectItems = new LinkedList<ConfigurationItem>();
    private List<ConfigurationItem> eclipseProjectItems = new LinkedList<ConfigurationItem>();
    private List<ConfigurationItem> cmdProjectItems = new LinkedList<ConfigurationItem>();
    private List<ConfigurationItem> xcodeProjectItems = new LinkedList<ConfigurationItem>();
    private List<ConfigurationItem> otherProjectItems = new LinkedList<ConfigurationItem>();

    private static CIBuilder builder = new CIBuilder();

    private static CIScheduler scheduler = new CIScheduler();

    public static CIBuilder getBuilder()
    {
        return builder;
    }

    public static CIScheduler getScheduler()
    {
        return scheduler;
    }

    public void start( BundleContext bc )
    {
        super.start( bc );

        links = new LinkedList<MenuLink>();

        eventTypes = new LinkedList<String>();
        eventTypes.add( "buildfailed" );
        eventTypes.add( "buildsucceeded" );

        builder.setApplication( this );
        Manager.getInstance().addProjectListener( builder );

        globalItems.add( CONFIGURATION_MAVEN_HOME );
        globalItems.add( CONFIGURATION_ANT_HOME );
        globalItems.add( CONFIGURATION_ECLIPSE_HOME );
        globalItems.add( CONFIGURATION_NOTIFY_REPEAT_PASS );

        List<ConfigurationItem> items = new LinkedList<ConfigurationItem>();
        items.add( CONFIGURATION_MAVEN_GOALS );
        items.add( CONFIGURATION_MAVEN_PROFILES );
        items.add( CONFIGURATION_MAVEN_HOME_OVERRIDE );
        mavenProjectItems.add( new ConfigurationItem( "schedule.default", "Default Build Schedule", items ) );
        items = new LinkedList<ConfigurationItem>();
        items.add( CONFIGURATION_MAVEN_GOALS );
        items.add( CONFIGURATION_MAVEN_PROFILES );
        items.add( CONFIGURATION_MAVEN_HOME_OVERRIDE );
        items.add( CONFIGURATION_CRON_EXPRESSION );
        mavenProjectItems.add( new ConfigurationItem( "schedule", "Build Schedule",
                new ConfigurationItem( "schedule", "Build Schedule", items ) ) );

        items = new LinkedList<ConfigurationItem>();
        items.add( CONFIGURATION_ANT_HOME_OVERRIDE );
        antProjectItems.add( new ConfigurationItem( "schedule.default", "Default Build Schedule", items ) );
        items = new LinkedList<ConfigurationItem>();
        items.add( CONFIGURATION_ANT_HOME_OVERRIDE );
        items.add( CONFIGURATION_CRON_EXPRESSION );
        antProjectItems.add( new ConfigurationItem( "schedule", "Build Schedule",
                new ConfigurationItem( "schedule", "Build Schedule", items ) ) );

        items = new LinkedList<ConfigurationItem>();
        items.add( CONFIGURATION_ECLIPSE_HOME_OVERRIDE );
        eclipseProjectItems.add( new ConfigurationItem( "schedule.default", "Default Build Schedule", items ) );
        items = new LinkedList<ConfigurationItem>();
        items.add( CONFIGURATION_ECLIPSE_HOME_OVERRIDE );
        items.add( CONFIGURATION_CRON_EXPRESSION );
        eclipseProjectItems.add( new ConfigurationItem( "schedule", "Build Schedule",
                new ConfigurationItem( "schedule", "Build Schedule", items ) ) );

        items = new LinkedList<ConfigurationItem>();
        items.add( CONFIGURATION_COMMAND_LINE );
        cmdProjectItems.add( new ConfigurationItem( "schedule.default", "Default Build Schedule", items ) );
        items = new LinkedList<ConfigurationItem>();
        items.add( CONFIGURATION_COMMAND_LINE );
        items.add( CONFIGURATION_CRON_EXPRESSION );
        cmdProjectItems.add( new ConfigurationItem( "schedule", "Build Schedule",
                new ConfigurationItem( "schedule", "Build Schedule", items ) ) );

        items = new LinkedList<ConfigurationItem>();
        items.add( CONFIGURATION_XCODE_CONFIG );
        items.add( CONFIGURATION_XCODE_TARGET );
        items.add( CONFIGURATION_XCODE_SDK ) ;
        items.add( CONFIGURATION_XCODE_COPYSYMBOLS );
        xcodeProjectItems.add( new ConfigurationItem( "schedule.default", "Default Build Schedule", items ) );
        items = new LinkedList<ConfigurationItem>();
        items.add( CONFIGURATION_XCODE_CONFIG );
        items.add( CONFIGURATION_XCODE_TARGET );
        items.add( CONFIGURATION_XCODE_SDK );
        items.add( CONFIGURATION_XCODE_COPYSYMBOLS );
        items.add( CONFIGURATION_CRON_EXPRESSION );
        xcodeProjectItems.add( new ConfigurationItem( "schedule", "Build Schedule",
                new ConfigurationItem( "schedule", "Build Schedule", items ) ) );

// no configuration for unknown projects in the default schedule and just a cron expression for other schedules...
//        otherProjectItems.add( new ConfigurationItem( "schedule.default", "Default Build Schedule", items ) );
        items = new LinkedList<ConfigurationItem>();
        items.add( CONFIGURATION_CRON_EXPRESSION );
        otherProjectItems.add( new ConfigurationItem( "schedule", "Build Schedule",
                new ConfigurationItem( "schedule", "Build Schedule", items ) ) );

        for ( Project project : Manager.getStorageInstance().getProjects() )
        {
            scheduler.resetProject( project );
        }

        // find a sensible default for maven.home
        File maven = FileUtil.lookupGrandparentInPath( "mvn" );
        if ( maven == null )
        {
            maven = FileUtil.lookupGrandparentInPath( "mvn.bat" );
        }
        if ( maven != null )
        {
            CONFIGURATION_MAVEN_HOME.setDefault( maven.getAbsolutePath() );
        }

        // find a sensible default for ant.home
        File ant = FileUtil.lookupParentInPath( "ant" );
        if ( ant == null )
        {
            ant = FileUtil.lookupParentInPath( "ant.bat" );
        }
        if ( ant != null )
        {
            CONFIGURATION_ANT_HOME.setDefault( ant.getAbsolutePath() );
        }

        // lookup the different binaries for eclipse...
        File eclipse = FileUtil.lookupParentInPath( "eclipse" );
        if ( eclipse == null )
        {
            eclipse = FileUtil.lookupParentInPath( "eclipse.exe" );
        }

        if ( eclipse != null )
        {
            CONFIGURATION_ECLIPSE_HOME.setDefault( eclipse.getAbsolutePath() );
        }
    }

    public String getName()
    {
        return "Builds";
    }

    public String getApplicationId()
    {
        return ID;
    }

    public String getDescription()
    {
        return "The " + Manager.getStorageInstance().getGlobalConfiguration().getProductName() + " continuous integration application";
    }

    public List<MenuLink> getLinks()
    {
        return links;
    }

    public List<String> getEventTypes() {
        return eventTypes;
    }

    public Class[] getPersistantClasses() {
        return new Class[] { BuildFailedEvent.class, BuildSucceededEvent.class, UploadApplicationEvent.class };
    }

    @Override
    public Class<? extends Page>[] getPages()
    {
        return new Class[] { CI.class, Tests.class, View.class };
    }

    @Override
    public Class<? extends Page> getHomePage()
    {
        return CI.class;
    }

    @Override
    public Permission[] getPermissions()
    {
        return new Permission[] { new BuildForcePermission(), new BuildListPermission(), new BuildViewPermission() };
    }

    @Override
    public LinkProvider[] getLinkProviders()
    {
        return new LinkProvider[]{ new BuildLinkProvider() };
    }

    @Override
    public IRCCommand[] getIRCCommands()
    {
        return new IRCCommand[]{ new BuildCommand() };
    }

    @Override
    public List<ConfigurationItem> getConfigurationItems() {
        return globalItems;
    }

    @Override
    public List<ConfigurationItem> getProjectConfigurationItems( Project project ) {
        if ( project instanceof MavenTwoProject )
        {
            return mavenProjectItems;
        }
        else if ( project instanceof AntProject )
        {
            return antProjectItems;
        }
        else if ( project instanceof EclipseProject )
        {
            return eclipseProjectItems;
        }
        else if ( project instanceof CommandLineProject )
        {
            return cmdProjectItems;
        }
        else if ( project instanceof XCodeProject )
        {
            return xcodeProjectItems;
        }
        else if ( project.equals( StoredProject.getDefault() ) )
        {
            return new LinkedList<ConfigurationItem>();
        }
        else
        {
            return otherProjectItems;
        }
    }

    @Override
    public void onProjectConfigurationChanged( Project project )
    {
        scheduler.resetProject( project );
    }

    public List<Build> getBuildsForProject( Project project )
    {
        Session session = ( (HibernateStorage) Manager.getStorageInstance() ).getHibernateSession();

        Query q = session.createQuery( "from Build b where id.project.id = :pid order by id.id desc" );
        q.setString( "pid", project.getId() );
        q.setMaxResults( 25 );
        return q.list();
    }

    public List<Build> getRunningBuilds()
    {
        Session session = ( (HibernateStorage) Manager.getStorageInstance() ).getHibernateSession();
        Transaction tx = session.beginTransaction();

        Query q = session.createQuery( "from Build b where status = " + Build.BUILD_RUNNING );
        List<Build> ret = q.list();

        tx.commit();
        return ret;
    }
    
    static public Build getBuild( long id, Project project )
    {
        Session session = ( (HibernateStorage) Manager.getStorageInstance() ).getHibernateSession();

        Query q = session.createQuery( "from Build b where id.id = :id and id.project.id = :pid" );
        q.setLong( "id", id );
        q.setString( "pid", project.getId() );
        return (Build) q.uniqueResult();
    }

    public Build getLatestBuildForProject( Project project )
    {
        Build build = null;
        Session session = ( (HibernateStorage) Manager.getStorageInstance() ).getHibernateSession();

        Query q = session.createQuery( "from Build b where id.project.id = :pid order by id.id desc" );
        q.setString( "pid", project.getId() );
        q.setMaxResults( 1 );
        List<Build> builds = q.list();
        if ( builds.size() > 0 )
        {
            build = builds.get( 0 );
        }

        return build;
    }

    public static String getLastChangePassed( Project project )
    {
        Build build = null;
        Session session = ( (HibernateStorage) Manager.getStorageInstance() ).getHibernateSession();

        Query q = session.createQuery( "from Build b where id.project.id = :pid and status = " + Build.BUILD_SUCCEEDED +
                " order by endTime desc" );
        q.setString( "pid", project.getId() );
        q.setMaxResults( 1 );
        List<Build> builds = q.list();
        if ( builds.size() > 0 )
        {
            build = builds.get( 0 );
        }

        if ( build == null )
        {
            return "";
        }
        return build.getRevision();
    }

    public static Build getPreviousLastChangePassed( Build current, Project project )
    {
        Build build = null;
        Session session = ( (HibernateStorage) Manager.getStorageInstance() ).getHibernateSession();

        Query q = session.createQuery( "from Build b where id.project.id = :pid and status = " + Build.BUILD_SUCCEEDED +
                " and startTime < :beforeDate order by endTime desc" );
        q.setString( "pid", project.getId() );
        q.setTimestamp( "beforeDate", current.getStartTime() );
        q.setMaxResults( 1 );
        List<Build> builds = q.list();
        if ( builds.size() > 0 )
        {
            build = builds.get( 0 );
        }

        return build;
    }

    public long addBuild( Build build )
    {
        Session session = HibernateUtil.getCurrentSession();

        Transaction tx = session.beginTransaction();
        IdProjectId id = (IdProjectId) session.save( build );
        long ret = id.getId();
        tx.commit();

        return ret;
    }

    public void saveBuild( Build build )
    {
        Session session = HibernateUtil.getCurrentSession();

        Transaction tx = session.beginTransaction();
        session.saveOrUpdate( build );
        tx.commit();
    }

    public static File getProjectDir( Project project )
    {
        File appDir = new File( Manager.getStorageInstance().getDataDirectory(), "builds" );
        return new File( appDir, project.getId().replace( ':', '_' ) );
    }
}