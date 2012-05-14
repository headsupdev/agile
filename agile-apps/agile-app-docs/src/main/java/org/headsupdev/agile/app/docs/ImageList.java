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

package org.headsupdev.agile.app.docs;

import org.headsupdev.agile.api.mime.Mime;
import org.apache.wicket.markup.html.WebResource;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringBufferResourceStream;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.Query;

import java.util.Iterator;

import org.headsupdev.agile.web.MountPoint;
import org.headsupdev.agile.api.Storage;
import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.storage.docs.Document;
import org.headsupdev.agile.storage.Attachment;
import org.headsupdev.agile.storage.HibernateStorage;

/**
 * A resource to return the list of images attached to a page - used for the TinyMCE plugin.
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@MountPoint( "imagelist" )
public class ImageList
    extends WebResource
{
    private StringBufferResourceStream resource = new StringBufferResourceStream( "text/plain" );

    @Override
    public IResourceStream getResourceStream() {
        resource.clear();
        resource.append( "var " );
        resource.append( getArrayName() );
        resource.append( " = new Array(\n" );

        String projectStr = getParameters().getString( "project" );
        String docStr = getParameters().getString( "page" );

        if ( projectStr == null || projectStr.length() == 0 || docStr == null || docStr.length() == 0 )
        {
            return resource;
        }

        Storage storage = Manager.getStorageInstance();
        Project project = storage.getProject( projectStr );
        if ( project != null )
        {
            Document doc = getDocument( docStr, project );
            if ( doc != null )
            {
                Iterator<Attachment> attIter = doc.getAttachments().iterator();

                while ( attIter.hasNext() )
                {
                    Attachment attachment = attIter.next();
                    String filename = attachment.getFilename();

                    if ( !acceptFile( filename ) )
                    {
                        continue;
                    }

                    resource.append( "[\"" + filename + "\", \"" + getFileUrl( filename, project, doc ) + "\"]" );
                    if ( attIter.hasNext() )
                    {
                        resource.append( "," );
                    }
                    resource.append( "\n" );
                }
            }
        }

        resource.append( ");\n" );
        return resource;
    }

    protected String getArrayName()
    {
        return "tinyMCEImageList";
    }

    protected boolean acceptFile( String filename )
    {
        Mime mime = Mime.get( filename );

        return mime.isEmbeddableImage();
    }

    protected String getFileUrl( String filename, Project project, Document doc )
    {
        return "/" + project.getId() + "/docs/figure/page/" + doc.getName() + "/file/" + filename;
    }

    /* don't cache for long - we need to invalidate this ideally when a doc changes... */
    @Override
    protected int getCacheDuration()
    {
        return 10;
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
}
