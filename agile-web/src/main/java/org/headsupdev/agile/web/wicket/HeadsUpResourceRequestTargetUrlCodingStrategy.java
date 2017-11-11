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

package org.headsupdev.agile.web.wicket;

import org.apache.wicket.request.target.coding.SharedResourceRequestTargetUrlCodingStrategy;
import org.apache.wicket.request.target.resource.SharedResourceRequestTarget;
import org.apache.wicket.request.target.resource.ISharedResourceRequestTarget;
import org.apache.wicket.request.RequestParameters;
import org.apache.wicket.util.string.AppendingStringBuffer;
import org.apache.wicket.util.value.ValueMap;
import org.apache.wicket.IRequestTarget;
import org.apache.wicket.PageParameters;

import java.util.Map;

/**
 * A variation on the SharedResourceRequestTargetUrlCodingStrategy that insets and reads the project id from the
 * beginning of the url (requires a custom RequestCodingStrategy to let the URL through).
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class HeadsUpResourceRequestTargetUrlCodingStrategy
    extends SharedResourceRequestTargetUrlCodingStrategy
{
    private String key;
    public HeadsUpResourceRequestTargetUrlCodingStrategy( String mount, String key )
    {
        super( mount, key );
        this.key = key;
    }

    public IRequestTarget decode(RequestParameters requestParameters) {
        ProjectUrl matching = HeadsUpRequestCodingStrategy.decodePath( requestParameters.getPath() );
        requestParameters.setPath( matching.getUrl() );

        final String parametersFragment = requestParameters.getPath().substring(
            getMountPath().length());
        final ValueMap parameters = decodeParameters(parametersFragment, requestParameters.getParameters());
        parameters.put( "project", matching.getProject().getId() );

        requestParameters.setParameters(parameters);
        requestParameters.setResourceKey(key);
        return new SharedResourceRequestTarget(requestParameters);
    }

    public CharSequence encode(IRequestTarget requestTarget)
    {
        if (!(requestTarget instanceof ISharedResourceRequestTarget))
        {
            throw new IllegalArgumentException("This encoder can only be used with "
                + "instances of " + ISharedResourceRequestTarget.class.getName());
        }

        final ISharedResourceRequestTarget target = (ISharedResourceRequestTarget)requestTarget;
        RequestParameters requestParameters = target.getRequestParameters();

        Map pageParameters = requestParameters.getParameters();
        if (pageParameters == null)
        {
            pageParameters = new PageParameters();
        }
        PageParameters paramsNoProject = new PageParameters( pageParameters );
        Object project = paramsNoProject.remove( "project" );

        final AppendingStringBuffer url = new AppendingStringBuffer(40);
        HeadsUpRequestCodingStrategy.encodePath( url, project );

        if ( !getMountPath().startsWith( "/" ) )
        {
            url.append( "/" );
        }
        url.append(getMountPath());

        appendParameters( url, paramsNoProject );
        return url;
    }
}
