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

package org.headsupdev.agile.app.dashboard;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.validation.EqualPasswordInputValidator;
import org.apache.wicket.PageParameters;
import org.apache.wicket.model.PropertyModel;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.headsupdev.agile.storage.StoredUser;
import org.headsupdev.agile.storage.HibernateStorage;
import org.headsupdev.agile.api.Permission;
import org.headsupdev.agile.app.dashboard.permission.MemberEditPermission;
import org.headsupdev.agile.web.MountPoint;
import org.headsupdev.agile.web.HeadsUpPage;
import org.headsupdev.agile.web.HeadsUpSession;
import org.headsupdev.agile.web.BookmarkableMenuLink;
import org.headsupdev.agile.security.permission.AdminPermission;

/**
 * A simple page for changing a users password
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@MountPoint( "changepassword" )
public class ChangePassword
    extends HeadsUpPage
{
    private String username;

    public Permission getRequiredPermission()
    {
        return new MemberEditPermission();
    }

    public void layout()
    {
        super.layout();

        username = getPageParameters().getString( "username" );
        if ( username == null )
        {
            username = getSession().getUser().getUsername();
        }

        org.headsupdev.agile.api.User user = getSecurityManager().getUserByUsername( username );
        if ( user == null || user.equals( HeadsUpSession.ANONYMOUS_USER ) )
        {
            notFoundError();
            return;
        }

        boolean me = username.equals( getSession().getUser().getUsername() );
        if ( !me )
        {
            requirePermission( new AdminPermission() );
        }
        addLink( new BookmarkableMenuLink( getPageClass( "account" ), getPageParameters(), "view" ) );
        addLink( new BookmarkableMenuLink( getPageClass( "subscriptions" ), getPageParameters(), "subscriptions" ) );

        add( new ChangePasswordForm( "edituser", user, me ) );
    }

    @Override
    public String getTitle()
    {
        return "Change Password for " + username;
    }

    class ChangePasswordForm extends Form
    {
        private org.headsupdev.agile.api.User user;
        private boolean me;
        private String pass1, pass2;

        public ChangePasswordForm( String id, org.headsupdev.agile.api.User user, boolean me )
        {
            super( id );
            this.user = user;
            this.me = me;

            PasswordTextField pass1, pass2;
            add( pass1 = new PasswordTextField( "password", new PropertyModel( this, "pass1" ) ) );
            add( pass2 = new PasswordTextField( "password2", new PropertyModel( this, "pass2" ) ) );

            add( new EqualPasswordInputValidator( pass1, pass2 ) );
        }

        public void onSubmit()
        {
            Session session = ( (HibernateStorage) getStorage() ).getHibernateSession();
            Transaction tx = session.beginTransaction();
            user = (org.headsupdev.agile.api.User) session.merge( user );

            ( (StoredUser) user ).setPassword( pass1 );
            if ( me ) {
                ( (HeadsUpSession) getSession() ).setUser( user );
            }

            session.update( user );
            tx.commit();

            PageParameters params = new PageParameters();
            params.add( "username", user.getUsername() );
            setResponsePage( getPageClass( "account" ), params );
        }
    }
}