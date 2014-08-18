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

package org.headsupdev.agile.app.dashboard;

import org.headsupdev.agile.api.Event;
import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.api.Permission;
import org.headsupdev.agile.api.User;
import org.headsupdev.agile.app.dashboard.permission.MemberViewPermission;
import org.headsupdev.agile.security.permission.AdminPermission;
import org.headsupdev.agile.storage.HibernateStorage;
import org.headsupdev.agile.storage.StoredProject;
import org.headsupdev.agile.storage.issues.Duration;
import org.headsupdev.agile.storage.resource.DurationWorked;
import org.headsupdev.agile.storage.issues.Issue;
import org.headsupdev.agile.storage.resource.ResourceManagerImpl;
import org.headsupdev.agile.storage.resource.Velocity;
import org.headsupdev.agile.web.BookmarkableMenuLink;
import org.headsupdev.agile.web.HeadsUpPage;
import org.headsupdev.agile.web.HeadsUpSession;
import org.headsupdev.agile.web.MountPoint;
import org.headsupdev.agile.web.components.FormattedDateModel;
import org.headsupdev.agile.web.components.UserDetailsPanel;
import org.headsupdev.agile.web.components.history.HistoryPanel;
import org.headsupdev.agile.web.components.issues.IssueListPanel;
import org.headsupdev.agile.web.components.issues.IssueStatusModifier;
import org.headsupdev.agile.web.wicket.SortableEntityProvider;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.headsupdev.support.java.DateUtil;
import org.headsupdev.support.java.StringUtil;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * The HeadsUp about page.
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@MountPoint("account")
public class Account
        extends HeadsUpPage
{
    private User user;

    public Permission getRequiredPermission()
    {
        return new MemberViewPermission();
    }

    public void layout()
    {
        super.layout();
        add( CSSPackageResource.getHeaderContribution( getClass(), "account.css" ) );

        user = getSession().getUser();
        String username = getPageParameters().getString( "username" );
        if ( username != null )
        {
            user = getSecurityManager().getUserByUsername( username );

            if ( user == null || user.equals( HeadsUpSession.ANONYMOUS_USER ) )
            {
                notFoundError();
                return;
            }
        }

        final org.headsupdev.agile.api.User finalUser = user;
        boolean showTools = ( user.equals( getSession().getUser() ) && !user.equals( HeadsUpSession.ANONYMOUS_USER ) ) ||
                getSecurityManager().userHasPermission( getSession().getUser(), new AdminPermission(), null );
        final boolean timeEnabled = Boolean.parseBoolean( StoredProject.getDefault().getConfigurationValue(
                StoredProject.CONFIGURATION_TIMETRACKING_ENABLED ) );
        final boolean showVelocity = timeEnabled && showTools;

        WebMarkupContainer velocityPanel = new WebMarkupContainer( "velocity" );
        add( velocityPanel.setVisible( showVelocity ) );
        if ( showVelocity )
        {
            Velocity velocity = getResourceManager().getUserVelocity( user );
            String velocityStr = "-";
            if ( !velocity.equals( Velocity.INVALID ) )
            {
                velocityStr = String.format( "%.1f", velocity.getVelocity() );
            }
            velocityPanel.add( new Label( "velocity", velocityStr ) );

            Velocity currentVelocity = getResourceManager().getCurrentUserVelocity( user );
            String currentVelocityStr = "-";
            if ( !currentVelocity.equals( Velocity.INVALID ) )
            {
                currentVelocityStr = String.format( "%.1f", currentVelocity.getVelocity() );
            }
            velocityPanel.add( new Label( "currentvelocity", currentVelocityStr ) );

            Velocity averageVelocity = getResourceManager().getAverageVelocity();
            String averageVelocityStr = "-";
            if ( !averageVelocity.equals( Velocity.INVALID ) )
            {
                averageVelocityStr = String.format( "%.1f", averageVelocity.getVelocity() );
            }
            velocityPanel.add( new Label( "averagevelocity", averageVelocityStr ) );
        }

        PageParameters params = new PageParameters();
        params.add( "username", user.getUsername() );
        params.add( "silent", "true" );

        add( new Image( "account", new ResourceReference( "member.png" ), params ) );

        add( new UserDetailsPanel( "details", user, getProject(), shouldShowFullDetails(), showTools, this) );

        if ( showTools )
        {
            params = getPageParameters();

            addLink( new BookmarkableMenuLink( getPageClass( "editaccount" ), params, "edit" ) );
            addLink( new BookmarkableMenuLink( getPageClass( "changepassword" ), params, "change-password" ) );
            addLink( new BookmarkableMenuLink( getPageClass( "subscriptions" ), params, "subscriptions" ) );
        }

        add( new Label( "issues-name", user.getFullnameOrUsername() ) );
        add( new IssueListPanel( "issues", getIssuesWatchedBy( finalUser ), this, false, false, null ) );

        Calendar calendar = Calendar.getInstance( user.getTimeZone() );
        Date startOfToday = DateUtil.getStartOfToday( calendar );
        Date endOfToday = DateUtil.getEndOfToday( calendar );

        calendar.setTime( startOfToday );
        calendar.add( Calendar.DATE, -7 );
        Date startOfWeek = calendar.getTime();

        Duration durationDay = getResourceManager().getLoggedTimeForUser( user, startOfToday, endOfToday );
        add( new Label( "loggedtimeday", durationDay == null ? " - " : durationDay.toString() ) );
        Duration durationWeek = getResourceManager().getLoggedTimeForUser( user, startOfWeek, endOfToday );
        add( new Label( "loggedtimeweek", durationWeek == null ? " - " : durationWeek.toString() ) );

        add( new ListView<DurationWorked>( "comments", getResourceManager().getDurationWorkedForUser( user, startOfWeek, endOfToday ) )
        {
            protected void populateItem( ListItem<DurationWorked> listItem )
            {
                DurationWorked worked = listItem.getModelObject();
                if ( worked.getIssue() == null )
                {
                    listItem.setVisible( false );
                    return;
                }

                WebMarkupContainer workedTitle = new WebMarkupContainer( "worked-title" );
                listItem.add( new Image( "icon", new ResourceReference( HeadsUpPage.class, "images/worked.png" ) ) );

                String time = "";
                if ( worked.getWorked() != null )
                {
                    time = worked.getWorked().toString();
                }
                workedTitle.add( new Label( "worked", time ) );

                Issue related = worked.getIssue();
                PageParameters params = new PageParameters();
                params.put( "project", related.getProject() );
                params.put( "id", related.getId() );
                BookmarkablePageLink link = new BookmarkablePageLink( "issue-link", getPageClass( "issues/view" ), params );
                String issueId = related.getProject().getId() + ":" + related.getId();
                link.add( new Label( "issue", "issue:" + issueId ) );
                workedTitle.add( link.add( new IssueStatusModifier( "relatedstatus", related ) ) );
                workedTitle.add( new Label( "summary", related.getSummary() )
                        .add( new IssueStatusModifier( "relatedstatus", related ) ) );

                workedTitle.add( new Label( "username", worked.getUser().getFullnameOrUsername() ) );
                workedTitle.add( new Label( "created", new FormattedDateModel( worked.getDay(),
                        ( (HeadsUpSession) getSession() ).getTimeZone() ) ) );

                listItem.add( workedTitle );
            }
        }.setVisible( showTools && timeEnabled ) );

        add( new Label( "history-name", user.getFullnameOrUsername() ) );
        add( new HistoryPanel( "events", getEventsForUser( user ), true, Account.this ) );
    }

    @Override
    public String getPageTitle()
    {
        return "Account:" + user.getUsername() + PAGE_TITLE_SEPARATOR + super.getPageTitle();
    }

    private boolean shouldShowFullDetails()
    {
        User currentUser = getSession().getUser();

        return ( currentUser != null && !currentUser.equals( HeadsUpSession.ANONYMOUS_USER ) );
    }

    public SortableEntityProvider<Issue> getIssuesWatchedBy( final org.headsupdev.agile.api.User user )
    {
        return new SortableEntityProvider<Issue>()
        {
            @Override
            protected Criteria createCriteria()
            {
                Session session = ( (HibernateStorage) Manager.getStorageInstance() ).getHibernateSession();

                Criteria c = session.createCriteria( Issue.class );
                c.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );
                c.add( Restrictions.lt( "status", Issue.STATUS_RESOLVED ) );
                c.createCriteria( "watchers" ).add( Restrictions.eq( "username", user.getUsername() ) );

                return c;
            }

            @Override
            protected List<Order> getDefaultOrder()
            {
                return Arrays.asList( Order.asc( "priority" ),
                        Order.asc( "status" ), Order.asc( "id.id" ) );
            }
        };
    }

    public List<Event> getEventsForUser( org.headsupdev.agile.api.User user )
    {
        Session session = ( (HibernateStorage) getStorage() ).getHibernateSession();
        Query q = session.createQuery( "from StoredEvent e where username = :username or " +
                "username like :emailLike or username like :nameLike order by time desc" );
        q.setString( "username", user.getUsername() );
        if ( !StringUtil.isEmpty( user.getEmail() ) )
        {
            q.setString( "emailLike", "%<" + user.getEmail() + ">" );
        }
        else
        {
            // a silly fallback for now
            q.setString( "emailLike", user.getUsername() );
        }

        if ( !StringUtil.isEmpty( user.getFullname() ) )
        {
            q.setString( "nameLike", user.getFullname() + " <%" );
        }
        else
        {
            // a silly fallback for now
            q.setString( "nameLike", user.getUsername() );
        }

        q.setMaxResults( 10 );
        return q.list();
    }

    public ResourceManagerImpl getResourceManager()
    {
        return ( (HibernateStorage) getStorage() ).getResourceManager();
    }
}
