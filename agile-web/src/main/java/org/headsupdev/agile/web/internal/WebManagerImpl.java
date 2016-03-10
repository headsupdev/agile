/*
 * HeadsUp Agile
 * Copyright 2009-2016 Heads Up Development Ltd.
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

package org.headsupdev.agile.web.internal;

import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.core.PrivateConfiguration;
import org.headsupdev.agile.web.ErrorPage;
import org.headsupdev.agile.web.HeadsUpPage;
import org.headsupdev.agile.web.WebManager;

import java.io.Serializable;
import java.util.TimeZone;

/**
 * A simple implementation of the web manager to provide basic information to web pages.
 * <p/>
 * Created: 26/05/2012
 *
 * @author Andrew Williams
 * @since 1.0
 */
public class WebManagerImpl
    extends WebManager
    implements Serializable
{
    @Override
    public String getHeaderLogo() {
        return "/resources/org.headsupdev.agile.web.HeadsUpPage/images/agile-title.png";
    }

    @Override
    public String getLozengeLogo() {
        return "/resources/org.headsupdev.agile.web.HeadsUpPage/images/agile-lozenge.png";
    }

    @Override
    public String getFooterDescriptionHTML( TimeZone timeZone )
    {
        StringBuilder ret = new StringBuilder( "Time zone: " );
        ret.append( timeZone.getID() );
        ret.append( "<br />" );
        ret.append( Manager.getStorageInstance().getGlobalConfiguration().getProductName() );
        ret.append( " is an open source project, <a href=\"" );
        ret.append( Manager.getStorageInstance().getGlobalConfiguration().getProductUrl() );
        ret.append( "\">download</a> the latest release now!" );

        return ret.toString();
    }

    @Override
    public String getFooterCopyrightHTML()
    {
        return "Copyright &copy; 2009 - 2016 <a href=\"http://headsupdev.com/\" target=\"_blank\">" +
            "Heads Up Development Ltd</a> and contributors.";
    }

    @Override
    public String getFooterNoteHTML()
    {
        return null;
    }

    @Override
    public void checkPermissions( HeadsUpPage page )
        throws RestartResponseAtInterceptPageException
    {
        /* install check */
        if ( ( !PrivateConfiguration.isInstalled() ||
            PrivateConfiguration.getSetupStep() < PrivateConfiguration.STEP_FINISHED )
            && !page.getClass().getName().equals( "org.headsupdev.agile.framework.Setup" )
            && !getClass().getName().endsWith( "Updates" ) && !( page instanceof ErrorPage) )
        {
            throw new RestartResponseAtInterceptPageException( page.getPageClass("setup") );
        }

    }
}
