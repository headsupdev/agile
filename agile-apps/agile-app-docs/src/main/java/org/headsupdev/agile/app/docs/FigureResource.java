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

package org.headsupdev.agile.app.docs;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.Query;

import java.io.FileInputStream;
import java.io.File;
import java.io.IOException;

import org.headsupdev.agile.web.CachedImageResource;
import org.headsupdev.agile.web.MountPoint;
import org.headsupdev.agile.api.Storage;
import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.storage.docs.Document;
import org.headsupdev.agile.storage.Attachment;
import org.headsupdev.agile.storage.HibernateStorage;

/**
 * A resource to return the contents of an attached image.
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@MountPoint( "figure" )
public class FigureResource
    extends CachedImageResource
{
    public FigureResource()
    {
        setFormat( "application/octet-stream" );
    }

    protected byte[] getImageData()
    {
        String projectStr = getParameters().getString( "project" );
        String docStr = getParameters().getString( "page" );
        String fileName = getParameters().getString( "file" );

        if ( projectStr == null || projectStr.length() == 0 || docStr == null || docStr.length() == 0 ||
            fileName == null || fileName.length() == 0)
        {
            return new byte[0];
        }

        Storage storage = Manager.getStorageInstance();
        Project project = storage.getProject( projectStr );
        if ( project == null )
        {
            return new byte[0];
        }
        Document doc = getDocument( docStr, project );
        if ( doc == null )
        {
            return new byte[0];
        }

        Attachment attachment = null;
        for ( Attachment a : doc.getAttachments() )
        {
            if ( a.getFilename().equals( fileName ) )
            {
                attachment = a;
            }
        }
        if ( attachment == null )
        {
            return new byte[0];
        }

        FileInputStream in = null;
        try
        {
            File file = attachment.getFile( storage );

            if ( file == null || file.length() > Integer.MAX_VALUE )
            {
                return new byte[0];
            }
            int length = (int) file.length();
            byte[] ret = new byte[length];
            in = new FileInputStream( file );

            int off = 0;
            while ( off < length - 1 )
            {
                off += in.read( ret, off, length - off );
            }

            return ret;
        }
        catch ( IOException e )
        {
            Manager.getLogger( getClass().getName() ).error( "Error loading figure image", e );
            return new byte[0];
        }
        finally
        {
            try
            {
                if ( in != null )
                {
                    in.close();
                }
            }
            catch ( IOException e )
            {
                // ignore
            }
        }
    }

    public Document getDocument( String name, Project project )
    {
        Session session = ( (HibernateStorage) Manager.getStorageInstance() ).getHibernateSession();

        Transaction tx = session.beginTransaction();
        Query q = session.createQuery( "from Document d where name.name = :name and name.project = :project" );
        q.setEntity( "project", project );
        q.setString( "name", name );
        Document ret = (Document) q.uniqueResult();
        tx.commit();

        return ret;
    }

    public long getExpireTimeout()
    {
        return 1000 * 60 * 10;
    }

    // these are not actually used
    protected int getWidth()
    {
        return 0;
    }

    protected int getHeight()
    {
        return 0;
    }
}
