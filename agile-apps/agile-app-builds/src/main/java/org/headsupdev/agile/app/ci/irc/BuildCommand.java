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

package org.headsupdev.agile.app.ci.irc;

import org.headsupdev.agile.app.ci.CIApplication;
import org.headsupdev.agile.storage.HibernateStorage;
import org.headsupdev.irc.AbstractIRCCommand;
import org.headsupdev.irc.IRCUser;
import org.headsupdev.irc.IRCConnection;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.Query;
import org.headsupdev.agile.web.HeadsUpSession;
import org.headsupdev.agile.storage.ci.Build;
import org.headsupdev.agile.storage.HibernateUtil;
import org.headsupdev.agile.app.ci.CIBuilder;
import org.headsupdev.agile.app.ci.permission.BuildForcePermission;
import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.api.User;

import java.util.HashSet;
import java.util.Set;
import java.util.List;

/**
 * A build command for managing continuous integration from the IRC bot
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class BuildCommand
    extends AbstractIRCCommand
{
    public String getId()
    {
        return "build";
    }

    public void onCommand( String channel, IRCUser user, String message, IRCConnection conn )
    {
        if ( message.length() == 0 )
        {
            conn.sendMessage( channel, "Missing command, please try 'list', 'all' or '<projectId>'" );
        }
        else
        {
            if ( message.equals( "list" ) )
            {
                listProjects( channel, new HashSet<Project>( Manager.getStorageInstance().getRootProjects() ), "", conn );
            }
            else
            {
                Project project = Manager.getStorageInstance().getProject( message );

                if ( !canAnonUserBuild( project ) )
                {
                    conn.sendMessage( channel, "Sorry, you do not have permission to force builds" );
                    return;
                }

                if ( message.equals( "all" ) )
                {
                    conn.sendMessage( channel, "Queued all projects" );

                    CIApplication.getBuilder().queueAllProjects();
                }
                else
                {
                    if ( project == null )
                    {
                        conn.sendMessage( channel, "Unknown command or project '" + message + "'" );
                    }
                    else
                    {
                        if ( !CIApplication.getHandlerFactory().supportsBuilding(project) )
                        {
                            conn.sendMessage( channel, "Project '" + message + "' does not support building" );
                        }
                        else
                        {
                            conn.sendMessage( channel, "Queued project \"" + project.getAlias() + "\"" );

                            CIApplication.getBuilder().buildProject( project );
                        }
                    }
                }
            }
        }
    }

    public String getHelp( String s )
    {
        return "Manage project builds. Use the list command to show build statuses\n" +
            "  or specify the <projectId> (or 'all') to queue the projects for building";
    }

    private void listProjects( String channel, Set<Project> projects, String indent, IRCConnection conn )
    {
        for ( Project project : projects )
        {
            String message = indent + project.getId() + " \t";

            Build build = getLatestBuildForProject( project );
            if ( build != null )
            {
                switch ( build.getStatus() )
                {
                    case Build.BUILD_FAILED:
                        message += "FAILED";
                        break;
                    case Build.BUILD_CANCELLED:
                        message += "CANCELED";
                        break;
                    case Build.BUILD_QUEUED:
                        message += "QUEUED";
                        break;
                    case Build.BUILD_RUNNING:
                        message += "RUNNING";
                        break;
                    default:
                        message += "OK";
                }
                message += " \t" + build.getId();

                if ( CIBuilder.isProjectQueued( project ) )
                {
                    message += " (QUEUED)";
                }
            }
            conn.sendMessage( channel, message );

            listProjects( channel, project.getChildProjects(), indent + "  ", conn );
        }
    }

    private Build getLatestBuildForProject( Project project )
    {
        Build build = null;
        Session session = HibernateUtil.getCurrentSession();

        Transaction tx = session.beginTransaction();
        Query q = session.createQuery( "from Build b where id.project.id = :pid order by id.id desc" );
        q.setString( "pid", project.getId() );
        q.setMaxResults( 1 );
        List<Build> builds = q.list();
        if ( builds.size() > 0 )
        {
            build = builds.get( 0 );
        }
        tx.commit();

        return build;
    }

    private boolean canAnonUserBuild( Project project )
    {
        Session session = ( (HibernateStorage) Manager.getStorageInstance() ).getHibernateSession();

        User anon = HeadsUpSession.ANONYMOUS_USER;
        boolean ret = Manager.getSecurityInstance().userHasPermission( anon, new BuildForcePermission(), project );

        session.close();
        return ret;
    }
}
