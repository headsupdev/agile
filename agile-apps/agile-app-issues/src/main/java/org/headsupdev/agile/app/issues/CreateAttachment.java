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

package org.headsupdev.agile.app.issues;

import org.headsupdev.agile.api.Event;
import org.headsupdev.agile.app.issues.event.UpdateIssueEvent;
import org.headsupdev.agile.storage.Comment;
import org.headsupdev.agile.web.MountPoint;
import org.headsupdev.agile.web.components.AttachmentPanel;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.util.lang.Bytes;

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
        form.setMultiPart(true);
        form.add( attachmentPanel = new AttachmentPanel( "attachmentPanel", this ) );
        attachmentPanel.setRequired( true );
        form.setMaxSize( Bytes.megabytes( 100 ) );

        setSubmitLabel( "Create Attachment" );
    }

    @Override
    protected void submitChild( Comment comment )
    {
        getIssue().getAttachments().add( attachmentPanel.getAttachment() );

        attachmentPanel.getAttachment().setComment( comment );
    }

    protected Event getUpdateEvent( Comment comment )
    {
        return new UpdateIssueEvent( getIssue(), getIssue().getProject(), getSession().getUser(), comment,
                "Attached file \"" + attachmentPanel.getFilename() + "\" to" );
    }

    @Override
    protected boolean willChildConsumeComment()
    {
        return true;
    }
}
