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

package org.headsupdev.agile.app.admin.project;

import org.apache.wicket.markup.html.CSSPackageResource;
import org.headsupdev.support.java.StringUtil;
import org.headsupdev.agile.security.permission.AdminPermission;
import org.headsupdev.agile.api.Permission;
import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.scm.HeadsUpScmManager;
import org.headsupdev.agile.storage.*;
import org.headsupdev.agile.app.admin.AdminApplication;
import org.headsupdev.agile.web.HeadsUpPage;
import org.headsupdev.agile.web.MountPoint;

import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.PageParameters;
import org.apache.wicket.model.Model;
import org.headsupdev.support.java.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Project importer
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@MountPoint( "add-project" )
public class AddProject
    extends HeadsUpPage
{
    static private HeadsUpScmManager scmManager = HeadsUpScmManager.getInstance();

    private Form confirmForm;
    private Label typeLabel;
    private WebMarkupContainer detail, scmForm;
    private List<Project> projectTree = new LinkedList<Project>();
    private File working;
    private String scmError;

    public Permission getRequiredPermission()
    {
        return new AdminPermission();
    }

    public void layout()
    {
        super.layout();
        add( CSSPackageResource.getHeaderContribution( AdminApplication.class, "admin.css" ) );

        add( scmForm = new ImportForm( "scm" ) );
        add( detail = new WebMarkupContainer( "detail" ) );
        detail.setVisible( false ).setOutputMarkupPlaceholderTag( true );

        confirmForm = new Form( "projects" ) {
            protected void onSubmit() {
                if ( projectTree.size() == 0 )
                {
                    return;
                }

                Project root = projectTree.get( 0 );
                Project duplicate = testForDuplicates( root, null );
                if ( duplicate != null )
                {
                    error( "Duplicate project id \"" + duplicate.getId() + "\" for project \"" + duplicate.getName() + "\"" );
                    return;
                }

                working.renameTo( getStorage().getWorkingDirectory( root ) );
                ( (AdminApplication) getHeadsUpApplication() ).addProject( root, AddProject.this );
                info( "Added project \"" + root.getName() + "\"" );

                PageParameters params = new PageParameters();
                params.add( "project", root.getId() );
                setResponsePage( getPageClass( "show" ), params );
            }
        };
        confirmForm.add( new ProjectImportListView( "projects", projectTree ) );
        confirmForm.add( typeLabel = new Label( "type", "" ) );
        confirmForm.add( new Button( "cancel" ) {
            public void onSubmit() {
                cancelImport( working );
            }
        }.setDefaultFormProcessing( false ) );
        add( confirmForm.setVisible( false ) );
    }

    class ImportForm extends Form
    {
        final List<String> providers = HeadsUpScmManager.getInstance().getScmIds();

        String provider = providers.get( 0 );
        String scm = "http://";
        String username = null, password = null, scmUrl;
        File checkoutDir;
        Maven2ProjectImporter importer;

        public ImportForm( String id )
        {
            super( id );

            setModel( new CompoundPropertyModel( this ) );
            add( new DropDownChoice( "provider", providers ).setRequired( true ) );
            add( new TextField( "scm" ).setRequired( true ) );
            add( new TextField( "username" ).setRequired( false ) );
            add( new PasswordTextField( "password" ).setRequired( false ) );
        }

        public void onSubmit()
        {
            scmUrl = "scm:" + provider + ":" + scm;
            checkoutDir = checkOut( scmUrl, username, password );

            if ( checkoutDir == null || !checkoutDir.exists() )
            {
                error( "SCM error - " + getCheckoutError() );
                cancelImport( checkoutDir );
                return;
            }

            if ( StoredMavenTwoProject.foundMaven2Metadata( checkoutDir ) )
            {
                importer = new Maven2ProjectImporter( "detail", AddProject.this, checkoutDir )
                {
                    @Override
                    protected void submitForm( Form form )
                    {
                        super.submitForm( form );
                        detailsSubmitted();
                    }
                };
            }
            else if ( ( new StoredAntProject() ).foundMetadata( checkoutDir ) )
            {
                importer = new AntProjectImporter( "detail", AddProject.this, checkoutDir )
                {
                    @Override
                    protected void submitForm( Form form )
                    {
                        super.submitForm( form );
                        detailsSubmitted();
                    }
                };
            }
            else if ( ( new StoredEclipseProject() ).foundMetadata( checkoutDir ) )
            {
                importer = new EclipseProjectImporter( "detail", AddProject.this, checkoutDir )
                {
                    @Override
                    protected void submitForm( Form form )
                    {
                        super.submitForm( form );
                        detailsSubmitted();
                    }
                };
            }
            else if ( ( new StoredXCodeProject() ).foundMetadata( checkoutDir ) )
            {
                importer = new XCodeProjectImporter( "detail", AddProject.this, checkoutDir )
                {
                    @Override
                    protected void submitForm( Form form )
                    {
                        super.submitForm( form );
                        detailsSubmitted();
                    }
                };
            }
            else
            {
                importer = new ProjectImporter( "detail", AddProject.this, checkoutDir )
                {
                    @Override
                    protected void submitForm( Form form )
                    {
                        super.submitForm( form );
                        detailsSubmitted();
                    }
                };
            }
            detail.replaceWith( importer );
            detail = importer;
            detail.setVisible( importer.requiresInput() );

            scmForm.setVisible( false );
            typeLabel.setDefaultModel( new Model<String>( importer.getTypeName() ) );

            if ( !importer.requiresInput() )
            {
                detailsSubmitted();
            }
        }

        public void detailsSubmitted()
        {
            StoredProject root;

            try {
                root = importer.importProjects( checkoutDir, null, scmUrl, username, password );
            } catch ( IllegalStateException e ) {
                cancelImport( checkoutDir );
                return;
            }

            if ( root == null )
            {
                error( "No project metadata found" );
                cancelImport( checkoutDir );
                return;
            }

            setProject( root );
            checkoutDir.renameTo( getStorage().getWorkingDirectory( root ) );
        }
        public String getProvider()
        {
            return provider;
        }

        public void setProvider( String provider )
        {
            this.provider = provider;
        }

        public String getScm()
        {
            return scm;
        }

        public void setScm( String scm )
        {
            this.scm = scm;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    protected Project testForDuplicates( Project root, Set<Project> passed )
    {
        List<Project> existing = getStorage().getProjects();

        // test fore stored projects
        if ( existing.contains( root ) )
        {
            return root;
        }
        // test for other projects in this import
        if ( passed != null && passed.contains( root ) )
        {
            return root;
        }

        if ( root.getChildProjects().size() > 0 )
        {
            if ( passed == null )
            {
                passed = new HashSet<Project>();
            }
            passed.add( root );

            for ( Project child : root.getChildProjects() )
            {
                Project duplicate = testForDuplicates( child, passed );
                if ( duplicate != null ) {
                    return duplicate;
                }

                passed.add( child );
            }
        }

        return null;
    }

    @Override
    public String getTitle()
    {
        return "Add Project";
    }

    protected void setProject( Project root )
    {
        projectTree.add( root );
        confirmForm.setVisible( true );
        detail.setVisible( false );
        working = getStorage().getWorkingDirectory( root );
    }

    public File checkOut( String scm, String user, String password )
    {
        projectTree.clear();
        try
        {
            ScmRepository repository = scmManager.makeScmRepository( scm );
            repository.getProviderRepository().setPersistCheckout( true );
            if ( !StringUtil.isEmpty( user ) )
            {
                repository.getProviderRepository().setUser( user );
            }
            if ( !StringUtil.isEmpty( password ) )
            {
                repository.getProviderRepository().setPassword( password );
            }

            File working = FileUtil.createTempDir( "checkout-", "", getStorage().getDataDirectory() );
            CheckOutScmResult result = scmManager.checkOut( repository, new ScmFileSet( working ) );

            if ( !result.isSuccess() )
            {
                scmError = result.getCommandOutput();
                if ( StringUtil.isEmpty( scmError ) )
                {
                    scmError = result.getProviderMessage();
                }
                FileUtil.delete( working, true );
            }

            return working;
        }
        catch ( Exception e )
        {
            scmError = e.getMessage();
            Manager.getLogger( "AddProject" ).error( "Error when loading project source", e );
        }

        return null;
    }

    protected void cancelImport( File dir )
    {
        projectTree.clear();
        scmForm.setVisible( true );
        confirmForm.setVisible( false );
        detail.setVisible( false );

        try
        {
            if ( dir != null && dir.exists() )
            {
                FileUtil.delete( dir, true );
            }
        }
        catch ( IOException e )
        {
            // could not delete the directory - rather unlikely, we just created it
            Manager.getLogger( "AddProject" ).error( "Unable to clean cancelled import", e );
        }

    }

    public String getCheckoutError() {
        return scmError;
    }
}
