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

package org.headsupdev.agile.runtime;

import java.io.*;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.Properties;
import java.util.Enumeration;
import java.util.Dictionary;

import org.osgi.framework.launch.*;
import org.apache.felix.main.AutoProcessor;
import org.apache.felix.framework.util.Util;

public class Main
{
    /**
     * The property name used to specify an URL to the system
     * property file.
     */
    public static final String SYSTEM_PROPERTIES_PROP = "felix.system.properties";

    /**
     * The default name used for the system properties file.
     */
    public static final String SYSTEM_PROPERTIES_FILE_VALUE = "system.properties";

    /**
     * The property name used to specify an URL to the configuration
     * property file to be used for the created the framework instance.
     */
    public static final String CONFIG_PROPERTIES_PROP = "felix.config.properties";

    /**
     * The default name used for the configuration properties file.
     */
    public static final String CONFIG_PROPERTIES_FILE_VALUE = "config.properties";

    /**
     * The property name used to indicate we should run in debug mode
     */
    public static final String DEBUG_PROP = "debug";

    /**
     * Name of the configuration directory.
     */
    public static final String CONFIG_DIRECTORY = "conf";

    private static Framework m_fwk = null;

    private static boolean help = false;
    private static boolean debug = false;
    private static boolean verbose = false;
    private static boolean restarting = true;

    /**
     * Start the framework instance. Load properites and install a shutdown hook.
     * Wait for shutdown or restart commands and respond accordingly.
     *
     * @param args command line parameters, none required
     * @throws Exception If an error occurs.
     */
    public static void main( String[] args )
        throws Exception
    {
        String mesg = "         ";
        for ( String arg : args )
        {
            if ( "-d".equals( arg ) || "--debug".equals( arg ) )
            {
                debug = true;
                mesg = " (debug) ";
                System.setProperty( "agile.runtime.debug", "true" );
            }
            else if ( "-v".equals( arg ) || "--verbose".equals( arg ) )
            {
                verbose = true;
                mesg = "(verbose)";
                System.setProperty( "agile.runtime.verbose", "true" );
            }
            else if ( "-c".equals( arg ) || "--color".equals( arg ) )
            {
                System.setProperty( "agile.runtime.color", "true" );
            }
            else if ( "-n".equals( arg ) || "--nocolor".equals( arg ) )
            {
                System.setProperty( "agile.runtime.color", "false" );
            }
            else if ( "-h".equals( arg ) || "--help".equals( arg ) )
            {
                help = true;
            }
        }

        String defaultColor = System.getProperty( "agile.runtime.color" );
        boolean useColor = defaultColor != null && defaultColor.equalsIgnoreCase( "true" );

        String yellow = "";
        String gray = "";
        String darkGray = "";
        String blank = "";
        if ( useColor )
        {
            yellow = "\033[1;33m";
            gray = "\033[1;37m";
            darkGray = "\033[0;37m";
            blank = "\033[0m";
        }

        // text from compacted "chunky" figlet font (with a cool arrowed "h"
        String product1 = gray + "              __ __       ";
        String product2 = gray + " .---.-.-----|__|  |-----.";
        String product3 = gray + " |  _  |  _  |  |  |  -__|";
        String product4 = gray + " |___._|___  |__|__|_____|";
        String product5 = gray + "       |_____|            ";

        // Print welcome banner.
        System.out.println( yellow +        " /\\                   __                   " + product1 );
        System.out.println( yellow +         "|  |--.-----.---.-.--|  |-----.--.--.-----." + product2 );
        System.out.println( yellow +         "|     |  -__|  _  |  _  |__ --|  |  |  _  |" + product3 );
        System.out.println( yellow +         "|__|__|_____|___._|_____|_____|_____|   __|" + product4 );
        System.out.println( gray + "              "+mesg+ "       "+yellow+"      |__|   " + product5 );
        System.out.println( blank );

        if ( help )
        {
            System.out.println( "Options:" );
            System.out.println( " -h --help   \tShow this help message and then exit" );
            System.out.println( " -d --debug  \tDebug mode enables the framework shell and logs debugging info" );
            System.out.println( " -v --verbose\tDisplay some useful information about background tasks etc" );
            System.out.println( " -c --color  \toutput colour output - default if supported" );
            System.out.println( " -n --nocolor\tdisable colour output" );

            return;
        }

        // register a shutdown hook to make sure the framework is
        // cleanly shutdown when the VM exits.
        Runtime.getRuntime().addShutdownHook( new Thread( "Felix Shutdown Hook" )
        {
            public void run()
            {
                try
                {
                    if ( m_fwk != null )
                    {
                        m_fwk.stop();
                        m_fwk.waitForStop( 0 );
                    }
                }
                catch ( Exception ex )
                {
                    System.err.println( "Error stopping framework: " + ex );
                }
            }
        } );

        try
        {
            String extraBundles = "";
            if ( debug )
            {
                extraBundles = Main.getDebugBundles();
            }

            while ( restarting )
            {
                restarting = false;
                Properties configProps = loadConfiguration();
                String frameworkBundles = configProps.getProperty( "felix.auto.start.1" );
                frameworkBundles = frameworkBundles + extraBundles;
                configProps.setProperty( "felix.auto.start.1", frameworkBundles );

                // TODO should we auto detect the framework jars here (not part of the auto start in felix)?

                // Create an instance of the framework.
                FrameworkFactory factory = getFrameworkFactory();
                m_fwk = factory.newFramework( configProps );
                // Initialize the framework, but don't start it yet.
                m_fwk.init();
                // Use the system bundle context to process the auto-deploy
                // and auto-install/auto-start properties.
                AutoProcessor.process( configProps, m_fwk.getBundleContext() );

                Dictionary props = new Properties();
                m_fwk.getBundleContext().registerService( HeadsUpRuntime.class.getName(),
                    new HeadsUpRuntimeImpl(), props );

                // Start the framework.
                m_fwk.start();
                // Wait for framework to stop to exit the VM.
                m_fwk.waitForStop( 0 );
            }

            // remove debug bundles from cache
            if ( debug )
            {
                Main.cleanDebug();
            }

            System.exit( 0 );
        }
        catch ( Exception ex )
        {
            System.err.println( "Could not create framework: " + ex );
            ex.printStackTrace();
            System.exit( -1 );
        }
    }

