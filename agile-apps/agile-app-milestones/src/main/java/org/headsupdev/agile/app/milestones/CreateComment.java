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

import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.headsupdev.agile.storage.issues.Milestone;
import org.headsupdev.agile.web.HeadsUpPage;
import org.headsupdev.agile.web.BookmarkableMenuLink;
import org.headsupdev.agile.web.MountPoint;
import org.headsupdev.agile.api.Permission;
import org.headsupdev.agile.storage.HibernateStorage;
import org.headsupdev.agile.storage.Comment;
import org.headsupdev.agile.api.Event;
import org.headsupdev.agile.app.milestones.permission.MilestoneEditPermission;
import org.headsupdev.agile.app.milestones.event.UpdateMilestoneEvent;

import java.util.Date;

/**
 * Add a comment for a milestone
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@MountPoint( "comment" )
public class CreateComment
    extends HeadsUpPage
{
    private Milestone milestone;
    private String submitLabel = "Create Comment";

    public Permission getRequiredPermission() {
        return new MilestoneEditPermission();
    }

    public void layout()
    {
        super.layout();
        add( CSSPackageResource.getHeaderContribution( getClass(), "milestone.css" ));

        String name = getPageParameters().getString( "id" );

        Milestone milestone = MilestonesApplication.getMilestone( name, getProject() );
        if ( milestone == null )
        {
            notFoundError();
            return;
        }

        addLink( new BookmarkableMenuLink( getPageClass( "milestones/view" ), getPageParameters(), "view" ) );
        this.milestone = milestone;

        add( new CommentForm( "comment" ) );
    }

    @Override
    public String getTitle()
    {
        return submitLabel + " for milestone " + milestone.getName();
    }

    public Milestone getMilestone()
    {
        return milestone;
    }

    public void setSubmitLabel( String submitLabel )
    {
        this.submitLabel = submitLabel;
    }

    protected void layoutChild( Form form )
    {
    }

    protected void submitChild()
    {
    }

    protected Event getUpdateEvent( Comment comment )
    {
        return new UpdateMilestoneEvent( milestone, milestone.getProject(), getSession().getUser(), comment, "Comment" );
    }

    class CommentForm
        extends Form<Comment>
    {
        private Comment create = new Comment();
        public CommentForm( String id )
        {
            super( id );

            setModel( new CompoundPropertyModel<Comment>( create ) );
            add( new TextArea( "comment" ) );

            layoutChild( this );

            add( new Button( "submit", new Model<String>()
            {
                public String getObject()
                {
                    return submitLabel;
                }
            } ) );
        }

        public void onSubmit()
        {
            milestone = (Milestone) ( (HibernateStorage) getStorage() ).getHibernateSession().merge( milestone );
            submitChild();

            Date now = new Date();
            if ( create.getComment() != null )
            {
                create.setUser( CreateComment.this.getSession().getUser() );
                create.setCreated( now );
                ( (HibernateStorage) getStorage() ).save( create );

                milestone.getComments().add( create );
            }

            // this line is needed by things that extend our form...
            milestone.setUpdated( now );
            getHeadsUpApplication().addEvent( getUpdateEvent( create ) );

            setResponsePage( getPageClass( "milestones/view" ), getPageParameters() );
        }
    }
}