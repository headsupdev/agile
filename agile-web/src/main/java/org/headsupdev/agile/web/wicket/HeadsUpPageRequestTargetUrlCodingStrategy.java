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

package org.headsupdev.agile.web.wicket;

import org.apache.wicket.request.target.coding.AbstractRequestTargetUrlCodingStrategy;
import org.apache.wicket.request.target.component.IBookmarkablePageRequestTarget;
import org.apache.wicket.request.target.component.BookmarkablePageRequestTarget;
import org.apache.wicket.request.target.component.BookmarkableListenerInterfaceRequestTarget;
import org.apache.wicket.request.RequestParameters;
import org.apache.wicket.util.string.AppendingStringBuffer;
import org.apache.wicket.PageParameters;
import org.apache.wicket.IRequestTarget;
import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.request.WebRequestCodingStrategy;

import java.lang.ref.WeakReference;

/**
 * A variation on the BookmarkablePageRequestTargetUrlCodingStrategy that insets and reads the project id from the
 * beginning of the url (requires a custom RequestCodingStrategy to let the URL through).
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class HeadsUpPageRequestTargetUrlCodingStrategy
    extends AbstractRequestTargetUrlCodingStrategy
{
    /** bookmarkable page class. */
    protected final WeakReference<Class<? extends Page>> bookmarkablePageClassRef;

    public HeadsUpPageRequestTargetUrlCodingStrategy( String mount, Class page )
    {
        super( mount );

        if ( page == null )
        {
            throw new IllegalArgumentException( "Argument bookmarkablePageClass must be not null" );
        }

        bookmarkablePageClassRef = new WeakReference<Class<? extends Page>>( page );
    }

    public IRequestTarget decode( RequestParameters requestParameters )
    {
        ProjectUrl matching = HeadsUpRequestCodingStrategy.decodePath( requestParameters.getPath() );
        requestParameters.setPath( matching.getUrl() );

        final String parametersFragment = requestParameters.getPath().substring(
            getMountPath().length() );

        final PageParameters parameters = new PageParameters( decodeParameters( parametersFragment,
            requestParameters.getParameters() ) );
        parameters.put( "project", matching.getProject().getId() );

        // do some extra work for checking whether this is a normal request to a
        // bookmarkable page, or a request to a stateless page (in which case a
        // wicket:interface parameter should be available

        // the page map name can be defined already by logic done in
        // WebRequestCodingStrategy.decode(),
        // but it could also be done by the decodeParameters() call
        // So we always remove the pagemap parameter just in case.
        String pageMapNameEncoded = (String) parameters.remove( WebRequestCodingStrategy.PAGEMAP );
        if ( requestParameters.getPageMapName() == null )
        {
            requestParameters.setPageMapName( pageMapNameEncoded );
        }

        // the interface can be defined already by logic done in
        // WebRequestCodingStrategy.decode(),
        // but it could also be done by the decodeParameters() call
        // So we always remove the interface parameter just in case.
        String interfaceParameter = (String) parameters.remove( WebRequestCodingStrategy.INTERFACE_PARAMETER_NAME );
        if ( requestParameters.getInterfaceName() == null )
        {
            WebRequestCodingStrategy.addInterfaceParameters( interfaceParameter, requestParameters );
        }

        // if an interface name was set prior to this method or in the
        // above block, process it
        if ( requestParameters.getInterfaceName() != null )
        {
            return new BookmarkableListenerInterfaceRequestTarget(
                requestParameters.getPageMapName(), bookmarkablePageClassRef.get(), parameters,
                requestParameters.getComponentPath(), requestParameters.getInterfaceName(),
                requestParameters.getVersionNumber() );
        }
        // otherwise process as a normal bookmark page request
        else
        {
            return new BookmarkablePageRequestTarget( requestParameters.getPageMapName(),
                bookmarkablePageClassRef.get(), parameters );
        }
    }

    public CharSequence encode( final IRequestTarget requestTarget )
    {
        if ( !( requestTarget instanceof IBookmarkablePageRequestTarget ) )
        {
            throw new IllegalArgumentException( "This encoder can only be used with " +
                "instances of " + IBookmarkablePageRequestTarget.class.getName() );
        }
        final IBookmarkablePageRequestTarget target = (IBookmarkablePageRequestTarget) requestTarget;

        PageParameters pageParameters = target.getPageParameters();
        if ( pageParameters == null )
        {
            pageParameters = new PageParameters();
        }
        PageParameters paramsNoProject = new PageParameters( pageParameters );
        Object project = paramsNoProject.remove( "project" );

        final AppendingStringBuffer url = new AppendingStringBuffer( 40 );
        HeadsUpRequestCodingStrategy.encodePath( url, project );

        if ( !getMountPath().startsWith( "/" ) )
        {
            url.append( "/" );
        }
        url.append( getMountPath() );
        String pagemap = target.getPageMapName(); // always null in this strategy... pageMapName != null ? pageMapName : target.getPageMapName();
        if ( pagemap != null )
        {
            pageParameters.put( WebRequestCodingStrategy.PAGEMAP,
                WebRequestCodingStrategy.encodePageMapName( pagemap ) );
            paramsNoProject.put( WebRequestCodingStrategy.PAGEMAP,
                WebRequestCodingStrategy.encodePageMapName( pagemap ) );
        }
        appendParameters( url, paramsNoProject );
        return url;
    }

    public boolean matches( IRequestTarget requestTarget )
    {
        if ( requestTarget instanceof IBookmarkablePageRequestTarget )
        {
            IBookmarkablePageRequestTarget target = (IBookmarkablePageRequestTarget) requestTarget;
            if ( ( bookmarkablePageClassRef.get() ).equals( target.getPageClass() ) )
            {
                return true;
            }
        }
        return false;
    }
}
