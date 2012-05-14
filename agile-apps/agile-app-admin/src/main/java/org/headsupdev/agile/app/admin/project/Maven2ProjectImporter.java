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

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.form.Form;

import java.io.File;

import org.headsupdev.agile.storage.StoredMavenTwoProject;
import org.headsupdev.agile.storage.StoredProject;

/**
 * TODO Document me!
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class Maven2ProjectImporter
    extends Panel
{
    private File checkoutDir;

    public Maven2ProjectImporter( String id, final AddProject page, final File dir )
    {
        super( id );
        checkoutDir = dir;

        add( new Label( "type", getTypeName() ) );

        Form form = new Form( "details" )
        {
            @Override
            protected void onSubmit() {
                Maven2ProjectImporter.this.submitForm( this );
            }
        };
        add( form );
        layoutForm( form );

        form.add( new Button( "cancel" )
        {
            @Override
            public void onSubmit()
            {
                page.cancelImport( dir );
            }
        }.setDefaultFormProcessing( false ) );
    }

    protected File getCheckoutDir()
    {
        return checkoutDir;
    }

    protected void layoutForm( Form form )
    {
    }

    protected void submitForm( Form form )
    {
    }

    public boolean requiresInput()
    {
        return false;
    }

    public String getTypeName()
    {
        return "Maven2";
    }

    public StoredProject importProjects( File dir, StoredMavenTwoProject parent, String scm, String username,
                                                 String password )
    {
        File pom = new File( dir, "pom.xml" );
        if ( !pom.exists() )
        {
            return null;
        }

        StoredMavenTwoProject child = new StoredMavenTwoProject( pom );
        if ( parent != null )
        {
            child.setParent( parent );
            parent.addChildProject( child );
        }
        child.setScm( scm );
        child.setScmUsername( username );
        child.setScmPassword( password );

        if ( child.getPackaging().equals( "pom" ) )
        {
            for ( String module : child.getModules() )
            {
                File moduleDir = new File( dir, module );
                String childScm;
                if ( scm.endsWith( "/" ) )
                {
                    childScm = scm + module + "/";
                }
                else
                {
                    childScm = scm + "/" + module;
                }

                if ( moduleDir.isDirectory() )
                {
                    importProjects( moduleDir, child, childScm, username, password );
                }
            }
        }

        return child;
    }
}
