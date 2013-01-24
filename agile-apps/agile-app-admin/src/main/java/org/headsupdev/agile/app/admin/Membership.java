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
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.headsupdev.agile.api.*;
import org.headsupdev.agile.security.permission.AdminPermission;
import org.headsupdev.agile.storage.*;
import org.headsupdev.agile.web.HeadsUpPage;
import org.headsupdev.agile.web.MountPoint;
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
        private User currentUser;

        public ProjectPermissionsForm( String id )
        {
            super( id );

            projects = new LinkedList<Project>( getStorage().getProjects() );
            projects.add( StoredProject.getDefault() );

            users = getSecurityManager().getRealUsers();
            Iterator<User> userIter = users.iterator();
            while ( userIter.hasNext() )
            {
                User user = userIter.next();
                if ( !user.getRoles().contains( new MemberRole() ) )
                {
                    userIter.remove();
                }
            }
            if ( users.size() > 0 )
            {
                currentUser = users.get( 0 );
            }

            add( new DropDownChoice<User>( "user", new PropertyModel<User>( this, "currentUser" ), users, new IChoiceRenderer<User>()
            {
                public Object getDisplayValue( User user )
                {
                    return user.getFullnameOrUsername();
                }

                public String getIdValue( User user, int i )
                {
                    return user.getUsername();
                }
            } )
            {
                @Override
                protected boolean wantOnSelectionChangedNotifications()
                {
                    return true;
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
        }

        public class ProjectPermissionsListView
            extends Panel
        {
            private static final String INDENT = "&nbsp;&nbsp;&nbsp;";

            public ProjectPermissionsListView( String id, List<Project> projects )
            {
                super( id );

                add( new ListView<Project>( "project", projects ) {
                    @Override
                    protected void populateItem( ListItem<Project> listItem )
                    {
                        final Project project = listItem.getModelObject();
                        listItem.add( new Label( "projectname", indentedName( project ) ).setEscapeModelStrings( false ) );
                        listItem.add( new ProjectUserCheckBox( "user", project ) );
                    }
                } );
            }

            private String indentedName( Project project )
            {
                String prefix = "";
                Project parent = project.getParent();
                while ( parent != null )
                {
                    prefix += INDENT;
                    parent = parent.getParent();
                }

                return prefix + project.getAlias();
            }

            public class ProjectUserCheckBox extends CheckBox
            {
                public ProjectUserCheckBox( final String id, final Project project )
                {
                    super( id, new Model<Boolean>()
                    {
                        public Boolean getObject()
                        {
                            return project.getUsers().contains( currentUser );
                        }

                        @Override
                        public void setObject( Boolean member )
                        {
                            if ( member )
                            {
                                project.getUsers().add( currentUser );
                            }
                            else
                            {
                                project.getUsers().remove( currentUser );
                            }
                        }
                    } );
                }
            }
        }
    }
}

