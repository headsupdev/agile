/*
 * HeadsUp Agile
 * Copyright 2009-2014 Heads Up Development.
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

package org.headsupdev.agile.core.notifiers;

import org.headsupdev.agile.api.*;
import org.headsupdev.agile.api.util.MailUtil;
import org.headsupdev.support.java.StringUtil;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * Mail notifier
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class EmailNotifier
    implements Notifier
{
    public static final String IGNORE_EVENTS_KEY = "ignore-events";
    public static final String IGNORE_EVENTS_JOIN = ",";

    public static enum FooterType
    {
        Notification, Subscription
    }

    private PropertyTree config = new PropertyTree();

    public String getId()
    {
        return "email";
    }

    public String getDescription()
    {
        return "An email notifier";
    }

    public void eventAdded( Event event )
    {
        sendEventEmail( event, getTo(), getFrom(), FooterType.Notification );
    }

    public void sendEventEmail( Event event, String to, FooterType footerType )
    {
        sendEventEmail( event, to, getFrom(), footerType );
    }

    public void sendEventEmail( Event event, String to, String from, FooterType footerType )
    {
        HeadsUpConfiguration config = Manager.getStorageInstance().getGlobalConfiguration();
        String title = "[HeadsUp Agile] " + event.getTitle();
        if ( event.getProject() != null ) {
            title += " (project " + event.getProject().getAlias() + ")";
        }

        StringBuilder body = new StringBuilder( "<html><head><title>" );
        body.append( title );
        body.append( "</title>" );
        body.append( "<base href=\"" );
        body.append( config.getBaseUrl() );
        body.append( "\" />" );
        body.append( "<link rel=\"stylesheet\" type=\"text/css\" href=\"/resources/org.headsupdev.agile.web.HeadsUpPage/common.css\" />" );
        body.append( event.getBodyHeader() );
        body.append( "</head><body><div id=\"page\" style=\"background:#fff;\n" +
                "            width:100%;\n" +
                "            margin:0;\n" +
                "            padding:0;\n" +
                "            font-family: \"Myriad Pro\", helvetica, sans-serif;\n" +
                "            text-align:center;\n" +
                "            color:#333;\n" +
                "            font-size:13px;\n" +
                "            color:#555b5b;\">" );
        body.append( "<div class=\"header\" style=\"position:absolute;\n" +
                "            left:0;\n" +
                "            top:0;\n" +
                "            width:100%;\n" +
                "            background-color: #505050;\n" +
                "            background-image: url(http://headsupdev.com/api/agile/images/2/header-bg.png);\n" +
                "            background-repeat: repeat-x;\n" +
                "            color: white;\n" +
                "            height: 34px;\n" +
                "            overflow: hidden;\">" );
        body.append( "<img style=\"padding:1px;float:left;margin-left:30px\" src=\"http://headsupdev.com/api/agile/images/2/header-logo.png\" />" );
        if ( event.getUsername() != null )
        {
            body.append( "<span class=\"user\" style=\"float: right; padding: 8px 30px; margin-right: 30px;\">" );
            body.append( Manager.getSecurityInstance().getUserByUsername( event.getUsername() ) );
            body.append( "</span>" );
        }
        body.append( "</div>" );
        body.append( "<h1 style=\"width:100%;\n" +
                "            margin:0;\n" +
                "            margin-top:34px;\n" +
                "            padding:10px 0;\n" +
                "            font-size:38px;\n" +
                "            background-color:#f0f1eb;\n" +
                "            height:45px;\n" +
                "            color:#555b5b;\"><div style=\"margin: 0 30px;\">" );
        body.append( event.getProject() );
        body.append( "</div></h1>" );

        body.append( "<div id=\"content\" style=\"padding: 0 30px;\">" );

        String time = new SimpleDateFormat( "hh:mm aa" ).format( event.getTime() );
        String link = config.getFullUrl( "/activity/event/id/" ) + event.getId();
        body.append( "<div class=\"history-item\" style=\"margin-top: 15pt;\n" +
                "    padding: 0;\n" +
                "    margin-bottom: 15px;\n" +
                "    border-bottom: 1px solid #c3c3c3;\">\n" +
                "        <span class=\"history-time\" style=\"font-weight: bold;\n" +
                "    color: #5b5b5b;\n" +
                "\n" +
                "    padding-left: 15pt;\">" );
        body.append( time );
        body.append( "</span>\n" );

// TODO investigate if we can (sensibly) look up the event icons...
//        String image = "images/events/" + event.getClass().getName().substring( event.getClass().getPackage().getName().length() + 1) + ".png";
//        if ( !PackageResource.exists( HeadsUpPage.class, image, null, null ) ) {
//            image = "images/events/StoredEvent.png";
//        }
//        body.append( "        <img alt=\"item icon\" class=\"history-icon\" src=\"" + image + "\">\n" );

        body.append( "        <a class=\"history-link\" style=\"text-decoration: none;\n" +
                "    display: block;\n" +
                "\n" +
                "    padding-top: 10pt;\n" +
                "    padding-left: 15pt;\" href=\"" );
        body.append( link );
        body.append( "\">\n" +
                "          <span class=\"history-title\">" );
        body.append( event.getTitle() );
        body.append( "</span>\n" +
                "        </a>\n" +
                "        \n" );

        if ( event.getSummary() != null )
        {
        body.append( "        <div class=\"history-summary\" style=\"padding: 10pt 15pt;\n" +
                "    margin: 15pt 0;\n" +
                "\n" +
                "    color:  #5b5b5b;\n" +
                "    background-color: #f7f8f3;\">\n" +
                "          <span>" );
        body.append( event.getSummary() );
        body.append( "</span>\n" +
                "          \n" +
                "        </div>\n" );
        }

        body.append( "        \n" +
                "      </div>" );

        String content = event.getBody();
        if ( content != null ) {
            body.append( content );
        }

        body.append( "</div>" );
        body.append( "<div class=\"footer\" style=\"width:100%;\n" +
                "            padding: 5px 30px;\n" +
                "            background-color: #cecfca;" +
                "            color: gray;\n" +
                "            font-size: 75%;\">" );
        body.append( getFooterText( footerType, to ) );
        body.append( "</div></div></body></html>" );

        sendNotification( to, from, title, body.toString(), config );
    }

    protected void sendNotification( String to, String from, String title, String body, HeadsUpConfiguration config )
    {
        boolean secure = "smtp.google.com".equalsIgnoreCase( config.getSmtpHost() )|| "smtp.gmail.com".equalsIgnoreCase( config.getSmtpHost() );
        MailUtil.sendEmail( to, from, title, body, config.getSmtpHost(),
                config.getSmtpUsername(), config.getSmtpPassword(), secure );
    }

    public PropertyTree getConfiguration()
    {
        return config;
    }

    public void setConfiguration( PropertyTree config )
    {
        this.config = config;
    }

    @Override
    public List<String> getConfigurationKeys()
    {
        return Arrays.asList( "to", "from", "<smtp>" );
    }

    @Override
    public Collection<String> getIgnoredEvents()
    {
        String eventIds = getConfiguration().getProperty( "ignore-events" );
        if ( StringUtil.isEmpty( eventIds ) )
        {
            return new HashSet<String>();
        }

        return Arrays.asList( eventIds.split( IGNORE_EVENTS_JOIN ) );
    }

    public void setIgnoredEvents( Collection<String> eventIds )
    {
        String ignoreList = StringUtil.join( eventIds, IGNORE_EVENTS_JOIN );
        getConfiguration().setProperty( IGNORE_EVENTS_KEY, ignoreList );
    }

    public void start()
    {
    }

    public void stop()
    {
    }

    public String getTo()
    {
        return config.getProperty( "to" );
    }

    public String getFrom()
    {
        return config.getProperty( "from" );
    }

    public String getFooterText( FooterType footerType, String to )
    {
        switch ( footerType )
        {
            case Subscription:
                return "This email was sent to " + to + " because they are subscribed to this project. " +
                        "To unsubscribe from these updates please change your subscription settings from your account page.";
            default:
                return "This email was sent to " + to + " because a notification has been set up to this address." +
                        "To stop further emails please speak to your administrator or unsubscribe from this mailing list.";
        }
    }
}
