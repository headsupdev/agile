/*
 * HeadsUp Agile
 * Copyright 2009-2012 Heads Up Development Ltd.
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

import org.headsupdev.support.java.Base64;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.web.components.BooleanImage;
import org.headsupdev.agile.core.PrivateConfiguration;

import java.net.URL;
import java.net.URLConnection;
import java.net.HttpURLConnection;
import java.io.IOException;

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

        add( new UpdatesForm( "updates" ) );
    }

    class UpdatesForm extends Form
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
