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

package org.headsupdev.agile.web.components.issues;

import org.headsupdev.agile.api.User;
import org.headsupdev.agile.web.HeadsUpSession;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.CompoundPropertyModel;
import org.headsupdev.agile.web.components.FilterBorder;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import java.util.Arrays;
import java.util.List;
import java.util.LinkedList;

/**
 * A filter panel for managing the filtering of issues.
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class IssueFilterPanel extends Panel
{
    // TODO read these from a current set of values in the session/user pref map...
    private boolean showStatusNew = true;
    private boolean showStatusFeedback = true;
    private boolean showStatusAssigned = true;
    private boolean showStatusReopened = true;
    private boolean showStatusInProgress = true;
    private boolean showStatusResolved = false;
    private boolean showStatusClosed = false;

    private int assigns = 0;

    private User user;

    public IssueFilterPanel( String id, final User user ) {
        super( id );

        this.user = user;
        loadFilters();
        FilterBorder filter = new FilterBorder( "filter" );
        add( filter );

        final Form<IssueFilterPanel> filterForm = new Form<IssueFilterPanel>( "filterform" )
        {
            @Override
            protected void onSubmit()
            {
                super.onSubmit();

                saveFilters();
            }
        };
        filter.add( filterForm.setMarkupId( "filterForm" ).setOutputMarkupId( true ) );
        Button cancelButton = new Button( "cancelbutton" );
        filterForm.add( cancelButton );
        cancelButton.add( new AttributeModifier( "onclick", true, new Model<String>()
        {
            public String getObject()
            {
                return "filterbuttonAnimator.reverse();";
            }
        } ) );

        Button applyButton = new Button( "applybutton" );
        filterForm.add( applyButton );
        applyButton.add( new AttributeModifier( "onclick", true, new Model<String>()
        {
            public String getObject()
            {
                return "filterbuttonAnimator.reverse();";
            }
        } ) );

        filterForm.setModel( new CompoundPropertyModel<IssueFilterPanel>( this ) );
        final CheckBox[] checkboxes = new CheckBox[]{new CheckBox( "showStatusNew" ), new CheckBox( "showStatusFeedback" ),
                new CheckBox( "showStatusAssigned" ), new CheckBox( "showStatusReopened" ), new CheckBox( "showStatusInProgress" ),
                new CheckBox( "showStatusResolved" ), new CheckBox( "showStatusClosed" )};

        for ( CheckBox checkbox : checkboxes )
        {
            filterForm.add( checkbox );
        }

        RadioGroup assignments = new RadioGroup( "assigns" );
        filterForm.add( assignments );

        ListView assignment = new ListView<Integer>( "assignment", Arrays.asList( 1, 2, 3, 0 ) )
        {
            protected void populateItem( final ListItem<Integer> listItem )
            {
                final int value = listItem.getModelObject();
                WebMarkupContainer cell = new WebMarkupContainer( "cell" );
                cell.add( new AttributeModifier( "colspan", new Model<String>()
                {
                    public String getObject()
                    {
                        if ( listItem.getIndex() == 3 )
                        {
                            return "1";
                        }
                        return "2";
                    }
                } ) );
                listItem.add( cell );

                String label = "all";
                switch ( value ) {
                    case 1:
                        label = "assigned to me";
                        break;
                    case 2:
                        label = "assigned to someone";
                        break;
                    case 3:
                        label = "unassigned";
                }
                cell.add( new Label( "label", label ) );
                cell.add( new Radio<Integer>( "radio", listItem.getModel() ) );
            }
        };
        assignments.add( assignment );
    }

    private void loadFilters()
    {
        showStatusNew = user.getPreference( "filter.issue.showStatusNew", showStatusNew );
        showStatusFeedback = user.getPreference( "filter.issue.showStatusFeedback", showStatusFeedback );
        showStatusAssigned = user.getPreference( "filter.issue.showStatusAssigned", showStatusAssigned );
        showStatusReopened = user.getPreference( "filter.issue.showStatusReopened", showStatusReopened );
        showStatusInProgress = user.getPreference( "filter.issue.showStatusInProgress", showStatusInProgress );
        showStatusResolved = user.getPreference( "filter.issue.showStatusResolved", showStatusResolved );
        showStatusClosed = user.getPreference( "filter.issue.showStatusClosed", showStatusClosed );

        assigns = user.getPreference( "filter.issue.assigns", assigns );
    }

    private void saveFilters()
    {
        user.setPreference( "filter.issue.showStatusNew", showStatusNew );
        user.setPreference( "filter.issue.showStatusFeedback", showStatusFeedback );
        user.setPreference( "filter.issue.showStatusAssigned", showStatusAssigned );
        user.setPreference( "filter.issue.showStatusReopened", showStatusReopened );
        user.setPreference( "filter.issue.showStatusInProgress", showStatusInProgress );
        user.setPreference( "filter.issue.showStatusResolved", showStatusResolved );
        user.setPreference( "filter.issue.showStatusClosed", showStatusClosed );

        user.setPreference( "filter.issue.assigns", assigns );
    }

    public void setStatuses( boolean[] statuses )
    {
        for ( int i = 0; i < 6; i++ )
        {
            if ( statuses.length <= i )
            {
                break;
            }

            switch ( i )
            {
                case 0:
                    showStatusNew = statuses[0];
                    break;
                case 1:
                    showStatusFeedback = statuses[1];
                    break;
                case 2:
                    showStatusAssigned = statuses[2];
                    break;
                case 3:
                    showStatusReopened = statuses[3];
                    break;
                case 4:
                    showStatusInProgress = statuses[4];
                    break;
                case 5:
                    showStatusResolved = statuses[5];
                    break;
                case 6:
                    showStatusClosed = statuses[6];
            }
        }

        loadFilters();
    }

    public List<Integer> getStatuses()
    {
        List<Integer> ret = new LinkedList<Integer>();
        if ( showStatusNew )
            ret.add( 210 );
        if ( showStatusFeedback )
            ret.add( 220 );
        if ( showStatusAssigned )
            ret.add( 230 );
        if ( showStatusReopened )
            ret.add( 240 );
        if ( showStatusInProgress )
            ret.add( 245 );
        if ( showStatusResolved )
            ret.add( 250 );
        if ( showStatusClosed )
            ret.add( 260 );

        return ret;
    }
    
    public Criterion getStatusCriterion()
    {
        if ( getStatuses().size() > 0 )
        {
            return Restrictions.in( "status", getStatuses() );
        }
        else
        {
            return Restrictions.in( "status", Arrays.asList( 999 ) );
        }
    }

    public int getAssignments() {
        return assigns;
    }

    public Criterion getAssignmentCriterion() {
        switch ( getAssignments() ) {
            case 1:
                return Restrictions.eq( "assignee.username",
                        ( (HeadsUpSession) org.apache.wicket.Session.get() ).getUser().getUsername() );
            case 2:
                return Restrictions.isNotNull( "assignee" );
            case 3:
                return Restrictions.isNull( "assignee" );
        }

        return null;
    }
}
