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

package org.headsupdev.agile.core.notifiers.irc;

import org.headsupdev.agile.api.*;
import org.headsupdev.agile.storage.HibernateStorage;
import org.headsupdev.agile.storage.StoredUser;
import org.headsupdev.irc.AbstractIRCCommand;
import org.headsupdev.irc.IRCUser;
import org.headsupdev.irc.IRCConnection;
import org.hibernate.Session;

import java.util.List;
import java.util.Set;
import java.util.HashSet;

/**
 * A simple command that displays information about projects loaded into this software
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class ProjectCommand
    extends AbstractIRCCommand
{
    public String getId()
    {
        return "project";
    }

    public void onCommand( String channel, IRCUser user, String message, IRCConnection conn )
    {
        if ( message.length() == 0 )
        {
            conn.sendMessage( channel, "Missing command, please try 'list' or 'show <projectId>'" );
        }
        else
        {
            int pos = message.indexOf( ' ' );
            String command;
            if ( pos == -1 )
            {
                command = message;
                message = "";
            }
            else
            {
                command = message.substring( 0, pos );
                message = message.substring( pos + 1 );
            }

            if ( command.equals( "list" ) )
            {
                if ( canAnonUserListProjects() )
                {
                    listProjects( channel, new HashSet<Project>( Manager.getStorageInstance().getRootProjects() ), "", conn );
                }
                else
                {
                    conn.sendMessage( channel, "Permission denied" );
                }
            }
            else if ( command.equals( "show" ) )
            {
                Project project = Manager.getStorageInstance().getProject( message );
                if ( !canAnonUserViewProject( project) )
                {
                    conn.sendMessage( channel, "Permission denied" );
                    return;
                }

                if ( project == null )
                {
                    conn.sendMessage( channel, "Project '" + message + "' not found" );
                }
                else
                {
                    conn.sendMessage( channel, "Project details for \"" + project.getAlias() + "\"" );
                    conn.sendMessage( channel, "  type: \t" + project.getTypeName() );
                    if ( project instanceof MavenTwoProject)
                    {
                        MavenTwoProject m2 = (MavenTwoProject) project;
                        conn.sendMessage( channel, "  groupId: \t" + m2.getGroupId() );
                        conn.sendMessage( channel, "  artifactId: \t" + m2.getArtifactId() );
                        conn.sendMessage( channel, "  version: \t" + m2.getVersion() );
                    }
                    else if ( project instanceof AntProject)
                    {
                        AntProject a = (AntProject) project;
                        conn.sendMessage( channel, "  organisation: \t" + a.getOrganisation() );
                        conn.sendMessage( channel, "  module: \t" + a.getModule() );
                        conn.sendMessage( channel, "  version: \t" + a.getVersion() );
                    }
                    else if ( project instanceof XCodeProject)
                    {
                        XCodeProject xc = (XCodeProject) project;
                        conn.sendMessage( channel, "  platform: \t" + xc.getPlatform() );
                        conn.sendMessage( channel, "  version: \t" + xc.getVersion() );
                    }
                    conn.sendMessage( channel, "  revision: \t" + project.getRevision() );

                    if ( project.getParent() != null )
                    {
                        conn.sendMessage( channel, "  parent: \t" + project.getParent().getId() + " \"" +
                            project.getParent().getName() + "\"" );
                    }
                }
            }
            else
            {
                conn.sendMessage( channel, "Unknown command '" + command + "'" );
            }
        }
    }

    public String getHelp( String s )
    {
        return "Display project information, use the list command to show all projects\n" +
            "  or the show <projectId> command to show the details of a project";
    }

    private void listProjects( String channel, Set<Project> projects, String indent, IRCConnection conn )
    {
        for ( Project project : projects )
        {
            conn.sendMessage( channel, indent + project.getId() + " \t\"" + project.getAlias() + "\"" );

            listProjects( channel, project.getChildProjects(), indent + "  ", conn );
        }
    }

    private boolean canAnonUserListProjects()
    {
        Session session = ( (HibernateStorage) Manager.getStorageInstance() ).getHibernateSession();

        // dummy anonymous user
        User anon = new StoredUser( "anonymous" );
        // use a dummy PROJECT-LIST permission as we don't have access to the security package
        boolean ret = Manager.getSecurityInstance().userHasPermission( anon, new Permission()
        {
            public String getId() {
                return "PROJECT-LIST";
            }

            public String getDescription() {
                return null;
            }

            public List<String> getDefaultRoles() {
                return null;
            }
        }, null );

        session.close();
        return ret;
    }

    private boolean canAnonUserViewProject( Project project )
    {
        Session session = ( (HibernateStorage) Manager.getStorageInstance() ).getHibernateSession();

        // dummy anonymous user
        User anon = new StoredUser( "anonymous" );
        // use a dummy PROJECT-LIST permission as we don't have access to the security package
        boolean ret = Manager.getSecurityInstance().userHasPermission( anon, new Permission()
        {
            public String getId() {
                return "PROJECT-VIEW";
            }

            public String getDescription() {
                return null;
            }

            public List<String> getDefaultRoles() {
                return null;
            }
        }, project );

        session.close();
        return ret;
    }
}
