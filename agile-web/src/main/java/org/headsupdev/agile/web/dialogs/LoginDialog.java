/*
 * HeadsUp Agile
 * Copyright 2009-2014 Heads Up Development.
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

import org.apache.wicket.protocol.http.WebRequest;
import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.api.Page;
import org.headsupdev.agile.api.util.HashUtil;
import org.headsupdev.agile.security.permission.AccountCreatePermission;
import org.headsupdev.agile.web.HeadsUpPage;
import org.headsupdev.agile.web.HeadsUpSession;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.protocol.http.WebResponse;
import org.headsupdev.agile.web.auth.WebLoginManager;

/**
 * The main login panel for HeadsUp Agile - used for the login screen or within a popover dialog.
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class LoginDialog
    extends Panel
{
    private HeadsUpPage owner;
    private boolean popup;

    public LoginDialog( String id, boolean isDialog, HeadsUpPage owner )
    {
        super( id );
        this.owner = owner;
        popup = isDialog;

        add( new Label( "product", Manager.getStorageInstance().getGlobalConfiguration().getProductName() ) );
        add( new LoginForm( "login" ) );

        Class<? extends Page> addUserClass = owner.getPageClass( "admin/add-account" );
        WebMarkupContainer register = new WebMarkupContainer( "register" );
        if ( addUserClass != null )
        {
            register.add( new BookmarkablePageLink( "link", addUserClass ) );
        }

        boolean canRegister = addUserClass != null &&
                owner.userHasPermission( HeadsUpSession.ANONYMOUS_USER, new AccountCreatePermission(), null );
        add( register.setVisible( canRegister ) );
        add( new WebMarkupContainer( "noregister" ).setVisible( !canRegister ) );
    }

    class LoginForm
        extends Form
    {
        private String username, password;
        private boolean remember = true;

        public LoginForm( String id )
        {
            super( id );

            add( new TextField<String>( "username", new PropertyModel<String>( this, "username" ) ).setRequired( true ) );
            add( new PasswordTextField( "password", new PropertyModel<String>( this, "password" ) ).setRequired( true ) );
            add( new CheckBox( "remember", new PropertyModel<Boolean>( this, "remember" ) ) );

            add( new AjaxFallbackButton( "cancel", this )
            {
                @Override
                protected void onSubmit( AjaxRequestTarget target, Form form )
                {
                    if ( target == null || !popup )
                    {
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
                    else
                    {
                        LoginDialog.this.setVisible( false );
                        target.addComponent( LoginDialog.this );
                    }
                }
            }.setDefaultFormProcessing( false ) );
        }

        protected void onSubmit()
        {
            WebLoginManager loginManager = WebLoginManager.getInstance();
            loginManager.removeRememberCookie( ( (WebResponse) getResponse() ).getHttpServletResponse() );

            org.headsupdev.agile.api.User user = owner.getSecurityManager().getUserByUsername( username );
            if ( user == null )
            {
                info( "Invalid username" );
                return;
            }

            String encodedPass = HashUtil.getMD5Hex( password );

            if ( !encodedPass.equals( user.getPassword() ) )
            {
                info( "Incorrect password" );
                return;
            }

            if ( !user.canLogin() )
            {
                info( "Account is not currently active" );
                return;
            }

            loginManager.logUserIn( user, remember, ( (WebRequest) getRequest() ).getHttpServletRequest(),
                    ( (WebResponse) getResponse() ).getHttpServletResponse() );

            if ( !continueToOriginalDestination() )
            {
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
        }
    }
}
