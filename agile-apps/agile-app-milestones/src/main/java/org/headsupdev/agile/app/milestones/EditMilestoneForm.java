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

package org.headsupdev.agile.app.milestones;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.PropertyModel;
import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.storage.HibernateStorage;
import org.headsupdev.agile.storage.dao.MilestoneGroupsDAO;
import org.headsupdev.agile.storage.dao.MilestonesDAO;
import org.headsupdev.agile.storage.issues.Milestone;
import org.headsupdev.agile.storage.issues.MilestoneGroup;
import org.headsupdev.agile.web.HeadsUpPage;
import org.headsupdev.agile.web.components.DateTimeWithTimeZoneField;
import org.headsupdev.agile.web.components.IdPatternValidator;
import org.headsupdev.agile.web.components.OnePressSubmitButton;

import java.util.Date;

/**
 * The form used when editing / creating a milestone
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class EditMilestoneForm
        extends Panel
{
    private final HeadsUpPage owner;
    Milestone milestone;
    boolean creating;

    public EditMilestoneForm( String id, final Milestone mile, final boolean creating, final HeadsUpPage owner )
    {
        super( id );
        this.milestone = mile;
        this.creating = creating;
        this.owner = owner;

        Form<Milestone> form = new Form<Milestone>( "edit" )
        {
            public void onSubmit()
            {
                if ( !creating )
                {
                    milestone = (Milestone) ( (HibernateStorage) owner.getStorage() ).getHibernateSession().merge( milestone );
                }
                milestone.setUpdated( new Date() );
                MilestonesDAO dao = new MilestonesDAO();
                if ( creating )
                {
                    boolean alreadyExists = dao.find( milestone.getName(), milestone.getProject() ) != null;
                    if ( alreadyExists )
                    {
                        warn( "Cannot create milestone. A milestone with that name already exists." );
                        return;
                    }
                }

                submitParent();

                PageParameters params = new PageParameters();
                params.add( "project", milestone.getProject().getId() );
                params.add( "id", milestone.getName() );
                setResponsePage( owner.getPageClass( "milestones/view" ), params );
            }

        };
        form.add( new OnePressSubmitButton( "submitMilestone" ) );
        layout( form );
        add( form );
    }

    public void layout( final Form<Milestone> form )
    {
        form.setModel( new CompoundPropertyModel<Milestone>( milestone ) );

        form.add( new Label( "project", milestone.getProject().getAlias() ) );
        if ( creating )
        {
            form.add( new TextField<String>( "name" ).add( new IdPatternValidator() ).setRequired( true ) );
            form.add( new WebMarkupContainer( "name-label" ).setVisible( false ) );
//            form.add( new Label( "created", new FormattedDateModel( new Date() ) ) );
        }
        else
        {
            form.add( new Label( "name-label", milestone.getName() ) );
            form.add( new WebMarkupContainer( "name" ).setVisible( false ) );
//            form.add( new Label( "created", new FormattedDateModel( milestone.getCreated() ) ) );
        }
        form.add( new DateTimeWithTimeZoneField( "startDate" ) );
        form.add( new DateTimeWithTimeZoneField( "dueDate", new PropertyModel<Date>(
                new PropertyModel<Milestone>( this, "milestone" ), "dueDate" )
        {
            @Override
            public void setObject( Date object )
            {
                // this piece of work is ensuring that we attach the milestone to the model before the wicket value
                // setting occurs which will require milestone lookup on a group if due is not null
                if ( !creating )
                {
                    milestone = (Milestone) ( (HibernateStorage) Manager.getStorageInstance() ).getHibernateSession().merge( milestone );
                    form.getModel().setObject( milestone );
                }

                super.setObject( object );
            }
        } ) );
        MilestoneGroupsDAO dao = new MilestoneGroupsDAO();
        form.add( new DropDownChoice<MilestoneGroup>( "group", dao.findAll( owner.getProject() ) ).setNullValid( true ) );

        form.add( new TextArea( "description" ) );
    }

    public void submitParent()
    {
        // allow others to override
    }

    public Milestone getMilestone()
    {
        return milestone;
    }

    public boolean isCreating()
    {
        return creating;
    }


}