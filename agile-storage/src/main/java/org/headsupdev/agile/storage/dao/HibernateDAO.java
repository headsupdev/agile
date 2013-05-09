package org.headsupdev.agile.storage.dao;

import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.storage.HibernateStorage;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import java.io.Serializable;
import java.util.List;

/**
 * TODO: Document Me 
 * <p/>
 * Created: 07/05/2013
 *
 * @author Andrew Williams
 * @since 1.0
 */
public abstract class HibernateDAO<T, U extends Serializable>
{
    protected abstract Class<T> getPersistentClass();
    protected Class<T> persistentClass = getPersistentClass();

    public T find( U id, Project project )
    {
        Query q;
        if ( id instanceof Long )
        {
            q = getSession().createQuery( "from " + persistentClass.getSimpleName() + " o where o.id.id = :id and o.id.project.id = :pid" );
            q.setLong( "id", (Long) id );
        }
        else
        {
            q = getSession().createQuery( "from " + persistentClass.getSimpleName() + " o where o.id.name = :name and o.id.project.id = :pid" );
            q.setString( "name", id.toString() );
        }
        q.setString( "pid", project.getId() );

        return (T) q.uniqueResult();
    }

    public List<T> findAll()
    {
        return search( null );
    }

    public List<T> findAll( Project project )
    {
        return search( null, project );
    }

    public void save( T object )
    {
        getSession().saveOrUpdate( object );
    }

    public void remove( T object )
    {
        getSession().delete( object );
    }

    public void removeById( U id, Project project )
    {
        getSession().delete( find( id, project ) );
    }

    public List<T> search( Criterion search )
    {
        Criteria c = getSession().createCriteria( persistentClass );
        c.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );

        if ( search != null )
        {
            c.add( search );
        }
        return (List<T>) c.list();
    }

    public List<T> search( Criterion search, Project project )
    {
        Criteria c = getSession().createCriteria( persistentClass );
        c.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );
        c.add( Restrictions.eq( "id.project", project ) );

        if ( search != null )
        {
            c.add( search );
        }
        return (List<T>) c.list();
    }

    public int count( Criterion search )
    {
        Criteria c = getSession().createCriteria( persistentClass );
        c.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );

        if ( search != null )
        {
            c.add( search );
        }
        return c.list().size();
    }

    public int count( Criterion search, Project project )
    {
        Criteria c = getSession().createCriteria( persistentClass );
        c.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );
        c.add( Restrictions.eq( "id.project", project ) );

        if ( search != null )
        {
            c.add( search );
        }
        return c.list().size();
    }

//    SearchResult<T> searchAndCount( Criteria search )
//    {
//
//    }
//    list = result.getResult();
//    count = result.getTotalCount();

    // hibernate stuff
    public Session getSession()
    {
        return ( (HibernateStorage) Manager.getStorageInstance() ).getHibernateSession();
    }
}
