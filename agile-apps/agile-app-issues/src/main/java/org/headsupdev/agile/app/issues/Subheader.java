/*
 * HeadsUp Agile
 * Copyright 2014 Heads Up Development Ltd.
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

package org.headsupdev.agile.app.issues;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.headsupdev.agile.storage.issues.Issue;

/**
 * Created by Gordon Edwards on 11/08/2014.
 *
 * Alternative <h2> header
 */
public class Subheader
        extends Panel
{
    public Subheader( String id, String preamble, Issue issue )
    {
        super( id );
        add( new Label( "preamble", preamble ) );
        add( new Label( "issueId", "" + issue.getId() ) );
        add( new Label( "summary", issue.getSummary() ) );
    }
}
