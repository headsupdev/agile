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

package org.headsupdev.agile.api;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * A base helper for link providers
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public abstract class LinkProvider
    implements Serializable
{
    public abstract String getId();

    public abstract String getPageName();

    public abstract String getParamName();

    public String getLink( String params, Project project )
    {
        String encoded = params.replace( '/', ':' );
        try
        {
            encoded = URLEncoder.encode( encoded, "UTF-8" );
        }
        catch ( UnsupportedEncodingException e )
        {
            // just return the non-encoded string
        }
        String projectId = Project.ALL_PROJECT_ID;
        if ( project != null )
        {
            projectId = project.getId();
        }

        return "/" + projectId + "/" + getPageName() + "/" + getParamName() + "/" + encoded;
    }
}
