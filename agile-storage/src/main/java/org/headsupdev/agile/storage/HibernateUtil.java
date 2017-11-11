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

package org.headsupdev.agile.storage;

import org.headsupdev.agile.api.Application;
import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.storage.hibernate.IdProjectId;
import org.headsupdev.agile.storage.docs.Document;
import org.headsupdev.agile.storage.issues.Issue;
import org.headsupdev.agile.storage.issues.Milestone;
import org.headsupdev.agile.storage.ci.Build;

import org.hibernate.*;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.connection.DBCPConnectionProvider;
import org.hibernate.cfg.AnnotationConfiguration;

import java.util.*;
import java.net.URL;
import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.lang.reflect.*;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

/**
 * Utility for working with Hibernate.
 * We have some funky classloader manipulation to allow hibernate to see all the applications.
 * A little twisted, but at this time it is needed as hibernate just uses the thread context classloader...
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class HibernateUtil {
    public static Properties properties = new Properties();
    private static HibernateThread thread;

    private static Map<Session, Exception> sessions = new HashMap<Session, Exception>();

    public static Map<Session, Exception> getOpenSessions()
    {
        return sessions;
    }

    public static void applicationAdded( Application application )
    {
        initThread();
        thread.applicationAdded( application );
    }

    public static Session getCurrentSession() {
        initThread();
        return thread.getCurrentSession();
    }

    public static org.hibernate.classic.Session openSession() {
        initThread();
        return thread.openSession();
    }

    public static void shutdown() {
        initThread();
        thread.shutdown();
    }

    public static List<String> getSearchFields()
    {
        return thread.getSearchFields();
    }

    public static DatabasePoolStatistics getStatistics()
    {
        return thread.getStatistics();
    }

    public static List<String> getEntityClassNames()
    {
        return thread.getEntityClassNames();
    }

    private static void initThread()
    {
        if ( thread == null )
        {
            thread = new HibernateThread( HibernateUtil.class.getClassLoader() );
        }
    }

    public static class SessionProxyImpl
            implements InvocationHandler
    {
        private final org.hibernate.classic.Session session;

        public static org.hibernate.classic.Session newInstance( org.hibernate.classic.Session session, boolean trace )
        {
            Class[] ifaces = session.getClass().getInterfaces();

            Set<Class> ifacesSet = new HashSet<Class>( Arrays.asList( ifaces ) );
            ifacesSet.add( SessionProxy.class );

            Class[] interfaces = new Class[ifacesSet.size()];
            ifacesSet.toArray( interfaces );

            if ( trace )
            {
                sessions.put( session, new Exception() );
            }
            return (org.hibernate.classic.Session) Proxy.newProxyInstance( session.getClass().getClassLoader(),
                    interfaces, new HibernateUtil.SessionProxyImpl( session ) );
        }

        public SessionProxyImpl( org.hibernate.classic.Session session )
        {
            this.session = session;
        }

        public Object invoke( Object o, Method method, Object[] args )
                throws Throwable
        {
            if ( method.getName().equals("getRealSession") ) {
                return session;
            }
            if ( method.getName().equals( "save" ) || method.getName().equals( "saveOrUpdate" ) )
            {
                Object saving = args[ args.length - 1 ];
                try
                {
                    // will have a length of 1 or 2 for save
                    Field field = saving.getClass().getDeclaredField( "id" );
                    field.setAccessible( true );

                    if ( field.get( saving ) instanceof IdProjectId )
                    {
                        IdProjectId id = (IdProjectId) field.get( saving );

                        Field idId = id.getClass().getDeclaredField( "id" );
                        idId.setAccessible( true );
                        if ( idId.getLong( id ) == 0 )
                        {
                            Field projectField = id.getClass().getDeclaredField( "project" );
                            projectField.setAccessible( true );
                            Project project = (Project) projectField.get( id );

                            synchronized( session )
                            {
                                long max = 0;
                                try
                                {
                                    max = (Long) session.createQuery( "select max(id.id) from " +
                                            saving.getClass().getName() + " where id.project.id = '" + project.getId() + "'" )
                                            .uniqueResult();
                                }
                                catch ( NullPointerException e )
                                {
                                    // thrown from hibernate when there is no id listed for the project (i.e. first time)
                                    // just use a max of 0 as above
                                }

                                idId.setLong( id, max + 1 );
                                // duplicate the method call here so we save in the syncronized block
                                // the session we are synced on has a transaction open already, so the SQL is fine too
                                return method.invoke( session, args );
                            }
                        }
                    }
                }
                catch ( NoSuchFieldException e )
                {
                    // ignore and fall through
                }
            }
            else if ( method.getName().equals( "close" ) )
            {
                //noinspection ThrowableResultOfMethodCallIgnored
                sessions.remove( session );
            }

            return method.invoke( session, args );
        }
    }
}

class HibernateThread extends Thread
{
    private SessionFactory sessionFactory, managedSessionFactory;
    private DBCPConnectionProvider provider;

    private HibernateClassLoader loader;

    private Vector<Class> annotated = new Vector<Class>();
    private List<String> searchFields = new LinkedList<String>();
    private List<String> classNames = new LinkedList<String>();

    public HibernateThread( ClassLoader core )
    {
        loader = new HibernateClassLoader( core );
        initFactory( new AnnotationConfiguration() );

        addSearchFields( new Class[] { StoredProject.class, StoredMavenTwoProject.class, StoredFileProject.class,
                StoredUser.class, Document.class, Issue.class, Milestone.class, Build.class, ScmChangeSet.class,
                TransactionalScmChangeSet.class, org.headsupdev.agile.storage.files.File.class } );
    }

    public void initFactory( AnnotationConfiguration config )
    {
        Thread.currentThread().setContextClassLoader( loader );

        config.setProperty( "hibernate.dialect", (String) HibernateUtil.properties.get( "headsup.db.dialect" ) );
        config.setProperty( "hibernate.connection.driver_class", (String) HibernateUtil.properties.get( "headsup.db.driver" ) );
        config.setProperty( "hibernate.connection.url", (String) HibernateUtil.properties.get( "headsup.db.url" ) );
        config.setProperty( "hibernate.connection.username", (String) HibernateUtil.properties.get( "headsup.db.username" ) );
        config.setProperty( "hibernate.connection.password", (String) HibernateUtil.properties.get( "headsup.db.password" ) );

        // we cannot find out this property until we have loaded the initial context.
        // Thankfully this will run a few times so we will get there in the end
        if ( sessionFactory != null ) {
            File dataDir = Manager.getStorageInstance().getDataDirectory();
            config.setProperty( "hibernate.search.default.indexBase", new File( dataDir, "index" ).getAbsolutePath() );
        }

        try
        {
            // here we parse the configuration ourselves so we can turn off validation...
            config.configure( parseConfiguration( "/hibernate.cfg.xml" ) );
        }
        catch ( Exception e )
        {
            System.err.println( "Unable to load hibernate configuration" );
            e.printStackTrace();
        }

        sessionFactory = config.buildSessionFactory();
        provider = (DBCPConnectionProvider) ( (SessionFactoryImplementor) sessionFactory ).getConnectionProvider();

        classNames.clear();
        Iterator iter = config.getClassMappings();
        while ( iter.hasNext()) {
            Object o = iter.next();
            if ( o instanceof PersistentClass) {
                classNames.add( ( (PersistentClass) o ).getEntityName() );
            } else {
                System.out.println( "Unrecognised persistent object " + o );
            }
        }
    }

    /**
     * Parse a document with validation switched off and the loading of external dtds disabled
     *
     * @param resourcePath The path to a resource on the classpath
     * @return a parsed document, without validation
     * @throws Exception if there was a problem parsing the file
     */
    public static org.w3c.dom.Document parseConfiguration( String resourcePath )
        throws Exception
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating( false ); // Don't validate DTD
        factory.setAttribute( "http://xml.org/sax/features/validation", Boolean.FALSE );
        factory.setAttribute("http://apache.org/xml/features/nonvalidating/load-external-dtd", Boolean.FALSE );

        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse( builder.getClass().getResourceAsStream( resourcePath ) );
    }

    public void applicationAdded( Application application )
    {
        loader.addClassLoader( application.getClass().getClassLoader() );
        AnnotationConfiguration config = new AnnotationConfiguration();

        Class[] annotations = application.getPersistantClasses();
        addSearchFields( annotations );

        Enumeration<Class> annotationIter = annotated.elements();
        while ( annotationIter.hasMoreElements() )
        {
            Class annotation = annotationIter.nextElement();
            config.addAnnotatedClass( annotation );

            if ( annotation.isAnnotationPresent( Indexed.class ) ) {
                String indexName = ( (Indexed) annotation.getAnnotation( Indexed.class ) ).index();
                if ( indexName == null || indexName.length() == 0 )
                {
                    indexName = annotation.getName();
                }
                config.setProperty( "hibernate.search." + indexName + ".directory_provider",
                    "org.hibernate.search.store.FSDirectoryProvider" );
            }
        }
        initFactory( config );
    }

    private void addSearchFields( Class[] classes ) {
        for ( Class annotation : classes )
        {
            annotated.add( annotation );

            if ( !annotation.isAnnotationPresent( Indexed.class ) )
            {
                continue;
            }

            Class myClass = annotation;
            while ( myClass != null )
            {
                for ( Field field : myClass.getDeclaredFields() )
                {
                    if ( field.isAnnotationPresent( org.hibernate.search.annotations.Field.class ) )
                    {
                        if ( !searchFields.contains( field.getName() ) )
                        {
                            searchFields.add( field.getName() );
                        }
                    }
                    if ( field.isAnnotationPresent( org.hibernate.search.annotations.IndexedEmbedded.class ) )
                    {
                        // TODO think about recursing
                        // this annotation provides an override or replacement to generics
                        Class subClass = field.getAnnotation( org.hibernate.search.annotations.IndexedEmbedded.class ).targetElement();
                        if ( subClass == null || subClass.equals( void.class ) )
                        {
                            // handle collections with generics
                            if ( field.getGenericType() instanceof ParameterizedType )
                            {
                                ParameterizedType type = (ParameterizedType)field.getGenericType();
                                subClass = (Class) type.getActualTypeArguments()[0];
                            }
                            else
                            {
                                subClass = field.getType();
                            }
                        }

                        for ( Field subField : subClass.getDeclaredFields() )
                        {
                            if ( subField.isAnnotationPresent( org.hibernate.search.annotations.Field.class ) )
                            {
                                String fullName = field.getName() + "." + subField.getName();
                                if ( !searchFields.contains( fullName ) )
                                {
                                    searchFields.add( fullName );
                                }
                            }
                        }
                    }
                }

                myClass = myClass.getSuperclass();
            }
        }
    }

    public org.hibernate.classic.Session getCurrentSession() {
        return HibernateUtil.SessionProxyImpl.newInstance( sessionFactory.getCurrentSession(), false );
    }

    public org.hibernate.classic.Session openSession() {
        return HibernateUtil.SessionProxyImpl.newInstance( sessionFactory.openSession(), true );
    }

    public void shutdown() {
        // Close caches and connection pools
        sessionFactory.close();
        provider.close();
    }

    public List<String> getSearchFields()
    {
        return searchFields;
    }

    public DatabasePoolStatistics getStatistics()
    {
        return provider.getStatistics();
    }

    public List<String> getEntityClassNames()
    {
        return classNames;
    }

    class HibernateClassLoader extends ClassLoader
    {
        private Vector<ClassLoader> loaders = new Vector<ClassLoader>();

        public HibernateClassLoader( ClassLoader core )
        {
            super( core );
        }

        public void addClassLoader( ClassLoader child )
        {
            loaders.add( child );
        }

        public Class<?> loadClass( String name )
            throws ClassNotFoundException
        {
            Enumeration<ClassLoader> loaderIter = loaders.elements();
            while ( loaderIter.hasMoreElements() )
            {
                ClassLoader loader = loaderIter.nextElement();
                try
                {
                    return loader.loadClass( name );
                }
                catch ( ClassNotFoundException e )
                {
                    // continue
                }
            }

            return super.loadClass( name );
        }

        public URL getResource( String name )
        {
            Enumeration<ClassLoader> loaderIter = loaders.elements();
            while ( loaderIter.hasMoreElements() )
            {
                ClassLoader loader = loaderIter.nextElement();
                URL ret = loader.getResource( name );

                if ( ret != null )
                {
                    return ret;
                }
            }

            return super.getResource( name );
        }

        public Enumeration<URL> getResources( String name )
            throws IOException
        {
            Vector<URL> resources = new Vector<URL>();

            Enumeration<ClassLoader> loaderIter = loaders.elements();
            while ( loaderIter.hasMoreElements() )
            {
                ClassLoader loader = loaderIter.nextElement();
                Enumeration<URL> ret = loader.getResources( name );

                while ( ret.hasMoreElements() )
                {
                    resources.add( ret.nextElement() );
                }
            }

            Enumeration<URL> ret = super.getResources( name );
            while ( ret.hasMoreElements() )
            {
                resources.add( ret.nextElement() );
            }

            return resources.elements();
        }

        public InputStream getResourceAsStream( String name )
        {
            Enumeration<ClassLoader> loaderIter = loaders.elements();
            while ( loaderIter.hasMoreElements() )
            {
                ClassLoader loader = loaderIter.nextElement();
                InputStream ret = loader.getResourceAsStream( name );

                if ( ret != null )
                {
                    return ret;
                }
            }

            return super.getResourceAsStream( name );
        }

        public String toString()
        {
            StringBuffer ret = new StringBuffer( super.toString() );
            ret.append( " children: " );

            Enumeration<ClassLoader> loaderIter = loaders.elements();
            while ( loaderIter.hasMoreElements() )
            {
                ClassLoader loader = loaderIter.nextElement();
                ret.append( loader.toString() );

                if ( loaderIter.hasMoreElements() )
                {
                    ret.append( ", " );
                }
            }

            return ret.toString();
        }
    }
}