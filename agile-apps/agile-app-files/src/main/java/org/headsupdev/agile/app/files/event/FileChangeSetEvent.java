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

package org.headsupdev.agile.app.files.event;

import org.headsupdev.agile.api.User;
import org.headsupdev.agile.api.service.Change;
import org.headsupdev.agile.api.service.ChangeSet;
import org.headsupdev.agile.app.files.BrowseApplication;
import org.headsupdev.agile.app.files.BrowseChange;
import org.headsupdev.agile.app.files.ChangeSetPanel;
import org.headsupdev.agile.storage.TransactionalScmChangeSet;
import org.headsupdev.agile.web.BookmarkableMenuLink;
import org.headsupdev.agile.web.AbstractEvent;
import org.headsupdev.agile.web.RenderUtil;
import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.api.Manager;

import javax.persistence.Entity;
import javax.persistence.DiscriminatorValue;
import java.io.File;
import java.util.List;
import java.util.LinkedList;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.panel.Panel;

/**
 * Event added when a changeset is loaded from a project scm
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@Entity
@DiscriminatorValue( "filechangeset" )
public class FileChangeSetEvent
    extends AbstractEvent
{
    FileChangeSetEvent()
    {
    }

    public FileChangeSetEvent( ChangeSet changeSet, Project project )
    {
        super( "Changeset " + ((changeSet instanceof TransactionalScmChangeSet)?changeSet.getId():"") + " (" +
                getFileCount( changeSet, project ) + " " + getFileCountString( changeSet, project) + ") committed by " +
                getDisplayNameForAuthor( changeSet.getAuthor() ), changeSet.getComment(), changeSet.getDate() );

        setApplicationId( BrowseApplication.ID );
        setProject( project );

        setUsername( getUsernameForAuthor( changeSet.getAuthor() ) );
        setObjectId( changeSet.getId() );
    }

    private String renderChangeSet( final ChangeSet changeSet, final String stripPrefix )
    {
        return new RenderUtil()
        {
            public Panel getPanel()
            {
                return new ChangeSetPanel( RenderUtil.PANEL_ID, changeSet, stripPrefix );
            }
        }.getRenderedContent();
    }

    public String getBody() {
        Project project = getProject();
        String stripPrefix = "";
        File searchDir = Manager.getStorageInstance().getWorkingDirectory( getProject() );
        while ( project.getParent() != null )
        {
            project = project.getParent();
            stripPrefix = searchDir.getName() + File.separatorChar + stripPrefix;
            searchDir = searchDir.getParentFile();
        }

        ChangeSet changeSet = Manager.getInstance().getScmService().getChangeSet( project, getObjectId() );
        if ( changeSet == null )
        {
            return "<p>Changeset " + getObjectId() + " does not exist for project " + getProject().getAlias() + "</p>";
        }

        if ( changeSet.getPrevious() != null )
        {
            PageParameters params = new PageParameters();
            params.add( "project", project.getId() );
            params.add( "id", changeSet.getPrevious().getId() );

            addLink( new BookmarkableMenuLink( BrowseChange.class, params, "\u25c0 previous changeset" ) );
        }
        if ( changeSet.getNext() != null )
        {
            PageParameters params = new PageParameters();
            params.add( "project", project.getId() );
            params.add( "id", changeSet.getNext().getId() );

            addLink( new BookmarkableMenuLink( BrowseChange.class, params, "\u25ba next changeset" ) );
        }

        return renderChangeSet( changeSet, stripPrefix );
    }

    public List<CssReference> getBodyCssReferences()
    {
        List<CssReference> ret = new LinkedList<CssReference>();
        ret.add( referenceForCss( BrowseApplication.class, "change.css" ) );

        return ret;
    }

    private static int getFileCount( ChangeSet changeSet, Project project )
    {
        String prefix = "";
        Project root = project;
        File searchDir = Manager.getStorageInstance().getWorkingDirectory( project );
        while ( root.getParent() != null )
        {
            prefix = searchDir.getName() + File.separatorChar + prefix;
            root = root.getParent();
            searchDir = searchDir.getParentFile();
        }

        int matches = 0;
        for ( Change change : changeSet.getChanges() )
        {
            String fileName = change.getName();
            if ( fileName.startsWith( prefix ) )
            {
                matches++;
            }
        }

        return matches;
    }

    private static String getFileCountString( ChangeSet changeSet, Project project )
    {
        if ( getFileCount( changeSet, project ) == 1 ) {
            return "file";
        }

        return "files";
    }

    private static String getUsernameForAuthor( String author )
    {
        User match = Manager.getSecurityInstance().getUserByUsernameEmailOrFullname( author );

        if ( match == null )
        {
            return author;
        }

        return match.getUsername();
    }

    private static String getDisplayNameForAuthor( String author )
    {
        User match = Manager.getSecurityInstance().getUserByUsernameEmailOrFullname( author );

        if ( match == null )
        {
            return author;
        }

        return match.getFullnameOrUsername();
    }
}