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

package org.headsupdev.agile.framework.error;

import org.apache.wicket.markup.html.basic.Label;

import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;

import org.headsupdev.agile.web.MountPoint;
import org.headsupdev.agile.web.ErrorPage;
import org.apache.wicket.protocol.http.WebRequestCycle;

/**
 * An error page that shows a short message if about a missing page
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@MountPoint( "filenotfound" )
public class Error404Page
    extends ErrorPage
    implements Serializable
{
    public void layout()
    {
        super.layout();

        String uri = getPageParameters().getString( "uri", null );
        add( new Label( "uri", uri ).setVisible( uri != null ) );
    }

    @Override
    public String getTitle()
    {
        return "File Not Found";
    }

    @Override
    protected void configureResponse()
    {
        super.configureResponse();
        ( (WebRequestCycle) getRequestCycle() ).getWebResponse().getHttpServletResponse().setStatus( HttpServletResponse.SC_NOT_FOUND );
    }

    @Override
    public boolean isVersioned()
    {
        return false;
    }
}