    protected static Properties loadConfiguration()
    {
        // Load system properties.
        Main.loadSystemProperties();

        // Read configuration properties.
        Properties configProps = Main.loadConfigProperties();
        // If no configuration properties were found, then create
        // an empty properties object.
        if ( configProps == null )
        {
            System.err.println( "No " + CONFIG_PROPERTIES_FILE_VALUE + " found." );
            configProps = new Properties();
        }

        // Copy framework properties from the system properties.
        Main.copySystemProperties( configProps );

        return configProps;
    }

    /**
     * Force the framework to restart from scratch - reloading all configuration properties
     */
    static void restart()
    {
        restarting = true;
        stop();
    }

    /**
     * Force the framework to shut down and exit
     */
    static void stop()
    {
        try
        {
            m_fwk.stop();
        }
        catch ( Exception e )
        {
            System.err.println( "Unable to restart framework: " + e );
        }
    }

    /**
     * Simple method to parse META-INF/services file for framework factory.
     * Currently, it assumes the first non-commented line is the class name
     * of the framework factory implementation.
     * @return The created <tt>FrameworkFactory</tt> instance.
     * @throws Exception if any errors occur.
     */
    private static FrameworkFactory getFrameworkFactory() throws Exception
    {
        URL url = Main.class.getClassLoader().getResource(
            "META-INF/services/org.osgi.framework.launch.FrameworkFactory" );
        if ( url != null )
        {
            BufferedReader br = new BufferedReader( new InputStreamReader( url.openStream() ) );
            try
            {
                for ( String s = br.readLine(); s != null; s = br.readLine() )
                {
                    s = s.trim();
                    // Try to load first non-empty, non-commented line.
                    if ( ( s.length() > 0 ) && ( s.charAt(0) != '#' ) )
                    {
                        return (FrameworkFactory) Class.forName( s ).newInstance();
                    }
                }
            }
            finally
            {
                if ( br != null )
                {
                    br.close();
                }
            }
        }

        throw new Exception( "Could not find framework factory." );
    }

