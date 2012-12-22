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

import org.headsupdev.agile.storage.HibernateStorage;
import org.headsupdev.agile.storage.issues.Milestone;
import org.headsupdev.agile.web.HeadsUpPage;
import org.headsupdev.agile.web.HeadsUpSession;
import org.headsupdev.agile.web.components.IdPatternValidator;
import org.apache.wicket.PageParameters;
import org.apache.wicket.extensions.yui.calendar.DateTimeField;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;

import java.util.Date;
import java.util.TimeZone;

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
    Milestone milestone;
    boolean creating;

    public EditMilestoneForm( String id, final Milestone mile, final boolean creating, final HeadsUpPage owner )
    {
        super( id );
        this.milestone = mile;
        this.creating = creating;

        Form<Milestone> form = new Form<Milestone>( "edit" ) {
            public void onSubmit()
            {
                if ( !creating )
                {
                    milestone = (Milestone) ( (HibernateStorage) owner.getStorage() ).getHibernateSession().merge( milestone );
                }
                milestone.setUpdated( new Date() );
                submitParent();

                PageParameters params = new PageParameters();
                params.add( "project", milestone.getProject().getId() );
                params.add( "id", milestone.getName() );
                setResponsePage( owner.getPageClass( "milestones/view" ), params );
            }

        };

        layout( form );
        add( form );
    }

    public void layout( Form<Milestone> form )
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
        form.add( new DateTimeField( "startDate" )
        {
            protected TimeZone getClientTimeZone()
            {
                return ((HeadsUpSession) getSession()).getTimeZone();
            }
        } );
        form.add( new DateTimeField( "dueDate" )
        {
            protected TimeZone getClientTimeZone()
            {
                return ((HeadsUpSession) getSession()).getTimeZone();
            }
        } );

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