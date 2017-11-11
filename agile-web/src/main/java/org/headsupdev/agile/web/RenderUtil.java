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

package org.headsupdev.agile.web;

import org.headsupdev.agile.storage.StorageThread;
import org.apache.wicket.util.tester.BaseWicketTester;
import org.apache.wicket.util.tester.ITestPageSource;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.*;
import org.apache.wicket.session.ISessionStore;
import org.apache.wicket.application.IClassResolver;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.HttpSessionStore;

import java.io.Serializable;

import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.web.wicket.HeadsUpPageRequestTargetUrlCodingStrategy;

/**
 * TODO document me
 *
 * @author Andrew Williams
 * @since 1.0
 */
public abstract class RenderUtil
    implements Serializable
{
    public static final String PANEL_ID = "testpanel";
    private static IClassResolver resolver;

    public static void setClassResolver( IClassResolver resolver )
    {
        RenderUtil.resolver = resolver;
    }

    public static Class getPageClass( String hint )
    {
        return ApplicationPageMapper.get().getPageClass( hint );
    }

    public String getRenderedContent()
    {
        final StringBuilder out = new StringBuilder();
        final Object wait = new Serializable(){};

        new StorageThread() {
            public void runWithSession() {
                try
                {
                    BaseWicketTester tester = new BaseWicketTester( new TestApplication() );
                    tester.getApplication().getMarkupSettings().setStripWicketTags( true );

                    tester.startPage( new ITestPageSource()
                    {
                        public Page getTestPage()
                        {
                            return new TestPage( getPanel() );
                        }
                    });

                    out.append( tester.getServletResponse().getDocument() );
                }
                catch ( Exception e )
                {
                    out.append( "<p>Error rendering content - " );
                    out.append( e.getMessage() );
                    out.append( "</p>" );
                }
                finally
                {
                    synchronized( wait )
                    {
                        wait.notifyAll();
                    }
                }
            }
        }.start();

        while ( out.length() == 0 )
        {
            try
            {
                synchronized( wait )
                {
                    wait.wait();
                }
            }
            catch ( InterruptedException e )
            {
                // got the content - if not just try again
            }
        }

        return out.toString().replace( "\"resources/", "\"/resources/" );
    }

    public abstract Panel getPanel();

    static class TestPage extends WebPage
    {
        public TestPage( Panel testPanel )
        {
            add( testPanel );
        }
    }

    static class TestApplication
        extends WebApplication
        implements Serializable        
    {
        protected void init() {
            super.init();
            getApplicationSettings().setClassResolver( resolver );

            for ( String url : ApplicationPageMapper.get().getMountPaths() )
            {
                if ( !url.equals( "" ) )
                {
                    mount( new HeadsUpPageRequestTargetUrlCodingStrategy( url, ApplicationPageMapper.get().getPageClass( url ) )
                    {
                        public CharSequence encode(IRequestTarget requestTarget)
                        {
                            return "/" + super.encode(requestTarget);
                        }
                    } );
                }
            }
        }

        public String getConfigurationType()
        {
            return DEPLOYMENT;
        }

        public Class getHomePage()
        {
            return ApplicationPageMapper.get().getPageClass( "" );
        }

        public RequestCycle newRequestCycle( Request request, Response response )
        {
            return new HibernateRequestCycle( this, (WebRequest) request, response )
            {
                public Page onRuntimeException( Page page, RuntimeException e )
                {
                    Manager.getLogger( getClass().getName() ).error( "Error rendering static page", e );

                    return super.onRuntimeException( page, e );
                }
            };
        }

        @Override
        protected ISessionStore newSessionStore()
        {
            return new HttpSessionStore( this );
        }

        @Override
        public Session newSession( Request request, Response response )
        {
            return new HeadsUpSession( request );
        }
    }
}
