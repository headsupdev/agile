/*
 * HeadsUp Agile
 * Copyright 2013-2015 Heads Up Development.
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

package org.headsupdev.agile.app.issues.rest;

import com.google.gson.*;
import org.apache.wicket.model.util.ListModel;
import org.headsupdev.agile.api.Permission;
import org.headsupdev.agile.app.issues.dao.SortableIssuesProvider;
import org.headsupdev.agile.app.issues.permission.IssueListPermission;
import org.headsupdev.agile.storage.StoredProject;
import org.headsupdev.agile.storage.issues.Issue;
import org.headsupdev.agile.storage.issues.Milestone;
import org.headsupdev.agile.web.MountPoint;
import org.headsupdev.agile.web.components.issues.IssueFilter;
import org.headsupdev.agile.web.rest.HeadsUpApi;

import org.apache.wicket.PageParameters;
import org.headsupdev.agile.web.wicket.SortableEntityProvider;
import org.hibernate.criterion.Criterion;

import java.lang.reflect.Type;

/**
 * An issues API that provides a simple list of issues per project.
 * <p/>
 * Created: 16/02/2013
 *
 * @author Andrew Williams
 * @since 2.0
 */
@MountPoint( "issues" )
public class IssuesApi
        extends HeadsUpApi
{
    public IssuesApi( PageParameters params )
    {
        super( params );
    }

    @Override
    public void setupJson( GsonBuilder builder )
    {
        super.setupJson( builder );

        builder.registerTypeHierarchyAdapter( Milestone.class, new MilestoneAdapter() );
    }

    @Override
    public Permission getRequiredPermission()
    {
        return new IssueListPermission();
    }

    @Override
    public void doGet( PageParameters params )
    {
        IssueFilter filter = createEmptyFilter();
        if ( getProject().equals( StoredProject.getDefault() ) )
        {
            SortableEntityProvider<Issue> provider = new SortableIssuesProvider( filter );
            setModel( new ListModel<Issue>( provider.list() ) );
        }
        else
        {
            SortableEntityProvider<Issue> provider = new SortableIssuesProvider( getProject(), filter );
            setModel( new ListModel<Issue>( provider.list() ) );
        }
    }

    private class MilestoneAdapter implements JsonSerializer<Milestone>
    {
        @Override
        public JsonElement serialize( Milestone milestone, Type type, JsonSerializationContext jsonSerializationContext )
        {
            return new JsonPrimitive( milestone.getName() );
        }
    }

    protected IssueFilter createEmptyFilter()
    {
        return new IssueFilter()
        {
            @Override
            public Criterion getStatusCriterion()
            {
                return null;
            }

            @Override
            public Criterion getAssignmentCriterion()
            {
                return null;
            }

            @Override
            public Criterion getDateCriterionUpdated()
            {
                return null;
            }

            @Override
            public Criterion getDateCriterionCreated()
            {
                return null;
            }
        };
    }
}