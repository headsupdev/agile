package org.headsupdev.agile.web.components;

import com.timgroup.jgravatar.Gravatar;
import com.timgroup.jgravatar.GravatarDownloadException;
import org.apache.wicket.PageParameters;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.image.NonCachingImage;
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
 * Created by Gordon Edwards on 12/08/2014.
 */
public class GravatarLinkPanel
        extends Panel
{
    private final Logger log;
    private User user;
    private boolean hasGravatar;

    public GravatarLinkPanel( String id, User user, int iconEdgeLength )
    {
        super( id );
        this.user = user;
        log = Manager.getLogger( getClass().getName() );

        NonCachingImage icon = null;
        if ( user != null && !user.equals( HeadsUpSession.ANONYMOUS_USER ) )
        {
            try
            {
                icon = getIcon( user, iconEdgeLength );
            }
            catch ( IOException e )
            {
                log.error( e.getMessage() );
                icon = null;
            }
            if ( icon == null )
            {
                addBlankImage();
                return;
            }
            if ( getLink() instanceof BookmarkablePageLink )
            {
                Link link = (Link) getLink();
                link.add( new SimpleAttributeModifier( "title", user.getFullname() ) );
                link.add( icon.setVisible( true ) );
                if ( hasGravatar && user.getPreference( "gravatar.show", true) )
                {
                    link.add( new NonCachingImage( "alternativeIcon" ).setVisible( false ) );
                }
                else
                {
                    link.add( new NonCachingImage( "avatar" ).setVisible( false ) );
                }
                add( link );
            }
            else if ( getLink() instanceof ExternalLink )
            {
                ExternalLink link = (ExternalLink) getLink();
                link.add( new SimpleAttributeModifier( "title", user.getFullname() ) );
                link.add( icon.setVisible( true ) );
                if ( hasGravatar && user.getPreference( "gravatar.show", true) )
                {
                    link.add( new NonCachingImage( "alternativeIcon" ).setVisible( false ) );
                }
                else
                {
                    link.add( new NonCachingImage( "avatar" ).setVisible( false ) );
                }
                add( link );
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


    private NonCachingImage getIcon( User user, int iconEdgeLength )
            throws IOException
    {
        byte[] avatarBytes = null;
        try
        {
            avatarBytes = new Gravatar().download( user.getEmail() );
        }
        catch ( GravatarDownloadException e )
        {
            warn("noimage" );
        }
        hasGravatar = avatarBytes != null;
        user.setPreference( "user.hasGravatar", hasGravatar );
        if ( !hasGravatar || !user.getPreference( "gravatar.show", true ) )
        {
            BufferedImage alternativeIcon = new BufferedImage( iconEdgeLength, iconEdgeLength,
                    BufferedImage.TYPE_INT_RGB );
            Graphics graphics = alternativeIcon.getGraphics();
            graphics.setColor( Color.BLACK );
            graphics.setColor( Color.WHITE );
            graphics.setFont( new Font( "Arial Black", Font.BOLD, iconEdgeLength / 2 ) );
            String initials = user.getInitials();
            if ( initials == null )
            {
                InputStream stream = getClass().getClassLoader().getResourceAsStream( "/org/headsupdev/agile/web/images/person-icon.png" );
                alternativeIcon = ImageIO.read( stream );
                byte[] iconBytes = getImageBytes( alternativeIcon );
                iconBytes = scale( iconBytes , iconEdgeLength, iconEdgeLength );
                return new NonCachingImage( "alternativeIcon", new ByteArrayResource( "image/jpeg", iconBytes ) );
            }
            int stringLen = (int) graphics.getFontMetrics().getStringBounds( initials, graphics ).getWidth();
            int start = iconEdgeLength / 2 - stringLen / 2;
            graphics.drawString( initials, start, (int) ( iconEdgeLength / 1.4 ) );
            byte[] iconBytes = getImageBytes( alternativeIcon );
            iconBytes = scale( iconBytes , iconEdgeLength, iconEdgeLength );
            return new NonCachingImage( "alternativeIcon", new ByteArrayResource( "image/jpeg", iconBytes ) );
        }
        else
        {
            avatarBytes = scale( avatarBytes, iconEdgeLength, iconEdgeLength );
            return new NonCachingImage( "avatar", new ByteArrayResource( "image/jpeg", avatarBytes ) );
        }

//        avatarBytes = scale( avatarBytes, iconEdgeLength, iconEdgeLength );
//        return new NonCachingImage( "avatar", new ByteArrayResource( "image/jpeg", avatarBytes ) );
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
        return new BookmarkablePageLink( "link", ApplicationPageMapper.get().getPageClass( "account" ), params );
    }

    public boolean hasGravatar()
    {
        return hasGravatar;
    }
}
