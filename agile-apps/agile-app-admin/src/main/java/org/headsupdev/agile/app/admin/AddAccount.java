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

package org.headsupdev.agile.app.admin;

import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.security.permission.AccountCreatePermission;
import org.headsupdev.agile.storage.StoredProject;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.validation.EqualPasswordInputValidator;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.PageParameters;
import org.headsupdev.agile.web.HeadsUpPage;
import org.headsupdev.agile.web.MountPoint;
import org.headsupdev.agile.api.Permission;
import org.headsupdev.agile.api.User;
import org.headsupdev.agile.storage.StoredUser;
import org.headsupdev.agile.storage.MemberRole;
import org.headsupdev.agile.web.components.OnePressButton;

import java.util.HashSet;
import java.util.Set;

/**
 * Create account page
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@MountPoint( "add-account" )
public class AddAccount
    extends HeadsUpPage
{
    public Permission getRequiredPermission()
    {
        return new AccountCreatePermission();
    }

    public void layout()
    {
        super.layout();

        add( CSSPackageResource.getHeaderContribution( getClass(), "admin.css" ));

        add( new CreateUserForm( "create" ) );
    }

    protected boolean isSetupPage()
    {
        return true;
    }

    @Override
    public String getTitle()
    {
        return "Add Account";
    }

    class CreateUserForm extends Form
    {
        String username, password, password2, email, telephone;
        String firstname, lastname = "";

        public CreateUserForm( String id )
        {
            super(id);

            TextField userField = new TextField( "username", new PropertyModel( this, "username" ) );
            add( userField.setRequired( true ) );
            add( new TextField( "firstname", new PropertyModel( this, "firstname" ) ) );
            add( new TextField( "lastname", new PropertyModel( this, "lastname" ) ) );
            add( new TextField( "email", new PropertyModel( this, "email" ) ).setRequired( true ) );
            add( new TextField<String>( "telephone", new PropertyModel<String>( this, "telephone" ) ) );

            PasswordTextField pass, pass2;
            pass = new PasswordTextField( "password", new PropertyModel( this, "password" ) );
            pass2 = new PasswordTextField( "password2", new PropertyModel( this, "password2" ) );
            add( pass.setRequired( true ) );
            add( pass2.setRequired( true ) );

            add( new EqualPasswordInputValidator( pass, pass2 ) );
            add( new OnePressButton( "submitUser" ) );
        }

        public void onSubmit()
        {
            User exists = getSecurityManager().getUserByUsername( username );
            if ( exists != null )
            {
                info( "An account with username " + username + " already exists" );
                return;
            }

            StoredUser created = new StoredUser( username );
            created.setPassword( password );
            created.setEmail( email );
            created.setTelephone( telephone );
            created.setFirstname( firstname );
            created.setLastname( lastname );

            // add the user to all loaded projects
            created.addRole( getSecurityManager().getRoleById( ( new MemberRole() ).getId() ) );
            HashSet<Project> allProjects = new HashSet<Project>( getStorage().getProjects() );
            allProjects.add( StoredProject.getDefault() );
            created.setProjects( allProjects );

            // bi-directional relationship
            for ( Project project : allProjects )
            {
                project.getUsers().add( created );
            }

            // and force them into the "all" project too
            Set<User> defaultProjectMembers = StoredProject.getDefault().getUsers();
            defaultProjectMembers.add( created );
            StoredProject.setDefaultProjectMembers( defaultProjectMembers );

            ( (AdminApplication) getHeadsUpApplication() ).addUser( created );

            PageParameters params = new PageParameters();
            params.add( "username", created.getUsername() );
            setResponsePage( getPageClass( "account" ), params );
        }

        public String getUsername()
        {
            return username;
        }

        public void setUsername( String username )
        {
            this.username = username;
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

        public String getTelephone()
        {
            return telephone;
        }

        public void setTelephone( String telephone )
        {
            this.telephone = telephone;
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
