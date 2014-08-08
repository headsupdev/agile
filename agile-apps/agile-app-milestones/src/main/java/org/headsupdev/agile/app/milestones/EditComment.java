package org.headsupdev.agile.app.milestones;

/**
 * HeadsUp Agile
 * Copyright 2014 Heads Up Development Ltd.
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

import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.string.StringValueConversionException;
import org.headsupdev.agile.api.Event;
import org.headsupdev.agile.api.Permission;
import org.headsupdev.agile.app.milestones.event.UpdateMilestoneEvent;
import org.headsupdev.agile.app.milestones.permission.MilestoneEditPermission;
import org.headsupdev.agile.storage.Comment;
import org.headsupdev.agile.storage.HibernateStorage;
import org.headsupdev.agile.storage.dao.MilestonesDAO;
import org.headsupdev.agile.storage.issues.Milestone;
import org.headsupdev.agile.web.BookmarkableMenuLink;
import org.headsupdev.agile.web.HeadsUpPage;
import org.headsupdev.agile.web.MountPoint;
import org.headsupdev.agile.web.components.OnePressButton;

import java.util.Date;

/**
 * Created by Gordon Edwards on 04/08/2014.
 */


@MountPoint("editComment")
public class EditComment
        extends HeadsUpPage

{
    private String submitLabel = "Edit Comment";

    private Milestone milestone;
    protected long itemId;
    protected Comment create = new Comment();

    public Permission getRequiredPermission()
    {
        return new MilestoneEditPermission();
    }

    @Override
    public void layout()
    {
        super.layout();
        add( CSSPackageResource.getHeaderContribution( getClass(), "milestone.css" ) );

        String milestoneId;
        try
        {
            milestoneId = getPageParameters().getString( "id" );
            itemId = getPageParameters().getInt( getIdName() );
        }
        catch ( NumberFormatException e )
        {
            notFoundError();
            return;
        }

        catch ( StringValueConversionException e )
        {
            notFoundError();
            return;
        }

        MilestonesDAO dao = new MilestonesDAO();
        Milestone milestone = dao.find( milestoneId, getProject() );

        if ( milestone == null )
        {
            notFoundError();
            return;
        }

        this.milestone = milestone;
        add( new CommentForm( "comment" ) );
        addLink( new BookmarkableMenuLink( getPageClass( "milestones/view" ), getPageParameters(), "view" ) );

    }

    @Override
    public String getTitle()
    {
        return "Edit Comment";
    }

    public Milestone getMilestone()
    {
        return milestone;
    }


    protected void layoutChild( Form form )
    {
    }

    protected void submitChild( Comment comment )
    {
    }

    protected boolean willChildConsumeComment()
    {
        return false;
    }


    public void setSubmitLabel( String submitLabel )
    {
        this.submitLabel = submitLabel;
    }

    protected Event getUpdateEvent( Comment comment )
    {
        return new UpdateMilestoneEvent( milestone, milestone.getProject(), getSession().getUser(), comment, "commented on" );
    }

    class CommentForm
            extends Form<Comment>
    {
        private TextArea input;

        public CommentForm( String id )
        {
            super( id );
            for ( Comment comment : milestone.getComments() )
            {
                if ( comment.getId() == itemId )
                {
                    create = comment;
                    break;
                }
            }

            setModel( new CompoundPropertyModel<Comment>( create ) );
            input = new TextArea( "comment" );
            add( input );
            layoutChild( this );

            add( new OnePressButton( "submit", new Model<String>()
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
            create = (Comment) ( (HibernateStorage) getStorage() ).getHibernateSession().merge( create );

            Date now = new Date();

            if ( create.getComment() != null )
            {
                create.setUser( EditComment.this.getSession().getUser() );
                create.setComment( input.getInput() );
            }

            submitChild( create );

            // this line is needed by things that extend our form...
            milestone.setUpdated( now );
            getHeadsUpApplication().addEvent( getUpdateEvent( create ) );

            setResponsePage( getPageClass( "milestones/view" ), getPageParameters() );
        }
    }

    protected String getIdName()
    {
        return "commentId";
    }
}

