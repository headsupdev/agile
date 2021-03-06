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

import org.apache.wicket.validation.validator.StringValidator;
import org.apache.wicket.validation.IValidatable;
import org.headsupdev.agile.api.ConfigurationItem;
import org.quartz.CronExpression;

/**
 * A quick validator for cron expressions.
 * Also allowable is "never" for something that should not be scheduled.
 *
 * @author Andrew Williams
 * @since 1.0
 */
public class CronValidator
    extends StringValidator
{
    protected void onValidate( IValidatable<String> validatable )
    {
        if ( ConfigurationItem.CRON_VALUE_NEVER.equals( validatable.getValue() ) )
        {
            // nice and simple
            return;
        }

        try
        {
            new CronExpression( validatable.getValue() );
        }
        catch ( Exception e )
        {
            error( validatable /* TODO figure how to pass the value */ );
        }
    }
}
