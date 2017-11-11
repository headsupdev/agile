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

package org.headsupdev.agile.app.docs.event;

import org.headsupdev.agile.web.AbstractEvent;
import org.headsupdev.agile.api.User;
import org.headsupdev.agile.storage.docs.Document;
import org.headsupdev.agile.app.docs.View;
import org.headsupdev.agile.app.docs.DocsApplication;

import javax.persistence.Entity;
import javax.persistence.DiscriminatorValue;
import java.util.Date;

/**
 * Event added when a document is created
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@Entity
@DiscriminatorValue( "createdocument" )
public class CreateDocumentEvent
    extends AbstractEvent
{
    CreateDocumentEvent()
    {
    }

    public CreateDocumentEvent( Document doc, String content )
    {
        this( doc, content, "Document \"" + doc.getName() + "\" created by " + doc.getCreator().getFullnameOrUsername(),
            doc.getCreated(), doc.getCreator() );
    }

    protected CreateDocumentEvent( Document doc, String content, String title, Date date, User user )
    {
        super( title, content, date );

        setApplicationId( DocsApplication.ID );
        setProject( doc.getProject() );
        setUser( user );
        setObjectId( doc.getName() );
    }

    public String getBody()
    {
        Document doc = DocsApplication.getDocument( getObjectId(), getProject() );

        if ( doc == null )
        {
            return "<p>Unable to find document requested.</p>";
        }

        return "<div wicket:id=\"content\">\n" + View.getContent( doc ) + "</div>";
    }

    @Override
    public boolean shouldNotify( User user )
    {
        Document doc = DocsApplication.getDocument( getObjectId(), getProject() );

        return doc.getWatchers().contains( user );
    }
}