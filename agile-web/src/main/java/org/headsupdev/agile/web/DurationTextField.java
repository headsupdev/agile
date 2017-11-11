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
package org.headsupdev.agile.web;

import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.convert.IConverter;
import org.headsupdev.agile.storage.issues.Duration;
import org.headsupdev.agile.web.wicket.DurationConverter;

/**
 * Created by Gordon Edwards on 01/07/2014.
 * @since 2.1
 */
public class DurationTextField
        extends TextField<Duration>
{
    public DurationTextField( String id )
    {
        super( id );
    }

    public DurationTextField( String id, IModel<Duration> model )
    {
        super( id, model );
    }

    @Override
    public IConverter getConverter( Class<?> type )
    {
        return new DurationConverter();
    }

}
