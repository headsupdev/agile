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

package org.headsupdev.agile.web;

import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.PageParameters;
import org.headsupdev.agile.api.MenuLink;

/**
 * TODO document me
 *
 * @author Andrew Williams
 * @since 1.0
 */
public class BookmarkableMenuLink implements MenuLink
{
    private Class page;
    private PageParameters params;
    private String label;

    public BookmarkableMenuLink( Class page, PageParameters params, String label )
    {
        this.page = page;
        this.params = params;
        this.label = label;
    }

    public String getLabel()
    {
        return label;
    }

    public final void onClick() {
        // ignored for this type of link
    }

    public BookmarkablePageLink getLink()
    {
        return new BookmarkablePageLink( "submenu-link", page, params );
    }
}