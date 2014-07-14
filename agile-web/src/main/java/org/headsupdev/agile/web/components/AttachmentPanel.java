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

package org.headsupdev.agile.web.components;

import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.storage.Attachment;
import org.headsupdev.agile.storage.HibernateStorage;
import org.headsupdev.agile.web.HeadsUpPage;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * This handles the UI interaction of uploading a file to HeadsUp.
 * The class that uses this will still need to persist the attachment by adding it to some model.
 * This should generally be implemented in the onSubmit() on the class.
 * There are getters for the filename and attachment if either are required.
 * Currently the filename is used as a helper for creating a comment.
 * <p/>
 * Created: 26/04/2012
 *
 * @author roberthewitt
 * @since 2.0-alpha-2
 */
public class AttachmentPanel
        extends Panel
        implements Serializable
{

    private FileUploadField upload;
    private String filename;
    private Attachment attachment;

    public AttachmentPanel( String id, final HeadsUpPage page )
    {
        super( id );

        upload = new FileUploadField( "attachment", new Model<FileUpload>()
        {
            @Override
            public void setObject( FileUpload object )
            {
                super.setObject( object );

                HibernateStorage storage = (HibernateStorage) page.getStorage();

                attachment = new Attachment();
                attachment.setCreated( new Date() );
                attachment.setUser( page.getSession().getUser() );

                FileUpload file = upload.getFileUpload();
                filename = file.getClientFileName();
                attachment.setFilename( filename );
                storage.save( attachment );

                File destination = attachment.getFile( storage );
                destination.getParentFile().mkdirs();
                try
                {
                    file.writeTo( destination );
                }
                catch ( Exception e )
                {
                    Manager.getLogger( "CreateAttachment" ).error( "Failed to upload attachment", e );
                    storage.delete( attachment );
                }
            }
        } );
        add( upload );
    }

    public Attachment getAttachment()
    {
        return attachment;
    }

    public String getFilename()
    {
        return filename;
    }

    public boolean isRequired()
    {
        return upload.isRequired();
    }

    public void setRequired( boolean required )
    {
        upload.setRequired( required );
    }
}
