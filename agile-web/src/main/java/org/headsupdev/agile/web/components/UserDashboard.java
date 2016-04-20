/*
 * HeadsUp Agile
 * Copyright 2009-2013 Heads Up Development Ltd.
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

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.Button;
import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.api.User;
import org.headsupdev.agile.storage.HibernateUtil;
import org.headsupdev.agile.storage.issues.Issue;
import org.headsupdev.agile.storage.issues.Milestone;
import org.headsupdev.agile.web.HeadsUpPage;
import org.headsupdev.agile.web.HeadsUpSession;
import org.headsupdev.agile.web.components.issues.IssuePanelRow;
import org.headsupdev.agile.web.components.milestones.MilestoneStatusModifier;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.headsupdev.agile.web.model.UserDashboardModel;

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
    private UserDashboardModel model;
    private HeadsUpPage page;

    private Button close;

    public UserDashboard( String id, HeadsUpPage page )
    {
        super( id );
        this.page = page;

        model = new UserDashboardModel( ( (HeadsUpSession) getSession() ).getUser() );
        layout();
    }

    public UserDashboard( String id, User user, HeadsUpPage page )
    {
        super( id );
        this.page = page;

        model = new UserDashboardModel( user );
        layout();
    }

    private void layout()
    {
        add( CSSPackageResource.getHeaderContribution( "resources/org.headsupdev.agile.app.milestones.Milestones/milestone.css" ) );

        List<Milestone> userMilestones = model.getMilestones();
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
                listItem.add( label.setVisible( displayed == null || !displayed.equals( milestone.getProject() ) ) );
                displayed = milestone.getProject();

                label = new Label( "due", new FormattedDateModel( milestone.getDueDate(),
                        ( (HeadsUpSession) getSession() ).getTimeZone() ) );
                label.add( new MilestoneStatusModifier( "due", milestone ) );
                listItem.add( label );

                listItem.add( new ListView<Issue>( "issuelist", model.getIssuesInMilestone( milestone ) )
                {
                    protected void populateItem( ListItem<Issue> listItem )
                    {
                        Issue issue = listItem.getModelObject();

                        listItem.add( new IssuePanelRow( "issue", issue, page, true, true, true ) );
                    }
                } );
            }
        } );

        List<Issue> noMilestone = model.getIssuesInMilestone( null );
        add( new ListView<Issue>( "issuelist", noMilestone )
        {
            private Project displayed;

            protected void populateItem( ListItem<Issue> listItem )
            {
                Issue issue = listItem.getModelObject();

                // hide project label if we have already started listing that project's milestones
                Label label = new Label( "project", issue.getProject().toString() );
                listItem.add( label .setVisible( displayed == null || !displayed.equals( issue.getProject() ) ));
                displayed = issue.getProject();

                listItem.add( new IssuePanelRow( "issue", issue, page, true, true, true ) );
            }
        }.setVisible( noMilestone.size() > 0 ) );

        close = new Button( "close" );
        add( close );
    }

    public Component getCloseButton()
    {
        return close;
    }
}
