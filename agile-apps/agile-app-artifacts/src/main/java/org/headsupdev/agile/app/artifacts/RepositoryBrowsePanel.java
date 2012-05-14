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

package org.headsupdev.agile.app.artifacts;

import org.headsupdev.agile.api.AntProject;
import org.headsupdev.agile.api.util.FileUtil;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.link.DownloadLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.panel.Panel;
import org.headsupdev.support.java.StringUtil;
import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.api.MavenTwoProject;
import org.headsupdev.agile.api.mime.Mime;
import org.headsupdev.agile.web.components.FormattedDurationModel;
import org.headsupdev.agile.web.components.FormattedSizeModel;
import org.headsupdev.agile.web.components.StripedListView;

import java.io.File;
import java.util.*;

/**
 * TODO enter description!
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class RepositoryBrowsePanel
    extends Panel
{
    private Project project;
    private File resolvedPath;

    public RepositoryBrowsePanel( String id, File path, String pathStr, final Project project, final Class pageClass )
    {
        super( id );
        this.project = project;

        add( CSSPackageResource.getHeaderContribution( getClass(), "repository.css" ) );

        if ( pathStr == null )
        {
            pathStr = "";
            if ( project != null )
            {
                if ( pageClass.equals( ProjectsRepository.class ) )
                {
                    pathStr = project.getId();
                }
                else
                {
                    if ( project instanceof MavenTwoProject )
                    {
                        MavenTwoProject m2Project = (MavenTwoProject) project;
                        pathStr = m2Project.getGroupId().replace( '.', File.separatorChar ) + File.separatorChar + m2Project.getArtifactId();
                    }
                    else if ( project instanceof AntProject )
                    {
                        AntProject antProject = (AntProject) project;
                        String org = antProject.getOrganisation();
                        String module = antProject.getModule();

                        if ( org != null && org.trim().length() > 0 && module != null && module.trim().length() > 0 )
                        {
                            pathStr = org.replace( '.', File.separatorChar ) + File.separatorChar + module;
                        }
                    }
                }
            }

        }
        if ( pathStr.length() > 1 && pathStr.charAt( pathStr.length() - 1 ) != File.separatorChar )
        {
            pathStr += File.separatorChar;
        }

        if ( !StringUtil.isEmpty( pathStr ) )
        {
            resolvedPath = new File( path, pathStr );
        }
        else
        {
            resolvedPath = path;
        }
        boolean missing = !resolvedPath.exists();

        if ( missing && pathStr.contains( FileUtil.LATEST_ITEM_NAME ) )
        {
            resolvedPath = FileUtil.replaceLatest( resolvedPath );
            missing = !resolvedPath.exists();
        }

        File[] fileArr = resolvedPath.listFiles();
        List<File> files;
        if ( fileArr == null )
        {
            files = new LinkedList<File>();
        }
        else
        {
            files = Arrays.asList( fileArr );
        }
        Collections.sort( files );

        WebMarkupContainer parent = new WebMarkupContainer( "parent" );
        if ( StringUtil.isEmpty( pathStr ) || ( pageClass.equals( ProjectsRepository.class ) &&
                ( pathStr.equals( project.getId() + File.separatorChar ) || pathStr.equals( File.separatorChar + project.getId() + File.separatorChar )) ) )
        {
            Mime parentFolder = Mime.get( "parent-folder" );
            Link iconLink = new BookmarkablePageLink( "parent-icon-link", ListRepositories.class, getProjectPageParameters() );
            iconLink.add( new Image( "parent-icon", new ResourceReference( Mime.class, parentFolder.getIconName() ) ) );
            parent.add( iconLink );
            Link link = new BookmarkablePageLink( "parent-link", ListRepositories.class, getProjectPageParameters() );
            parent.add( link );
        }
        else
        {
            Mime parentFolder = Mime.get( "parent-folder" );
            PageParameters params = new PageParameters();
            String parentPath = new File( pathStr ).getParent();
            if ( parentPath == null )
            {
                params.add( "project", "all" );
            }
            else
            {
                params.add( "project", project.getId() );
                params.add( "path", parentPath.replace( File.separatorChar, ':' ) + ":" );
            }
            Link iconLink = new BookmarkablePageLink( "parent-icon-link", pageClass, params );
            iconLink.add( new Image( "parent-icon", new ResourceReference( Mime.class, parentFolder.getIconName() ) ) );
            parent.add( iconLink );
            Link link = new BookmarkablePageLink( "parent-link", pageClass, params );
            parent.add( link );
        }
        add( parent );

        // assume we should show latest until we find a file that is not suitable
        boolean showLatest = files.size() > 0;
        File _latestFile = null;
        for ( File file : files )
        {
            showLatest = showLatest && FileUtil.isFileNumeric( file );
            if ( showLatest )
            {
                // was a number, so let's create a latest item;
                if ( _latestFile == null )
                {
                    _latestFile = file;
                }
                else
                {
                    if ( _latestFile.lastModified() < file.lastModified() )
                    {
                        _latestFile = file;
                    }
                }
            }
        }

        if ( showLatest )
        {
            List<File> newFiles = new LinkedList<File>();
            newFiles.addAll( files );
            newFiles.add( 0, new File( FileUtil.LATEST_ITEM_NAME ) );

            files = newFiles;
        }
        final File latestFile = _latestFile;
        final boolean latestIsFile = latestFile != null && latestFile.isFile();

        final String repoPath = pathStr;
        add( new StripedListView<File>( "browse-items", files )
        {
            protected void populateItem( ListItem<File> listItem )
            {
                super.populateItem( listItem );

                File file = listItem.getModelObject();

                Link link, iconLink;
                Mime mime;
                String label = file.getName();
                if ( file.getName().equals( FileUtil.LATEST_ITEM_NAME ) )
                {
                    label = FileUtil.LATEST_ITEM_NAME + " (" + latestFile.getName() + ")";
                    if ( latestIsFile )
                    {
                        mime = Mime.get( Mime.MIME_FILE_LINK );
                        listItem.add( new Label( "size", new FormattedSizeModel( file.length() ) ) );
                    }
                    else
                    {
                        mime = Mime.get( Mime.MIME_FOLDER_LINK );
                        listItem.add( new Label( "size", "" ) );
                    }

                    file = latestFile;
                    PageParameters params = getProjectPageParameters();
                    params.add( "path", repoPath.replace( File.separatorChar, ':' ) + FileUtil.LATEST_ITEM_NAME + ":" );
                    iconLink = new BookmarkablePageLink( "browse-icon-link", pageClass, params );
                    link = new BookmarkablePageLink( "browse-link", pageClass, params );
                }
                else if ( file.isDirectory() )
                {
                    File metadata = new File( file, "maven-metadata.xml" );
                    File releaseMetadata = new File( file, file.getParentFile().getName() + "-" + file.getName() + ".pom" );
                    File parentMeta = new File( file.getParentFile(), "maven-metadata.xml" );
                    if ( metadata.exists() || releaseMetadata.exists() )
                    {
                        mime = Mime.get( "package" );
                        if ( parentMeta.exists() )
                        {
                            String artifact = repoPath.substring( 0, repoPath.length() - 1 ).replace( File.separatorChar, '.' );
                            int lastDot = artifact.lastIndexOf( '.' );
                            artifact = artifact.substring( 0, lastDot ) + ":" + artifact.substring( lastDot + 1 );
                            label = artifact + ":" + label;
                        }
                        else
                        {
                            label = repoPath.substring( 0, repoPath.length() - 1 ).replace( File.separatorChar, '.' ) +
                                    ":" + label;
                        }
                    }
                    else
                    {
                        mime = Mime.get( "folder" );
                    }
                    PageParameters params = getProjectPageParameters();
                    params.add( "path", repoPath.replace( File.separatorChar, ':' ) + file.getName() + ":" );
                    iconLink = new BookmarkablePageLink( "browse-icon-link", pageClass, params );
                    link = new BookmarkablePageLink( "browse-link", pageClass, params );

                    listItem.add( new Label( "size", "" ) );
                }
                else
                {
                    mime = Mime.get( file.getName() );

                    iconLink = new DownloadLink( "browse-icon-link", file );
                    link = new DownloadLink( "browse-link", file );

                    listItem.add( new Label( "size", new FormattedSizeModel( file.length() ) ) );
                }
                link.add( new Label( "browse-label", label ) );
                listItem.add( link );
                iconLink.add( new Image( "browse-icon", new ResourceReference( Mime.class, mime.getIconName() ) ) );
                listItem.add( iconLink );

                Date modified = new Date( file.lastModified() );
                listItem.add( new Label( "modified",  new FormattedDurationModel( modified, new Date() )
                {
                    public String getObject()
                    {
                        return super.getObject() + " ago";
                    }
                } ) );
            }
        }.setVisible( !missing ) );
        add( new WebMarkupContainer( "missing" ).setVisible( missing ) );
    }

    public File getResolvedPath()
    {
        return resolvedPath;
    }

    private PageParameters getProjectPageParameters()
    {
        PageParameters ret = new PageParameters();
        ret.put( "project", project.getId() );

        return ret;
    }
}
