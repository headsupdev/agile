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

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.headsupdev.agile.api.User;
import org.headsupdev.agile.web.components.DateTimeWithTimeZoneField;
import org.headsupdev.agile.web.components.FilterBorder;
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
public abstract class MilestoneFilterPanel
        extends Panel
        implements MilestoneFilter
{
    public static final int FILTER_DATES = -1;
    private int dues = 0;

    private boolean showIncomplete = true;
    private boolean showComplete = false;

    private User user;
    private Date startDateDue, endDateDue;
    private boolean filterByDateUpdated;
    private Date startDateUpdated, endDateUpdated;
    private boolean filterByDateCreated;
    private Date startDateCreated, endDateCreated;
    private boolean filterByDateCompleted;
    private Date startDateCompleted, endDateCompleted;
    private int value;

    public MilestoneFilterPanel( String id, final User user )
    {
        super( id );

        this.user = user;
        loadFilters();
        FilterBorder filter = new FilterBorder( "filter" );
        add( filter );

        final Form<MilestoneFilterPanel> filterForm = new Form<MilestoneFilterPanel>( "filterform" )
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

        filterForm.setModel( new CompoundPropertyModel<MilestoneFilterPanel>( this ) );
        final RadioGroup dueGroup = new RadioGroup<Integer>( "dues", new PropertyModel<Integer>( dues, "dues" )
        {
            @Override
            public void setObject( Integer object )
            {
                dues = object;
            }

            @Override
            public Integer getObject()
            {
                return dues;
            }
        } );

        final DateTimeWithTimeZoneField startDateFieldDue = new DateTimeWithTimeZoneField( "startDateDue", new PropertyModel<Date>( this, "startDateDue" )
        {
            @Override
            public Date getObject()
            {
                return startDateDue;
            }

            @Override
            public void setObject( Date object )
            {
                if ( object == null &&  (Integer) dueGroup.getConvertedInput() != FILTER_DATES )
                {
                    return;
                }
                startDateDue = object;
            }
        } );
        final DateTimeWithTimeZoneField endDateFieldDue = new DateTimeWithTimeZoneField( "endDateDue", new PropertyModel<Date>( this, "endDateDue" ){
            @Override
            public Date getObject()
            {
                return endDateDue;
            }

            @Override
            public void setObject( Date object )
            {
                if ( object == null &&  (Integer) dueGroup.getConvertedInput() != FILTER_DATES )
                {
                    return;
                }
                endDateDue = object;
            }  
        });
        startDateFieldDue.setOutputMarkupId( true ).setMarkupId( "startDateDue" );
        endDateFieldDue.setOutputMarkupId( true ).setMarkupId( "endDateDue" );



        ListView dueView = new ListView<Integer>( "due", Arrays.asList( MilestonesApplication.QUERY_DUE_OVERDUE, MilestonesApplication.QUERY_DUE_SOON, MilestonesApplication.QUERY_DUE_DEFINED, 0 ) )
        {
            protected void populateItem( final ListItem<Integer> listItem )
            {
                value = listItem.getModelObject();
                String label = "all";
                switch ( value )
                {
                    case MilestonesApplication.QUERY_DUE_DEFINED:
                        label = "due later";
                        break;
                    case MilestonesApplication.QUERY_DUE_SOON:
                        label = "due soon";
                        break;
                    case MilestonesApplication.QUERY_DUE_OVERDUE:
                        label = "overdue";
                }
                listItem.add( new Label( "label", label ) );
                listItem.add( new Radio<Integer>( "radio", listItem.getModel() ) );
            }
        };
        dueGroup.add( dueView );
        dueGroup.add( new Radio<Integer>( "radioBetweenDates", new Model<Integer>( FILTER_DATES ) ).setMarkupId( "radioBetweenDates" ) );

        dueGroup.add( startDateFieldDue );
        dueGroup.add( endDateFieldDue );

        dueGroup.setRequired( true );
        filterForm.add( dueGroup.setOutputMarkupId( true ).setRenderBodyOnly( false ) );

        final DateTimeWithTimeZoneField startDateFieldUpdated = new DateTimeWithTimeZoneField( "startDateUpdated" );
        final DateTimeWithTimeZoneField endDateFieldUpdated = new DateTimeWithTimeZoneField( "endDateUpdated" );

        filterForm.add( startDateFieldUpdated.setOutputMarkupId( true ).setEnabled( filterByDateUpdated ) );
        filterForm.add( endDateFieldUpdated.setOutputMarkupId( true ).setEnabled( filterByDateUpdated ) );
        filterForm.add( new AjaxCheckBox( "filterByDateUpdated" )
        {
            @Override
            protected void onUpdate( AjaxRequestTarget target )
            {
                startDateFieldUpdated.setEnabled( filterByDateUpdated );
                endDateFieldUpdated.setEnabled( filterByDateUpdated );
                target.addComponent( startDateFieldUpdated );
                target.addComponent( endDateFieldUpdated );
            }
        } );

        final DateTimeWithTimeZoneField startDateFieldCreated = new DateTimeWithTimeZoneField( "startDateCreated" );
        final DateTimeWithTimeZoneField endDateFieldCreated = new DateTimeWithTimeZoneField( "endDateCreated" );

        filterForm.add( startDateFieldCreated.setOutputMarkupId( true ).setEnabled( filterByDateCreated ) );
        filterForm.add( endDateFieldCreated.setOutputMarkupId( true ).setEnabled( filterByDateCreated ) );
        filterForm.add( new AjaxCheckBox( "filterByDateCreated" )
        {
            @Override
            protected void onUpdate( AjaxRequestTarget target )
            {
                startDateFieldCreated.setEnabled( filterByDateCreated );
                endDateFieldCreated.setEnabled( filterByDateCreated );
                target.addComponent( startDateFieldCreated );
                target.addComponent( endDateFieldCreated );
            }
        } );

        final DateTimeWithTimeZoneField startDateFieldCompleted = new DateTimeWithTimeZoneField( "startDateCompleted" );
        final DateTimeWithTimeZoneField endDateFieldCompleted = new DateTimeWithTimeZoneField( "endDateCompleted" );
        final AjaxCheckBox filterByDateCompletedCheckbox = new AjaxCheckBox( "filterByDateCompleted" )
        {
            @Override
            protected void onUpdate( AjaxRequestTarget target )
            {
                startDateFieldCompleted.setEnabled( filterByDateCompleted );
                endDateFieldCompleted.setEnabled( filterByDateCompleted );
                target.addComponent( startDateFieldCompleted );
                target.addComponent( endDateFieldCompleted );
            }
        };

        filterForm.add( filterByDateCompletedCheckbox.setOutputMarkupId( true ).setVisible( showComplete ) );
        filterForm.add( startDateFieldCompleted.setOutputMarkupId( true ).setEnabled( false ) );
        filterForm.add( endDateFieldCompleted.setOutputMarkupId( true ).setEnabled( false ) );

        filterForm.add( new CheckBox( "showIncomplete" ) );
        filterForm.add( new AjaxCheckBox( "showComplete" )
        {
            @Override
            protected void onUpdate( AjaxRequestTarget target )
            {
                filterByDateCompletedCheckbox.setVisible( showComplete );
                target.addComponent( filterByDateCompletedCheckbox );
                target.addComponent( filterForm );
            }
        } );
    }

    private void loadFilters()
    {
        showIncomplete = user.getPreference( "filter.milestone.showIncomplete", showIncomplete );
        showComplete = user.getPreference( "filter.milestone.showComplete", showComplete );

        dues = user.getPreference( "filter.milestone.dues", dues );

        startDateDue = user.getPreference( "filter.milestone.startDateDue", startDateDue );
        endDateDue = user.getPreference( "filter.milestone.endDateDue", endDateDue );
        if ( dues == FILTER_DATES && isInvalidDatePeriod( startDateDue, endDateDue ) )
        {
            invalidDatePeriod();
            return;
        }

        filterByDateUpdated = user.getPreference( "filter.milestone.filterByDateUpdated", filterByDateUpdated );
        startDateUpdated = user.getPreference( "filter.milestone.startDateUpdated", startDateUpdated );
        endDateUpdated = user.getPreference( "filter.milestone.endDateUpdated", endDateUpdated );
        if ( filterByDateUpdated && isInvalidDatePeriod( startDateUpdated, endDateUpdated ) )
        {
            invalidDatePeriod();
            return;
        }

        filterByDateCreated = user.getPreference( "filter.milestone.filterByDateCreated", filterByDateCreated );
        startDateCreated = user.getPreference( "filter.milestone.startDateCreated", startDateCreated );
        endDateCreated = user.getPreference( "filter.milestone.endDateCreated", endDateCreated );
        if ( filterByDateCreated && isInvalidDatePeriod( startDateCreated, endDateCreated ) )
        {
            invalidDatePeriod();
            return;
        }

        filterByDateCompleted = user.getPreference( "filter.milestone.filterByDateCompleted", filterByDateCompleted );
        startDateCompleted = user.getPreference( "filter.milestone.startDateCompleted", startDateCompleted );
        endDateCompleted = user.getPreference( "filter.milestone.endDateCompleted", endDateCompleted );
        if ( filterByDateCompleted && isInvalidDatePeriod( startDateCompleted, endDateCompleted ) )
        {
            invalidDatePeriod();
            return;
        }
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


        if ( dues == FILTER_DATES && isInvalidDatePeriod( startDateDue, endDateDue ) )
        {
            invalidDatePeriod();
            return;
        }

        user.setPreference( "filter.milestone.startDateDue", startDateDue );
        user.setPreference( "filter.milestone.endDateDue", endDateDue );

        if ( filterByDateUpdated && isInvalidDatePeriod( startDateUpdated, endDateUpdated ) )
        {
            invalidDatePeriod();
            return;
        }
        user.setPreference( "filter.milestone.filterByDateUpdated", filterByDateUpdated );
        user.setPreference( "filter.milestone.startDateUpdated", startDateUpdated );
        user.setPreference( "filter.milestone.endDateUpdated", endDateUpdated );

        if ( filterByDateCreated && isInvalidDatePeriod( startDateCreated, endDateCreated ) )
        {
            invalidDatePeriod();
            return;
        }
        user.setPreference( "filter.milestone.filterByDateCreated", filterByDateCreated );
        user.setPreference( "filter.milestone.startDateCreated", startDateCreated );
        user.setPreference( "filter.milestone.endDateCreated", endDateCreated );

        if ( filterByDateCompleted && isInvalidDatePeriod( startDateCompleted, endDateCompleted ) )
        {
            invalidDatePeriod();
            return;
        }
        user.setPreference( "filter.milestone.filterByDateCompleted", filterByDateCompleted );
        user.setPreference( "filter.milestone.startDateCompleted", startDateCompleted );
        user.setPreference( "filter.milestone.endDateCompleted", endDateCompleted );
    }

    public boolean isInvalidDatePeriod( Date start, Date end )
    {
        if ( start == null || end == null )
        {
            return true;
        }
        return start.after( end );
    }


    @Override
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

    @Override
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
            case FILTER_DATES:
                return getDateCriterionDue();
            default:
                return null;
        }
    }

    private Criterion getDateCriterionDue()
    {
        if ( startDateDue != null && endDateDue != null )
        {
            if ( !startDateDue.after( endDateDue ) )
            {
                return Restrictions.between( "due", startDateDue, endDateDue );
            }
        }
        return null;
    }

    @Override
    public Criterion getDateCriterionUpdated()
    {
        if ( startDateUpdated != null && endDateUpdated != null && filterByDateUpdated )
        {
            if ( !startDateUpdated.after( endDateUpdated ) )
            {
                return Restrictions.between( "updated", startDateUpdated, endDateUpdated );
            }
        }
        return null;
    }

    @Override
    public Criterion getDateCriterionCreated()
    {
        if ( startDateCreated != null && endDateCreated != null && filterByDateCreated )
        {
            if ( !startDateCreated.after( endDateCreated ) )
            {
                return Restrictions.between( "created", startDateCreated, endDateCreated );
            }
        }
        return null;
    }

    @Override
    public Criterion getDateCriterionCompleted()
    {
        if ( startDateCompleted != null && endDateCompleted != null && filterByDateCompleted )
        {
            if ( !startDateCompleted.after( endDateCompleted ) )
            {
                return Restrictions.between( "completed", startDateCompleted, endDateCompleted );
            }
        }
        return null;
    }

    public abstract void invalidDatePeriod();
}
