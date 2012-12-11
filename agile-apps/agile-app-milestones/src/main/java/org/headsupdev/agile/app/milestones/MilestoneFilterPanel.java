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

import org.headsupdev.agile.api.User;
import org.headsupdev.agile.web.components.FilterBorder;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.headsupdev.agile.web.components.milestones.MilestoneStatusModifier;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import java.util.Arrays;
import java.util.Date;

/**
 * A filter panel for managing the filtering of milestones.
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 2.0
 */
public class MilestoneFilterPanel
    extends Panel
    implements MilestoneFilter
{
    private int dues = 0;

    private boolean showIncomplete = true;
    private boolean showComplete = false;

    private User user;

    public MilestoneFilterPanel( String id, final User user ) {
        super( id );

        this.user = user;
        loadFilters();
        FilterBorder filter = new FilterBorder( "filter" );
        add( filter );

        Form<MilestoneFilterPanel> filterForm = new Form<MilestoneFilterPanel>( "filterform" )
        {
            @Override
            protected void onSubmit()
            {
                super.onSubmit();

                saveFilters();
            }
        };
        filter.add( filterForm );
        Button cancelButton = new Button( "cancelbutton" );
        filterForm.add( cancelButton );
        cancelButton.add( new AttributeModifier( "onclick", true, new Model<String>() {
            public String getObject() {
                return "filterbuttonAnimator.reverse();";
            }
        } ) );

        Button applyButton = new Button( "applybutton" );
        filterForm.add( applyButton );
        applyButton.add( new AttributeModifier( "onclick", true, new Model<String>() {
            public String getObject() {
                return "filterbuttonAnimator.reverse();";
            }
        } ) );

        filterForm.setModel( new CompoundPropertyModel<MilestoneFilterPanel>( this ) );
        RadioGroup dueGroup = new RadioGroup( "dues" );
        filterForm.add( dueGroup );

        ListView dueView = new ListView<Integer>( "due", Arrays.asList( 3, 2, 1, 0 ) ) {
            protected void populateItem( final ListItem<Integer> listItem ) {
                final int value = listItem.getModelObject();
                String label = "all";
                switch ( value ) {
                    case 1:
                        label = "due later";
                        break;
                    case 2:
                        label = "due soon";
                        break;
                    case 3:
                        label = "overdue";
                }
                listItem.add( new Label( "label", label ) );
                listItem.add( new Radio<Integer>( "radio", listItem.getModel() ) );
            }
        };
        dueGroup.add( dueView );

        filterForm.add( new CheckBox( "showIncomplete" ) );
        filterForm.add( new CheckBox( "showComplete" ) );
    }

    private void loadFilters()
    {
        showIncomplete = user.getPreference( "filter.milestone.showIncomplete", showIncomplete );
        showComplete = user.getPreference( "filter.milestone.showComplete", showComplete );

        dues = user.getPreference( "filter.milestone.dues", dues );
    }
    
    public void setFilters( int due, boolean incomplete, boolean complete )
    {
        showIncomplete = incomplete;
        showComplete = complete;
        
        dues = due;
    }

    private void saveFilters()
    {
        user.setPreference( "filter.milestone.showIncomplete", showIncomplete );
        user.setPreference( "filter.milestone.showComplete", showComplete );

        user.setPreference( "filter.milestone.dues", dues );
    }

    public Criterion getCompletedCriterion()
    {
        if ( showIncomplete )
        {
            if ( !showComplete )
            {
                return Restrictions.isNull( "completed" );
            }
        }
        else
        {
            if ( showComplete )
            {
                return Restrictions.isNotNull( "completed" );
            }
            else
            {
                // will be nothing, can we return that faster?
                return Restrictions.and( Restrictions.isNull( "completed" ), Restrictions.isNotNull( "completed" ) );
            }
        }

        return null;
    }

    public Criterion getDueCriterion()
    {
        switch ( dues )
        {
            case MilestonesApplication.QUERY_DUE_DEFINED:
                return Restrictions.ge( "due", MilestoneStatusModifier.getDueSoonDate() );
            case MilestonesApplication.QUERY_DUE_SOON:
                return Restrictions.and( Restrictions.lt( "due", MilestoneStatusModifier.getDueSoonDate() ),
                        Restrictions.ge( "due", new Date() ) );
            case MilestonesApplication.QUERY_DUE_OVERDUE:
                return Restrictions.lt( "due", new Date() );
        }

        return null;
    }
}
