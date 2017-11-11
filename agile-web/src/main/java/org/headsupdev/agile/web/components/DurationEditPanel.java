/*
 * HeadsUp Agile
 * Copyright 2009-2012 Heads Up Development.
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

import org.headsupdev.agile.storage.issues.Duration;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

/**
 * Used to render a Duration object as three separate components.
 * comp1: unit
 * comp2: selector
 * comp3: minutes
 * <p/>
 * the minutes component is visible if the selector is set to hours.
 *
 * If you alter the model object outside of this class you can
 * call refreshUI to update the components to reflect the change.
 *
 * <p/>
 * <p/>
 * Created: 25/04/2012
 *
 * @author roberthewitt
 * @since 2.0-alpha-2
 */
public class DurationEditPanel
        extends Panel
{

    // model we are passed through, changes are replicated onto this object.
    private IModel<Duration> passedThroughModel;

    // model data tied to components
    private Duration main = new Duration( 0, Duration.UNIT_HOURS );
    private Duration minutes = new Duration( 0, Duration.UNIT_MINUTES );

    // wicket components to render on screen.
    private TextField mainTimeValue;
    private DropDownChoice mainTimeChoiceUnit;
    private DropDownChoice minuteChoiceValue;

    public DurationEditPanel( String id, IModel<Duration> model )
    {
        super( id );
        this.passedThroughModel = model;
        layout();
    }

    private void layout()
    {
        main.setHours( passedThroughModel.getObject().getWholeHours() );
        mainTimeValue = new TextField<Integer>( "main.time", new PropertyModel<Integer>( main, "time" ) );
        // register listener to update the model object when the main time is altered.
        mainTimeValue.add( new AjaxFormComponentUpdatingBehavior( "onchange" )
        {
            @Override
            protected void onUpdate( AjaxRequestTarget ajaxRequestTarget )
            {
                refreshModelObject();
            }
        } );
        add( mainTimeValue );

        mainTimeChoiceUnit = new DropDownChoice<String>( "main.timeUnit", new PropertyModel<String>( main, "timeUnit" )
        {
            @Override
            public String getObject()
            {
                String object = super.getObject();
                if ( object == null )
                {
                    return Duration.OPTIONS_TIME_UNIT.get( 0 );
                }
                return object;
            }
        }, Duration.OPTIONS_TIME_UNIT )
        {
            @Override
            protected boolean wantOnSelectionChangedNotifications()
            {
                return true;
            }

            @Override
            protected void onSelectionChanged( String newSelection )
            {
                super.onSelectionChanged( newSelection );

                // show or hide the minute selector
                if ( newSelection.equalsIgnoreCase( Duration.UNIT_HOURS ) )
                {
                    minuteChoiceValue.setVisible( true );
                }
                else
                {
                    minuteChoiceValue.setVisible( false );
                    minutes.setTime( 0 );
                }

                refreshModelObject();
            }

        };
        add( mainTimeChoiceUnit );

        minutes.setTime( getClosestMinuteValue( passedThroughModel.getObject().getWholeMinutes() ) );
        minuteChoiceValue = new DropDownChoice<Integer>( "minutes.time", new PropertyModel<Integer>( minutes, "time" )
        {
            @Override
            public Integer getObject()
            {
                Integer value = super.getObject();
                if ( value == null )
                {
                    return 0;
                }
                else
                {
                    return value;
                }
            }
        }, Duration.OPTIONS_MINUTE, new IChoiceRenderer<Integer>()
        {
            public Object getDisplayValue( Integer integer )
            {
                return getClosestMinuteValueAsString( integer );
            }

            public String getIdValue( Integer integer, int i )
            {
                return integer.toString();
            }
        }
        )
        {
            @Override
            protected boolean wantOnSelectionChangedNotifications()
            {
                return true;
            }

            @Override
            protected void onSelectionChanged( Integer newSelection )
            {
                super.onSelectionChanged( newSelection );
                refreshModelObject();
            }
        };

        add( minuteChoiceValue );
    }

    /**
     * this flushes out any changes back to the initial model passed through the constructor.
     */
    private void refreshModelObject()
    {
        Duration model = passedThroughModel.getObject();

        if ( minutes.getTime() == 0 )
        {
            // if we have no minutes to worry about our model should reflect the main componenet
            model.setTime( main.getTime() );
            model.setTimeUnit( main.getTimeUnit() );
        } else {
            // if we have minutes we need to combine the main and minutes
            // then update our model on the hours set.
            Duration combined = Duration.combine( minutes, main );
            model.setHours( combined.getHours() );
        }
    }

    public Panel setRequired( boolean required )
    {
        mainTimeValue.setRequired( required );
        mainTimeChoiceUnit.setRequired( required );
        minuteChoiceValue.setRequired( required );
        return this;
    }

    private int getClosestMinuteValue( int minuteValue )
    {
        for ( int i = 0; i < Duration.OPTIONS_MINUTE.size(); i++ )
        {
            int optionValue = Duration.OPTIONS_MINUTE.get( i );
            if ( minuteValue <= optionValue )
            {
                return optionValue;
            }
        }

        return Duration.OPTIONS_MINUTE.get( 0 );
    }


    private String getClosestMinuteValueAsString( int minuteValue )
    {
        String zeroValue = "00";
        if ( minuteValue == 0 )
        {
            return zeroValue;
        }
        else
        {
            return String.valueOf( minuteValue );
        }
    }

    @Override
    protected void onBeforeRender()
    {
        super.onBeforeRender();

        // based on the model data we have we should update our UI
        // if this is used incorrectly could cause the user some discomfort.
        // ie you can update the model outside of this component and it will update the fields directly
        Duration modelState = passedThroughModel.getObject();
        minutes.setTime( getClosestMinuteValue( modelState.getWholeMinutes() ) );

        if ( modelState.getTimeUnit().equalsIgnoreCase( Duration.UNIT_HOURS ) ||
                modelState.getTimeUnit().equalsIgnoreCase( Duration.UNIT_MINUTES ) )
        {
            main.setTimeUnit( Duration.UNIT_HOURS );
            main.setTime( modelState.getWholeHours() );
        }
        else
        {
            main.setTime( modelState.getTime() );
            main.setTimeUnit( modelState.getTimeUnit() );
        }

        minuteChoiceValue.setVisible( main.getTimeUnit().equalsIgnoreCase( Duration.UNIT_HOURS ) );

        mainTimeValue.modelChanged();
        mainTimeChoiceUnit.modelChanged();
        minuteChoiceValue.modelChanged();
    }
}
