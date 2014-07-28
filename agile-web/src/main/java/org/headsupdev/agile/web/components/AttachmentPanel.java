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
import org.apache.wicket.markup.html.panel.Panel;
import org.headsupdev.agile.storage.Attachment;
import org.headsupdev.agile.web.HeadsUpPage;

import java.io.Serializable;
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
        implements Serializable, AttachmentCommunication
{


    private Set<Attachment> attachments = new HashSet<Attachment>();
    private HeadsUpPage page;
    private Component addAttachmentLink;

    public AttachmentPanel( String id, final HeadsUpPage page )
    {
        super( id );
        this.page = page;
        add( new AttachmentUploadPanel( "addAttachmentLink", page, this ) );
//        final ArrayList<FileUploadField> fileUploadPanels = new ArrayList<FileUploadField>();
//        fileUploadPanels.add( createFileUploadField() );
//        add( fileUploadPanels.get( 0 ) );
//        final WebMarkupContainer addIconAttachment = new WebMarkupContainer( "addIconAttachment" );
//        addIconAttachment.setOutputMarkupId( true );
//        addAttachmentLink = new AjaxLink( "addAttachmentLink" )
//        {
//            @Override
//            public void onClick( AjaxRequestTarget target )
//            {
//                addAttachmentLink.replaceWith(  );
//                FileUploadField fileUpload = createFileUploadField();
//                fileUploadPanels.add( fileUpload );
//                WebMarkupContainer replacement = new WebMarkupContainer( "addAttachmentLink" );
//                replacement.add( fileUpload );
//
//                target.addComponent( replacement );
//
//            }
//        };
//        addAttachmentLink.setOutputMarkupId( true );
//        addIconAttachment.add( addAttachmentLink );
//        add( addIconAttachment );
//        add();
//        add( upload );
//        final ListView<FileUploadField> fileUploads = new ListView<FileUploadField>( "fileUploads", fileUploadPanels )
//        {
//            @Override
//            protected void populateItem( ListItem listItem )
//            {
//                listItem.add( fileUploadPanels.get( listItem.getIndex() ) );
//            }
//        };
//        add( fileUploads );
//
//        add( new Link<Void>( "addIconAttachment" )
//        {
//            @Override
//            public void onClick()
//            {
//                fileUploadPanels.add( createFileUploadField() );
//            }
//        } );

    }

    public Set<Attachment> getAttachments()
    {
        return attachments;
    }

    @Override
    public void registerAttachment( Attachment attachment )
    {
        attachments.add( attachment );
    }

//    private FileUploadField createFileUploadField()
//    {
//        return new FileUploadField( "attachment", new Model<FileUpload>()
//        {
//            @Override
//            public void setObject( FileUpload fileUpload )
//            {
//                super.setObject( fileUpload );
//
//                HibernateStorage storage = (HibernateStorage) page.getStorage();
//
//                Attachment attachment = new Attachment();
//                attachment.setCreated( new Date() );
//                attachment.setUser( page.getSession().getUser() );
//
//                String filename = fileUpload.getClientFileName();
//                attachment.setFilename( filename );
//                storage.save( attachment );
//
//                File destination = attachment.getFile( storage );
//                destination.getParentFile().mkdirs();
//                try
//                {
//                    fileUpload.writeTo( destination );
//                    attachments.add( attachment );
//                }
//                catch ( Exception e )
//                {
//                    Manager.getLogger( "CreateAttachment" ).error( "Failed to upload attachment", e );
//                    storage.delete( attachment );
//                }
//            }
//        } );
//
//    }

}
