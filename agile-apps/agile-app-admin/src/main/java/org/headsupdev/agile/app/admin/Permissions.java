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

package org.headsupdev.agile.app.admin;

import org.headsupdev.agile.api.*;
import org.headsupdev.agile.security.DefaultSecurityManager;
import org.headsupdev.agile.storage.*;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.CheckGroup;
import org.apache.wicket.markup.html.form.Check;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.string.Strings;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.headsupdev.agile.web.components.StripedListView;
import org.headsupdev.agile.web.HeadsUpPage;
import org.headsupdev.agile.web.HeadsUpSession;
import org.headsupdev.agile.web.MountPoint;
import org.headsupdev.agile.security.permission.AdminPermission;

import java.util.*;

/**
 * The HeadsUp about page.
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@MountPoint( "permissions" )
public class Permissions
    extends HeadsUpPage
{
    public Permission getRequiredPermission()
    {
        return new AdminPermission();
    }

    public void layout()
    {
        super.layout();

        add( CSSPackageResource.getHeaderContribution( getClass(), "admin.css" ) );

        add( new UserRolesForm( "users" ) );
        add( new RolePermissionsForm( "roles" ) );
    }

    @Override
    public String getTitle()
    {
        return "Manage User Roles";
    }

    class UserRolesForm
        extends Form
    {
        private List<User> users;
        public UserRolesForm( String id )
        {
            super( id );

            users = getSecurityManager().getUsers();

            add( new ListView<Role>( "rolenames", getSecurityManager().getRoles() )
            {
                protected void populateItem( ListItem<Role> listItem )
                {
                    final Role role = listItem.getModelObject();

                    listItem.add( new Label( "rolename", role.getId() ) );
                    Link delete = new Link( "deleterole" )
                    {
                        @Override
                        public void onClick()
                        {
                            for ( User user : getSecurityManager().getUsers() )
                            {
                                boolean me = user.getUsername().equals( ( (HeadsUpSession) getSession() ).getUser().getUsername() );

                                if ( user.getRoles().contains( role ) )
                                {
                                    user.getRoles().remove( role );

                                    if ( me )
                                    {
                                        ( (HeadsUpSession) getSession() ).setUser( user );
                                    }
                                }
                            }

                            ( (DefaultSecurityManager) Manager.getSecurityInstance() ).removeRole( role );
                            setResponsePage( Permissions.class );
                        }
                    };
                    delete.add( new Image( "delete-icon", new ResourceReference( HeadsUpPage.class, "images/delete.png" ) ) );
                    listItem.add( delete.setVisible( !role.isBuiltin() ) );
                }
            } );
            Link add = new BookmarkablePageLink( "addrole", AddRole.class );
            add.add( new Image( "add-icon", new ResourceReference( HeadsUpPage.class, "images/add.png" ) ) );
            add( add );

            add( new StripedListView<User>( "userlist", users )
            {
                protected void populateItem( ListItem<User> listItem )
                {
                    super.populateItem( listItem );

                    final User user = listItem.getModelObject();
                    final boolean anon;
                    if ( user.equals( HeadsUpSession.ANONYMOUS_USER ) )
                    {
                        user.getRoles().add( new AnonymousRole() );
                        anon = true;
                    }
                    else
                    {
                        anon = false;
                    }

                    listItem.add( new Label( "username", new Model<String>()
                    {
                        @Override
                        public String getObject() {
                            String username = user.getUsername();
                            if ( ( (StoredUser) user ).isDisabled() )
                            {
                                username += " (disabled)";
                            }

                            return username;
                        }
                    } ).add( new AttributeModifier( "class", true, new Model<String>() {
                        @Override
                        public String getObject()
                        {
                            if ( !user.canLogin() )
                            {
                                return "disabled";
                            }

                            return "";
                        }
                    } ) ) );
                    Link disable = new Link( "disable" )
                    {
                        @Override
                        public void onClick()
                        {
                            ( (StoredUser) user ).setDisabled( !( (StoredUser) user ).isDisabled() );
                            HibernateUtil.getCurrentSession().update( user );
                        }
                    };
                    disable.add( new Image( "disable-icon", new ResourceReference( HeadsUpPage.class, "images/delete.png" ) ) );
                    listItem.add( disable.setVisible( !user.equals( ( (HeadsUpSession) getSession() ).getUser() ) &&
                            !user.equals( HeadsUpSession.ANONYMOUS_USER ) ) );

                    CheckGroup group = new CheckGroup<Role>( "rolelist", user.getRoles() );
                    group.add( new ListView<Role>( "rolegroup", getSecurityManager().getRoles() )
                    {
                        protected void populateItem( ListItem<Role> listItem )
                        {
                            final Role role = listItem.getModelObject();

                            listItem.add( new Check<Role>( "role", new Model<Role>()
                            {
                                public Role getObject()
                                {
                                    return role;
                                }
                            } ).setEnabled( !anon ) );
                        }
                    } );
                    listItem.add( group );
                }
            } );
        }

        protected void onSubmit()
        {
            Session session = HibernateUtil.getCurrentSession();
            Transaction tx = session.beginTransaction();

            for ( User user : users )
            {
                user = (User) session.merge( user );
                if ( user.getUsername().equals( ( (HeadsUpSession) getSession() ).getUser().getUsername() ) )
                {
                    ( (HeadsUpSession) getSession() ).setUser( user );
                }
            }

            tx.commit();
        }
    }

    class RolePermissionsForm
            extends Form
    {
        private List<Permission> permissionNames;
        private Map<Permission, List<Role>> permissions = new HashMap<Permission, List<Role>>();

        public RolePermissionsForm( String id )
        {
            super( id );
            convertToMap( permissions );
            permissionNames = getSecurityManager().getPermissions();

            Collections.sort( permissionNames, new PermissionIdComparator() );

            add( new ListView<Role>( "rolenames", getSecurityManager().getRoles() )
            {
                protected void populateItem( ListItem<Role> listItem )
                {
                    Role role = listItem.getModelObject();

                    listItem.add( new Label( "rolename", role.getId() ) );
                }
            } );

            add( new StripedListView<Permission>( "permissionlist", permissionNames )
            {
                protected void populateItem( ListItem<Permission> listItem )
                {
                    super.populateItem( listItem );

                    Permission permission = listItem.getModelObject();

                    listItem.add( new Label( "permission", permission.getId().replace( '-', ' ' ) ) );
                    CheckGroup group = new CheckGroup<Role>( "rolelist", permissions.get( permission ) );
                    group.add( new ListView<Role>( "rolegroup", getSecurityManager().getRoles() )
                    {
                        protected void populateItem( ListItem<Role> listItem )
                        {
                            final Role role = listItem.getModelObject();

                            listItem.add( new Check<Role>( "role", new Model<Role>( role ) ) );
                        }

                    } );
                    listItem.add( group );
                }
            } );
        }

        protected void onSubmit()
        {
            Session session = HibernateUtil.getCurrentSession();
            List<Role> roles = convertFromMap( permissions );
            for ( Role role : roles )
            {
                session.update( role );
            }
        }

        private void convertToMap( Map<Permission, List<Role>> map )
        {
            for ( Permission permission : getSecurityManager().getPermissions() )
            {
                List<Role> roles = new LinkedList<Role>();

                for ( Role role : getSecurityManager().getRoles() )
                {
                    if ( role.getPermissions().contains( permission.getId() ) )
                    {
                        roles.add( role );
                    }
                }

                map.put( permission, roles );
            }
        }

        private List<Role> convertFromMap( Map<Permission, List<Role>> map )
        {
            List<Role> ret = getSecurityManager().getRoles();

            for ( Role role : ret )
            {
                role.getPermissions().clear();

                for ( Permission permission : map.keySet() )
                {
                    List<Role> roleList = map.get( permission );
                    if ( roleList != null && roleList.contains( role ) )
                    {
                        role.getPermissions().add( permission.getId() );
                    }
                }
            }

            return ret;
        }
    }
}

