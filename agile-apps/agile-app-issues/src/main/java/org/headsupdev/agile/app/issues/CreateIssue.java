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

package org.headsupdev.agile.app.issues;

import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.api.Permission;
import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.app.issues.event.CreateIssueEvent;
import org.headsupdev.agile.app.issues.permission.IssueEditPermission;
import org.headsupdev.agile.storage.HibernateStorage;
import org.headsupdev.agile.storage.StoredProject;
import org.headsupdev.agile.storage.issues.Duration;
import org.headsupdev.agile.storage.issues.Issue;
import org.headsupdev.agile.storage.issues.Milestone;
import org.headsupdev.agile.web.HeadsUpPage;
import org.headsupdev.agile.web.MountPoint;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.Date;

/**
 * Create an issue page
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@MountPoint("create")
public class CreateIssue
        extends HeadsUpPage
{
    public Permission getRequiredPermission()
    {
        return new IssueEditPermission();
    }

    public void layout()
    {
        super.layout();
        add( CSSPackageResource.getHeaderContribution( getClass(), "issue.css" ) );

        Issue create = new Issue( getProject() );
        create.setReporter( getSession().getUser() );
        create.setStatus( Issue.STATUS_NEW );

        String mName = getPageParameters().getString( "milestone" );

        Milestone milestone = getMilestone( mName, getProject() );
        if ( milestone != null )
        {
            create.setMilestone( milestone );
            if ( milestone.getStartDate() != null && milestone.getStartDate().after( new Date() ) )
            {
                create.setIncludeInInitialEstimates( true );
            }
        }

        // remove the create option from the menu
        removeLink( ( (IssuesApplication) getHeadsUpApplication() ).getMenuItemCreate() );

        add( new EditIssueForm( "create", create, true, this )
        {
            public void onSubmit( Issue create )
            {
                boolean timeBurndown = Boolean.parseBoolean( getProject().getConfigurationValue( StoredProject.CONFIGURATION_TIMETRACKING_BURNDOWN ) );

                create.setCreated( new Date() );
                if ( create.getTimeEstimate() != null )
                {
                    if ( timeBurndown )
                    {
                        create.setTimeRequired( create.getTimeEstimate() );
                    }
                    else
                    {
                        create.setTimeRequired( new Duration( 0 ) );
                    }
                }
                ( (IssuesApplication) getHeadsUpApplication() ).addIssue( create );

                getHeadsUpApplication().addEvent( new CreateIssueEvent( create, create.getProject() ) );
            }
        } );
    }

    @Override
    public String getTitle()
    {
        return "Create Issue";
    }

    private Milestone getMilestone( String name, Project project )
    {
        Session session = ( (HibernateStorage) Manager.getStorageInstance() ).getHibernateSession();

        Transaction tx = session.beginTransaction();
        Query q = session.createQuery( "from Milestone m where id.name = :name and id.project.id = :pid" );
        q.setString( "name", name );
        q.setString( "pid", project.getId() );
        Milestone ret = (Milestone) q.uniqueResult();
        tx.commit();

        return ret;
    }
}
