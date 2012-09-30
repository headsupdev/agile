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

import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Check;
import org.apache.wicket.markup.html.form.CheckGroup;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.string.Strings;
import org.headsupdev.agile.api.*;
import org.headsupdev.agile.security.permission.AdminPermission;
import org.headsupdev.agile.storage.*;
import org.headsupdev.agile.web.HeadsUpPage;
import org.headsupdev.agile.web.MountPoint;
import org.headsupdev.agile.web.components.StripedListView;
import org.hibernate.Session;

import java.util.*;

/**
 * The HeadsUp about page.
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@MountPoint( "membership" )
public class Membership
    extends HeadsUpPage
{
    public Permission getRequiredPermission()
    {
        return new AdminPermission();
    }

    public void layout()
    {
        super.layout();

        add( CSSPackageResource.getHeaderContribution( getClass(), "admin.css" ));

        add( new ProjectPermissionsForm( "projects" ) );
    }

    @Override
    public String getTitle()
    {
        return "Manage Project Membership";
    }

    class ProjectPermissionsForm
        extends Form
    {
        private List<Project> projects;
        private List<User> users;

        public ProjectPermissionsForm( String id )
        {
            super( id );

            projects = new LinkedList<Project>( getStorage().getRootProjects() );
            projects.add( StoredProject.getDefault() );

            users = getSecurityManager().getUsers();
            Iterator<User> userIter = users.iterator();
            while ( userIter.hasNext() )
            {
                User user = userIter.next();
                if ( !user.getRoles().contains( new MemberRole() ) )
                {
                    userIter.remove();
                }
            }

            add( new ListView<User>( "usernames", users )
            {
                protected void populateItem( ListItem<User> listItem )
                {
                    final User user = listItem.getModelObject();

                    listItem.add( new Label( "username", user.getUsername() ) );
                }
            } );

            add( new ProjectPermissionsListView( "projectlist", projects ) );
        }

        protected void onSubmit()
        {
            Session session = HibernateUtil.getCurrentSession();

            Set<User> defaultProjectMembers = new HashSet<User>();
            for ( User user : users )
            {
                user = (User) session.merge( user );

                // copy the project settings into the user object where it is stored
                // this is needed to keep the table the right way up...
                user.getProjects().clear();
                for ( Project project : projects )
                {
                    submitProject( project, user, defaultProjectMembers );
                }
            }
            StoredProject.setDefaultProjectMembers( defaultProjectMembers );
            StoredProject.getDefault().getUsers().clear();
            StoredProject.getDefault().getUsers().addAll( defaultProjectMembers );
        }

        private void submitProject( Project project, User user, Set<User> defaultProjectMembers )
        {
            if ( project.getUsers().contains( user ) )
            {
                if ( project.equals( StoredProject.getDefault() ) )
                {
                    defaultProjectMembers.add( user );
                }
                else
                {
                    user.getProjects().add( project );
                }
            }

            for ( Project child : project.getChildProjects() )
            {
                submitProject( child, user, defaultProjectMembers );
            }
        }

        public class ProjectPermissionsListView
            extends Panel
        {
            private static final String INDENT = "&nbsp;&nbsp;&nbsp;";
            private int myRow;

            public ProjectPermissionsListView( String id, List<Project> projects )
            {
                this( id, projects, "", 0 );
            }

            public ProjectPermissionsListView( String id, List<Project> projects, final String indent, final int row )
            {
                super( id );
                myRow = row;
                Collections.sort( projects );

                add( new StripedListView<Project>( "project", projects ) {
                    @Override
                    protected void populateItem( ListItem<Project> listItem )
                    {
                        super.populateItem( listItem );
                        final Project project = listItem.getModelObject();

                        listItem.add( new Label( "projectname", indent + Strings.escapeMarkup( project.getAlias() ) )
                                .setEscapeModelStrings( false ) );
                        CheckGroup group = new CheckGroup<User>( "userlist",  project.getUsers() );
                        group.add( new ListView<User>( "usergroup", users )
                        {
                            protected void populateItem( ListItem<User> listItem )
                            {
                                final User user = listItem.getModelObject();

                                listItem.add( new Check<User>( "user", new Model<User>()
                                {
                                    public User getObject()
                                    {
                                        return user;
                                    }
                                } ) );
                            }
                        } );
                        listItem.add( group );

                        List<Project> children = new LinkedList<Project>( project.getChildProjects() );
                        listItem.add( new ProjectPermissionsListView( "childProjects", children, INDENT + indent, myRow + 1 ) );

                        // increment row by number that will appear
                        myRow += countProjects( project );
                    }
                } );
            }
        }

        private int countProjects( Project project )
        {
            int ret = 1;
            if ( project.getChildProjects() != null )
            {
                for ( Project child : project.getChildProjects() )
                {
                    ret += countProjects( child );
                }
            }

            return ret;
        }
    }
}

