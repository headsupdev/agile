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

package org.headsupdev.agile.web.dialogs;

import org.headsupdev.agile.api.User;
import org.headsupdev.agile.web.HeadsUpPage;
import org.headsupdev.agile.web.HeadsUpSession;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.protocol.http.WebResponse;
import org.headsupdev.agile.web.auth.WebLoginManager;

/**
 * TODO document me
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class LogoutDialog
    extends Panel
{
    private HeadsUpPage owner;
    private boolean popup;

    public LogoutDialog( String id, boolean isDialog, final HeadsUpPage owner )
    {
        super( id );
        this.owner = owner;
        popup = isDialog;

        Form form = new Form( "dialogform" );
        form.add( new Button( "yes" )
        {
            @Override
            public void onSubmit()
            {
                WebLoginManager loginManager = WebLoginManager.getInstance();

                User user = ( (HeadsUpSession) getSession() ).getUser();
                loginManager.logUserOut( user, ( (WebResponse) getResponse() ).getHttpServletResponse() );

                Class previous = ( (HeadsUpSession) getSession() ).getPreviousPageClass();

                if ( previous != null )
                {
                    setResponsePage( previous, ( (HeadsUpSession) getSession() ).getPreviousPageParameters() );
                }
                else
                {
                    setResponsePage( owner.getPageClass( "" ) );
                }
            }

        }.setDefaultFormProcessing( false ) );
        form.add( new AjaxFallbackButton( "no", form )
        {
            @Override
            protected void onSubmit( AjaxRequestTarget target, Form form )
            {
                if ( target == null || !popup )
                {
                    Class previous = ( (HeadsUpSession) getSession() ).getPreviousPageClass();

                    if ( previous != null && !previous.getName().endsWith( "Logout" ) )
                    {
                        setResponsePage( previous, ( (HeadsUpSession) getSession() ).getPreviousPageParameters() );
                    }
                    else
                    {
                        setResponsePage( owner.getPageClass( "" ) );
                    }
                }
                else
                {
                    LogoutDialog.this.setVisible( false );
                    target.addComponent( LogoutDialog.this );
                }
            }
        }.setDefaultFormProcessing( false ) );
        add( form );
    }
}
