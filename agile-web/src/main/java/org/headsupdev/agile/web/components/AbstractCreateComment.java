/*
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

package org.headsupdev.agile.web.components;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.headsupdev.agile.api.Event;
import org.headsupdev.agile.api.MenuLink;
import org.headsupdev.agile.api.Page;
import org.headsupdev.agile.api.Permission;
import org.headsupdev.agile.storage.Comment;
import org.headsupdev.agile.storage.CommentableEntity;
import org.headsupdev.agile.storage.HibernateStorage;
import org.headsupdev.agile.web.HeadsUpPage;
import org.headsupdev.agile.web.SubmitChildException;

import java.util.Date;

/**
 * A generic comment panel
 *
 * Created: Created: 29/08/2014
 *
 * @author Gordon Edwards
 * @since 2.1
 */
public abstract class AbstractCreateComment<T extends CommentableEntity>
        extends HeadsUpPage
{
    protected T commentable;
    protected String submitLabel = "Create Comment";
    private Comment create = new Comment();

    protected abstract Subheader<T> getSubheader();

    public abstract Permission getRequiredPermission();

    protected abstract T getObject();

    protected abstract Event getUpdateEvent( Comment comment );

    protected abstract MenuLink getViewLink();

    protected abstract void layoutChild( Form form );

    protected abstract void submitChild( Comment comment )
            throws SubmitChildException;

    protected abstract PageParameters getSubmitPageParameters();

    protected abstract Class<? extends Page> getSubmitPageClass();

    protected boolean willChildConsumeComment()
    {
        return false;
    }

    public void setSubmitLabel( String submitLabel )
    {
        this.submitLabel = submitLabel;
    }

    @Override
    public void layout()
    {
        super.layout();

        if ( getObject() == null )
        {
            notFoundError();
            return;
        }
        addLink( getViewLink() );
        commentable = getObject();
        add( new CommentForm( "comment" ) );
    }


    private class CommentForm
            extends Form<Comment>
    {
        public CommentForm( String id )
        {
            super( id );
            setModel( new CompoundPropertyModel<Comment>( create ) );

            add( new TextArea( "comment" ) );
            layoutChild( this );

            add( getSubheader() );
            add( new OnePressSubmitButton( "submit", new Model<String>()
            {
                public String getObject()
                {
                    return submitLabel;
                }
            } ) );
        }

        @Override
        protected void onSubmit()
        {
            commentable = (T) ( (HibernateStorage) getStorage() ).merge( commentable );

            Date now = new Date();
            if ( create.getComment() != null )
            {
                create.setUser( AbstractCreateComment.this.getSession().getUser() );
                create.setCreated( now );
                ( (HibernateStorage) getStorage() ).save( create );

                if ( !willChildConsumeComment() )
                {
                    commentable.addComment( create );
                }
            }

            try
            {
                submitChild( create );
            }
            catch ( SubmitChildException e )
            {
                error( e.getMessage() );
                return;
            }

            commentable.setUpdated( now );

            getHeadsUpApplication().addEvent( getUpdateEvent( create ) );
            setResponsePage( getSubmitPageClass(), getSubmitPageParameters() );
        }
    }

    @Override
    public String getPageTitle()
    {
        return getSubheader().toString() + PAGE_TITLE_SEPARATOR + getAppProductTitle();
    }

}