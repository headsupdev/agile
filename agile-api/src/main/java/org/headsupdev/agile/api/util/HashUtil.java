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

package org.headsupdev.agile.api.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utilities for hash creation and parsing
 * <p/>
 * Created: 12/05/2012
 *
 * @author Andrew Williams
 * @since 2.0
 */
public class HashUtil
{
    public static String getMD5Hex( String in )
    {
        MessageDigest messageDigest;
        try
        {
            messageDigest = java.security.MessageDigest.getInstance( "MD5" );
            messageDigest.update( in.getBytes(), 0, in.length() );

            return new BigInteger( 1, messageDigest.digest() ).toString( 16 );
        }
        catch ( NoSuchAlgorithmException e )
        {
            // not going to happen, MD5 always present
            e.printStackTrace();
        }

        return "";
    }
}
