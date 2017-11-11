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

package org.headsupdev.agile.app.issues;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.util.lang.Bytes;
import org.headsupdev.agile.api.Event;
import org.headsupdev.agile.app.issues.event.UpdateIssueEvent;
import org.headsupdev.agile.storage.Attachment;
import org.headsupdev.agile.storage.Comment;
import org.headsupdev.agile.web.MountPoint;
import org.headsupdev.agile.web.SubmitChildException;
import org.headsupdev.agile.web.components.AttachmentPanel;

/**
 * Add an attachment to an issue
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@MountPoint("attach")
public class CreateAttachment
        extends CreateComment
{
    private AttachmentPanel attachmentPanel;

    protected void layoutChild( Form form )
    {
        form.setMultiPart( true );
        attachmentPanel = new AttachmentPanel( "attachmentPanel", this );
        form.add( attachmentPanel );
        form.setMaxSize( Bytes.megabytes( 100 ) );

        setSubmitLabel( "Add Attachments" );
    }

    @Override
    protected void submitChild( Comment comment )
            throws SubmitChildException
    {
        if ( attachmentPanel.getAttachments().isEmpty() )
        {
            throw new SubmitChildException( "No attachments added!" );
        }
        for ( Attachment attachment : attachmentPanel.getAttachments() )
        {
            attachment.setComment( comment );
            getIssue().addAttachment( attachment );
        }
    }

    @Override
    protected IssueSubheader getSubheader()
    {
        String preamble = submitLabel + " to ";
        return new IssueSubheader( "subHeader", preamble, commentable );
    }

    protected Event getUpdateEvent( Comment comment )
    {
        StringBuilder stringBuilder = new StringBuilder();
        int attachmentNo = 0;

        for ( Attachment attachment : attachmentPanel.getAttachments() )
        {
            if ( attachmentPanel.getAttachments().size() == 1 )
            {
                return new UpdateIssueEvent( getIssue(), getIssue().getProject(), getSession().getUser(), comment,
                        "attached file " + attachment.getFilename() + " to" );
            }
            attachmentNo++;
            if ( attachmentNo == attachmentPanel.size() )
            {
                stringBuilder.append( "\"" + attachment.getFilename() + "\"" );
            }
            else
            {
                stringBuilder.append( "\"" + attachment.getFilename() + "\", " );
            }
        }
        String attachmentFilenames = stringBuilder.toString();
        return new UpdateIssueEvent( getIssue(), getIssue().getProject(), getSession().getUser(), comment,
                "attached files " + attachmentFilenames + " to" );
    }

    @Override
    protected boolean willChildConsumeComment()
    {
        return true;
    }
}
