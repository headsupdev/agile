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

import org.apache.wicket.extensions.ajax.markup.html.autocomplete.*;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.CompoundPropertyModel;
import org.headsupdev.agile.api.Permission;
import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.app.issues.permission.IssueEditPermission;
import org.headsupdev.agile.storage.HibernateStorage;
import org.headsupdev.agile.storage.dao.IssuesDAO;
import org.headsupdev.agile.storage.issues.Issue;
import org.headsupdev.agile.storage.issues.IssueRelationship;
import org.headsupdev.agile.web.BookmarkableMenuLink;
import org.headsupdev.agile.web.HeadsUpPage;
import org.headsupdev.agile.web.MountPoint;
import org.headsupdev.agile.web.components.ProjectTreeDropDownChoice;
import org.headsupdev.agile.web.components.issues.IssueUtils;
import org.hibernate.criterion.Restrictions;

import java.util.Date;
import java.util.Iterator;

/**
 * Add a relationship for an issue
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@MountPoint("relate")
public class CreateRelationship
        extends HeadsUpPage
{
    private Issue issue;
    private IssuesDAO dao;

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
            id = getPageParameters().getLong( "id" );
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

        this.dao = new IssuesDAO();
        addLink( new BookmarkableMenuLink( getPageClass( "issues/view" ), getPageParameters(), "view" ) );
        this.issue = issue;

        add( new RelationshipForm( "relationship" ) );
    }

    class RelationshipForm
            extends Form
    {
        private Project relatedProject = issue.getProject();
        private String relatedIssueText;
        private int type = IssueRelationship.TYPE_LINKED;

        public RelationshipForm( String id )
        {
            super( id );
            setModel( new CompoundPropertyModel( this ) );
            add( new Subheader( "subHeader", "Create Relationship for Issue:", issue ) );
            add( new ProjectTreeDropDownChoice( "relatedProject" )
            {
                @Override
                protected boolean wantOnSelectionChangedNotifications()
                {
                    return true;
                }
            } );
            add( new AutoCompleteTextField<Issue>( "relatedIssueText" )
            {
                @Override
                protected AutoCompleteBehavior<Issue> newAutoCompleteBehavior( IAutoCompleteRenderer<Issue> renderer,
                                                                               AutoCompleteSettings settings )
                {
                    return super.newAutoCompleteBehavior( new AbstractAutoCompleteTextRenderer<Issue>()
                    {
                        @Override
                        protected String getTextValue( Issue issue )
                        {
                            return issue.getId() + ": " + issue.getSummary();
                        }
                    }, settings );
                }

                @Override
                protected Iterator<Issue> getChoices( String text )
                {
                    String searchText = "%" + text + "%";
                    long maybeId = 0;
                    try
                    {
                        maybeId = Long.parseLong( text );
                    }
                    catch ( NumberFormatException e )
                    {
                        // ignore
                    }
                    return dao.search( Restrictions.or( Restrictions.eq( "id.id", maybeId ),
                            Restrictions.like( "summary", searchText ) ), relatedProject ).iterator();
                }
            } );

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
            long relatedIssueId = getIdFromAutocomplete( relatedIssueText );
            Issue related = IssuesApplication.getIssue( relatedIssueId, relatedProject );
            if ( related == null )
            {
                error( "Could not find issue " + relatedIssueText + " in project " + relatedProject );
                return;
            }
            if ( related.equals( issue ) )
            {
                error( "Cannot relate to the current issue" );
                return;
            }

            IssueRelationship relationship = new IssueRelationship( issue, related, type );
            if ( issue.hasRelationship( relationship ) )
            {
                error( "Cannot add a duplicate relationship" );
                return;
            }

            if ( type > IssueRelationship.REVERSE_RELATIONSHIP )
            {
                relationship = relationship.getInverseRelationship();

                ( (HibernateStorage) getStorage() ).save( relationship );
                related.getRelationships().add( relationship );
            }
            else
            {
                ( (HibernateStorage) getStorage() ).save( relationship );
                issue.getRelationships().add( ( relationship ) );
            }
            issue.setUpdated( new Date() );

            setResponsePage( getPageClass( "issues/view" ), getPageParameters() );
        }
    }

    protected long getIdFromAutocomplete( String autocompleteText )
    {
        if ( autocompleteText == null )
        {
            return 0;
        }

        int colonPos = autocompleteText.indexOf( ':' );
        String idString;
        if ( colonPos == -1 )
        {
            idString = autocompleteText;
        }
        else
        {
            idString = autocompleteText.substring( 0, colonPos );
        }

        try
        {
            return Long.parseLong( idString );
        }
        catch ( NumberFormatException e )
        {
            return 0;
        }
    }
}
