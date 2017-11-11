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

package org.headsupdev.agile.web.components;

import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.JavascriptPackageResource;
import org.headsupdev.support.java.IOUtil;
import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.api.mime.Mime;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.image.resource.DynamicImageResource;
import org.apache.wicket.markup.html.link.DownloadLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

import javax.imageio.ImageIO;
import java.io.*;
import java.net.URLEncoder;

/**
 * Embedded file panel that renders all known file types in HeadsUp
 * <p/>
 * Created: 06/08/2011
 *
 * @author Andrew Williams
 * @since 1.0
 */
public class EmbeddedFilePanel
    extends Panel
{
    public EmbeddedFilePanel( final String id, final File file, final Project project )
    {
        super( id );

        add( CSSPackageResource.getHeaderContribution( getClass(), "embeddedfile.css" ) );

        add( JavascriptPackageResource.getHeaderContribution( getClass(), "highlight/shCore.js" ) );
        add( JavascriptPackageResource.getHeaderContribution( getClass(), "highlight/shAutoloader.js" ) );
        add( JavascriptPackageResource.getHeaderContribution( getClass(), "highlight/bootstrap.js" ) );
        add( CSSPackageResource.getHeaderContribution( getClass(), "highlight/shCoreDefault.css" ) );

        final Mime mime = Mime.get( file.getName() );

        if ( mime.isEmbeddableImage() )
        {
            WebMarkupContainer image = new WebMarkupContainer( "image-content" );
            image.add( new Image( "image", new DynamicImageResource()
            {
                protected byte[] getImageData()
                {
                    try
                    {
                        return toImageData( ImageIO.read( file ) );
                    }
                    catch ( IOException e )
                    {
                        Manager.getLogger( "BrowseFile" ).error( "Unable to load data to image", e );
                        return null;
                    }
                }
            }.setCacheable( false ) ) );
            add( image );

            add( new WebMarkupContainer( "text-content" ).setVisible( false ) );
            add( new WebMarkupContainer( "binary-content" ).setVisible( false ) );
            add( new WebMarkupContainer( "object-content" ).setVisible( false ) );
            return;
        }
        if ( mime.isEmbeddableAudio() || mime.isEmbeddableVideo() )
        {
            WebMarkupContainer container = new WebMarkupContainer( "object-content" );
            final WebMarkupContainer object = new WebMarkupContainer( "object" );
            object.add( new AttributeModifier( "type", true, new Model<String>()
            {
                @Override
                public String getObject()
                {
                    // TODO add real mime types to the mime library
                    if ( mime.isEmbeddableAudio() )
                    {
                        return "audio/" + mime.getExtension();
                    }
                    else
                    {
                        return "video/" + mime.getExtension();
                    }
                }
            } ) );

            object.add( new AttributeModifier( "data", true, new Model<String>()
            {
                @Override
                public String getObject()
                {
                    String storagePath = Manager.getStorageInstance().getDataDirectory().getAbsolutePath();

                    if ( file.getAbsolutePath().length() > storagePath.length() + 1 )
                    {
                        String filePath = file.getAbsolutePath().substring(storagePath.length() + 1);
                        filePath = filePath.replace( File.separatorChar, ':' );
                        try
                        {
                            filePath = URLEncoder.encode( filePath, "UTF-8" );
                            // funny little hack here, guess the decoding is not right
                            filePath = filePath.replace( "+", "%20" );
                        }
                        catch ( UnsupportedEncodingException e )
                        {
                            // ignore
                        }

                        String urlPath = object.urlFor( new ResourceReference( "embed" ) ).toString();
                        return urlPath.replace( "/all/", "/" + project.getId() + "/" ) + "/path/" + filePath;
                    }

                    return ""; // not supported, someone is hacking the system...
                }
            } ) );

            container.add( object );
            add( container );

            add( new WebMarkupContainer( "text-content" ).setVisible( false ) );
            add( new WebMarkupContainer( "binary-content" ).setVisible( false ) );
            add( new WebMarkupContainer( "image-content" ).setVisible( false ) );
            return;
        }

        // offer a download link for binary (or unknown) files
        if ( mime.isBinary() )
        {
            WebMarkupContainer binary = new WebMarkupContainer( "binary-content" );
            binary.add( new DownloadLink( "download", file ) );
            add( binary );

            add( new WebMarkupContainer( "text-content" ).setVisible( false ) );
            add( new WebMarkupContainer( "image-content" ).setVisible( false ) );
            add( new WebMarkupContainer( "object-content" ).setVisible( false ) );

            return;
        }

        String content = "(unable to read file)";
        // for other types try to parse the content
        FileInputStream in = null;
        try
        {
            in = new FileInputStream( file );
            content = IOUtil.toString( in ).replace( "<", "&lt;" ).replace( ">", "&gt;" );
        }
        catch ( IOException e )
        {
            Manager.getLogger( "BrowseFile" ).error( "Exception rendering file highlighting", e );
        }
        finally
        {
            IOUtil.close(in);
        }

        add( new Label( "text-content", content ).setEscapeModelStrings( false ).add(
            new AttributeModifier( "class", true, new Model<String>()
            {
                @Override
                public String getObject()
                {
                    if ( mime.getSyntax() != null )
                    {
                        return "code brush: " + mime.getSyntax();
                    }

                    return "code brush: text";
                }
            } )
        ) );

        add( new WebMarkupContainer( "binary-content" ).setVisible( false ) );
        add( new WebMarkupContainer( "image-content" ).setVisible( false ) );
        add( new WebMarkupContainer( "object-content" ).setVisible( false ) );
    }
}