    /**
     * <p>
     * Loads the properties in the system property file associated with the
     * framework installation into <tt>System.setProperty()</tt>. These properties
     * are not directly used by the framework in anyway. By default, the system
     * property file is located in the <tt>conf/</tt> directory of the Felix
     * installation directory and is called "<tt>system.properties</tt>". The
     * installation directory of Felix is assumed to be the parent directory of
     * the <tt>felix.jar</tt> file as found on the system class path property.
     * The precise file from which to load system properties can be set by
     * initializing the "<tt>felix.system.properties</tt>" system property to an
     * arbitrary URL.
     * </p>
     */
    public static void loadSystemProperties()
    {
        // The system properties file is either specified by a system
        // property or it is in the same directory as the Felix JAR file.
        // Try to load it from one of these places.

        // See if the property URL was specified as a property.
        URL propURL = null;
        String custom = System.getProperty( SYSTEM_PROPERTIES_PROP );
        if ( custom != null )
        {
            try
            {
                propURL = new URL( custom );
            }
            catch ( MalformedURLException ex )
            {
                System.err.print( "Main: " + ex );
                return;
            }
        }
        else
        {
            // Determine where the configuration directory is by figuring
            // out where felix.jar is located on the system class path.
            File confDir = null;
            String classpath = System.getProperty( "java.class.path" );
            int index = classpath.toLowerCase().indexOf( "felix.jar" );
            int start = classpath.lastIndexOf( File.pathSeparator, index ) + 1;
            if ( index >= start )
            {
                // Get the path of the felix.jar file.
                String jarLocation = classpath.substring( start, index );
                // Calculate the conf directory based on the parent
                // directory of the felix.jar directory.
                confDir = new File( new File( new File( jarLocation ).getAbsolutePath() ).getParent(),
                    CONFIG_DIRECTORY );
            }
            else
            {
                // Can't figure it out so use the current directory as default.
                confDir = new File( System.getProperty( "user.dir" ), CONFIG_DIRECTORY );
            }

            try
            {
                propURL = new File( confDir, SYSTEM_PROPERTIES_FILE_VALUE ).toURI().toURL();
            }
            catch ( MalformedURLException ex )
            {
                System.err.print( "Main: " + ex );
                return;
            }
        }

        // Read the properties file.
        Properties props = new Properties();
        InputStream is = null;
        try
        {
            is = propURL.openConnection().getInputStream();
            props.load( is );
            is.close();
        }
        catch ( FileNotFoundException ex )
        {
            // Ignore file not found.
        }
        catch ( Exception ex )
        {
            System.err.println( "Main: Error loading system properties from " + propURL );
            System.err.println( "Main: " + ex );
            try
            {
                if ( is != null )
                {
                    is.close();
                }
            }
            catch ( IOException ex2 )
            {
                // Nothing we can do.
            }
            return;
        }

        // Perform variable substitution on specified properties.
        for ( Enumeration e = props.propertyNames(); e.hasMoreElements(); )
        {
            String name = (String) e.nextElement();
            System.setProperty( name, Util.substVars( props.getProperty( name ), name, null, null ) );
        }
    }

