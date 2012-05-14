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

package org.headsupdev.agile.api.util;

import org.headsupdev.agile.api.Manager;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * A handy utility for managing email sending with a default headsupdev account
 * which can be overridden.
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class MailUtil
{
    public static void sendEmail( String to, String from, String subject, String body, String host,
                                  String username, String password, boolean secure )
    {
        Properties mailProps = new Properties();
        mailProps.setProperty( "mail.transport.protocol", "smtp" );
        mailProps.setProperty( "mail.smtp.host", host );
        mailProps.setProperty( "mail.smtp.submitter", username );

        // a little workaround for mac platform - also need to install a certificate that was removed, see:
        //   http://www.chrissearle.org/blog/technical/java_cant_send_mail_due_a_certificate_error
        //   http://cleversoft.wordpress.com/2011/02/05/how-to-add-server-certificate-to-java-keystore/

        if ( System.getProperty( "os.name" ).toLowerCase().contains( "mac" ) )
        {
            mailProps.setProperty("mail.smtp.starttls.enable", "true");
            mailProps.setProperty("mail.smtp.ssl.trust", "smtpserver");
        }

        if ( secure )
        {
            mailProps.setProperty( "mail.smtp.socketFactory.port", "465" );
            mailProps.setProperty( "mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory" );
        }
//        if ( tls )
//        {
//            mailProps.setProperty( "mail.smtp.socketFactory.port", "587" );
//            mailProps.setProperty( "mail.smtp.starttls.enable", "true" );
//        }

        Authenticator mailAuth = null;
        if ( username != null )
        {
            mailAuth = new MailAuthenticator( username, password );

            mailProps.setProperty( "mail.smtp.auth", "true" );
        }
        Session session = Session.getDefaultInstance( mailProps, mailAuth );
        Message message = new MimeMessage( session );

        try {

            message.setFrom( new InternetAddress( from ) );
            message.setSubject( subject );

            // some more headers to reduce spam probability
            message.setHeader( "To", to );

            message.setContent( body, "text/html" );
            Transport.send(message, new InternetAddress[]{new InternetAddress(to)});
        }
        catch ( MessagingException e )
        {
            Manager.getLogger( MailUtil.class.getName() ).error( "Error sending email", e );
        }

    }
}

class MailAuthenticator extends Authenticator
{
    private PasswordAuthentication authentication;

    public MailAuthenticator( String username, String password )
    {
        authentication = new PasswordAuthentication( username, password );
    }

    protected PasswordAuthentication getPasswordAuthentication()
    {
        return authentication;
    }
}
