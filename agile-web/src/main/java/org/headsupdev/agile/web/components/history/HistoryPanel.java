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

package org.headsupdev.agile.web.components.history;

import org.headsupdev.agile.api.rest.Api;
import org.headsupdev.agile.web.HeadsUpSession;
import org.headsupdev.agile.web.components.FormattedDateModel;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.PackageResource;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.ResourceReference;
import org.headsupdev.agile.api.Event;
import org.headsupdev.agile.web.components.MarkedUpTextModel;
import org.headsupdev.agile.web.HeadsUpPage;

import java.util.*;

/**
 * A shared history component for rendering a chronological list of events.
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class HistoryPanel
    extends Panel
{
    public static final int SUMMARY_LENGTH = 500;
    private boolean showProjects;

    public HistoryPanel( String id, final List<? extends Event> events, boolean showProjects )
    {
        super( id );
        this.showProjects = showProjects;
        add( CSSPackageResource.getHeaderContribution( HistoryPanel.class, "history.css" ) );

        IModel<List<? extends Event>> listModel = new AbstractReadOnlyModel<List<? extends Event>>() {
            public List<? extends Event> getObject()
            {
                return events;
            }
        };

        add( new HistoryPanel.HistoryListView( "history", listModel ) );
    }

    public HistoryPanel( String id, final IModel<List<? extends Event>> events, boolean showProjects )
    {
        super( id );
        this.showProjects = showProjects;
        add( CSSPackageResource.getHeaderContribution( HistoryPanel.class, "history.css" ) );

        add( new HistoryPanel.HistoryListView( "history", events ) );
    }

    class HistoryListView
        extends ListView<Event>
    {
        private Date displayed;

        public HistoryListView( String id, IModel<List<? extends Event>> model )
        {
            super( id, model );
        }

        protected void populateItem( ListItem<Event> listItem )
        {
            final Event event = listItem.getModelObject();

            WebMarkupContainer date = new WebMarkupContainer( "history-date" );
            if ( displayed == null || !sameDay( displayed, event.getTime() ) )
            {
                Calendar cal = new GregorianCalendar();
                cal.setTime( event.getTime() );
                date.add( new Label( "day", String.valueOf( cal.get( Calendar.DAY_OF_MONTH ) ) ) );
                date.add( new Label( "month", String.valueOf( cal.get( Calendar.MONTH ) + 1 ) ) );
                date.add( new Label( "year", String.valueOf( cal.get( Calendar.YEAR ) ) ) );

                displayed = event.getTime();
            }
            else
            {
                date.setVisible( false );
            }
            listItem.add( date );

            String image = "images/events/" + Api.getClassName( event ) + ".png";
            if ( !PackageResource.exists( HeadsUpPage.class, image, null, null ) ) {
                image = "images/events/StoredEvent.png";
            }
            ResourceReference icon = new ResourceReference( HeadsUpPage.class, image );

            listItem.add( new Image( "history-icon", icon ) );
            listItem.add( new Label( "history-time", new FormattedDateModel( event.getTime(),
                    ( (HeadsUpSession) getSession() ).getTimeZone(), "hh:mm aa" ) ) );

            if ( event.getProject() != null )
            {
                ExternalLink projectLink = new ExternalLink( "project-link", "/" + event.getProject().getId() + "/activity/" );
                projectLink.add( new Label( "history-project", event.getProject().getAlias() ) );
                listItem.add( projectLink.setVisible( showProjects ) );
            }
            else
            {
                listItem.add( new WebMarkupContainer( "project-link" ).setVisible( false ) );
            }

            ExternalLink link = new ExternalLink( "history-link", "/activity/event/id/" + event.getId() );
            link.add( new Label( "history-title", event.getTitle() ) );
            listItem.add( link );

            String summary = event.getSummary();
            boolean tooLong = summary != null && summary.length() > SUMMARY_LENGTH;
            if ( tooLong )
            {
                // TODO make a better job of trimming the content
                summary = summary.substring( 0, SUMMARY_LENGTH ) + "...";
            }
            IModel content;
            if ( event.isSummaryHTML() )
            {
                content = new Model<String>()
                {
                    @Override
                    public String getObject()
                    {
                        return event.getSummary();
                    }
                };
            }
            else
            {
                content = new MarkedUpTextModel( summary, event.getProject() );
            }
            listItem.add( new Label( "history-summary", content )
                .setEscapeModelStrings( false ).setVisible( summary != null && summary.length() > 0 ) );

            WebMarkupContainer moreLink = new ExternalLink( "history-more-link", "/activity/event/id/" + event.getId() );
            listItem.add( moreLink.setVisible( tooLong ) );
        }

        protected boolean sameDay( Date date1, Date date2 )
        {
            Calendar calendar1 = new GregorianCalendar();
            calendar1.setTimeZone( ( (HeadsUpSession) getSession() ).getTimeZone() );
            calendar1.setTime( date1 );
            Calendar calendar2 = new GregorianCalendar();
            calendar2.setTimeZone( ( (HeadsUpSession) getSession() ).getTimeZone() );
            calendar2.setTime( date2 );

            return calendar1.get( Calendar.DAY_OF_MONTH ) == calendar2.get( Calendar.DAY_OF_MONTH )
                && calendar1.get( Calendar.MONTH ) == calendar2.get( Calendar.MONTH )
                && calendar1.get( Calendar.YEAR ) == calendar2.get( Calendar.YEAR );
        }
    }
}
