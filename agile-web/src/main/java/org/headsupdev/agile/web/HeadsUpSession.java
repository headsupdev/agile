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

package org.headsupdev.agile.web;

import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.core.PrivateConfiguration;
import org.headsupdev.agile.storage.HibernateUtil;
import org.apache.wicket.Request;
import org.apache.wicket.PageParameters;
import org.apache.wicket.protocol.http.WebSession;
import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.api.User;
import org.headsupdev.agile.storage.StoredUser;

import java.util.TimeZone;

/**
 * A simple wicket session used for keeping track of session-wide variables, i.e. the currently selected project
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class HeadsUpSession
    extends WebSession
{
    // the anonymous use has no role, as the anonymous role is assumed if permission checks fail
    public static final StoredUser ANONYMOUS_USER = new StoredUser( "anonymous" );

    private Project current;
    private Class previousPageClass;
    private PageParameters previousPageParameters;

    private User user = ANONYMOUS_USER;

    public HeadsUpSession( Request request )
    {
        super( request );
    }

    public Project getProject()
    {
        return current;
    }

    public void setProject( Project current )
    {
        this.current = current;
    }

    public User getUser()
    {
        if ( !PrivateConfiguration.isInstalled() )
        {
            return null;
        }
        return (User) HibernateUtil.getCurrentSession().load( StoredUser.class, user.getUsername() );
    }

    public void setUser( User user )
    {
        if ( user == null )
        {
            this.user = ANONYMOUS_USER;
        }
        else
        {
            this.user = user;
        }
    }

    public Class getPreviousPageClass()
    {
        return previousPageClass;
    }

    public void setPreviousPageClass( Class previousPageClass )
    {
        this.previousPageClass = previousPageClass;
    }

    public PageParameters getPreviousPageParameters()
    {
        return previousPageParameters;
    }

    public void setPreviousPageParameters( PageParameters previousPageParameters )
    {
        this.previousPageParameters = previousPageParameters;
    }

    public TimeZone getTimeZone()
    {
        if ( getUser() == null ) {
            return Manager.getStorageInstance().getGlobalConfiguration().getDefaultTimeZone();
        }

        return getUser().getTimeZone();
    }
}
