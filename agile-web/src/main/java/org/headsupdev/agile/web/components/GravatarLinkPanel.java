/*
 * HeadsUp Agile
 * Copyright 2014 Heads Up Development Ltd.
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

import com.timgroup.jgravatar.Gravatar;
import com.timgroup.jgravatar.GravatarDownloadException;
import org.apache.wicket.PageParameters;
import org.apache.wicket.Resource;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.resource.ByteArrayResource;
import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.api.User;
import org.headsupdev.agile.api.logging.Logger;
import org.headsupdev.agile.web.ApplicationPageMapper;
import org.headsupdev.agile.web.HeadsUpSession;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Component that displays the user's Gravatar if they have one, or displays an alternative image: their intials if they have
 * provided their full name. Provides link to the user's account page.
 *
 * @author Gordon Edwards
 * @version $Id$
 * @since 2.1
 */
public class GravatarLinkPanel
        extends Panel
{
    private final Logger log;
    private static final int defaultID = -1;
    private User user;

    public GravatarLinkPanel( String id, final User gravatarUser, final int iconEdgeLength )
    {
        super( id );
        if ( gravatarUser == null )
        {
            this.user = HeadsUpSession.ANONYMOUS_USER;
        }
        else
        {
            this.user = gravatarUser;
        }
        log = Manager.getLogger( getClass().getName() );


        HeadsUpTooltip tooltip = new HeadsUpTooltip( user.getFullname() );

        boolean displayGravatar = user.getPreference( "gravatar.show", true );
        String uniqueId;
        if ( user.getEmail() != null )
        {
            uniqueId = user.getUsername() + user.getFullname() + user.getEmail().hashCode();
        }
        else
        {
            uniqueId = "" + defaultID;
        }
        ResourceReference reference = new ResourceReference( getClass(), uniqueId + iconEdgeLength + displayGravatar )
        {
            @Override
            protected Resource newResource()
            {
                try
                {
                    return getIcon( user, iconEdgeLength );
                }
                catch ( IOException e )
                {
                    log.error( e.getMessage() );
                }
                return null;
            }
        };

        Image icon;
        if ( user.equals( HeadsUpSession.ANONYMOUS_USER ) )
        {
            icon = new Image( "avatarNoLink", reference );
            add( new WebMarkupContainer( "link" ).setVisible( false ) );
            add( new WebMarkupContainer( "nolink" ).add( icon ) );
            return;
        }
        icon = new Image( "avatar", reference );
        if ( getLink() instanceof BookmarkablePageLink )
        {
            Link link = (Link) getLink();
            link.add( icon );
            if ( displayHoverText() )
            {
                link.add( tooltip );
            }

            add( link );
        }
        else if ( getLink() instanceof ExternalLink )
        {
            ExternalLink link = (ExternalLink) getLink();
            link.add( icon );
            if ( displayHoverText() )
            {
                link.add( tooltip );
            }

            add( link );
        }

        add( new WebMarkupContainer( "nolink" ).add( new WebMarkupContainer( "avatarNoLink" ) ).setVisible( false ) );
    }

    private ByteArrayResource getIcon( User user, int iconEdgeLength )
            throws IOException
    {

        if ( user.equals( HeadsUpSession.ANONYMOUS_USER ) )
        {
            return getDefaultResource( iconEdgeLength );
        }
        byte[] avatarBytes = null;
        try
        {
            avatarBytes = new Gravatar().download( user.getEmail() );
        }
        catch ( GravatarDownloadException e )
        {
            log.error( "No Gravatar for user" );
        }
        boolean hasGravatar = avatarBytes != null;
        user.setPreference( "user.hasGravatar", hasGravatar );
        if ( !hasGravatar || !user.getPreference( "gravatar.show", true ) )
        {
            String initials = user.getInitials();
            if ( initials == null )
            {
                return getDefaultResource( iconEdgeLength );
            }
            BufferedImage alternativeIcon = new BufferedImage( iconEdgeLength, iconEdgeLength,
                    BufferedImage.TYPE_INT_RGB );
            Graphics graphics = alternativeIcon.getGraphics();
            graphics.setColor( Color.BLACK );
            graphics.setColor( Color.WHITE );
            graphics.setFont( new Font( "Arial Black", Font.BOLD, iconEdgeLength / 2 ) );
            int stringLen = (int) graphics.getFontMetrics().getStringBounds( initials, graphics ).getWidth();
            int start = iconEdgeLength / 2 - stringLen / 2;
            graphics.drawString( initials, start, (int) ( iconEdgeLength / 1.4 ) );
            byte[] iconBytes = getImageBytes( alternativeIcon );
            iconBytes = scale( iconBytes, iconEdgeLength, iconEdgeLength );
            return new ByteArrayResource( "image/jpeg", iconBytes );
        }
        else
        {
            avatarBytes = scale( avatarBytes, iconEdgeLength, iconEdgeLength );
            return new ByteArrayResource( "image/jpeg", avatarBytes );
        }
    }

    public byte[] scale( byte[] imageBytes, int width, int height )
            throws IOException
    {
        ByteArrayInputStream in = new ByteArrayInputStream( imageBytes );
        BufferedImage img = ImageIO.read( in );
        if ( height == 0 )
        {
            height = ( width * img.getHeight() ) / img.getWidth();
        }
        if ( width == 0 )
        {
            width = ( height * img.getWidth() ) / img.getHeight();
        }
        java.awt.Image scaledImage = img.getScaledInstance( width, height, java.awt.Image.SCALE_SMOOTH );
        BufferedImage imageBuff = new BufferedImage( width, height, BufferedImage.TYPE_INT_RGB );
        imageBuff.getGraphics().drawImage( scaledImage, 0, 0, new Color( 0, 0, 0 ), null );

        return getImageBytes( imageBuff );
    }

    public byte[] getImageBytes( BufferedImage image )
            throws IOException
    {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ImageIO.write( image, "jpg", buffer );
        return buffer.toByteArray();
    }

    public Object getLink()
    {
        PageParameters params = new PageParameters();
        if ( user != HeadsUpSession.ANONYMOUS_USER )
        {
            params.add( "username", user.getUsername() );
            params.add( "silent", "true" );
        }
        return new BookmarkablePageLink( "link", ApplicationPageMapper.get().getPageClass( "account" ), params );
    }

    public boolean displayHoverText()
    {
        return true;
    }

    public ByteArrayResource getDefaultResource( int iconEdgeLength )
            throws IOException
    {
        InputStream stream = getClass().getClassLoader().getResourceAsStream( "/org/headsupdev/agile/web/images/person-icon.png" );
        BufferedImage alternativeIcon = ImageIO.read( stream );
        byte[] iconBytes = getImageBytes( alternativeIcon );
        iconBytes = scale( iconBytes, iconEdgeLength, iconEdgeLength );
        return new ByteArrayResource( "image/jpeg", iconBytes );
    }
}
