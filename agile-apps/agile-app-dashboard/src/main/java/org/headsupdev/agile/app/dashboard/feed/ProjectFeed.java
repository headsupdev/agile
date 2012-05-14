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

package org.headsupdev.agile.app.dashboard.feed;

import org.headsupdev.agile.api.*;
import org.headsupdev.agile.security.permission.ProjectListPermission;
import org.headsupdev.agile.storage.StoredProject;
import org.headsupdev.agile.web.MountPoint;
import org.headsupdev.agile.web.feed.*;
import com.sun.syndication.feed.synd.*;

import java.util.*;

/**
 * A simple feed page that reports the loaded projects
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@MountPoint( "project-feed.xml" )
public class ProjectFeed
   extends AbstractFeed
{
    private Storage storage = Manager.getStorageInstance();

    public Permission getRequiredPermission() {
        return new ProjectListPermission();
    }

    public String getTitle()
    {
        String title = HeadsUpConfiguration.getProductName() + " Project Feed";
        if ( !getProject().equals( StoredProject.getDefault() ) )
        {
            title += " :: " + getProject().getAlias();
        }

        return title;
    }

    public String getDescription()
    {
        String description = HeadsUpConfiguration.getProductName() + " Project feed for ";
        if ( getProject().equals( StoredProject.getDefault() ) )
        {
            description += "root projects";
        }
        else
        {
            description += "project " + getProject().getAlias();
        }
        return description;
    }

    protected void populateFeed( SyndFeed feed )
    {
        List<Project> list;
        if ( getProject().equals( StoredProject.getDefault() ) )
        {
            list = Manager.getStorageInstance().getRootProjects();
        }
        else
        {
            list = new LinkedList<Project>( getProject().getChildProjects() );
        }
        Collections.sort( list );

        List<SyndEntry> entries = new ArrayList<SyndEntry>();
        for ( Project project : list )
        {
            SyndEntry entry = new SyndEntryImpl();

            entry.setTitle( project.getAlias() );
            entry.setLink( storage.getGlobalConfiguration().getFullUrl( "/" + project.getId() + "/show" ) );
            entry.setPublishedDate( project.getImported() );
            if ( project.getUpdated() != null )
            {
                entry.setUpdatedDate( project.getUpdated() );
            }
            SyndContent content = new SyndContentImpl();
            content.setType( "text/plain" );
            content.setValue( storage.getGlobalConfiguration().getFullUrl( "/" + project.getId() + "/show" ) );
            entry.setDescription( content );

            RomeModule module = new RomeModuleImpl();
            module.setId( project.getId() );
            String type = project.getClass().getName().substring( project.getClass().getPackage().getName().length() + 1 );
            if ( type.startsWith( "Stored" ) )
            {
                type = type.substring( 6 );
            }
            module.setType( type );
            entry.getModules().add( module );

            if ( project instanceof MavenTwoProject )
            {
                MavenTwoProject m2 = (MavenTwoProject) project;

                MavenModule m2Mod = new MavenModuleImpl();

                m2Mod.setGroupId( m2.getGroupId() );
                m2Mod.setArtifactId( m2.getArtifactId() );
                m2Mod.setVersion( m2.getVersion() );
                m2Mod.setPackaging( m2.getPackaging() );

                entry.getModules().add( m2Mod );
            }

            entries.add( entry );
        }

        feed.setEntries( entries );
    }
}
