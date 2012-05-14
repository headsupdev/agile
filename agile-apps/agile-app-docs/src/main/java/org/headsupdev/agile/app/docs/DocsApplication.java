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

import org.headsupdev.agile.api.*;
import org.headsupdev.agile.app.docs.event.CreateDocumentEvent;
import org.headsupdev.agile.app.docs.event.UpdateDocumentEvent;
import org.headsupdev.agile.app.docs.permission.DocViewPermission;
import org.headsupdev.agile.app.docs.permission.DocListPermission;
import org.headsupdev.agile.app.docs.permission.DocEditPermission;
import org.headsupdev.agile.storage.Comment;
import org.headsupdev.agile.web.WebApplication;
import org.headsupdev.agile.storage.HibernateStorage;
import org.headsupdev.agile.storage.docs.Document;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.Query;
import org.osgi.framework.BundleContext;

import java.util.List;
import java.util.LinkedList;

/**
 * The application descriptor for documents
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class DocsApplication
    extends WebApplication
{
    public static final String ID = "docs";

    List<MenuLink> links;
    List<String> eventTypes;

    public DocsApplication()
    {
        links = new LinkedList<MenuLink>();
        links.add( new SimpleMenuLink( "contents" ) );

        eventTypes = new LinkedList<String>();
        eventTypes.add( "createdocument" );
        eventTypes.add( "updatedocument" );
    }

    public String getName()
    {
        return "Documents";
    }

    public String getApplicationId()
    {
        return ID;
    }

    public String getDescription()
    {
        return "The " + HeadsUpConfiguration.getProductName() + " documents application";
    }

    public List<MenuLink> getLinks()
    {
        return links;
    }

    public List<String> getEventTypes() {
        return eventTypes;
    }

    @Override
    public Class[] getResources() {
        return new Class[]{ FigureResource.class, ImageList.class, LinkList.class };
    }

    @Override
    public Class<? extends Page> getHomePage()
    {
        return View.class;
    }

    @Override
    public Class<? extends Page>[] getPages()
    {
        return new Class[]{ CreateAttachment.class, CreateComment.class, Edit.class, Index.class, MavenSite.class,
            View.class };
    }

    @Override
    public Permission[] getPermissions()
    {
        return new Permission[]{ new DocEditPermission(), new DocListPermission(), new DocViewPermission() };
    }

    @Override
    public LinkProvider[] getLinkProviders()
    {
        return new LinkProvider[]{ new DocLinkProvider() };
    }

    public List<Document> getDocuments( Project project )
    {
        Session session = ( (HibernateStorage) Manager.getStorageInstance() ).getHibernateSession();

        Query q;
        if ( project == null )
        {
            q = session.createQuery( "from Document d where name.project is null" );
        }
        else
        {
            q = session.createQuery( "from Document d where name.project = :project" );
            q.setEntity( "project", project );
        }
        return q.list();
    }

    public static Document getDocument( String name, Project project )
    {
        Session session = ( (HibernateStorage) Manager.getStorageInstance() ).getHibernateSession();

        Transaction tx = session.beginTransaction();
        Query q;
        if ( project == null )
        {
            q = session.createQuery( "from Document d where name.name = :name and name.project is null" );
        }
        else
        {
            q = session.createQuery( "from Document d where name.name = :name and name.project = :project" );
            q.setEntity( "project", project );
        }
        q.setString( "name", name );
        Document ret = (Document) q.uniqueResult();
        tx.commit();

        return ret;
    }

    public void addDocument( Document doc )
    {
        Session session = ( (HibernateStorage) Manager.getStorageInstance() ).getHibernateSession();

        Transaction tx = session.beginTransaction();
        session.save( doc );
        tx.commit();
    }

    public static Comment getComment( long id )
    {
        Session session = ( (HibernateStorage) Manager.getStorageInstance() ).getHibernateSession();

        Query q = session.createQuery( "from Comment c where id.id = :id" );
        q.setLong( "id", id );
        return (Comment) q.uniqueResult();
    }

    public Class[] getPersistantClasses() {
        return new Class[] { CreateDocumentEvent.class, UpdateDocumentEvent.class };
    }

    @Override
    public void start( BundleContext bc )
    {
        super.start( bc );
    }
}