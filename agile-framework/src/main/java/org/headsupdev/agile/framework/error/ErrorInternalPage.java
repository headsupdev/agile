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

package org.headsupdev.agile.framework.error;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.Model;
import org.wicketstuff.animator.Animator;
import org.wicketstuff.animator.MarkupIdModel;

import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.Serializable;

import org.headsupdev.agile.web.MountPoint;
import org.headsupdev.agile.web.ErrorPage;
import org.headsupdev.agile.api.Manager;

/**
 * An error page that shows information about the error that just occurred and emails support the details.
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@MountPoint( "error" )
public class ErrorInternalPage
    extends ErrorPage
    implements Serializable
{
    private boolean userError = false;

    public void layout()
    {
        super.layout();

        if ( isUserError() )
        {
            userError = true;
            WebMarkupContainer container = new WebMarkupContainer( "userError" );
            container.add( new BookmarkablePageLink( "home", getApplication().getHomePage() ) );
            add( container );
            add( new Label( "message", "Sorry" ) );

            add( new WebMarkupContainer( "systemError" ).setVisible( false ) );
            return;
        }

        add( new WebMarkupContainer( "userError" ).setVisible( false ) );
        add( new Label( "message", "Oops!" ) );

        WebMarkupContainer container = new WebMarkupContainer( "systemError" );
        add( container );

        container.add( new Label( "cause", new Model<String>()
        {
            @SuppressWarnings({"ThrowableResultOfMethodCallIgnored"})
            public String getObject()
            {
                if ( getError() == null )
                {
                    return "unknown";
                }

                return getError().getMessage();
            }
        } ) );

        Label stack = new Label( "stack", new Model<String>()
        {
            public String getObject()
            {
                return getStack();
            }
        } );
        stack.setMarkupId( "stacktrace" );
        container.add( stack );

        WebMarkupContainer button = new WebMarkupContainer( "debug" );
        button.setMarkupId( "debug" );
        container.add( button );

        Animator animator = new Animator();
        animator.addCssStyleSubject( new MarkupIdModel( stack ), "stackhidden", "stackshown" );
        animator.attachTo( button, "onclick", Animator.Action.toggle() );
    }

    protected boolean isUserError()
    {
        String userParam = getPageParameters().getString( "userError" );
        return userParam != null && userParam.equals( "true" );
    }

    protected String getReason()
    {
        return getPageParameters().getString( "reason" );
    }

    @Override
    public String getTitle()
    {
        return "Error";
    }

    @SuppressWarnings({"ThrowableResultOfMethodCallIgnored"})
    protected void onBeforeRender()
    {
        super.onBeforeRender();

        if ( !userError )
        {
            Manager.getLogger( "ErrorInternalPage" ).error( "Unexpected exception rendering page", getError() );
        }
    }

    @SuppressWarnings({"ThrowableResultOfMethodCallIgnored"})
    protected String getStack()
    {
        if ( getError() == null )
        {
            return "No stack provided";
        }

        StringWriter writer = new StringWriter();
        getError().printStackTrace( new PrintWriter( writer ) );

        return writer.toString();
    }
}
