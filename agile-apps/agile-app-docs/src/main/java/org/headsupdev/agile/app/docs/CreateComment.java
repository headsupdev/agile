/*
 * HeadsUp Agile
 * Copyright 2009-2014 Heads Up Development.
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

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.form.Form;
import org.headsupdev.agile.api.Event;
import org.headsupdev.agile.api.MenuLink;
import org.headsupdev.agile.api.Page;
import org.headsupdev.agile.api.Permission;
import org.headsupdev.agile.app.docs.event.UpdateDocumentEvent;
import org.headsupdev.agile.app.docs.permission.DocEditPermission;
import org.headsupdev.agile.storage.Comment;
import org.headsupdev.agile.storage.docs.Document;
import org.headsupdev.agile.web.BookmarkableMenuLink;
import org.headsupdev.agile.web.MountPoint;
import org.headsupdev.agile.web.SubmitChildException;
import org.headsupdev.agile.web.components.AbstractCreateComment;
import org.headsupdev.agile.web.components.Subheader;

/**
 * Add a comment for a document
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@MountPoint("comment")
public class CreateComment
        extends AbstractCreateComment<Document>
{
    @Override
    protected Subheader<Document> getSubheader()
    {
        String preamble = submitLabel + " for Document " + commentable.getName();
        return new Subheader<Document>( "subHeader", preamble, commentable );
    }

    @Override
    public Permission getRequiredPermission()
    {
        return new DocEditPermission();
    }

    @Override
    protected Document getObject()
    {
        String page = getPageParameters().getString( "page" );
        if ( page == null || page.length() == 0 )
        {
            page = "Welcome";
        }

        return DocsApplication.getDocument( page, getProject() );
    }

    @Override
    protected Event getUpdateEvent( Comment comment )
    {
        return new UpdateDocumentEvent( commentable, getSession().getUser(), comment, "commented on" );
    }

    @Override
    protected MenuLink getViewLink()
    {
        return new BookmarkableMenuLink( getPageClass( "docs/" ), getPageParameters(), "view" );
    }

    @Override
    protected void layoutChild( Form form )
    {
    }

    @Override
    protected void submitChild( Comment comment )
            throws SubmitChildException
    {
    }

    @Override
    protected PageParameters getSubmitPageParameters()
    {
        return getPageParameters();
    }

    @Override
    protected Class<? extends Page> getSubmitPageClass()
    {
        return getPageClass( "docs/" );
    }
}