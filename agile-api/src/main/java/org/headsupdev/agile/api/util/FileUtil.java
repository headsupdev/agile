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

package org.headsupdev.agile.api.util;

import org.headsupdev.agile.api.Manager;

import java.io.File;
import java.io.IOException;

/**
 * Utilities for file management and repository / url handling
 * <p/>
 * Created: 15/10/2011
 *
 * @author Andrew Williams
 * @since 1.0
 */
public class FileUtil
{
    public static final String LATEST_ITEM_NAME = "latest";

    public static boolean isFileNumeric( File file )
    {
        if ( file == null )
        {
            return false;
        }

        return isFileNumeric( file.getName() );
    }

    public static boolean isFileNumeric( String fileName )
    {
        if ( fileName == null || fileName.trim().length() == 0 )
        {
            return false;
        }

        try
        {
            if ( fileName.contains( "." ) )
            {
                String noExt = fileName.substring( 0, fileName.lastIndexOf( "." ) );

                Integer.parseInt( noExt.replace( ".", "" ) );
            }
            else
            {
                Integer.parseInt( fileName );
            }

            return true;
        }
        catch ( NumberFormatException e )
        {
            return false;
        }
    }

    public static File replaceLatest( File in )
    {
        if ( !in.getPath().contains( LATEST_ITEM_NAME ) )
        {
            return in;
        }

        String resolvedFile = in.getName();

        File resolvedParent = replaceLatest( in.getParentFile() );
        if ( resolvedFile.equals( LATEST_ITEM_NAME ) )
        {
            resolvedFile = getLatestFileIn( resolvedParent ).getName();
        }

        return new File( resolvedParent, resolvedFile );
    }

    public static File getLatestFileIn( File folder )
    {
        File latest = null;

        for ( File file : folder.listFiles() )
        {
            if ( latest == null || latest.lastModified() < file.lastModified() )
            {
                latest = file;
            }
        }

        return latest;
    }

    public static File getTempDir()
    {
        File temp = new File( Manager.getStorageInstance().getDataDirectory(), "temp" );
        if ( !temp.exists() )
        {
            temp.mkdirs();
        }

        return temp;
    }

    public static File createTempDir( String prefix, String postfix )
        throws IOException
    {
        File tmpDir = getTempDir();

        return org.headsupdev.support.java.FileUtil.createTempDir( prefix, postfix, tmpDir );
    }

    public static File createTempFile( String prefix, String postfix )
        throws IOException
    {
        File tmpDir = getTempDir();

        return File.createTempFile( prefix, postfix, tmpDir );
    }
}
