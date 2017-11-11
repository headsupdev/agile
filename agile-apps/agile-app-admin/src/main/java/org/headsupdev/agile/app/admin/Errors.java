/*
 * HeadsUp Agile
 * Copyright 2009-2012 Heads Up Development.
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

package org.headsupdev.agile.app.admin;

import org.headsupdev.agile.web.HeadsUpPage;
import org.headsupdev.agile.web.MountPoint;
import org.headsupdev.agile.web.components.history.HistoryPanel;
import org.headsupdev.agile.web.components.MarkedUpTextModel;
import org.headsupdev.agile.storage.StoredProject;
import org.headsupdev.agile.security.permission.AdminPermission;
import org.headsupdev.agile.api.Permission;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.model.Model;
import org.jdom.Element;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Simple page to examine / reset the error log
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@MountPoint( "errors" )
public class Errors
    extends HeadsUpPage
{
    private static final int ISSUES_PER_PAGE = 50;

    public void layout()
    {
        super.layout();
        add( CSSPackageResource.getHeaderContribution( HistoryPanel.class, "history.css" ) );

        int from = 0;
        String fromParam = getPageParameters().getString( "from" );
        if ( fromParam != null )
        {
            try
            {
                from = Integer.parseInt( fromParam );
            }
            catch ( NumberFormatException e )
            {
                // ignore
            }
        }

        File log = getErrorFile();
        add( new WebMarkupContainer( "noerrors" ).setVisible( log.length() == 0 ) );
        WebMarkupContainer someErrors = new WebMarkupContainer( "someerrors" );
        someErrors.add( new Link( "clear" )
        {
            public void onClick() {
                getErrorFile().delete();

                this.setResponsePage( Errors.class );
            }
        } );
        add( someErrors.setVisible( log.length() > 0 ) );

        List<Element> errorList = getErrorList( from );
        add( new ListView<Element>( "errors", errorList )
        {
            private Date displayed;

            protected void populateItem( ListItem<Element> listItem )
            {
                Element error = listItem.getModelObject();
                final String title = error.getChildText( "message" );
                final String version = error.getChildText( "version" );
                final Date time = new Date( Long.parseLong( error.getChildText( "time" ) ) );
                final String stack = error.getChildText( "stack" );
                final String summary = getSummary( stack );

                WebMarkupContainer date = new WebMarkupContainer( "history-date" );
                if ( displayed == null || !sameDay( displayed, time ) )
                {
                    Calendar cal = new GregorianCalendar();
                    cal.setTime( time );
                    date.add( new Label( "day", String.valueOf( cal.get( Calendar.DAY_OF_MONTH ) ) ) );
                    date.add( new Label( "month", String.valueOf( cal.get( Calendar.MONTH ) + 1 ) ) );
                    date.add( new Label( "year", String.valueOf( cal.get( Calendar.YEAR ) ) ) );

                    displayed = time;
                }
                else
                {
                    date.setVisible( false );
                }
                listItem.add( date );

                ResourceReference icon = new ResourceReference( Errors.class, "images/error.png" );
                listItem.add( new Image( "error-icon", icon ) );
                listItem.add( new Label( "error-time", new HistoryTimeModel( time ) ) );
                listItem.add( new Label( "error-version", version ) );
                listItem.add( new Label( "error-title", title ) );

                listItem.add( new Label( "error-summary", new MarkedUpTextModel( summary, StoredProject.getDefault() ) )
                    .setEscapeModelStrings( false ) );
            }

            protected boolean sameDay( Date date1, Date date2 )
            {
                Calendar calendar1 = new GregorianCalendar();
                calendar1.setTime( date1 );
                Calendar calendar2 = new GregorianCalendar();
                calendar2.setTime( date2 );

                return calendar1.get( Calendar.DAY_OF_MONTH ) == calendar2.get( Calendar.DAY_OF_MONTH )
                    && calendar1.get( Calendar.MONTH ) == calendar2.get( Calendar.MONTH )
                    && calendar1.get( Calendar.YEAR ) == calendar2.get( Calendar.YEAR );
            }

            protected String getSummary( String stack )
            {
                int pos = stack.indexOf( '\n' );
                pos = stack.indexOf( '\n', pos + 1 );
                pos = stack.indexOf( '\n', pos + 1 );
                pos = stack.indexOf( '\n', pos + 1 );
                pos = stack.indexOf( '\n', pos + 1 );

                if ( pos == -1 )
                {
                    return stack;
                }

                return stack.substring( 0, pos + 1 ) + "...";
            }
        } );

        if ( errorList.size() == ISSUES_PER_PAGE )
        {
            PageParameters params = getProjectPageParameters();
            params.add( "from", String.valueOf( from + ISSUES_PER_PAGE ) );

            someErrors.add( new BookmarkablePageLink( "previous", getClass(), params ) );
        }
        else
        {
            someErrors.add( new WebMarkupContainer( "previous" ).setVisible( false ) );
        }
    }

    @Override
    public String getTitle()
    {
        return "Error Log";
    }

    protected File getErrorFile()
    {
        return new File( getStorage().getGlobalConfiguration().getDataDir(), "error.xml" );
    }

    public Permission getRequiredPermission()
    {
        return new AdminPermission();
    }

    protected List<Element> getErrorList( int from )
    {
        List<Element> errors = new LinkedList<Element>();
        try
        {
            Document doc = new SAXBuilder().build( getErrorFile() );

            List<Element> elements = doc.getRootElement().getChildren( "error" );
            ListIterator<Element> iter = elements.listIterator();

            while ( iter.hasNext() )
            {
                iter.next();
            }

            int i = 0;
            while ( iter.hasPrevious() && i < from )
            {
                iter.previous();
                i++;
            }
            i = 0;
            while ( iter.hasPrevious() && i < ISSUES_PER_PAGE )
            {
                errors.add( iter.previous() );
                i++;
            }

        }
        catch ( IOException e )
        {
            // should we error here? probably no point - cannot access the file...
        }
        catch ( JDOMException e )
        {
            e.printStackTrace();
        }

        return errors;
    }

    private static DateFormat format = new SimpleDateFormat( "hh:mm aa" );

    class HistoryTimeModel extends Model<String>
    {
        private Date date;

        public HistoryTimeModel( Date date )
        {
            this.date = date;
        }

        public String getObject()
        {
            return formatDate( date );
        }

        public synchronized String formatDate( Date date )
        {
            format.setTimeZone( getSession().getTimeZone() );
            return format.format( date );
        }
    }
}