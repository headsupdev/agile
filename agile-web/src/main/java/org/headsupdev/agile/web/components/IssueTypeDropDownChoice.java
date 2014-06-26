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


package org.headsupdev.agile.web.components;

import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.headsupdev.agile.web.components.issues.IssueUtils;

import java.util.List;

/**
 * A drop down choice component that displays active users and can include a specific user if not active.
 *
 * Created: 24/06/2014
 *
 * @author Gordon Edwards
 * @since 2.0
 */


public class IssueTypeDropDownChoice
        extends DropDownChoice<Integer>
        implements IChoiceRenderer<Integer>
{
    public IssueTypeDropDownChoice( String id, List<Integer> choices )
    {

        super( id, choices );
        setChoiceRenderer( this );
    }

    public boolean isNullValid()
    {
        return false;
    }

    @Override
    public Object getDisplayValue( Integer integer )
    {
        return IssueUtils.getTypeName( integer );
    }

    @Override
    public String getIdValue( Integer integer, int i )
    {
        return Integer.toString( i );
    }
}

