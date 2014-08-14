package org.headsupdev.agile.web.components;

import com.timgroup.jgravatar.Gravatar;
import com.timgroup.jgravatar.GravatarDownloadException;
import org.apache.wicket.Application;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.OrderByLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.image.resource.BufferedDynamicImageResource;
import org.apache.wicket.markup.html.image.resource.DynamicImageResource;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.resource.ByteArrayResource;
import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.api.User;
import org.headsupdev.agile.api.logging.Logger;
import org.headsupdev.agile.web.HeadsUpPage;
import org.headsupdev.agile.web.HeadsUpSession;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

/**
 * Created by Gordon Edwards on 12/08/2014.
 */
public class GravatarLinkPanel
        extends Panel
{
    private final Logger log;
    private User user;
    private HeadsUpPage page;
    private boolean hasGravatar;

    public GravatarLinkPanel( String id, User user, int iconEdgeLength, HeadsUpPage page )
    {
        super( id );
        this.user = user;
        this.page = page;
        log = Manager.getLogger( getClass().getName() );

        NonCachingImage avatar = null;
        if ( user != null && !user.equals( HeadsUpSession.ANONYMOUS_USER ))
        {
            try
            {
                avatar = getAvatar( user, iconEdgeLength );
            }
            catch ( IOException e )
            {
                log.error( e.getMessage() );
                avatar = null;
            }
            if ( avatar == null )
            {
                addBlankImage();
                return;
            }
            String style = "text-decoration: none; display:inline; ";
            if ( getLink() instanceof BookmarkablePageLink )
            {
                Link link = (Link) getLink();
                link.add( new SimpleAttributeModifier( "title", user.getFullname() ) );
                style += "cursor: pointer";
                link.add( new SimpleAttributeModifier( "style", style ) );
                link.add( avatar );
                add( link );
            }
            else if ( getLink() instanceof ExternalLink )
            {
                ExternalLink link = (ExternalLink) getLink();
                link.add( new SimpleAttributeModifier( "title", user.getFullname() ) );
                style += "cursor: pointer";
                link.add( new SimpleAttributeModifier( "style", style ) );
                link.add( avatar );
                add( link );
            }
            else if ( getLink() instanceof WebMarkupContainer )
            {
                add( ( (WebMarkupContainer) getLink() ).add( avatar ) );
            }
        }
        else
        {
            addBlankImage();
        }
    }

    private void addBlankImage()
    {
        Image avatar = new Image( "avatar" );
        Link link = new Link( "link" )
        {
            @Override
            public void onClick()
            {
            }
        };
        link.add( avatar.setVisible( false ) );
        add( link.setVisible( false ) );
    }


    private NonCachingImage getAvatar( User user, int iconEdgeLength )
            throws IOException
    {
        byte[] avatarBytes = null;
        try
        {
            avatarBytes = new Gravatar().download( user.getEmail() );
        }
        catch ( GravatarDownloadException e )
        {
        }
        hasGravatar = avatarBytes != null;
        if ( !hasGravatar )
        {
            BufferedImage avatar = new BufferedImage( iconEdgeLength, iconEdgeLength,
                    BufferedImage.TYPE_INT_RGB );
            Graphics graphics = avatar.getGraphics();
            graphics.setColor( Color.BLACK );
            graphics.setColor( Color.WHITE );
            graphics.setFont( new Font( "Arial Black", Font.BOLD, iconEdgeLength / 2 ) );
            String initials = user.getInitials();
            if ( initials == null )
            {
                InputStream stream = getClass().getClassLoader().getResourceAsStream( "/org/headsupdev/agile/web/images/person-icon.png" );
                avatar = ImageIO.read( stream );
                avatarBytes = getImageBytes( avatar );
                avatarBytes = scale( avatarBytes, iconEdgeLength, iconEdgeLength );
                return new NonCachingImage( "avatar", new ByteArrayResource( "image/jpeg", avatarBytes ) );
            }
            int stringLen = (int) graphics.getFontMetrics().getStringBounds( initials, graphics ).getWidth();
            int start = iconEdgeLength / 2 - stringLen / 2;
            graphics.drawString( initials, start, (int) ( iconEdgeLength / 1.4 ) );
            avatarBytes = getImageBytes( avatar );
        }

        avatarBytes = scale( avatarBytes, iconEdgeLength, iconEdgeLength );
        return new NonCachingImage( "avatar", new ByteArrayResource( "image/jpeg", avatarBytes ) );
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
        params.add( "username", user.getUsername() );
        params.add( "silent", "true" );
        return new BookmarkablePageLink( "link", page.getPageClass( "account" ), params );
    }

    public boolean hasGravatar()
    {
        return hasGravatar;
    }
}
