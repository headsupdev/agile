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

import org.headsupdev.agile.api.Manager;

/**
 * A special type of thread that cleans up database sessions before its run method exits.
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class StorageThread extends Thread
{
    public final void run()
    {
        // any storage init code
        HibernateStorage storage = (HibernateStorage) Manager.getStorageInstance();

        runWithSession();

        // any storage tidying code
        storage.closeSession();
    }

    public void runWithSession()
    {
        // implementation here
    }
}
