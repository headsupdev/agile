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

package org.headsupdev.agile.web.components;

import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.api.User;
import org.headsupdev.agile.storage.HibernateStorage;
import org.headsupdev.agile.storage.HibernateUtil;
import org.headsupdev.agile.storage.issues.Issue;
import org.headsupdev.agile.storage.issues.Milestone;
import org.headsupdev.agile.storage.issues.MilestoneComparator;
import org.headsupdev.agile.web.HeadsUpPage;
import org.headsupdev.agile.web.HeadsUpSession;
import org.headsupdev.agile.web.components.issues.IssuePanelRow;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.hibernate.Query;
import org.hibernate.Session;

import java.util.*;

/**
 * Sweet new dashboard for user stats (issues, milestones etc)
 * <p/>
 * Created: 11/09/2011
 *
 * @author Andrew Williams
 * @since 1.0
 */
public class UserDashboard extends Panel
{
    private Map<Milestone,List<Issue>> userMilestoneIssues;
    private List<Issue> userNoMilestoneIssues;
    private HeadsUpPage page;

    public UserDashboard( String id, HeadsUpPage page )
    {
        super( id );
        this.page = page;
        setUser( ( (HeadsUpSession) getSession() ).getUser() );
    }

    public UserDashboard( String id, User user, HeadsUpPage page )
    {
        super( id );
        this.page = page;
        setUser( user );
    }

    private void setUser( final User user )
    {
        initIssueList( user );
        add( CSSPackageResource.getHeaderContribution( "resources/org.headsupdev.agile.app.milestones.Milestones/milestone.css" ) );

        List<Milestone> userMilestones = getMilestonesAssignedTo( user );
        add( new ListView<Milestone>( "milestones", userMilestones )
        {
            private Project displayed;

            @Override
            protected void populateItem( ListItem<Milestone> listItem )
            {
                final Milestone milestone = (Milestone) HibernateUtil.getCurrentSession().load(
                        Milestone.class, listItem.getModelObject().getInternalId() );

                PageParameters params = new PageParameters();
                params.add( "project", milestone.getProject().getId() );
                params.add( "id", milestone.getName() );

                WebMarkupContainer cell = new WebMarkupContainer( "id-cell" );
                Link idLink = new BookmarkablePageLink( "id-link", page.getPageClass( "milestones/view" ), params );
                idLink.add( new Label( "id-label", milestone.getName() ) );
                cell.add( idLink );
                listItem.add( cell );

                // hide project label if we have already started listing that project's milestones
                Label label = new Label( "project", milestone.getProject().toString() );
                listItem.add( label .setVisible( displayed == null || !displayed.equals( milestone.getProject() ) ));
                displayed = milestone.getProject();

                label = new Label( "due", new FormattedDateModel( milestone.getDueDate(),
                        ( (HeadsUpSession) getSession() ).getTimeZone() ) );
                label.add( new MilestoneStatusModifier( "due", milestone ) );
                listItem.add( label );

                listItem.add( new ListView<Issue>( "issuelist", getIssuesInMilestoneAssignedTo( milestone, user ) )
                {
                    protected void populateItem( ListItem<Issue> listItem )
                    {
                        Issue issue = listItem.getModelObject();

                        listItem.add( new IssuePanelRow( "issue", issue, page, true, true, true ) );
                    }
                });
            }
        } );

        List<Issue> noMilestone = getIssuesInMilestoneAssignedTo( null, user );
        add( new ListView<Issue>( "issuelist", noMilestone )
        {
            protected void populateItem( ListItem<Issue> listItem )
            {
                Issue issue = listItem.getModelObject();

                listItem.add( new IssuePanelRow( "issue", issue, page, true, true, true ) );
            }
        }.setVisible( noMilestone.size() > 0 ) );
    }

    private void initIssueList( User user )
    {
        userMilestoneIssues = new HashMap<Milestone,List<Issue>>();
        userNoMilestoneIssues = new ArrayList<Issue>();

        for ( Issue issue : getIssuesAssignedTo( user ) )
        {
            if ( issue.getMilestone() == null )
            {
                userNoMilestoneIssues.add( issue );
                continue;
            }

            Milestone milestone = issue.getMilestone();
            List<Issue> milestoneIssues = userMilestoneIssues.get( milestone );
            if ( milestoneIssues == null )
            {
                milestoneIssues = new ArrayList<Issue>();
                userMilestoneIssues.put( milestone, milestoneIssues );
            }

            milestoneIssues.add( issue );
        }
    }

    private List<Milestone> getMilestonesAssignedTo( User user )
    {
        List<Milestone> milestones = new ArrayList<Milestone>();
        milestones.addAll( userMilestoneIssues.keySet() );
        Collections.sort( milestones, new MilestoneComparator() );

        return milestones;
    }

    private List<Issue> getIssuesInMilestoneAssignedTo( Milestone milestone, User user )
    {
        if ( milestone == null )
        {
            return userNoMilestoneIssues;
        }

        return userMilestoneIssues.get( milestone );
    }

    public List<Issue> getIssuesAssignedTo( org.headsupdev.agile.api.User user )
    {
        Session session = ( (HibernateStorage) Manager.getStorageInstance() ).getHibernateSession();

        Query q = session.createQuery( "from Issue i where status < 250 and assignee = :user order by priority, status" );
        q.setEntity( "user", user );
        List<Issue> list = q.list();

        // force loading...
        for ( Issue issue : list )
        {
            if ( issue.getMilestone() != null )
            {
                // force a load
                issue.getMilestone().getIssues().size();
            }
            issue.getAttachments().size();
        }

        return list;
    }
}

class MilestoneStatusModifier
    extends AttributeModifier
{
    public MilestoneStatusModifier( final String className, final Milestone milestone )
    {
        super ( "class", true, new Model<String>() {
            public String getObject()
            {
                if ( milestone.getCompletedDate() != null )
                {
                    return className + " statuscomplete";
                }

                if ( milestone.getDueDate() != null )
                {
                    if ( milestone.getDueDate().before( new Date() ) )
                    {
                        return className + " statusoverdue";
                    }

                    if ( milestone.getDueDate().before( getDueSoonDate() ) )
                    {
                        return className + " statusduesoon";
                    }
                }

                return className + " statusnotdue";
            }
        } );
    }

    public static Date getDueSoonDate()
    {
        return new Date( System.currentTimeMillis() + (1000L * 60 * 60 * 24 * 14 ) );
    }
}