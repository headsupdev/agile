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

package org.headsupdev.agile.app.files;

import org.headsupdev.support.java.StringUtil;
import org.headsupdev.agile.api.*;
import org.headsupdev.agile.scm.HeadsUpScmManager;
import org.headsupdev.agile.scm.ScmVariant;
import org.headsupdev.agile.storage.*;
import org.headsupdev.agile.storage.ScmChangeSet;
import org.headsupdev.agile.app.files.event.FileChangeSetEvent;
import org.headsupdev.agile.api.logging.Logger;

import org.apache.maven.scm.*;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.scm.command.changelog.ChangeLogSet;
import org.apache.maven.scm.command.diff.DiffScmResult;
import org.apache.maven.scm.command.update.UpdateScmResult;
import org.headsupdev.support.java.FileUtil;
import org.hibernate.Transaction;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import java.io.File;
import java.util.*;

/**
 * A class used to run the scm updater thread - this keeps projects up to date and loads the metadata about the chates.
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class BrowseScmUpdater
    implements ProjectListener
{
    private HeadsUpScmManager scmManager = HeadsUpScmManager.getInstance();

    private Thread updater;
    private List<String> pendingUpdates = new LinkedList<String>();
    private boolean shutdown = false;
    private boolean updating = false;

    private Logger log = Manager.getLogger( getClass().getName() );

    public BrowseScmUpdater()
    {
        updater = new Thread()
        {
            public void run()
            {
                // wait before starting
                try
                {
                    Thread.sleep( 1000 * 60 * 15/* 60 */);
                }
                catch ( InterruptedException e )
                {
                    // ignore and run anyway I guess
                }

                while ( !shutdown )
                {
                    try
                    {
                        // add each (not currently updating) project to the list again and make sure we are updating
                        for ( Project project : Manager.getStorageInstance().getRootProjects() )
                        {
                            queueProject( project );
                        }
                        updateProjects();
                    }
                    catch ( Exception e )
                    {
                        e.printStackTrace();
                    }

                    try
                    {
                        Thread.sleep( 1000 * 60 * 15/* 60 */);
                    }
                    catch ( InterruptedException e )
                    {
                        // ignore and run anyway I guess
                    }
                }
            }

        };

        updater.start();
    }

    public void stop()
    {
        shutdown = true;
        updater.interrupt();
    }

    public void updateAll()
    {
        updater.interrupt();
    }

    public void updateProject( Project project )
    {
        if ( project.equals( StoredProject.getDefault() ) )
        {
            updateAll();
        }
        else
        {
            queueProject( project );
            updateProjects();
        }
    }

    public void queueProject( Project project )
    {
        if ( !pendingUpdates.contains( project ) )
        {
            if ( project.getParent() == null )
            {
                pendingUpdates.add( project.getId() );
            }
        }
    }

    protected void updateProjects()
    {
        if ( updating )
        {
            return;
        }

        Task check = new CheckUpdateTask();
        Manager.getInstance().addTask( check );
        updating = true;
        while ( pendingUpdates.size() > 0 )
        {
            String next = pendingUpdates.remove( 0 );

            loadChangeSets( next );
        }
        updating = false;
        Manager.getInstance().removeTask( check );
    }

    // TODO if we shut down whilst this is running we need to resume when we restart
    // (currently it waits for the next update before it sees we have changes to load)
    private void loadChangeSets( String projectId )
    {
        Project project = getProject( projectId );
        String scm = project.getScm();
        File working = Manager.getStorageInstance().getWorkingDirectory( project );
        boolean importing = false;
        boolean first = false;

        UpdateScmResult result;
        // this date is set in the future so we get all changes from any timezone
        // TODO maybe there is a better way of knowing when the last change for the changeLog is?...
        Date end = new Date( System.currentTimeMillis() + ( 24 * 60 * 60 * 1000 ) );
        String previousId = project.getRevision();
        try
        {
            result = scmManager.update( scmManager.makeScmRepository( scm ), new ScmFileSet( working ), false );

            if ( previousId == null || previousId.equals( "0" ) )
            {
                importing = true;
                first = true;
            }
            else if ( result.getUpdatedFiles() == null || result.getUpdatedFiles().size() == 0 )
            {
                return;
            }
        }
        catch ( Exception e )
        {
            log.error( "Error updating scm copy", e );
            return;
        }

        Task updateTask = new UpdateTask( project );
        try
        {
            Manager.getInstance().addTask( updateTask );

            Session session = HibernateUtil.openSession();
            Transaction tx = session.beginTransaction();

            org.headsupdev.agile.api.service.ChangeSet lastChanges = getChangeSet( project, previousId );
            Date start;
            if ( lastChanges != null )
            {
                start = lastChanges.getDate();
            }
            else
            {
                start = new Date( 0 );
            }

            ScmRepository repository = scmManager.makeScmRepository( scm );
            ScmVariant variant = HeadsUpScmManager.getInstance().getScmVariant( repository.getProvider() );

            ChangeLogSet changes = scmManager.changeLog( repository, new ScmFileSet( working ),
                                                         start, end, 0, null ).getChangeLog();
            File loadedWorking = new File( Manager.getStorageInstance().getApplicationDataDirectory( application ), project.getId() );
            List<ScmFile> checkedOutFiles = null;
            // create a checkout at the latest version we have recorded
            if ( !loadedWorking.exists() )
            {
                repository.getProviderRepository().setPersistCheckout( true );
                if ( !StringUtil.isEmpty( project.getScmUsername() ) )
                {
                    repository.getProviderRepository().setUser( project.getScmUsername() );
                }
                if ( !StringUtil.isEmpty( project.getScmPassword() ) )
                {
                    repository.getProviderRepository().setPassword( project.getScmPassword() );
                }

                loadedWorking.mkdirs();
                if ( changes == null || changes.getChangeSets() == null || changes.getChangeSets().size() == 0 )
                {
                    return;
                }

                org.apache.maven.scm.ChangeSet firstChange;
                if ( !variant.isLogOldestFirst() )
                {
                    firstChange = (org.apache.maven.scm.ChangeSet) changes.getChangeSets().get( changes.getChangeSets().size() - 1 );
                }
                else
                {
                    firstChange = (org.apache.maven.scm.ChangeSet) changes.getChangeSets().get( 0 );
                }
                ScmRevision firstRevision = new ScmRevision( ( (ChangeFile) firstChange.getFiles().get( 0 ) ).getRevision() );
                checkedOutFiles = scmManager.checkOut( repository, new ScmFileSet( loadedWorking ), firstRevision ).getCheckedOutFiles();
            }
            tx.commit();
            session.close();

            if ( changes == null || changes.getChangeSets() == null )
            {
                return;
            }

            log.info( "Found " + (variant.isTransactional()?"":"non") + "transactional changeset with " +
                changes.getChangeSets().size() + " changes" );

            List changeListings = changes.getChangeSets();
            if ( !variant.isLogOldestFirst() ) {
                log.info( "reversing list for provider " + repository.getProvider() );
                Collections.reverse( changeListings );
            }
            ListIterator changeList = changeListings.listIterator();
            while ( changeList.hasNext() )
            {
                org.apache.maven.scm.ChangeSet changeSet = (org.apache.maven.scm.ChangeSet) changeList.next();

                String revision;
                Date current = changeSet.getDate();
                if ( variant.isTransactional() )
                {
                    revision = changeSet.getRevision();

                    if ( revision == null )
                    {
                        // Compatibility code with maven-scm bugs - TODO remove
                        revision = ( (ChangeFile) changeSet.getFiles().get( 0 ) ).getRevision();
                    }
                }
                else
                {
                    revision = changeSet.getAuthor() + ":" + changeSet.getDate();
                }

                // some scms return the revision before this time for completeness...
                if ( getChangeSet( project, revision ) != null )
                {
                    // note that here we may have updated info if the scm supports duplicates - i.e. 2 merges into 1 commit...
                    continue;
                }

                // get the status of each updated file. For the first revision this is from a checkout, others an update
                List<ScmChange> changedFiles = new LinkedList<ScmChange>();
                List<ScmFile> updatedFiles = null;
                boolean moved = false;
                if ( first )
                {
                    updatedFiles = checkedOutFiles;
                }
                else
                {
                    if ( variant.isTransactional() )
                    {
                        UpdateScmResult updateResult = scmManager.update( repository, new ScmFileSet( loadedWorking ),
                            new ScmRevision( revision ), false );

                        if ( updateResult.isSuccess() )
                        {
                            updatedFiles = updateResult.getUpdatedFiles();
                        }
                        else
                        {
                            if ( shutdown )
                            {
                                log.warn( "Terminating update, not all changes imported" );
                                return;
                            }
                            moved = true;

                            log.warn( "Failed to update, perhaps the repository has moved - re-checking out at the current revision" );
                            FileUtil.delete( loadedWorking, true );
                            scmManager.checkOut( repository, new ScmFileSet( loadedWorking ), new ScmRevision( revision ) );
                        }
                    }
                    else
                    {
                        // TODO fix maven-scm bug for update using Date... (then we can remove the if above)
                        UpdateScmResult updateResult = scmManager.update( repository, new ScmFileSet( loadedWorking ), current );
                        updatedFiles = updateResult.getUpdatedFiles();
                    }
                }

                org.headsupdev.agile.api.service.ChangeSet set;
                if ( variant.isTransactional() )
                {
                    set = new TransactionalScmChangeSet( revision, changeSet.getAuthor(), changeSet.getComment(), current, project );
                }
                else
                {
                    set = new ScmChangeSet( changeSet.getAuthor(), changeSet.getComment(), current, project );
                }

                log.info( "Requesting diff from " + previousId + " to " + revision );
                Set<Project> affected = new HashSet<Project>();
                DiffScmResult diff = null;
                if ( variant.isTransactional() )
                {
                    diff = scmManager.diff( scmManager.makeScmRepository( scm ), new ScmFileSet( working ),
                            variant.getStartRevisionForDiff( previousId, revision ),
                            variant.getEndRevisionForDiff( previousId, revision ) );
                }
                else
                {
                    // TODO fix glaring omission in maven-scm where the diff( Date...Date ) is not supported...
                }
                log.info( "Found " + diff.getChangedFiles() + " file diffs" );

                session = HibernateUtil.openSession();
                tx = session.beginTransaction();
                try {
                    project = (Project) session.merge( project );
                    ( (ScmChangeSet) set ).setPrevious( lastChanges );

                    // enter the changes with diffs to the database
                    List<ScmFile> scmFiles;
                    if ( moved || ( !StringUtil.isEmpty( previousId ) && variant.useDiffForFileListing() ) )
                    {
                        // using extended diff is much richer than the update results (but provides nonsense for #1)
                        scmFiles = diff.getChangedFiles();
                    }
                    else
                    {
                        scmFiles = updatedFiles;
                    }

                    if ( scmFiles != null )
                    {
                        for ( ScmFile scmFile : scmFiles )
                        {
                            int type;
                            if ( scmFile.getStatus().equals( ScmFileStatus.ADDED ) )
                            {
                                type = ScmChange.TYPE_ADDED;
                            }
                            else if ( scmFile.getStatus().equals( ScmFileStatus.DELETED ) )
                            {
                                type = ScmChange.TYPE_REMOVED;
                            }
                            else
                            {
                                type = ScmChange.TYPE_CHANGED;
                            }

                            ScmChange adding;
                            // a small hack to match the maven-scm output
                            String path = scmFile.getPath().replace( File.separatorChar, '/' );
                            String difference = null;
                            if ( diff != null && diff.getDifferences() != null && diff.getDifferences().containsKey( path ) )
                            {
                                difference = diff.getDifferences().get( path ).toString();
                            }
                            if ( variant.isTransactional() )
                            {
                                adding = new ScmChange( path, type, difference, set );
                            }
                            else
                            {
                                adding = new ScmChange( path, findRevisionForScmFile( changeSet, scmFile ), type, difference, set );
                            }
                            changedFiles.add( adding );
                            set.getChanges().add( adding );
                            session.save( adding );
                        }
                    }

                    session.save( set );
                    log.info( "Saved changeset " + set.getId() + " with " + set.getChanges().size() + " files" );

                    if ( lastChanges != null )
                    {
                        ( (ScmChangeSet) lastChanges ).setNext( set );
                        session.merge( lastChanges );
                    }

                    // update the file revision links
                    for ( ScmChange file : changedFiles )
                    {
                        if ( variant.isTransactional() )
                        {
                            affected.add( updateFile( project, file.getName(), revision, session, importing ) );
                        }
                        else
                        {
                            affected.add( updateFile( project, file.getName(), file.getRevision(), session, importing ) );
                        }
                    }

                    if ( affected.isEmpty() )
                    {
                        affected.add( project );
                    }

                    for ( Project affect : affected )
                    {
                        log.info( "Setting revision to " + revision + " for project " + affect.getId() );
                        setRevision( affect, set.getId(), session );
                    }

                    ScmCommentParser.parseComment( set.getComment(), set );

                    tx.commit();
                }
                catch ( Exception e )
                {
                    // something failed in the database, log it and try again
                    // TODO find the real cause and remove this (previous(); continue) hack...
                    log.error( "Failed to load project change set", e );
e.printStackTrace();
                    tx.rollback();
//                    changeList.previous();

                    continue;
                }
                finally
                {
                    session.close();
                }

                previousId = set.getId();
                lastChanges = set;
                first = false;

                for ( Project affect : affected )
                {
                    application.addEvent( new FileChangeSetEvent( set, affect ), !importing );
                }
            }
        }
        catch ( Throwable t )
        {
            log.error( "Error updating projects", t );
        }
        finally
        {
            Manager.getInstance().removeTask( updateTask );
        }
    }

    private Project getProject( String id )
    {
        Session session = HibernateUtil.getCurrentSession();
        Transaction tx = session.beginTransaction();

        Project ret = (Project) session.createCriteria( StoredProject.class ).add( Restrictions.idEq( id ) ) .uniqueResult();
        session.close();

        return ret;
    }

    private void setRevision( Project project, String revision, Session session )
    {
        project.setRevision( revision );

        if ( project.getParent() != null )
        {
            setRevision( project.getParent(), revision, session );
        }
    }

    public void projectAdded( Project project )
    {
        if ( project.getParent() == null )
        {
            queueProject( project );
            updateProjects();
        }
    }

    public void projectModified( Project project )
    {
    }

    public void projectFileModified( Project project, String path, File file )
    {
    }

    public void projectRemoved( Project project )
    {
    }

    protected Project updateFile( Project project, String path, String revision, Session session, boolean importing )
    {
        File file = new File( path );
        session.merge( new org.headsupdev.agile.storage.files.File( path, revision, project ) );

        while ( file.getParentFile() != null )
        {
            file = file.getParentFile();

            session.merge( new org.headsupdev.agile.storage.files.File( file.getPath(), revision, project ) );
        }

        return getChangedProjects( path, project, "", session, importing );
    }

    private Project getChangedProjects( String path, Project project, String rel, Session session, boolean importing )
    {
        char sep;
        String test = rel;
        // scm might not end with a slash, but we need it to, but should not start with one
        if ( rel.startsWith( "\\" ) || rel.startsWith( "/" ) )
        {
            test = rel.substring( 1 );
            sep = rel.charAt( 0 );

            if ( !( rel.endsWith( "\\" ) || rel.endsWith( "/" ) ) )
            {
                test = test + sep;
            }
        }

        if ( !path.startsWith( test ) ) {
            return null;
        }

        for ( Project child : project.getChildProjects() )
        {
            int scmDiff = child.getScm().length() - project.getScm().length();
            Project possible = getChangedProjects( path, child, rel + child.getScm().substring( child.getScm().length() - scmDiff ),
                session, importing );

            if ( possible != null )
            {
                return possible;
            }
        }

        if ( !importing )
        {
            Project root = project;
            while ( root.getParent() != null )
            {
                root = root.getParent();
            }
            File working = new File( Manager.getStorageInstance().getApplicationDataDirectory( application ), root.getId() );

            String projectFilePath = path.substring( rel.length() );
            File file = new File( working, path );
            Manager.getInstance().fireProjectFileModified( project, projectFilePath, file );
        }
        return project;
    }

    private BrowseApplication application;
    void setApplication( BrowseApplication application )
    {
        this.application = application;
    }

    public org.headsupdev.agile.api.service.ChangeSet getChangeSet( Project project, String revision )
    {
        Session session = ( (HibernateStorage) Manager.getStorageInstance() ).getHibernateSession();
        Transaction tx = session.beginTransaction();

        org.headsupdev.agile.api.service.ChangeSet ret = Manager.getInstance().getScmService().getChangeSet( project, revision );
        tx.commit();

        return ret;
    }

    public String findRevisionForScmFile( org.apache.maven.scm.ChangeSet changeSet, ScmFile scmFile )
    {
        String scmPath = scmFile.getPath();
        String version = "";

        for ( ChangeFile changeFile : (List<ChangeFile>) changeSet.getFiles() ) {
            if ( changeFile.getName().equals( scmPath ) ) {
                version = changeFile.getRevision();
                break;
            }
        }

        return version;
    }
}
