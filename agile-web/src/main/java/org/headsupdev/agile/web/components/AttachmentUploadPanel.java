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

package org.headsupdev.agile.web.components;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.storage.Attachment;
import org.headsupdev.agile.storage.HibernateStorage;
import org.headsupdev.agile.web.HeadsUpPage;

import java.io.File;
import java.util.Date;

/**
 * Created by Gordon Edwards on 28/07/2014.
 *
 * Panel to upload attachments
 */
public class AttachmentUploadPanel
        extends Panel
{
    private final Component addAttachmentLink;
    private final AttachmentPanel panel;
    private HeadsUpPage page;

    public AttachmentUploadPanel( String id, HeadsUpPage page, final AttachmentPanel panel )
    {
        super( id );
        this.page = page;
        this.panel = panel;
        add( createFileUploadField() );
        addAttachmentLink = new AjaxLink( "addAttachmentLink" )
        {
            @Override
            public void onClick( AjaxRequestTarget target )
            {
                Component replacement = new AttachmentUploadPanel( "addAttachmentLink", AttachmentUploadPanel.this.page, panel );
                replacement.setOutputMarkupId( true );
                addAttachmentLink.replaceWith( replacement );
                target.addComponent( addAttachmentLink );
                target.addComponent( replacement );
            }
        };
        addAttachmentLink.setOutputMarkupId( true );
        add( addAttachmentLink );
    }

    private FileUploadField createFileUploadField()
    {
        return new FileUploadField( "attachment", new Model<FileUpload>()
        {
            @Override
            public void setObject( FileUpload fileUpload )
            {
                super.setObject( fileUpload );

                HibernateStorage storage = (HibernateStorage) page.getStorage();

                Attachment attachment = new Attachment();
                attachment.setCreated( new Date() );
                attachment.setUser( page.getSession().getUser() );

                String filename = fileUpload.getClientFileName();
                attachment.setFilename( filename );
                storage.save( attachment );

                File destination = attachment.getFile( storage );
                destination.getParentFile().mkdirs();
                try
                {
                    fileUpload.writeTo( destination );
                    panel.registerAttachment( attachment );
                }
                catch ( Exception e )
                {
                    Manager.getLogger( "CreateAttachment" ).error( "Failed to upload attachment", e );
                    storage.delete( attachment );
                }
            }
        } );

    }
}
