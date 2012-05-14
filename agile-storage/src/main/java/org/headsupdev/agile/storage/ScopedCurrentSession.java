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

package org.headsupdev.agile.storage;

import org.hibernate.HibernateException;
import org.hibernate.classic.Session;
import org.hibernate.context.CurrentSessionContext;
import org.hibernate.engine.SessionFactoryImplementor;

import java.util.Map;

/**
 * A session manager that understands scopes (requests) and falls back to the default threadlocal...
 *
 * @author Andrew Williams
 * @since 1.0
 */
public class ScopedCurrentSession implements CurrentSessionContext {
//    private ThreadLocalSessionContext threadLocalFallback;
    private ThreadLocal<Session> threadSession = new ThreadLocal<Session>();
    private SessionFactoryImplementor factory; 

    public ScopedCurrentSession( SessionFactoryImplementor factory ) {
        this.factory = factory;
//        threadLocalFallback = new ThreadLocalSessionContext( factory );
    }

    public Session currentSession() throws HibernateException {
        if ( HibernateStorage.getCurrentScope() == null )
        {
            return getThreadLocalSession();
        }

        return getManagedSession();
    }

    protected Session getManagedSession()
    {
        org.hibernate.classic.Session s;

        Map<Object, Session> sessions = HibernateStorage.getManagedSessions();
        synchronized (sessions)
        {
            s = sessions.get( HibernateStorage.getCurrentScope() );
            if ( s == null || !s.isOpen() )
            {
                s = factory.openSession();
                HibernateStorage.getManagedSessions().put(HibernateStorage.getCurrentScope(), s);
            }
        }

        return s;
    }

    protected Session getThreadLocalSession()
    {
        // TODO maybe add some clever system where we expire old sessions?
        Session session = threadSession.get();
        if ( session == null || !session.isOpen() )
        {
            session = HibernateUtil.openSession();
            threadSession.set( session );
        }
        return session;

//        return threadLocalFallback.currentSession();
    }
}
