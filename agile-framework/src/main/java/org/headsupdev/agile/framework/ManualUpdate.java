/*
 * HeadsUp Agile
 * Copyright 2013 Heads Up Development Ltd.
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

package org.headsupdev.agile.framework;

import org.apache.wicket.markup.html.basic.Label;
import org.headsupdev.agile.api.Permission;
import org.headsupdev.agile.api.util.FileUtil;
import org.headsupdev.agile.core.UpdateDetails;
import org.headsupdev.agile.security.permission.AdminPermission;
import org.headsupdev.agile.web.HeadsUpPage;
import org.headsupdev.agile.web.MountPoint;

import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.model.Model;

import java.io.File;
import java.net.MalformedURLException;

/**
 * A page that allows updating from a manual file upload
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 2.0
 */
@MountPoint( "manualupdate" )
public class ManualUpdate
    extends HeadsUpPage
{
    private FileUploadField upload;
    private UpdatingPanel updating;

    private File uploadedFile;

    public Permission getRequiredPermission()
    {
        return new AdminPermission();
    }

    public void layout()
    {
        super.layout();
        add( CSSPackageResource.getHeaderContribution(getClass(), "updates.css") );

        add( new Label( "productName", getStorage().getGlobalConfiguration().getProductName() ) );

        updating = new UpdatingPanel( "updating", HomeApplication.getHeadsUpRuntime() )
        {
            @Override
            protected UpdateDetails getUpdate()
            {
                try
                {
                    return new UpdateDetails( "manual", uploadedFile.getName(), "Manual update",
                            uploadedFile.toURI().toURL().toString(), uploadedFile.length(), true );
                }
                catch ( MalformedURLException e )
                {
                    error( "Failed to load update: " + e.getMessage() );
                }

                return null;
            }

            @Override
            protected boolean hasUpdate()
            {
                return uploadedFile != null;
            }
        };
        add( updating.setOutputMarkupPlaceholderTag( true ) );

        final Form uploadForm = new Form( "upload" );
        uploadForm.setMultiPart( true );
        add( uploadForm );

        upload = new FileUploadField( "file", new Model<FileUpload>()
        {
            @Override
            public void setObject( FileUpload object )
            {
                super.setObject( object );
                if ( object == null )
                {
                    // ajax disconnect callback - don't try to upload again
                    return;
                }

                FileUpload file = upload.getFileUpload();
                String filename = file.getClientFileName();
                if ( !isValidUpateFile( filename ) )
                {
                    error( "Not a valid upgrade file");
                    return;
                }

                uploadedFile = new File( FileUtil.getTempDir(), filename );
                uploadedFile.getParentFile().mkdirs();
                try
                {
                    file.writeTo( uploadedFile );
                }
                catch ( Exception e )
                {
                    error( "Failed to upload attachment: " + e.getMessage() );
                }

                uploadForm.setVisible( false );
                updating.setVisible( true );
            }
        } );
        uploadForm.add( upload );
    }

    protected boolean isValidUpateFile( String filename )
    {
        return filename.endsWith( ".tar.gz" ) && filename.toLowerCase().contains( "agile" );
    }
}
