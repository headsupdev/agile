/*
 * HeadsUp Agile
 * Copyright 2009-2013 Heads Up Development.
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

import org.headsupdev.agile.api.Permission;
import org.headsupdev.agile.web.components.FormattedDateModel;
import org.headsupdev.agile.web.HeadsUpPage;
import org.headsupdev.agile.web.MountPoint;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.WebMarkupContainer;

/**
 * The HeadsUp about page.
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@MountPoint( "about" )
public class About
    extends HeadsUpPage
{
    public Permission getRequiredPermission()
    {
        return null;
    }

    public void layout()
    {
        super.layout();
        add( CSSPackageResource.getHeaderContribution( getClass(), "about.css" ) );
        add( new AboutPanel( "body" ) );

        add( new Label( "productname", getStorage().getGlobalConfiguration().getProductName() ) );
        add( new Label( "version", getStorage().getGlobalConfiguration().getBuildVersion() ) );
        add( new Label( "builddate", new FormattedDateModel( getStorage().getGlobalConfiguration().getBuildDate(),
                getSession().getTimeZone() ) ) );

        add( new WebMarkupContainer( "update" ).setVisible( getManager().isUpdateAvailable() ) );
    }

    @Override
    public String getPageTitle()
    {
        return "About" + PAGE_TITLE_SEPARATOR + super.getPageTitle();
    }
}
