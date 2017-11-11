/*
 * HeadsUp Agile
 * Copyright 2009-2017 Heads Up Development.
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

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Check;
import org.apache.wicket.markup.html.form.CheckGroup;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.Model;
import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.api.Permission;
import org.headsupdev.agile.api.Role;
import org.headsupdev.agile.api.User;
import org.headsupdev.agile.security.DefaultSecurityManager;
import org.headsupdev.agile.security.permission.AdminPermission;
import org.headsupdev.agile.storage.AnonymousRole;
import org.headsupdev.agile.storage.TesterRole;
import org.headsupdev.agile.storage.HibernateUtil;
import org.headsupdev.agile.storage.StoredUser;
import org.headsupdev.agile.web.ApplicationPageMapper;
import org.headsupdev.agile.web.HeadsUpPage;
import org.headsupdev.agile.web.HeadsUpSession;
import org.headsupdev.agile.web.MountPoint;
import org.headsupdev.agile.web.components.GravatarLinkPanel;
import org.headsupdev.agile.web.components.OnePressSubmitButton;
import org.headsupdev.agile.web.components.StripedListView;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.*;

/**
 * The HeadsUp about page.
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@MountPoint("permissions")
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
            add( new OnePressSubmitButton( "userRolesSubmit" ) );

            add( new StripedListView<User>( "userlist", users )
            {
                protected void populateItem( ListItem<User> listItem )
                {
                    super.populateItem( listItem );

                    final User user = listItem.getModelObject();
                    final boolean builtin;
                    listItem.add( new GravatarLinkPanel( "gravatar", user, HeadsUpPage.SMALL_AVATAR_EDGE_LENGTH ) );

                    Model<String> usernameModel = new Model<String>()
                    {
                        @Override
                        public String getObject()
                        {
                            String username = user.getUsername();
                            if ( ( (StoredUser) user ).isDisabled() )
                            {
                                username += " (disabled)";
                            }

                            return username;
                        }
                    };

                    AttributeModifier modifier = new AttributeModifier( "class", true, new Model<String>()
                    {
                        @Override
                        public String getObject()
                        {
                            if ( ( (StoredUser) user ).isDisabled() )
                            {
                                return "disabled";
                            }

                            return "";
                        }
                    } );

                    if ( user.equals( HeadsUpSession.ANONYMOUS_USER ) )
                    {
                        listItem.add( new Label( "username", usernameModel ).add( modifier ) );
                        Label usernameInLink = new Label( "usernameInLink" );
                        WebMarkupContainer userLink = new WebMarkupContainer( "userLink" );
                        userLink.add( usernameInLink.setVisible( false ) );
                        listItem.add( userLink.setVisible( false ) );
                        user.getRoles().add( new AnonymousRole() );
                        builtin = true;
                    }
                    else if ( user.getRoles().contains( new TesterRole() ) )
                    {
                        listItem.add( new Label( "username", usernameModel ).setVisible( false ) );
                        Label usernameInLink = new Label( "usernameInLink", usernameModel );
                        usernameInLink.add( modifier );
                        PageParameters params = new PageParameters();
                        params.add( "username", user.getUsername() );
                        params.add( "silent", "true" );
                        builtin = true;
                        BookmarkablePageLink userLink = new BookmarkablePageLink( "userLink", ApplicationPageMapper.get().getPageClass( "account" ), params );
                        userLink.add( usernameInLink );
                        listItem.add( userLink );
                    }
                    else
                    {
                        listItem.add( new Label( "username", usernameModel ).setVisible( false ) );
                        Label usernameInLink = new Label( "usernameInLink", usernameModel );
                        usernameInLink.add( modifier );
                        PageParameters params = new PageParameters();
                        params.add( "username", user.getUsername() );
                        params.add( "silent", "true" );
                        builtin = false;
                        BookmarkablePageLink userLink = new BookmarkablePageLink( "userLink", ApplicationPageMapper.get().getPageClass( "account" ), params );
                        userLink.add( usernameInLink );
                        listItem.add( userLink );
                    }

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
                            !builtin ) );

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
                            } ) );
                        }
                    } );
                    listItem.add( group.setEnabled( !builtin ) );
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
                            boolean disabled = role.equals( new TesterRole() );

                            listItem.add( new Check<Role>( "role", new Model<Role>( role ) ).setEnabled( !disabled ) );
                        }

                    } );
                    listItem.add( group );
                }
            } );
            add( new OnePressSubmitButton( "rolePermissionsSubmit" ) );
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

