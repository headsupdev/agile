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

package org.headsupdev.agile.app.admin.configuration;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.app.admin.AdminApplication;
import org.headsupdev.agile.storage.HibernateStorage;
import org.headsupdev.agile.storage.StoredProject;
import org.headsupdev.agile.web.MountPoint;
import org.headsupdev.agile.web.components.OnePressSubmitButton;
import org.headsupdev.agile.web.components.ProjectTreeDropDownChoice;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 * A page to configure project parameters - not much yet
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@MountPoint("configuration/projects")
public class ProjectConfiguration
        extends ConfigurationPage
{
    Project project;

    public void layout()
    {
        super.layout();
        project = getProject();
        add( CSSPackageResource.getHeaderContribution( AdminApplication.class, "admin.css" ) );

        Form projectConfig = new Form( "projectconfig" )
        {
            @Override
            protected void onSubmit()
            {
                Session session = ( (HibernateStorage) getStorage() ).getHibernateSession();
                Transaction tx = session.beginTransaction();
                session.update( project );
                tx.commit();
            }
        };
        projectConfig.add( new ChangeProjectDropDownChoice( "project" ) );

        TextField alias = new TextField( "alias", new PropertyModel( project, "alias" ) );
        alias.setVisible( !project.equals( StoredProject.getDefault() ) );
        projectConfig.add( alias );
        projectConfig.add( new Label( "defaultalias", project.getName() ) );

        CheckBox disabled = new CheckBox( "disabled", new PropertyModel( project, "disabled" ) );
        disabled.setVisible( !project.equals( StoredProject.getDefault() ) && getStorage().canEnableProject( project ) );
        projectConfig.add( disabled );

        projectConfig.add( new ConfigurationItemPanel( "timeenabled", StoredProject.CONFIGURATION_TIMETRACKING_ENABLED,
                project.getConfiguration(), null, project, false, 1 ) );
        projectConfig.add( new ConfigurationItemPanel( "timerequired", StoredProject.CONFIGURATION_TIMETRACKING_REQUIRED,
                project.getConfiguration(), null, project, false, 2 ) );
        projectConfig.add( new ConfigurationItemPanel( "timeburndown", StoredProject.CONFIGURATION_TIMETRACKING_BURNDOWN,
                project.getConfiguration(), null, project, false, 3 ) );
        projectConfig.add( new ConfigurationItemPanel( "timeweekend", StoredProject.CONFIGURATION_TIMETRACKING_IGNOREWEEKEND,
                project.getConfiguration(), null, project, false, 4 ) );
        projectConfig.add( new OnePressSubmitButton( "submitProjConfig" ) );
        add( projectConfig );
    }

    class ChangeProjectDropDownChoice
            extends ProjectTreeDropDownChoice
    {

        public ChangeProjectDropDownChoice( String id )
        {
            super( id, new Model<Project>()
            {
                @Override
                public Project getObject()
                {
                    return getProject();
                }

                @Override
                public void setObject( Project p )
                {
                    super.setObject( p );

                    PageParameters params = new PageParameters();
                    params.add( "project", p.getId() );
                    ProjectConfiguration.this.setResponsePage( ProjectConfiguration.class, params );
                }
            }, true, true );
        }

        @Override
        protected boolean wantOnSelectionChangedNotifications()
        {
            return true;
        }
    }
}
