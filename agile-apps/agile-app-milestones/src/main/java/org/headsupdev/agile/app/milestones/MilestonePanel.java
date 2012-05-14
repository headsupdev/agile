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

import org.headsupdev.agile.storage.DurationWorkedUtil;
import org.headsupdev.agile.storage.issues.DurationWorked;
import org.headsupdev.agile.storage.issues.Issue;
import org.headsupdev.agile.web.HeadsUpSession;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.basic.Label;
import org.headsupdev.agile.storage.issues.Milestone;
import org.headsupdev.agile.web.components.FormattedDateModel;
import org.headsupdev.agile.web.components.PercentagePanel;
import org.headsupdev.agile.web.components.MarkedUpTextModel;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO document me
 *
 * @author Andrew Williams
 * @since 1.0
 */
public class MilestonePanel
        extends Panel
{
    public MilestonePanel( String id, Milestone milestone )
    {
        super( id );

        add( new Label( "id", milestone.getName() ) );
        add( new Label( "name", milestone.getName() ) );
        add( new Label( "project", milestone.getProject().toString() ) );

        add( new Label( "created", new FormattedDateModel( milestone.getCreated(),
                ( (HeadsUpSession) getSession() ).getTimeZone() ) ) );
        add( new Label( "updated", new FormattedDateModel( milestone.getUpdated(),
                ( (HeadsUpSession) getSession() ).getTimeZone() ) ) );

        int total = milestone.getIssues().size();
        int open = milestone.getOpenIssues().size();
        int reopened = milestone.getReOpenedIssues().size();
        double part = DurationWorkedUtil.getMilestoneCompleteness( milestone );
        int percent = (int) ( part * 100 );
        add( new Label( "issues", reopened == 0 ? String.valueOf( total )  : String.format( "%d (%d reopened)", total, reopened ) ) );
        add( new Label( "open", String.valueOf( open ) ) );

        String percentStr = "";
        if ( milestone.getCompletedDate() == null )
        {
            percentStr = percent + "% ";
        }
        add( new Label( "percent", percentStr ) );

        add( new Label( "due", new FormattedDateModel( milestone.getDueDate(),
                ( (HeadsUpSession) getSession() ).getTimeZone() ) ).add( new MilestoneStatusModifier( "due", milestone ) ) );
        add( new Label( "completed", new FormattedDateModel( milestone.getCompletedDate(),
                ( (HeadsUpSession) getSession() ).getTimeZone() ) ) );

        add( new PercentagePanel( "bar", percent ) );

        add( new Label( "description", new MarkedUpTextModel( milestone.getDescription(), milestone.getProject() ) )
                .setEscapeModelStrings( false ) );

        List<DurationWorked> worked = new ArrayList<DurationWorked>();
        for ( Issue issue : milestone.getIssues() )
        {
            worked.addAll( issue.getTimeWorked() );
        }

        Double velocity = DurationWorkedUtil.getVelocity( worked, milestone );
        String velocityStr = "-";
        if ( !velocity.equals( Double.NaN ) )
        {
            velocityStr = String.format( "%.1f", velocity );
        }
        add( new Label( "velocity", velocityStr ) );
    }
}
