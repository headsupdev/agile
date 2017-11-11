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

package org.headsupdev.agile.app.admin.configuration;

import org.headsupdev.agile.web.HeadsUpPage;
import org.headsupdev.agile.api.Permission;
import org.headsupdev.agile.security.permission.AdminPermission;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.Model;

/**
 * The abstract configuration page - render tabs so we can inherit and still have nice urls etc
 *
 * @author Andrew Williams
 * @since 1.0
 */
public abstract class ConfigurationPage
    extends HeadsUpPage
{
    public Permission getRequiredPermission()
    {
        return new AdminPermission();
    }

    @Override
    public void layout()
    {
        super.layout();

        WebMarkupContainer tab = new WebMarkupContainer( "apptab" );
        tab.add( new AttributeModifier( "class", true, new Model<String>()
        {
            @Override
            public String getObject()
            {
                if ( ConfigurationPage.this instanceof ApplicationsConfiguration )
                {
                    return "selected";
                }

                return null;
            }
        } ) );
        tab.add( new BookmarkablePageLink( "link", ApplicationsConfiguration.class, getProjectPageParameters() ) );
        add( tab );

        tab = new WebMarkupContainer( "projtab" );
        tab.add( new AttributeModifier( "class", true, new Model<String>()
        {
            @Override
            public String getObject()
            {
                if ( ConfigurationPage.this instanceof ProjectConfiguration )
                {
                    return "selected";
                }

                return null;
            }
        } ) );
        tab.add( new BookmarkablePageLink( "link", ProjectConfiguration.class, getProjectPageParameters() ) );
        add( tab );

        tab = new WebMarkupContainer( "systab" );
        tab.add( new AttributeModifier( "class", true, new Model<String>()
        {
            @Override
            public String getObject()
            {
                if ( ConfigurationPage.this instanceof SystemConfiguration )
                {
                    return "selected";
                }

                return null;
            }
        } ) );
        tab.add( new BookmarkablePageLink( "link", SystemConfiguration.class, getProjectPageParameters() ) );
        add( tab );

        tab = new WebMarkupContainer( "nottab" );
        tab.add( new AttributeModifier( "class", true, new Model<String>()
        {
            @Override
            public String getObject()
            {
                if ( ConfigurationPage.this instanceof NotifiersConfiguration )
                {
                    return "selected";
                }

                return null;
            }
        } ) );
        tab.add( new BookmarkablePageLink( "link", NotifiersConfiguration.class, getProjectPageParameters() ) );
        add( tab );

        tab = new WebMarkupContainer( "uptab" );
        tab.add( new AttributeModifier( "class", true, new Model<String>()
        {
            @Override
            public String getObject()
            {
                if ( ConfigurationPage.this instanceof UpdatesConfiguration )
                {
                    return "selected last";
                }

                return "last";
            }
        } ) );
        tab.add( new BookmarkablePageLink( "link", UpdatesConfiguration.class, getProjectPageParameters() ) );
        add( tab );
    }

    @Override
    public String getTitle()
    {
        return "Configuration";
    }
}
