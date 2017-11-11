/*
 * HeadsUp Agile
 * Copyright 2014 Heads Up Development.
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

package org.headsupdev.agile.web.components;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.headsupdev.agile.storage.CommentableEntity;
import org.headsupdev.agile.storage.issues.Issue;

/** An alternative <h2> header
 *
 * Created on 11/08/2014
 * @author Gordon Edwards
 * @version $Id$
 * @since 2.1
 */

public class Subheader<T extends CommentableEntity>
        extends Panel
{
    protected String preamble;
    public Subheader( String id, String preamble, CommentableEntity commentable )
    {
        super( id );
        this.preamble = preamble;
        add( new Label( "preamble", preamble ) );
    }

    public String toString()
    {
        return preamble;
    }
}