    /**
     * <p>
     * Loads the configuration properties in the configuration property file
     * associated with the framework installation; these properties
     * are accessible to the framework and to bundles and are intended
     * for configuration purposes. By default, the configuration property
     * file is located in the <tt>conf/</tt> directory of the Felix
     * installation directory and is called "<tt>config.properties</tt>".
     * The installation directory of Felix is assumed to be the parent
     * directory of the <tt>felix.jar</tt> file as found on the system class
     * path property. The precise file from which to load configuration
     * properties can be set by initializing the "<tt>felix.config.properties</tt>"
     * system property to an arbitrary URL.
     * </p>
     * @return A <tt>Properties</tt> instance or <tt>null</tt> if there was an error.
     */
    public static Properties loadConfigProperties()
    {
        // The config properties file is either specified by a system
        // property or it is in the conf/ directory of the Felix
        // installation directory.  Try to load it from one of these
        // places.

        // See if the property URL was specified as a property.
        URL propURL = null;
        String custom = System.getProperty( CONFIG_PROPERTIES_PROP );
        if ( custom != null )
        {
            try
            {
                propURL = new URL( custom );
            }
            catch ( MalformedURLException ex )
            {
                System.err.print( "Main: " + ex );
                return null;
            }
        }
        else
        {
            // Determine where the configuration directory is by figuring
            // out where felix.jar is located on the system class path.
            File confDir = null;
            String classpath = System.getProperty( "java.class.path" );
            int index = classpath.toLowerCase().indexOf( "felix.jar" );
            int start = classpath.lastIndexOf( File.pathSeparator, index ) + 1;
            if ( index >= start )
            {
                // Get the path of the felix.jar file.
                String jarLocation = classpath.substring( start, index );
                // Calculate the conf directory based on the parent
                // directory of the felix.jar directory.
                confDir = new File( new File( new File( jarLocation ).getAbsolutePath() ).getParent(),
                    CONFIG_DIRECTORY );
            }
            else
            {
                // Can't figure it out so use the current directory as default.
                confDir = new File( System.getProperty( "user.dir" ), CONFIG_DIRECTORY );
            }

            try
            {
                propURL = new File( confDir, CONFIG_PROPERTIES_FILE_VALUE ).toURI().toURL();
            }
            catch ( MalformedURLException ex )
            {
                System.err.print( "Main: " + ex );
                return null;
            }
        }

        // Read the properties file.
        Properties props = new Properties();
        InputStream is = null;
        try
        {
            // Try to load config.properties.
            is = propURL.openConnection().getInputStream();
            props.load( is );
            is.close();
        }
        catch ( Exception ex )
        {
            // Try to close input stream if we have one.
            try
            {
                if ( is != null )
                {
                    is.close();
                }
            }
            catch ( IOException ex2 )
            {
                // Nothing we can do.
            }

            return null;
        }

        // Perform variable substitution for system properties.
        for ( Enumeration e = props.propertyNames(); e.hasMoreElements(); )
        {
            String name = (String) e.nextElement();
            props.setProperty( name, Util.substVars( props.getProperty( name ), name, null, props ) );
        }

        return props;
    }

    public static void copySystemProperties( Properties configProps )
    {
        for ( Enumeration e = System.getProperties().propertyNames(); e.hasMoreElements(); )
        {
            String key = (String) e.nextElement();
            if ( key.startsWith( "felix." ) || key.startsWith( "org.osgi.framework." ) )
            {
                configProps.setProperty( key, System.getProperty( key ) );
            }
        }
    }

    /**
     * Get the list of debug bundles formatted for a felix configuration property
     *
     * @return the string representing a list of all bunfles needed for debugging
     */
    protected static String getDebugBundles()
    {
        StringBuffer ret = new StringBuffer();
        File debugs = new File( "debug" );
        for ( File bundle : debugs.listFiles() )
        {
            ret.append( " \"file:debug/" );
            ret.append( bundle.getName() );
            ret.append( "\"" );
        }

        return ret.toString();
    }

    /**
     * Cleanup after a debug running - this means removing any bundles from the cache that were loaded
     * from the debug area.
     */
    protected static void cleanDebug()
    {
        File cache = new File( "felix-cache" );
        for ( File bundle : cache.listFiles() )
        {
            if ( !bundle.isDirectory() )
            {
                continue;
            }

            File location = new File( bundle, "bundle.location" );
            if ( !location.exists() )
            {
                continue;
            }

            BufferedReader in = null;
            try
            {
                in = new BufferedReader( new FileReader( location ) );

                String jarFile = in.readLine();
                if ( jarFile.startsWith( "file:" ) )
                {
                    jarFile = jarFile.substring( 5 );
                }

                if ( new File( jarFile ).getParentFile().getName().equals( "debug" ) )
                {
                    Main.delete( bundle );
                }
            }
            catch ( IOException e )
            {
                e.printStackTrace();
            }
            finally
            {
                if ( in != null )
                {
                    try
                    {
                        in.close();
                    }
                    catch ( IOException e )
                    {
                        // ignore
                    }
                }
            }
        }
    }

    /**
     * Recursively delete a directory
     *
     * @param dir The directory to delete
     * @throws IOException thrown if any file cannot be deleted
     */
    public static void delete( File dir )
        throws IOException
    {
        if ( dir.isDirectory() )
        {
            for ( File file : dir.listFiles() )
            {
                delete( file );
            }
        }

        if ( !dir.delete() )
        {
            throw new IOException( "Unable to delete" );
        }
    }
}
