/*
 * HeadsUp Agile
 * Copyright 2009-2014 Heads Up Development Ltd.
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

import org.headsupdev.agile.api.User;
import org.headsupdev.agile.app.docs.CommentPanel;
import org.headsupdev.agile.app.docs.DocsApplication;
import org.headsupdev.agile.storage.Comment;
import org.headsupdev.agile.storage.docs.Document;
import org.headsupdev.agile.web.RenderUtil;
import org.apache.wicket.markup.html.panel.Panel;

import javax.persistence.Entity;
import javax.persistence.DiscriminatorValue;

/**
 * Event added when a document is updated
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@Entity
@DiscriminatorValue( "updatedocument" )
public class UpdateDocumentEvent
    extends CreateDocumentEvent
{
    UpdateDocumentEvent()
    {
    }

    public UpdateDocumentEvent( Document doc, User user, String content )
    {
        super( doc, content, user.getFullnameOrUsername() + " updated document \"" + doc.getName() + "\"", doc.getUpdated(),
            user );
    }

    public UpdateDocumentEvent( Document doc, User user, Comment comment, String type )
    {
        super( doc, comment.getComment(), user.getFullnameOrUsername() + " " + type + " document \"" + doc.getName() + "\"", doc.getUpdated(),
            user );
        
        setSubObjectId( String.valueOf( comment.getId() ) );
    }

    public String getBody() {
        if ( getSubObjectId() == null || "0".equals( getSubObjectId() ) )
        {
            return super.getBody();
        }
        else
        {
            Comment comment = DocsApplication.getComment( Long.parseLong( getSubObjectId() ) );
            if ( comment == null )
            {
                return "<p>Comment " + getSubObjectId() + " does not exist</p>";
            }

            return renderComment( comment );
        }
    }

    private String renderComment( final Comment comment )
    {
        if ( comment == null )
        {
            return "";
        }

        String content = new RenderUtil()
        {
            public Panel getPanel()
            {
                return new CommentPanel( RenderUtil.PANEL_ID, comment, getProject() );
            }
        }.getRenderedContent();

        return "<table class=\"comments vertical\">" + content + "</table>";
    }
}