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

package org.headsupdev.agile.app.ci;

import org.headsupdev.agile.storage.ci.Build;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.model.Model;

/**
 * A class attribute modifier for highlighting test failures etc
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class CITestStatusModifier
    extends AttributeModifier
{
    public CITestStatusModifier( final String className, final Build build, final String type )
    {
        super ( "class", true, new Model<String>()
        {
            public String getObject()
            {
                if ( type.equals( "tests" ) )
                {
                    if ( build.getTests() > 0 )
                    {
                        return className + " testpass";
                    }
                    return className + " testwarn";
                }

                if ( type.equals( "failures" ) )
                {
                    if ( build.getFailures() > 0 )
                    {
                        return className + " testfail";
                    }
                }
                else if ( type.equals( "failerror" ) )
                {
                    if ( build.getFailures() > 0 || build.getErrors() > 0 )
                    {
                        return className + " testfail";
                    }
                }
                else if ( type.equals( "errors" ) )
                {
                    if ( build.getErrors() > 0 )
                    {
                        return className + " testerror";
                    }
                }
                else if ( type.equals( "warnings" ) )
                {
                    if ( build.getWarnings() > 0 )
                    {
                        return className + " testwarn";
                    }
                }

                return className + " testpass";
            }
        } );
    }
}
