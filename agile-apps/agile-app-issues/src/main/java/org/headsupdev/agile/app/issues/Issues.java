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

package org.headsupdev.agile.app.issues;

import org.headsupdev.agile.app.issues.dao.SortableIssuesProvider;
import org.headsupdev.agile.storage.issues.Issue;
import org.headsupdev.agile.web.wicket.SortableEntityProvider;
import org.headsupdev.agile.security.permission.ProjectListPermission;
import org.headsupdev.agile.web.HeadsUpPage;
import org.headsupdev.agile.web.components.issues.IssueListPanel;
import org.headsupdev.agile.web.components.issues.IssueFilterPanel;
import org.headsupdev.agile.api.Permission;
import org.headsupdev.agile.storage.StoredProject;
import org.headsupdev.agile.app.issues.permission.IssueListPermission;
import org.apache.wicket.markup.html.CSSPackageResource;

/**
 * Issues home page
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class Issues
    extends HeadsUpPage
{
    IssueFilterPanel filter;
    public Permission getRequiredPermission()
    {
        return new IssueListPermission();
    }

    public void layout()
    {
        super.layout();
        add( CSSPackageResource.getHeaderContribution( getClass(), "issue.css" ) );

        if ( getProject().equals( StoredProject.getDefault() ) )
        {
            requirePermission( new ProjectListPermission() );
        }

        filter = new IssueFilterPanel( "filter", getFilterButton(), getSession().getUser() )
        {
            @Override
            public void invalidDatePeriod()
            {
                warn( "Invalid date period" );
            }
        };
        add( filter );

        SortableEntityProvider<Issue> provider;
        if ( getProject().equals( StoredProject.getDefault() ) )
        {
            provider = new SortableIssuesProvider( filter );
        }
        else
        {
            provider = new SortableIssuesProvider( getProject(), filter );
        }

        add( new IssueListPanel( "issues", provider, this, !getProject().equals( StoredProject.getDefault() ), false, null ) );
    }

    @Override
    public String getTitle()
    {
        return null;
    }

    @Override
    public boolean hasFilter()
    {
        return true;
    }
}

