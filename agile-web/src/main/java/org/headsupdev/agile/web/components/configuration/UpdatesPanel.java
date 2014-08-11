/*
 * HeadsUp Agile
 * Copyright 2009-2014 Heads Up Development Ltd.
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

package org.headsupdev.agile.web.components.configuration;

import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.headsupdev.agile.core.PrivateConfiguration;
import org.headsupdev.agile.web.components.OnePressButton;

/**
 * Using this panel user can setup checking for updates (even testing versions).
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class UpdatesPanel
        extends Panel
{
    private Class setupPage;

    public UpdatesPanel( String id, Class setupPage )
    {
        super( id );
        this.setupPage = setupPage;

        add( new UpdatesForm( "updates" ).add( new OnePressButton( "submitUpdates" ) ) );
    }

    class UpdatesForm
            extends Form
    {
        private boolean updatesEnabled = PrivateConfiguration.getUpdatesEnabled();
        private boolean betaEnabled = PrivateConfiguration.getBetaUpdatesEnabled();

        public UpdatesForm( String id )
        {
            super( id );
            add( new CheckBox( "updatesEnabled", new PropertyModel( this, "updatesEnabled" ) ) );
            add( new CheckBox( "betaEnabled", new PropertyModel( this, "betaEnabled" ) ) );
        }

        public void onSubmit()
        {
            PrivateConfiguration.setUpdatesEnabled( updatesEnabled );
            PrivateConfiguration.setBetaUpdatesEnabled( betaEnabled );

            if ( setupPage != null )
            {
                PrivateConfiguration.setSetupStep( PrivateConfiguration.STEP_UPDATES );
                setResponsePage( setupPage );
            }
        }
    }
}
