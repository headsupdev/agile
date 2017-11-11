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

package org.headsupdev.agile.framework.setup;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.validation.EqualPasswordInputValidator;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.PropertyModel;
import org.headsupdev.agile.core.PrivateConfiguration;
import org.headsupdev.agile.security.DefaultSecurityManager;
import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.storage.StoredUser;
import org.headsupdev.agile.storage.AdminRole;
import org.headsupdev.agile.web.components.OnePressSubmitButton;

/**
 * A simple panel for the setup to add an administrator to the system.
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class AddAdminPanel
    extends Panel
{
    private Class setupPage;

    public AddAdminPanel( String id, Class setupPage )
    {
        super( id );
        this.setupPage = setupPage;

        add( new CreateAdminForm( "create" ) );
    }

    class CreateAdminForm
        extends Form
    {
        String password, password2, email;
        String firstname, lastname;

        public CreateAdminForm( String id )
        {
            super(id);

            add( new Label( "username", "admin" ) );
            add( new TextField<String>( "firstname", new PropertyModel<String>( this, "firstname" ) ) );
            add( new TextField<String>( "lastname", new PropertyModel<String>( this, "lastname" ) ) );
            add( new TextField<String>( "email", new PropertyModel<String>( this, "email" ) ).setRequired( true ) );

            PasswordTextField pass, pass2;
            pass = new PasswordTextField( "password", new PropertyModel<String>( this, "password" ) );
            pass2 = new PasswordTextField( "password2", new PropertyModel<String>( this, "password2" ) );
            add( pass.setRequired( true ) );
            add( pass2.setRequired( true ) );

            add( new EqualPasswordInputValidator( pass, pass2 ) );
            add( new OnePressSubmitButton( "submitUser" ) );
        }

        public void onSubmit()
        {
            StoredUser created = new StoredUser( "admin" );
            created.setPassword( password );
            created.setEmail( email );
            created.setFirstname( firstname );
            created.setLastname( lastname );

            created.addRole( Manager.getSecurityInstance().getRoleById( ( new AdminRole() ).getId() ) );

            ( (DefaultSecurityManager) Manager.getSecurityInstance() ).addUser( created );

            PrivateConfiguration.setSetupStep( PrivateConfiguration.STEP_ADMIN );
            setResponsePage( setupPage );
        }

        public String getPassword()
        {
            return password;
        }

        public void setPassword( String password )
        {
            this.password = password;
        }

        public String getPassword2()
        {
            return password2;
        }

        public void setPassword2( String password2 )
        {
            this.password2 = password2;
        }

        public String getEmail()
        {
            return email;
        }

        public void setEmail( String email )
        {
            this.email = email;
        }

        public String getFirstname()
        {
            return firstname;
        }

        public void setFirstname( String firstname )
        {
            this.firstname = firstname;
        }

        public String getLastname()
        {
            return lastname;
        }

        public void setLastname( String lastname )
        {
            this.lastname = lastname;
        }
    }
}
