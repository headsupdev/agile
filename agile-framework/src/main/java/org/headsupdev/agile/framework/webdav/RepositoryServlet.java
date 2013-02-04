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

package org.headsupdev.agile.framework.webdav;

import org.headsupdev.agile.api.util.HashUtil;
import org.headsupdev.agile.security.permission.RepositoryReadAppPermission;
import org.headsupdev.agile.security.permission.RepositoryWriteAppPermission;
import org.headsupdev.agile.web.HeadsUpSession;
import org.headsupdev.agile.web.WebLoginManager;
import org.headsupdev.support.java.Base64;
import org.headsupdev.support.java.FileUtil;
import org.headsupdev.support.java.StringUtil;
import org.headsupdev.agile.api.*;
import org.headsupdev.agile.security.permission.RepositoryReadPermission;
import org.headsupdev.agile.security.permission.RepositoryWritePermission;
import org.headsupdev.agile.storage.HibernateStorage;
import org.headsupdev.agile.storage.HibernateUtil;
import org.headsupdev.agile.storage.StoredProject;
import org.headsupdev.agile.web.ApplicationPageMapper;
import org.apache.catalina.servlets.WebdavServlet;
import org.hibernate.Session;
import org.hibernate.Transaction;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO Document me!
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class RepositoryServlet
    extends WebdavServlet
{
    private org.headsupdev.agile.api.SecurityManager securityManager;

    private static Storage storage;

    private static File repoRoot;

    private static final List<String> READ_METHODS;

    static
    {
        READ_METHODS = new ArrayList<String>();
        READ_METHODS.add( "HEAD" );
        READ_METHODS.add( "GET" );
        READ_METHODS.add( "PROPFIND" );
        READ_METHODS.add( "OPTIONS" );
        READ_METHODS.add( "REPORT" );

        storage = Manager.getStorageInstance();
        File dataDir = storage.getGlobalConfiguration().getDataDir();
        repoRoot = new File( dataDir, "repository" );

        if ( !repoRoot.exists() )
        {
            repoRoot.mkdirs();
        }
    }

    public static boolean isReadMethod( String method )
    {
        if ( StringUtil.isEmpty( method ) )
        {
            return false;
        }

        return READ_METHODS.contains( method.toUpperCase() );
    }

    public static boolean isWriteMethod( String method )
    {
        return !isReadMethod( method );
    }

    public synchronized void init()
        throws ServletException
    {
        super.init();

        securityManager = Manager.getSecurityInstance();

        setDebug( HeadsUpConfiguration.isDebug() );
        setRootDirectory( repoRoot );
        try {
            configureRepository( repoRoot, "release" );
            configureRepository( repoRoot, "snapshot" );
            configureRepository( repoRoot, "external" );
            configureRepository( repoRoot, "site" );

            configureRepository( repoRoot, "apps" );
            configureRepository( repoRoot, "accounts" );
            configureRepository( repoRoot, "projects" );
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }

    private void configureRepository( File root, String name )
            throws IOException
    {
        File repoDir = new File( root, name );

        if ( !repoDir.exists() )
        {
            FileUtil.mkdirs( repoDir );
        }
    }


    @Override
    public void collectionCreated( String resource )
    {
        super.collectionCreated( resource );
        String repository = getPathHead( resource );
        String path = getPathTail( resource );

        artifactAdded( repository, path );
    }

    @Override
    public void collectionRemoved( String resource )
    {
        super.collectionRemoved( resource );
    }

    @Override
    public void resourceCreated( String resource )
    {
        super.resourceCreated( resource );
        String repository = getPathHead( resource );
        String path = getPathTail( resource );

        artifactAdded( repository, path );
        metadataSent( repository, path );
    }

    @Override
    public void resourceRemoved( String resource )
    {
        super.resourceRemoved( resource );
    }

    @Override
    public void resourceModified( String resource )
    {
        super.resourceModified( resource );
        String repository = getPathHead( resource );
        String path = getPathTail( resource );

        metadataSent( repository, path );
    }

    @Override
    public boolean isAuthenticated(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException
    {
        String repository = getRepositoryName( req );
        String path = getRepositoryPath( req );

        return isAuthenticated( req, resp, repository, path );
    }

    public boolean isAuthenticated(HttpServletRequest req, HttpServletResponse resp, String repository, String path)
            throws ServletException, IOException
    {
        // don't allow anon to access accounts / projects area...
        if ( repository == null || !( repository.equals( "accounts" ) || repository.equals( "projects" ) ) )
        {
            // if anon access allowed then grant access to other areas
            Role anon = securityManager.getRoleById( "anonymous" );
            if ( anon.getPermissions().contains( getPermission( req ).getId() ) )
            {
                return true;
            }
        }

        User user = WebLoginManager.getInstance().getLoggedInUser( req );
        if ( ( user != null && !user.equals( HeadsUpSession.ANONYMOUS_USER ) ) || allowAnonRequest( req, repository ) )
        {
            return true;
        }

        String header = req.getHeader( "Authorization" );
        String message = "You must provide a username and password to access this resource.";
        if ( ( header != null ) && header.startsWith( "Basic " ) )
        {
            String base64Token = header.substring( 6 );
            String token = new String( Base64.decodeBase64( base64Token.getBytes() ) );

            String username = "";
            String password = "";
            int delim = token.indexOf( ':' );

            if ( delim != ( -1 ) )
            {
                username = token.substring( 0, delim );
                password = token.substring( delim + 1 );
            }

            String encodedPass = HashUtil.getMD5Hex( password );

            user = securityManager.getUserByUsername( username );
            if ( user != null )
            {
                if ( !user.getPassword().equals( encodedPass ) )
                {
                    message = "Invalid username or password";
                }
                else if ( !user.canLogin() )
                {
                    message = "Account is not currently active";
                }
                else
                {
                    req.setAttribute( "agile-user", user );
                    return true;
                }
            }
            else
            {
                message = "Invalid username or password";
            }
        }

        resp.addHeader( "WWW-Authenticate", "Basic realm=\"HeadsUp Webdav\"" );
        resp.sendError( HttpServletResponse.SC_UNAUTHORIZED, message );

        ( (HibernateStorage) storage ).closeSession();
        return false;
    }

    @Override
    public boolean isAuthorized( HttpServletRequest req, HttpServletResponse resp )
        throws ServletException, IOException
    {
        String repository = getRepositoryName( req );
        String path = getRepositoryPath( req );

        return isAuthorized( req, resp, repository, path );
    }

    public boolean isAuthorized( HttpServletRequest req, HttpServletResponse resp, String repository, String path )
        throws ServletException, IOException
    {
        Role anon = securityManager.getRoleById( "anonymous" );
        User user = (User) req.getAttribute( "agile-user" );
        if ( user == null )
        {
            user = WebLoginManager.getInstance().getLoggedInUser( req );
        }

        if ( repository == null || repository.length() == 0 )
        {
            if ( isWriteMethod( req.getMethod() ) )
            {
                return false;
            }
            else
            {
                Permission perm = getPermission( req );

                return ( anon.getPermissions().contains( perm.getId() ) ) ||
                    securityManager.userHasPermission( user, perm, null );
            }
        }
        else if ( allowAnonRequest( req, repository ) )
        {
            return true;
        }

        if ( repository.equals( "accounts" ) )
        {
            // anon user cannot go any further
            if ( user == null || user.getRoles().contains( anon ) )
            {
                return false;
            }

            // create my own directory before I see the listing / request it...
            File myDir = new File( new File( repoRoot, "accounts" ), user.getUsername() );
            if ( !myDir.exists() )
            {
                FileUtil.mkdir( myDir );
            }

            // allow anyone else to see the listing
            String resource = stripSlashes( path );
            if ( resource == null || resource.length() == 0 )
            {
                return isReadMethod( req.getMethod() );
            }

            String username = getPathHead( resource );
            return username.equalsIgnoreCase( user.getUsername() ) &&
                    securityManager.userHasPermission( user, getPermission( req ), null );
        }
        else if ( repository.equals( "projects" ) )
        {
            // anon user cannot go any further
            if ( user == null || user.getRoles().contains( anon ) )
            {
                return false;
            }

            // create project directory before I see the listing / request it...
            for ( Project project : user.getProjects() )
            {
                File myDir = new File( new File( repoRoot, "projects" ), project.getId() );
                if ( !myDir.exists() )
                {
                    FileUtil.mkdir( myDir );
                }
            }

            // allow anyone else to see the listing
            String resource = stripSlashes( path );
            if ( resource == null || resource.length() == 0 )
            {
                return isReadMethod( req.getMethod() );
            }

            String projectId = getPathHead( resource );
            if ( projectId.equals( Project.ALL_PROJECT_ID ) )
            {
                return StoredProject.getDefaultProjectMembers().contains( user );
            }
            else
            {
                for ( Project project : user.getProjects() )
                {
                    if ( project.getId().equalsIgnoreCase( projectId ) )
                    {
                        return securityManager.userHasPermission( user, getPermission( req ), project );
                    }
                }
            }
            return false;
        }

        Permission perm = getPermission( req );
        Project project = getProject( repository, path );

        boolean auth = ( anon.getPermissions().contains( perm.getId() ) ) ||
            securityManager.userHasPermission( user, perm, project );
        ( (HibernateStorage) storage ).closeSession();
        return auth;
    }

    private boolean allowAnonRequest( HttpServletRequest req, String repository )
    {
        if ( repository == null || !repository.equals( "apps" ) )
        {
            return false;
        }

        return WebLoginManager.getInstance().shouldAllowRequest( req );
    }

    private void artifactAdded( String repository, String resource )
    {
        try{
        if ( repository.equals( "site" ) || repository.equals( "accounts" ) || repository.equals( "projects" ) )
        {
            return;
        }

        Artifact art = new Artifact( resource, repository );

        Session session = HibernateUtil.getCurrentSession();

        Transaction tx = session.beginTransaction();
        session.saveOrUpdate( art );

        tx.commit();
        } catch ( Exception e ) {e.printStackTrace();}
    }

    @Override
    protected String getRelativePath( HttpServletRequest request )
    {
        String resolvedPath = super.getRelativePath( request );

        File resolved = new File( repoRoot, resolvedPath );
        resolved = org.headsupdev.agile.api.util.FileUtil.replaceLatest( resolved );
        return resolved.getPath().substring( repoRoot.getPath().length() );
    }

    private void metadataSent( String repository, String resource )
    {
        try {
        if ( repository.equals( "site" ) || repository.equals( "accounts" ) || repository.equals( "projects" ) )
        {
            return;
        }

        File repo = new File( repoRoot, repository );
        if ( resource.endsWith( ".pom" ) )
        {
            File dir = new File( repo, resource ).getParentFile();
            Project project = getProject( repository, new File( resource ).getParent() );

            if ( project != null && project instanceof MavenTwoProject )
            {
                MavenTwoProject m2Project = (MavenTwoProject) project;
                String artifactId = m2Project.getArtifactId();
                String groupId = m2Project.getGroupId();
                String version = dir.getName();

                // Add an event to the repository app to log the released artifact
                Application repoApp = ApplicationPageMapper.get().getApplication( "artifacts" );
                if ( repoApp != null )
                {
                    String resName = resource.substring( 0, new File( resource ).getName().length() );
                    String path = resource.substring( 0, resource.length() - resName.length() ).replace( String.valueOf( File.separatorChar ), ":" );
                    repoApp.addEvent( new UploadArtifactEvent( groupId, artifactId, version, repository, path,
                            project ) );
                }
            }
        }
        else if ( resource.contains( "ivy-" ) && resource.endsWith( ".xml" ) )
        {
            // double check this has the ivy structure (above test rather weak)...
            File dir = new File( repo, resource ).getParentFile();
            if ( resource.endsWith( "ivy-" + dir.getName() + ".xml" ) )
            {
                Project project = getProject( repository, new File( resource ).getParent() );

                if ( project != null && project instanceof AntProject )
                {
                    AntProject antProject = (AntProject) project;
                    String module = antProject.getModule();
                    String org = antProject.getOrganisation();
                    String version = dir.getName();

                    // Add an event to the repository app to log the released artifact
                    Application repoApp = ApplicationPageMapper.get().getApplication( "artifacts" );
                    if ( repoApp != null )
                    {
                        String resName = resource.substring( 0, new File( resource ).getName().length() );
                        String path = resource.substring( 0, resource.length() - resName.length() ).replace( String.valueOf( File.separatorChar ), ":" );
                        repoApp.addEvent( new UploadArtifactEvent( org, module, version, repository, path,
                                project ) );
                    }
                }
            }

        }

        } catch ( Exception e ) {e.printStackTrace();}
    }

    protected Project getProject( String repoName, String resource )
    {
        File repo = new File( repoRoot, repoName );

        Project project = null;
        resource = stripSlashes( resource );
        if ( resource != null )
        {
            if ( repoName.equals( "apps" ) || repoName.equals( "site" ) )
            {
                project = getProjectUsingId( resource );
            }
            else
            {
                project = getProjectDepthFirst( resource, repo );
            }
        }

        // there are actually no matches - return "all" project
        if ( project == null )
        {
            project = StoredProject.getDefault();
        }

        return project;
    }

    private Project getProjectUsingId( String path )
    {
        int firstSeparator = path.indexOf( File.separatorChar );
        if ( firstSeparator != -1 )
        {
            return storage.getProject( path.substring( 0, firstSeparator ) );
        }
        else
        {
            return storage.getProject( path );
        }
    }

    private Project getProjectDepthFirst( String path, File root )
    {
        // this heirarchy will not bottom out as we require this depth in $HEADSUP_HOME/repository/name/
        File file = new File( root, path );
        File parentFile = file.getParentFile();
        if ( root.equals( file ) || root.equals( parentFile ) )
        {
            return null;
        }

        String artifactId = file.getName();
        String groupId = path.substring( 0, path.length() - file.getName().length() - 1 );
        groupId = groupId.replace( File.separatorChar, '.' );

        for ( Project test : storage.getProjects() )
        {
            if ( test instanceof MavenTwoProject )
            {
                if ( artifactId.equals( ( (MavenTwoProject) test ).getArtifactId() ) &&
                        groupId.equals( ( (MavenTwoProject) test ).getGroupId() ) )
                {
                    return test;
                }
            }
            else if ( test instanceof AntProject )
            {
                if ( artifactId.equals( ( (AntProject) test ).getModule() ) &&
                    groupId.equals( ( (AntProject) test ).getOrganisation() ) )
                {
                    return test;
                }
            }
        }

        // no matches, try next resource up the path
        String parentPath = new File( path ).getParent();
        return getProjectDepthFirst( parentPath, root );
    }

    protected String getRepositoryName( HttpServletRequest request )
    {
        String path = request.getPathInfo();
        if (path == null) {
            path = request.getServletPath();
        }
        if ((path == null) || (path.equals(""))) {
            path = "/";
        }

        return getPathHead( path );
    }

    protected String getPathHead( String path )
    {
        String resource = stripSlashes( path );
        if ( resource == null || resource.length() == 0 )
        {
            return null;
        }


        int pos = resource.indexOf( '/' );
        String repoId = resource;
        if ( pos > -1 )
        {
            repoId = resource.substring( 0, pos );
        }

        return repoId;
    }

    protected String getRepositoryPath( HttpServletRequest request )
    {
        String path = request.getPathInfo();
        if ( path == null )
        {
            path = request.getServletPath();
        }
        if ( ( path == null ) || ( path.equals( "" ) ) )
        {
            path = "/";
        }

        return getPathTail( path );
    }

    protected String getPathTail( String path )
    {
        String resource = stripSlashes( path );
        if ( resource == null || resource.length() == 0 )
        {
            return null;
        }

        int pos = resource.indexOf( '/' );
        String repoPath = "/";
        if ( pos > -1 )
        {
            repoPath = resource.substring( pos + 1 );
        }

        return repoPath;
    }

    /**
     * strip any preceeding and proceesing slash from a file path
     *
     * @param path path to strip
     * @return file path with pre or post File.separatorChar's removed
     */
    private String stripSlashes( String path )
    {
        if ( path != null )
        {
            if ( path.length() > 0 && path.charAt( 0 ) == File.separatorChar )
            {
                path = path.substring( 1 );
            }
            if ( path.length() > 1 && path.charAt( path.length() - 1 ) == File.separatorChar )
            {
                path = path.substring( 0, path.length() - 1 );
            }
        }

        return path;
    }

    protected Permission getPermission( HttpServletRequest request )
    {
        boolean write = isWriteMethod( request.getMethod() );
        String repository = getRepositoryName(request);

        if ( write )
        {
            if ( repository != null && repository.equals( "apps" ) )
            {
                return new RepositoryWriteAppPermission();
            }
            return new RepositoryWritePermission();
        }
        else
        {
            if ( repository != null && repository.equals( "apps" ) )
            {
                return new RepositoryReadAppPermission();
            }
            return new RepositoryReadPermission();
        }
    }
}
