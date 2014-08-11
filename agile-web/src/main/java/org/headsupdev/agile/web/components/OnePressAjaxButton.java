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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;

/**
 * Created by Gordon Edwards on 11/08/2014.
 *
 * An AjaxButton that disables itself when pressed
 */
public class OnePressAjaxButton
        extends AjaxButton
{
    public OnePressAjaxButton( String id, Form<?> form )
    {
        super( id, form );
    }

    @Override
    protected void onSubmit( AjaxRequestTarget ajaxRequestTarget, Form<?> form )
    {
    }

    @Override
    protected String getOnClickScript()
    {
        return "this.disabled=true; this.form.submit();";
    }
}
