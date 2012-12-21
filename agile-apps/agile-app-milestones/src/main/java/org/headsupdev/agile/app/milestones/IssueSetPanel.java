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

package org.headsupdev.agile.app.milestones;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.storage.DurationWorkedUtil;
import org.headsupdev.agile.storage.issues.DurationWorked;
import org.headsupdev.agile.storage.issues.Issue;
import org.headsupdev.agile.web.HeadsUpSession;
import org.headsupdev.agile.web.components.FormattedDateModel;
import org.headsupdev.agile.web.components.MarkedUpTextModel;
import org.headsupdev.agile.web.components.PercentagePanel;
import org.headsupdev.agile.web.components.milestones.MilestoneStatusModifier;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * TODO document me
 *
 * @author Andrew Williams
 * @since 2.0
 */
abstract class IssueSetPanel
        extends Panel
{
    protected IssueSetPanel( String id )
    {
        super( id );
    }

    public void layout( String name, String description, Project project, Date created, Date updated,
                          Date start, Date due, Date completed )
    {
        add( new Label( "type", getType() ) );
        add( new Label( "id", name ) );
        add( new Label( "name", name ) );
        add( new Label( "project", project.toString() ) );

        add( new Label( "created", new FormattedDateModel( created, ( (HeadsUpSession) getSession() ).getTimeZone() ) ) );
        add( new Label( "updated", new FormattedDateModel( updated, ( (HeadsUpSession) getSession() ).getTimeZone() ) ) );

        int total = getIssues().size();
        int open = getOpenIssues().size();
        int reopened = getReOpenedIssues().size();
        int percent = (int) ( getCompleteness() * 100 );
        add( new Label( "issues", reopened == 0 ? String.valueOf( total )  : String.format( "%d (%d reopened)", total, reopened ) ) );
        add( new Label( "open", String.valueOf( open ) ) );

        String percentStr = "";
        if ( completed == null )
        {
            percentStr = percent + "% ";
        }
        add( new Label( "percent", percentStr ) );

        add( new Label( "due", new FormattedDateModel( due,
                ( (HeadsUpSession) getSession() ).getTimeZone() ) ).add( new MilestoneStatusModifier( "due", due, completed ) ) );
        add( new Label( "completed", new FormattedDateModel( completed, ( (HeadsUpSession) getSession() ).getTimeZone() ) ) );

        add( new PercentagePanel( "bar", percent ) );

        add( new Label( "description", new MarkedUpTextModel( description, project ) )
                .setEscapeModelStrings( false ) );

        List<DurationWorked> worked = new ArrayList<DurationWorked>();
        for ( Issue issue : getIssues() )
        {
            worked.addAll( issue.getTimeWorked() );
        }

        Double velocity = DurationWorkedUtil.getVelocity( worked, start, due );
        String velocityStr = "-";
        if ( !velocity.equals( Double.NaN ) )
        {
            velocityStr = String.format( "%.1f", velocity );
        }
        add( new Label( "velocity", velocityStr ) );
    }

    protected abstract Set<Issue> getIssues();
    protected abstract Set<Issue> getOpenIssues();
    protected abstract Set<Issue> getReOpenedIssues();

    protected abstract double getCompleteness();
    protected abstract String getType();
}
