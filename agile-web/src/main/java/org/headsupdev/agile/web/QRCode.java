/*
 * HeadsUp Agile
 * Copyright 2013 Heads Up Development.
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

package org.headsupdev.agile.web;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.qrcode.encoder.Encoder;
import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.web.CachedImageResource;
import org.headsupdev.agile.web.MountPoint;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * A QR Code renderer that can link to any given path within this Agile instance - just pass a path parameter.
 * <p/>
 * Created: 22/05/2013
 *
 * @author Andrew Williams
 * @since 2.0
 */
@MountPoint("qrcode.png")
public class QRCode
        extends CachedImageResource
{
    public static final Color FOREGROUND = Color.BLACK;
    public static final Color BACKGROUND = Color.WHITE;
    public static final int SIZE = 250;
    public static final int PAD_SIZE = 1;
    public static final int LOGO_CELLS = 3;

    private URL url;
    private int cellsInRow = 25;

    public QRCode()
    {
        try
        {
            this.url = new URL( Manager.getStorageInstance().getGlobalConfiguration().getBaseUrl() );
        }
        catch ( MalformedURLException e )
        {
            // not gonna happen
        }
    }

    public QRCode( URL url )
    {
        this.url = url;
    }

    protected URL getURL()
    {
        String url = Manager.getStorageInstance().getGlobalConfiguration().getBaseUrl();
        if ( getParameters().containsKey( "path" ) )
        {
            url = url + getParameters().getString( "path" );
        }

        try
        {
            return new URL( url );
        }
        catch ( MalformedURLException e )
        {
            return this.url;
        }
    }

    protected BitMatrix generateBits()
    {
        url = getURL();
        MultiFormatWriter barcodeWriter = new MultiFormatWriter();
        try
        {
            Map<EncodeHintType, Object> hints = new HashMap<EncodeHintType, Object>();
            hints.put( EncodeHintType.MARGIN, PAD_SIZE );

            com.google.zxing.qrcode.encoder.QRCode code = Encoder.encode( url.toString(), ErrorCorrectionLevel.L, hints );
            cellsInRow = code.getVersion().getDimensionForVersion();

            return barcodeWriter.encode( url.toString(), BarcodeFormat.QR_CODE, SIZE, SIZE, hints );
        }
        catch ( WriterException e )
        {
            Manager.getLogger( getClass().getName() ).error( "Failed to generate QR code", e );

            return new BitMatrix( 5 );
        }
    }

    protected BufferedImage getQRImage( BitMatrix matrix )
    {
        BufferedImage image = new BufferedImage( matrix.getWidth(), matrix.getHeight(), BufferedImage.TYPE_INT_RGB );

        paintDots( matrix, image.getGraphics() );

        return image;
    }

    protected void paintDots( BitMatrix matrix, Graphics g )
    {
        int dotsize = SIZE / matrix.getWidth();

        for (int x = 0; x < matrix.getWidth(); x++) {
            for (int y = 0; y < matrix.getHeight(); y++) {
                if ( matrix.get( x, y ) )
                {
                    g.setColor( FOREGROUND );
                }
                else
                {
                    g.setColor( BACKGROUND );
                }
                g.fillRect( x, y, dotsize, dotsize );
            }
        }
    }

    protected void overlayLogo( BitMatrix matrix, Graphics g )
    {
        int cellWidth = SIZE / ( cellsInRow + ( PAD_SIZE * 2 ) );
        int logoWidth = cellWidth * LOGO_CELLS;
        int offset = ( SIZE - logoWidth ) / 2;

        g.setColor( Color.WHITE );
        int padOffset = offset - cellWidth;
        int padWidth = logoWidth + ( cellWidth * 2 );
        g.fillRect( padOffset, padOffset, padWidth, padWidth );
        g.drawImage( loadLogo(), offset, offset, logoWidth, logoWidth, null );
    }

    public BufferedImage getImage()
    {
        BitMatrix matrix = generateBits();
        BufferedImage image = getQRImage( matrix );
        overlayLogo( matrix, image.getGraphics() );

        return image;
    }

    protected Image loadLogo()
    {
        InputStream stream = getClass().getClassLoader().getResourceAsStream( "/org/headsupdev/agile/web/images/agile-square.png" );
        try
        {
            return ImageIO.read( stream );
        }
        catch ( IOException e )
        {
            Manager.getLogger( getClass().getName() ).error( "Failed to load square logo image", e );

            return new BufferedImage( LOGO_CELLS, LOGO_CELLS, BufferedImage.TYPE_INT_RGB );
        }
    }

    // Image mounting stuff

    @Override
    protected int getWidth()
    {
        return SIZE;
    }

    @Override
    protected int getHeight()
    {
        return SIZE;
    }

    protected void renderImage( Graphics g )
    {
        BitMatrix matrix = generateBits();

        paintDots( matrix, g );

        overlayLogo( matrix, g );
    }
}