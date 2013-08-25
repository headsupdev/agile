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
 * A generic DAO based on the hibernate database sessions.
 * <p/>
 * Created: 07/05/2013
 *
 * @author Andrew Williams
 * @since 2.0
 */
public abstract class HibernateDAO<T, U extends Serializable> implements Serializable
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
