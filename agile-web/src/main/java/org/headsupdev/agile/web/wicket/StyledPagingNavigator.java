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

package org.headsupdev.agile.web.wicket;

import org.apache.wicket.markup.html.navigation.paging.IPageable;
import org.apache.wicket.markup.html.navigation.paging.IPagingLabelProvider;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;

/**
 * An overriding paged navigation view for adding styling
 * <p/>
 * Created: 22/01/2012
 *
 * @author Andrew Williams
 * @since 2.0
 */
public class StyledPagingNavigator extends PagingNavigator
{
    public StyledPagingNavigator(String id, IPageable pageable)
    {
        super( id, pageable );
    }

    public StyledPagingNavigator(String id, IPageable pageable, IPagingLabelProvider labelProvider)
    {
        super( id, pageable, labelProvider );
    }
}
