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

package org.headsupdev.agile.web.components;

import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.storage.Comment;
import org.headsupdev.agile.storage.resource.DurationWorked;
import org.headsupdev.agile.web.HeadsUpPage;
import org.headsupdev.agile.web.HeadsUpSession;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * TODO: Document me
 * <p/>
 * Created: 05/02/2012
 *
 * @author Andrew Williams
 * @since 1.0
 */
public class CommentPanel extends Panel
{
    private Project project;

    public CommentPanel( String id, Comment comment, Project project )
    {
        super( id, new Model( comment ) );
        this.project = project;
        
        layout();
    }

    public CommentPanel( String id, DurationWorked worked, Project project )
    {
        super( id, new Model( worked ) );
        this.project = project;
        
        layout();
    }

    public CommentPanel( String id, IModel model, Project project )
    {
        super( id, model );
        this.project = project;
        
        layout();
    }
    
    private void layout()
    {
        Object o = getDefaultModel().getObject();

        WebMarkupContainer commentTitle = new WebMarkupContainer( "comment-title" );
        WebMarkupContainer workedTitle = new WebMarkupContainer( "worked-title" );
        if ( o instanceof Comment )
        {
            add( new Image( "icon", new ResourceReference( HeadsUpPage.class, "images/comment.png" ) ) );

            Comment comment = (Comment) o;
            commentTitle.add( new Label( "username", comment.getUser().getFullnameOrUsername() ) );
            commentTitle.add( new Label( "created", new FormattedDateModel( comment.getCreated(),
                    ( (HeadsUpSession) getSession() ).getTimeZone() ) ) );

            add( new Label( "comment", new MarkedUpTextModel( comment.getComment(), project ) )
                    .setEscapeModelStrings( false ) );

            workedTitle.setVisible( false );
        }
        else if ( o instanceof DurationWorked )
        {
            add( new Image( "icon", new ResourceReference( HeadsUpPage.class, "images/worked.png" ) ) );

            DurationWorked worked = (DurationWorked) o;
            if ( worked.getWorked() == null || worked.getWorked().getHours() == 0 )
            {
                setVisible( false );
                return;
            }
            String time = "";
            if ( worked.getWorked() != null )
            {
                time = worked.getWorked().toString();
            }
            workedTitle.add( new Label( "worked", time ) );
            workedTitle.add( new Label( "username", worked.getUser().getFullnameOrUsername() ) );
            workedTitle.add( new Label( "created", new FormattedDateModel( worked.getDay(),
                    ( (HeadsUpSession) getSession() ).getTimeZone() ) ) );

            commentTitle.setVisible( false );

            Comment comment = worked.getComment();
            if ( comment != null )
            {
                Label commentLabel = new Label( "comment", new MarkedUpTextModel( comment.getComment(), project ) );
                commentLabel.setEscapeModelStrings( false );
                add( commentLabel );
            }
            else
            {
                add( new WebMarkupContainer( "comment" ).setVisible( false ) );
            }
        }
        else
        {
            commentTitle.setVisible( false );
            workedTitle.setVisible( false );
            add( new WebMarkupContainer( "comment" ).setVisible( false ) );
        }
        add( commentTitle );
        add( workedTitle );
    }
}
