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

package org.headsupdev.agile.framework;

import org.apache.wicket.IPageFactory;
import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.headsupdev.agile.web.HeadsUpPage;
import org.headsupdev.agile.web.ApplicationPageMapper;
import org.headsupdev.agile.web.feed.AbstractFeed;
import org.headsupdev.agile.api.Application;

/**
 * TODO document me
 *
 * @author Andrew Williams
 * @since 1.0
 */
public class HeadsUpPageFactory
    implements IPageFactory
{
    private IPageFactory parent;

    public HeadsUpPageFactory( IPageFactory parent )
    {
        this.parent = parent;
    }

    public Page newPage( Class aClass )
    {
        Page ret = parent.newPage( aClass );

        configurePage( ret, null );
        return ret;
    }

    public Page newPage( Class aClass, PageParameters pageParameters )
    {
        Page ret = parent.newPage( aClass, pageParameters );

        configurePage( ret, pageParameters );
        return ret;
    }

    protected void configurePage( Page ret, PageParameters params )
    {
        if ( ret instanceof HeadsUpPage )
        {
            HeadsUpPage page = ( (HeadsUpPage) ret );

            Application app = ApplicationPageMapper.get().getApplication( ret.getPageClass() );
            if ( app == null || ApplicationPageMapper.isHomeApp( app ) )
            {
                app = ApplicationPageMapper.get().getApplication( "dashboard" );
                if ( app == null )
                {
                    app = ApplicationPageMapper.get().getApplication( "home" );
                }
            }
            page.setApplication( app );

            if ( params != null )
            {
                page.setPageParameters( params );
            }

            page.layout();
        }
        else if ( ret instanceof AbstractFeed )
        {
            if ( params != null )
            {
                ( (AbstractFeed) ret ).setPageParameters( params );
            }
        }
        // TODO inject etc
    }
}
