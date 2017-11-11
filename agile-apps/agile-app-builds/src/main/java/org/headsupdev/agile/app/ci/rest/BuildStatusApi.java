/*
 * HeadsUp Agile
 * Copyright 2013 Heads Up Development.
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

package org.headsupdev.agile.app.ci.rest;

import org.apache.wicket.PageParameters;
import org.apache.wicket.model.Model;
import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.api.Permission;
import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.api.rest.Publish;
import org.headsupdev.agile.app.ci.CIApplication;
import org.headsupdev.agile.app.ci.CIBuilder;
import org.headsupdev.agile.app.ci.event.UploadApplicationEvent;
import org.headsupdev.agile.app.ci.permission.BuildListPermission;
import org.headsupdev.agile.storage.StoredProject;
import org.headsupdev.agile.storage.ci.Build;
import org.headsupdev.agile.web.MountPoint;
import org.headsupdev.agile.web.rest.HeadsUpApi;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * A build status API that provides a summary of the latest build for projects.
 * <p/>
 * Created: 16/02/2013
 *
 * @author Andrew Williams
 * @since 2.0
 */
@MountPoint( "status" )
public class BuildStatusApi
        extends HeadsUpApi
{
    public BuildStatusApi(PageParameters params)
    {
        super( params );
    }

    @Override
    public Permission getRequiredPermission()
    {
        return new BuildListPermission();
    }

    @Override
    public void doGet( PageParameters pageParameters )
    {
        if ( getProject().equals( StoredProject.getDefault() ) )
        {
            ArrayList<BuildStatus> summaries = new ArrayList<BuildStatus>();

            for ( Project project : Manager.getStorageInstance().getRootProjects() )
            {
                summaries.add( new BuildStatus( CIApplication.getLatestBuildForProject( project ) ) );
            }

            setModel( new Model<ArrayList<BuildStatus>>( summaries ) );
        }
        else
        {
            BuildStatus summary = new BuildStatus( CIApplication.getLatestBuildForProject( getProject() ) );

            setModel( new Model<ArrayList<BuildStatus>>( new ArrayList<BuildStatus>( Arrays.asList( summary ) ) ) );
        }
    }

    protected class BuildStatus
            implements Serializable
    {
        @Publish
        public String projectId, icon, downloadLink, downloadVersion, downloadBuildConfigName;

        @Publish
        public Long downloadBuildNumber;

        @Publish
        public int status;

        @Publish
        public Build latestBuild, latestSuccessfulBuild;

        public BuildStatus( Build latest )
        {
            if ( latest == null )
            {
                return;
            }
            this.projectId = latest.getProject().getId();

            this.status = latest.getStatus();
            this.icon = getIconForBuild( latest );

            this.latestBuild = latest;
            if ( latest.getStatus() != Build.BUILD_SUCCEEDED )
            {
                this.latestSuccessfulBuild = CIApplication.getLatestPassedBuildForProject( latest.getProject() );
            }
            getDownloadDetailsForProject( latest.getProject() );
        }

        protected String getIconForBuild( Build build )
        {
            String iconName = "passed";
            if ( build.getStatus() == Build.BUILD_QUEUED || CIBuilder.isProjectQueued( build.getProject() ) )
            {
                iconName = "queued";
            }
            else if ( build.getStatus() == Build.BUILD_FAILED || build.getStatus() == Build.BUILD_CANCELLED )
            {
                iconName = "failed";
            }
            else if ( build.getStatus() == Build.BUILD_RUNNING )
            {
                iconName = "running";
            }

            String image = iconName + ".png";
            return getURLForPath( "/resources/" + CIApplication.class.getCanonicalName() + "/" + image );
        }

        protected void getDownloadDetailsForProject( Project project )
        {
            UploadApplicationEvent upload = CIApplication.getLatestUploadEvent( project );
            if ( upload == null )
            {
                return;
            }

            this.downloadLink = upload.getLink();
            this.downloadBuildNumber = upload.getBuildNumber();
            this.downloadBuildConfigName = upload.getBuildConfigName();
            this.downloadVersion = upload.getVersion();
        }
    }
}
