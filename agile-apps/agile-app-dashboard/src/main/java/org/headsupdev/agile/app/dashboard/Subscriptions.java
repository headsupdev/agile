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

import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.api.Permission;
import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.api.User;
import org.headsupdev.agile.app.dashboard.permission.MemberEditPermission;
import org.headsupdev.agile.security.permission.AdminPermission;
import org.headsupdev.agile.storage.HibernateStorage;
import org.headsupdev.agile.storage.MemberRole;
import org.headsupdev.agile.storage.StoredProject;
import org.headsupdev.agile.web.BookmarkableMenuLink;
import org.headsupdev.agile.web.HeadsUpPage;
import org.headsupdev.agile.web.HeadsUpSession;
import org.headsupdev.agile.web.MountPoint;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.string.Strings;
import org.headsupdev.agile.web.components.OnePressButton;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * The page for managing a users subscriptions
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@MountPoint( "subscriptions" )
public class Subscriptions
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

        add( new ChangeSubscriptionsForm( "editsubscriptions", user, me ) );
    }

    @Override
    public String getTitle()
    {
        return "Change email subscriptions for " + username;
    }

    class ChangeSubscriptionsForm extends Form
    {
        private org.headsupdev.agile.api.User user;
        private boolean me;
        private Set<User> defaultProjectSubscribers = StoredProject.getDefaultProjectSubscribers();

        public  ChangeSubscriptionsForm( String id, final org.headsupdev.agile.api.User user, boolean me )
        {
            super( id );
            this.user = user;
            this.me = me;

            List<Project> projects = new LinkedList<Project>( getStorage().getRootProjects() );
            projects.add( StoredProject.getDefault() );

            add( new SubscriptionListView( "subscription", projects ) );
            add( new OnePressButton( "submitSubscriptions" ) );
        }

        @Override
        protected void onSubmit()
        {
            Session session = ( (HibernateStorage) getStorage() ).getHibernateSession();
            Transaction tx = session.beginTransaction();
            user = (org.headsupdev.agile.api.User) session.merge( user );

            if ( me ) {
                ( (HeadsUpSession) getSession() ).setUser( user );
            }

            session.update( user );
            tx.commit();

            StoredProject.setDefaultProjectSubscribers( defaultProjectSubscribers );

            PageParameters params = new PageParameters();
            params.add( "username", user.getUsername() );
            setResponsePage( getPageClass( "account" ), params );
        }

        public class SubscriptionListView
                extends Panel
        {
            private static final String INDENT = "&nbsp;&nbsp;&nbsp;";
            private int myRow;

            public SubscriptionListView( String id, List<? extends Project> projects )
            {
                this( id, projects, "", 0 );
            }

            public SubscriptionListView( String id, List<? extends Project> projects, final String indent, final int row )
            {
                super( id );
                myRow = row;

                add( new ListView<Project>( "project", projects ) {
                    @Override
                    protected void populateItem( ListItem<Project> listItem )
                    {
                        final String rowClass = ( myRow % 2 == 1 ) ? "odd" : "even";
                        listItem.add( new AttributeModifier( "class", true, new Model<String>()
                        {
                            public String getObject()
                            {
                                return rowClass;
                            }
                        } ) );
                        final Project project = listItem.getModelObject();

                        org.headsupdev.agile.api.SecurityManager sec = Manager.getSecurityInstance();
                        if ( !( sec.userHasPermission( user, new AdminPermission(), project ) ||
                                ( user.getRoles().contains( new MemberRole() ) && project.getUsers().contains( user ) ) ) )
                        {
                            listItem.setVisible( false );
                            return;
                        }

                        listItem.add( new Label( "projectname", indent + Strings.escapeMarkup( project.getAlias() ) )
                                .setEscapeModelStrings( false ) );
                        listItem.add( new CheckBox( "subscribe", new Model<Boolean>()
                        {
                            @Override
                            public Boolean getObject()

                            {
                                if ( project.equals( StoredProject.getDefault() ) )
                                {
                                    return defaultProjectSubscribers.contains( user );
                                }

                                return user.getSubscriptions().contains( project );
                            }

                            @Override
                            public void setObject( Boolean object )
                            {
                                if ( project.equals( StoredProject.getDefault() ) )
                                {
                                    if ( object )
                                    {
                                        defaultProjectSubscribers.add( user );
                                    }
                                    else
                                    {
                                        defaultProjectSubscribers.remove( user );
                                    }
                                }
                                else
                                {
                                    if ( object )
                                    {
                                        user.getSubscriptions().add( project );
                                    }
                                    else
                                    {
                                        user.getSubscriptions().remove( project );
                                    }
                                }
                            }
                        } ) );

                        List<Project> children = new LinkedList<Project>( project.getChildProjects() );
                        listItem.add( new SubscriptionListView( "childProjects", children, INDENT + indent, myRow + 1 ) );

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