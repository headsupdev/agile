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

import org.headsupdev.agile.web.HeadsUpPage;
import org.headsupdev.agile.web.BookmarkableMenuLink;
import org.headsupdev.agile.web.MountPoint;
import org.headsupdev.agile.web.components.ProjectTreeDropDownChoice;
import org.headsupdev.agile.web.components.issues.IssueUtils;
import org.headsupdev.agile.api.Permission;
import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.storage.HibernateStorage;
import org.headsupdev.agile.storage.issues.Issue;
import org.headsupdev.agile.storage.issues.IssueRelationship;
import org.headsupdev.agile.app.issues.permission.IssueEditPermission;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.model.CompoundPropertyModel;

import java.util.Date;

/**
 * Add a relationship for an issue
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@MountPoint( "relate" )
public class CreateRelationship
    extends HeadsUpPage
{
    private Issue issue;

    public Permission getRequiredPermission()
    {
        return new IssueEditPermission();
    }

    public void layout()
    {
        super.layout();
        add( CSSPackageResource.getHeaderContribution( getClass(), "issue.css" ) );

        long id;
        try
        {
            id = getPageParameters().getLong("id");
        }
        catch ( NumberFormatException e )
        {
            notFoundError();
            return;
        }

        Issue issue = IssuesApplication.getIssue( id, getProject() );
        if ( issue == null )
        {
            notFoundError();
            return;
        }

        addLink( new BookmarkableMenuLink( getPageClass( "issues/view" ), getPageParameters(), "view" ) );
        this.issue = issue;

        add( new RelationshipForm( "relationship" ) );
    }

    @Override
    public String getTitle()
    {
        return "Create relationship for issue " + issue.getId();
    }

    class RelationshipForm
        extends Form
    {
        private Project relatedProject = issue.getProject();
        private int relatedIssueId = 0;
        private int type = IssueRelationship.TYPE_LINKED;

        public RelationshipForm( String id )
        {
            super( id );
            setModel( new CompoundPropertyModel( this ) );

            add( new ProjectTreeDropDownChoice( "relatedProject" ) );
            add( new TextField( "relatedIssueId" ) );

            add( new DropDownChoice( "type", IssueUtils.getRelationships() ).setChoiceRenderer( new ChoiceRenderer()
            {
                public Object getDisplayValue( Object o )
                {
                    return IssueUtils.getRelationshipName( (Integer) o );
                }
            } ) );
        }

        public void onSubmit()
        {
            issue = (Issue) ( (HibernateStorage) getStorage() ).getHibernateSession().merge( issue );
            Issue related = IssuesApplication.getIssue( relatedIssueId, relatedProject );
            if ( related == null )
            {
                error( "Could not find issue " + relatedIssueId + " in project " + relatedProject );
                return;
            }

            for ( IssueRelationship relationship : issue.getRelationships() )
            {
                if ( relationship.getType() == type &&
                        relationship.getOwner().equals( issue ) &&
                        relationship.getRelated().equals( related ) )
                {
                    error( "Cannot add a duplicate relationship" );
                    return;
                }
            }

            if ( type > IssueRelationship.REVERSE_RELATIONSHIP )
            {
                type -= IssueRelationship.REVERSE_RELATIONSHIP;
                IssueRelationship relationship = new IssueRelationship( related, issue, type );

                ( (HibernateStorage) getStorage() ).save( relationship );
                related.getRelationships().add( relationship );
            }
            else
            {
                IssueRelationship relationship = new IssueRelationship( issue, related, type );

                ( (HibernateStorage) getStorage() ).save( relationship );
                issue.getRelationships().add( ( relationship ) );
            }
            issue.setUpdated( new Date() );

            setResponsePage( getPageClass( "issues/view" ), getPageParameters() );
        }
    }
}
