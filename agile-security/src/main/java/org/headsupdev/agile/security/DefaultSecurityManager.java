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

package org.headsupdev.agile.security;

import org.headsupdev.agile.security.permission.ProjectListPermission;
import org.headsupdev.agile.storage.*;
import org.hibernate.Transaction;
import org.hibernate.Session;
import org.hibernate.Query;

import java.util.*;
import java.io.Serializable;

import org.headsupdev.agile.api.*;
import org.headsupdev.agile.api.SecurityManager;
import org.headsupdev.agile.core.PrivateConfiguration;

/**
 * Simple SecurityManager implementation
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class DefaultSecurityManager
    implements SecurityManager, Serializable
{
    List<Permission> permissions = new LinkedList<Permission>();
    Map<String, Permission> permissionIdMap = new HashMap<String, Permission>();

    public void scanPermissions( Application application )
    {
        if ( !PrivateConfiguration.isInstalled() )
        {
            return;
        }

        for ( Permission perm : application.getPermissions() )
        {
            addPermission( perm );
        }

        // TODO figure why the last role_permission in the iteration is sometimes not committed
    }

    void addPermission( Permission perm )
    {
        String permID = perm.getId();

        if ( !PrivateConfiguration.getConfiguredPermissionIds().contains( permID ) ) {
            List<String> roles = perm.getDefaultRoles();
            boolean saved = true;

            if ( roles != null )
            {
                for ( String roleName : roles )
                {
                    Role role = getRoleById( roleName );
                    if ( role == null )
                    {
//                        System.err.println( "Could not add permission \"" + permID + "\" to missing role \"" +
//                                roleName + "\"" );
                        saved = false;
                        break;
                    }
                    else
                    {
                        if ( !role.getPermissions().contains( permID ) )
                        {
                            role.getPermissions().add( permID );
                        }
                    }
                }
            }

            if ( saved )
            {
                PrivateConfiguration.addConfiguredPermissionId( permID );
            }
        }

        permissions.add( perm );
        permissionIdMap.put( permID, perm );
    }

    public List<User> getUsers()
    {
        Session session = ( (HibernateStorage) Manager.getStorageInstance() ).getHibernateSession();
        Transaction tx = session.beginTransaction();
        List<User> list = session.createQuery( "from StoredUser u order by username" ).list();
        tx.commit();

        Collections.sort( list );
        return list;
    }

    public List<User> getRealUsers()
    {
        Session session = ( (HibernateStorage) Manager.getStorageInstance() ).getHibernateSession();
        Transaction tx = session.beginTransaction();
        List<User> list = session.createQuery( "from StoredUser u where username != 'anonymous' and (disabled is null or disabled = 0) order by username" ).list();
        tx.commit();

        Collections.sort( list );
        return list;
    }

    public List<User> getRealUsersIncluding( User user )
    {
        List<User> users = getRealUsers();
        if ( !users.contains( user ) )
        {
            users.add( user );
        }

        Collections.sort( users );
        return users;
    }

    public User getUserByUsername( String username )
    {
        Session session = ( (HibernateStorage) Manager.getStorageInstance() ).getHibernateSession();
        Transaction tx = session.beginTransaction();
        Query q = session.createQuery( "from StoredUser u where UPPER(username) = UPPER(:username)" );
        q.setString( "username", username );
        User ret = (User) q.uniqueResult();
        tx.commit();

        return ret;
    }

    public User getUserByUsernameEmailOrFullname( String userdetail )
    {
        String possibleUsername = userdetail;
        String possibleName = null, possibleEmail = null;
        int pos = userdetail.indexOf( '<' );
        int pos2 = userdetail.indexOf( '>', pos );
        if ( pos > -1 )
        {
            possibleName = userdetail.substring( 0, pos ).trim();

            if ( pos2 > -1 )
            {
                possibleEmail = userdetail.substring( pos + 1, pos2 ).trim();
            }
        }

        Session session = ( (HibernateStorage) Manager.getStorageInstance() ).getHibernateSession();
        Transaction tx = session.beginTransaction();
        Query q;
        if ( pos > -1 )
        {
            if ( pos2 > -1 )
            {
                q = session.createQuery( "from StoredUser u where UPPER(username) = UPPER(:username) or firstname||' '||lastname like :name or email = :email" );
                q.setString( "username", possibleUsername );
                q.setString( "name", possibleName );
                q.setString( "email", possibleEmail );
            }
            else
            {
                q = session.createQuery( "from StoredUser u where UPPER(username) = UPPER(:username) or firstname||' '||lastname like :name" );
                q.setString( "username", possibleUsername );
                q.setString( "name", possibleName );
            }
        }
        else
        {
            q = session.createQuery( "from StoredUser u where UPPER(username) = UPPER(:username)" );
            q.setString( "username", possibleUsername );

            if ( q.uniqueResult() == null && possibleUsername.contains( "@" ) )
            {
                q = session.createQuery( "from StoredUser u where email = :email" );
                q.setString( "email", possibleUsername );
            }
        }

        User ret = (User) q.uniqueResult();
        tx.commit();

        return ret;
    }

    public void addUser( User user )
    {
        ( (HibernateStorage) Manager.getStorageInstance() ).save( user );
    }

    public List<Role> getRoles()
    {
        Session session = ( (HibernateStorage) Manager.getStorageInstance() ).getHibernateSession();
        Transaction tx = session.beginTransaction();
        List<Role> list = session.createQuery( "from StoredRole r order by id" ).list();
        tx.commit();

        return list;
    }

    public Role getRoleById( String id )
    {
        Session session = ( (HibernateStorage) Manager.getStorageInstance() ).getHibernateSession();
        Transaction tx = session.beginTransaction();
        Query q = session.createQuery( "from StoredRole r where id = :id" );
        q.setString( "id", id );
        Role ret = (Role) q.uniqueResult();
        tx.commit();

        return ret;
    }

    public void addRole( Role role )
    {
        ( (HibernateStorage) Manager.getStorageInstance() ).save( role );
    }

    public void removeRole( Role role )
    {
        ( (HibernateStorage) Manager.getStorageInstance() ).delete( role );
    }

    public List<Permission> getPermissions()
    {
        return permissions;
    }

    public Permission getPermissionById( String id )
    {
        return permissionIdMap.get( id );
    }

    public boolean userHasPermission( User user, Permission permission, Project project )
    {
        String permId = permission.getId();
        if ( user != null && user.getRoles() != null )
        {
            for ( Role role : user.getRoles() )
            {
                if ( role.equals( new MemberRole() ) )
                {
                    if ( permission instanceof MemberPermission && !user.getUsername().equals( "anonymous" ) )
                    {
                        return true;
                    }

                    if ( !( project == null || project.getUsers().contains( user ) ) )
                    {
                        continue;
                    }
                }
                if ( role.getPermissions().contains( permId ) )
                {
                    return true;
                }
            }
        }

        Role anon = getRoleById( new AnonymousRole().getId() );
        if ( anon == null ) // can be null if we are setting up
        {
            return false;
        }

        Set<String> permissionIds = anon.getPermissions();
        return permissionIds.contains( permId );
    }

    public boolean userCanListProject( User user, Project project )
    {
        return userHasPermission( user, new ProjectListPermission(), project );
    }
}
