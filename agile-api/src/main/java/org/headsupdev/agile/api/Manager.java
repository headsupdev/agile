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

package org.headsupdev.agile.api;

import org.headsupdev.agile.api.logging.Logger;
import org.headsupdev.agile.api.service.ScmService;

import java.util.List;
import java.util.Map;
import java.util.Date;
import java.io.File;

/**
 * Interface for the heart of HeadsUp control.
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public abstract class Manager
{
    static Manager instance;
    static SecurityManager securityInstance;
    static Storage storageInstance;

    public static Manager getInstance()
    {
        return instance;
    }

    public static void setInstance( Manager manager )
    {
        Manager.instance = manager;
    }

    public static SecurityManager getSecurityInstance()
    {
        return securityInstance;
    }

    public static void setSecurityInstance( SecurityManager securityManager )
    {
        Manager.securityInstance = securityManager;
    }

    public static Storage getStorageInstance()
    {
        return storageInstance;
    }

    public static void setStorageInstance( Storage storage )
    {
        Manager.storageInstance = storage;
    }

    public static Logger getLogger( String component )
    {
        if ( instance == null )
        {
            return null;
        }
        return getInstance().getLoggerForComponent( component );
    }

    public abstract void addProjectListener( ProjectListener listener );
    public abstract void removeProjectListener( ProjectListener listener );

    public abstract void fireProjectAdded( Project project );
    public abstract void fireProjectModified( Project project );
    public abstract void fireProjectFileModified( Project project, String path, File file );

    public abstract void fireEventAdded( Event event );

    public abstract Map<String, LinkProvider> getLinkProviders();
    
    public abstract List<Task> getTasks();
    public abstract void addTask( Task task );
    public abstract void removeTask( Task task );

    public abstract Date getInstallDate();
    public abstract double getInstallVersion();

    public abstract boolean isUpdateAvailable();
    protected abstract Logger getLoggerForComponent( String component );

    /**
     * @since 2.0
     * @return A loaded ScmService that can bse used for querying version control.
     */
    public abstract ScmService getScmService();
}
