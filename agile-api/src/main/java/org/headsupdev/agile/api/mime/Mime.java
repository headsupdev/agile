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

package org.headsupdev.agile.api.mime;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * A central class for managing mime type data - get information on it's capabilities and an icon to display.
 * Usage as follows:
 * <code>Mime.get( "myfile.png" ).isWebImage()</code>
 * or
 * <code>getClass().getClassLoader().getResource( "org/headsupdev/agile/api/mime/" + Mime.get( "myfile.png" ) );</code>
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class Mime
    implements Serializable
{
    public static final String MIME_FOLDER = "folder";
    public static final String MIME_PARENT_FOLDER = "parent-folder";
    public static final String MIME_FOLDER_LINK = "folder-link";
    public static final String MIME_FILE_LINK = "file-link";

    public static enum MimeEmbed {
        NONE, IMAGE, AUDIO, VIDEO
    }

    private static Map<String, Mime> mimes = new HashMap<String, Mime>();

    private String extension, iconName, syntax;
    private boolean binary;
    private MimeEmbed embed;

    protected Mime( String extension, String iconName, boolean binary )
    {
        this( extension, iconName, binary, MimeEmbed.NONE );
    }

    protected Mime( String extension, String iconName, boolean binary, MimeEmbed embed )
    {
        this.extension = extension;
        this.iconName = iconName;
        this.binary = binary;
        this.embed = embed;

        mimes.put(extension, this);
    }

    protected Mime( String extension, String iconName, boolean binary, String syntax )
    {
        this.extension = extension;
        this.iconName = iconName;
        this.binary = binary;
        this.syntax = syntax;

        mimes.put( extension, this );
    }

    public String getExtension()
    {
        return extension;
    }

    public String getIconName()
    {
        return iconName;
    }

    public String getSyntax()
    {
        return syntax;
    }

    public boolean isBinary()
    {
        return binary;
    }

    public boolean isEmbeddableImage()
    {
        return embed == MimeEmbed.IMAGE;
    }

    public boolean isEmbeddableAudio()
    {
        return embed == MimeEmbed.AUDIO;
    }

    public boolean isEmbeddableVideo()
    {
        return embed == MimeEmbed.VIDEO;
    }

    public boolean isEmbeddable()
    {
        return embed != MimeEmbed.NONE;
    }

    public static Mime get( String fileName )
    {
        Mime ret;
        String key = fileName.toLowerCase();

        int sep = key.lastIndexOf( '.' );
        if ( sep > 0 ) // use the extension by default - not an extension if the last "." is the first character
        {
            ret = mimes.get( key.substring( sep + 1 ) );
        }
        else
        {
            ret = mimes.get( key );
        }

        if ( ret == null )
        {
            return mimes.get( "unknown" );
        }

        return ret;
    }

    static
    {
        new Mime( "unknown", "application.png", true );
        new Mime( MIME_FOLDER, "folder.png", true );
        new Mime( MIME_FOLDER_LINK, "folder_go.png", true );
        new Mime( MIME_PARENT_FOLDER, "folder_up.png", true );
        new Mime( "package", "package.png", true );
        new Mime( MIME_FILE_LINK, "page_white_go.png", true );

        new Mime( "apt", "page_white_text.png", false );
        new Mime( "bat", "page_white_gear.png", false );
        new Mime( "bmp", "page_white_picture.png", true );
        new Mime( "c", "page_white_c.png", false, "c" );
        new Mime( "cpp", "page_white_cplusplus.png", false, "cpp" );
        new Mime( "class", "page_white_cup.png", true );
        new Mime( "command", "page_white_gear.png", false );
        new Mime( "conf", "page_white_text.png", false );
        new Mime( "cpp", "page_white_cplusplus.png", false );
        new Mime( "cs", "page_white_csharp.png", false, "csharp" );
        new Mime( "css", "page_white_css.png", false, "css" );
        new Mime( "diff", "page_white_text.png", false, "diff" );
        new Mime( "doc", "page_white_word.png", true );
        new Mime( "fml", "page_white_code.png", false );
        new Mime( "gem", "page_white_ruby.png", true );
        new Mime( "gif", "page_white_picture.png", true, MimeEmbed.IMAGE );
        new Mime( "groovy", "page_white_cup.png", false, "groovy" );
        new Mime( "gz", "page_white_compressed.png", true );
        new Mime( "h", "page_white_h.png", false );
        new Mime( "ico", "page_white_picture.png", true, MimeEmbed.IMAGE );
        new Mime( "iso", "page_white_cd.png", true );
        new Mime( "htm", "page_white_world.png", false, "html" );
        new Mime( "html", "page_white_world.png", false, "html" );
        new Mime( "jar", "page_white_compressed.png", true );
        new Mime( "java", "page_white_cup.png", false, "java" );
        new Mime( "jfx", "page_white_cup.png", false, "javafx" );
        new Mime( "javascript", "page_white_gear.png", false, "javascript" );
        new Mime( "jpeg", "page_white_picture.png", true, MimeEmbed.IMAGE );
        new Mime( "jpg", "page_white_picture.png", true, MimeEmbed.IMAGE );
        new Mime( "js", "page_white_gear.png", false, "javascript" );
        new Mime( "jsp", "page_white_world.png", false );
        new Mime( "m", "page_white_c.png", false );
        new Mime( "md5", "page_white_text.png", false );
        new Mime( "mm", "page_white_cplusplus.png", false );
        new Mime( "m4v", "page_white_film.png", true, MimeEmbed.VIDEO );
        new Mime( "mov", "page_white_film.png", true, MimeEmbed.VIDEO );
        new Mime( "mp3", "page_white_sound.png", true, MimeEmbed.AUDIO );
        new Mime( "mp4", "page_white_film.png", true, MimeEmbed.VIDEO );
        new Mime( "patch", "page_white_text.png", false, "patch" );
        new Mime( "pbxproj", "page_white_wrench.png", false );
        new Mime( "pch", "page_white_h.png", false );
        new Mime( "pdf", "page_white_acrobat.png", true );
        new Mime( "pl", "page_white_gear.png", false, "perl" );
        new Mime( "php", "page_white_php.png", false, "php" );
        new Mime( "plist", "page_white_code.png", false );
        new Mime( "png", "page_white_picture.png", true, MimeEmbed.IMAGE );
        new Mime( "pom", "page_white_code.png", false );
        new Mime( "ppt", "page_white_powerpoint.png", true );
        new Mime( "prefs", "page_white_wrench.png", false );
        new Mime( "properties", "page_white_text.png", false );
        new Mime( "py", "page_white_gear.png", false, "python" );
        new Mime( "rar", "page_white_compressed.png", true );
        new Mime( "rb", "page_white_ruby.png", false, "ruby" );
        new Mime( "scala", "page_white_cup.png", false, "scala" );
        new Mime( "sh", "page_white_gear.png", false, "shell" );
        new Mime( "sha1", "page_white_text.png", false );
        new Mime( "sql", "page_white_database.png", false, "sql" );
        new Mime( "tar", "page_white_compressed.png", true );
        new Mime( "tif", "page_white_picture.png", true );
        new Mime( "tiff", "page_white_picture.png", true );
        new Mime( "todo", "page_white_text.png", false );
        new Mime( "txt", "page_white_text.png", false );
        new Mime( "war", "page_white_compressed.png", true );
        new Mime( "wav", "page_white_sound.png", true, MimeEmbed.AUDIO );
        new Mime( "xdoc", "page_white_code.png", false );
        new Mime( "xib", "page_white_code.png", false );
        new Mime( "xls", "page_white_excel.png", true );
        new Mime( "xhtml", "page_white_code.png", false, "xml" );
        new Mime( "xml", "page_white_code.png", false, "xml" );
        new Mime( "xsd", "page_white_code.png", false, "xml" );
        new Mime( "xsl", "page_white_code.png", false, "xml" );
        new Mime( "xslt", "page_white_code.png", false, "xml" );
        new Mime( "zip", "page_white_compressed.png", true );

        // filenames
        new Mime( ".classpath", "page_white_wrench.png", false );
        new Mime( ".project", "page_white_wrench.png", false );
        new Mime( "authors", "page_white_text.png", false );
        new Mime( "bugs", "page_white_text.png", false );
        new Mime( "changelog", "page_white_text.png", false );
        new Mime( "copying", "page_white_text.png", false );
        new Mime( "install", "page_white_text.png", false );
        new Mime( "license", "page_white_text.png", false );
        new Mime( "makefile", "page_white_text.png", false );
        new Mime( "news", "page_white_text.png", false );
        new Mime( "readme", "page_white_text.png", false );
        new Mime( "todo", "page_white_text.png", false );
    }
}
