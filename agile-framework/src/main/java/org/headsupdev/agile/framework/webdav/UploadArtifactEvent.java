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

package org.headsupdev.agile.framework.webdav;

import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.web.AbstractEvent;

import javax.persistence.Entity;
import javax.persistence.DiscriminatorValue;
import java.util.Date;

/**
 * Event added when an artifact is uploaded to our repository
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@Entity
@DiscriminatorValue( "uploadartifact" )
public class UploadArtifactEvent
    extends AbstractEvent
{
    UploadArtifactEvent()
    {
    }

    public UploadArtifactEvent( String groupId, String artifactId, String version, String repoName, String path,
                                Project project )
    {
        super( "Artifact " + groupId + ":" + artifactId + ":" + version + " deployed",
            "Version " + version + " of the artifact " + groupId + ":" + artifactId + " was deployed to the " +
             repoName + " repository", new Date() );

        setApplicationId( "artifacts" );
        setProject( project );
        setObjectId( repoName + ',' + path );
    }

    public String getBody() {
        // todo add some info, as can be seen on "/artifacts/<oid[..,]>path/<oid[,...]>"
        return super.getBody();    //To change body of overridden methods use File | Settings | File Templates.
    }
}
