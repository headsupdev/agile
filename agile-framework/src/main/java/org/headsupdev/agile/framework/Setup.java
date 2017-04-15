/*
 * HeadsUp Agile
 * Copyright 2009-2017 Heads Up Development Ltd.
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

package org.headsupdev.agile.framework;

import org.headsupdev.agile.framework.setup.DatabasePanel;
import org.headsupdev.agile.framework.setup.FinishedPanel;
import org.headsupdev.agile.framework.setup.AddAdminPanel;

import org.headsupdev.agile.storage.*;
import org.headsupdev.agile.web.components.configuration.UpdatesPanel;
import org.headsupdev.agile.web.*;
import org.headsupdev.agile.api.*;
import org.headsupdev.agile.api.SecurityManager;
import org.headsupdev.agile.core.PrivateConfiguration;
import org.headsupdev.agile.security.DefaultSecurityManager;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.model.Model;

/**
 * The HeadsUp setup wizard.
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@MountPoint( "setup" )
public class Setup
    extends HeadsUpPage
{
    public Permission getRequiredPermission()
    {
        return null;
    }

    public void layout()
    {
        SecurityManager securityManager = getSecurityManager();
        super.layout();
        add( CSSPackageResource.getHeaderContribution( getClass(), "setup.css" ) );

        boolean[] tabSelected = {false, false, false, false};

        // check if we have already been setup
        if ( PrivateConfiguration.isInstalled() &&
            PrivateConfiguration.getSetupStep() >= PrivateConfiguration.STEP_FINISHED )
        {
            add( new Label( "setup-content", "<p>The setup wizard has already been completed!</p>" ).setEscapeModelStrings( false ) );
            drawTabs( tabSelected );

            return;
        }

        if ( PrivateConfiguration.getSetupStep() < PrivateConfiguration.STEP_DATABASE )
        {
            add( new DatabasePanel( "setup-content", getClass(), HomeApplication.getHeadsUpRuntime() ) );
            tabSelected[0] = true;
        }
        else if ( PrivateConfiguration.getSetupStep() < PrivateConfiguration.STEP_UPDATES )
        {
            if ( PrivateConfiguration.getSetupStep() < PrivateConfiguration.STEP_POPULATE )
            {
                // install the "All Projects" project to get round null projects...
                getStorage().addProject( StoredProject.getDefault() );

                ( (DefaultSecurityManager) securityManager ).addUser( new StoredUser( "anonymous" ) );
                ( (DefaultSecurityManager) securityManager ).addRole( new AdminRole() );
                ( (DefaultSecurityManager) securityManager ).addRole( new MemberRole() );
                ( (DefaultSecurityManager) securityManager ).addRole( new AnonymousRole() );

                PrivateConfiguration.setSetupStep( PrivateConfiguration.STEP_POPULATE );
            }

            add( new UpdatesPanel( "setup-content", getClass() ) );
            tabSelected[1] = true;
        }
        else if ( PrivateConfiguration.getSetupStep() < PrivateConfiguration.STEP_ADMIN )
        {
            add( new AddAdminPanel( "setup-content", getClass() ) );
            tabSelected[2] = true;
        }
        else
        {
            add( new FinishedPanel( "setup-content" ) );
        
            if ( PrivateConfiguration.getSetupStep() < PrivateConfiguration.STEP_TESTER )
            {
                ( (DefaultSecurityManager) securityManager ).addRole( new TesterRole() );
            }
 
            PrivateConfiguration.setSetupStep( PrivateConfiguration.STEP_FINISHED );
            PrivateConfiguration.setInstalled( true );

            for ( Application app : ApplicationPageMapper.get().getApplications() )
            {
                ( (DefaultSecurityManager) securityManager ).scanPermissions( app );
            }

            String body = renderAbout();
            getHeadsUpApplication().addEvent( new SystemEvent( getStorage().getGlobalConfiguration().getProductName() + " installed :)",
                    getStorage().getGlobalConfiguration().getProductName() + " has been installed - congratulations", body ) );

            getManager().setupCompleted();
            tabSelected[3] = true;
        }

        drawTabs( tabSelected );
    }

    @Override
    public String getTitle()
    {
        return getStorage().getGlobalConfiguration().getProductName() + " Setup";
    }

    protected void drawTabs( final boolean[] tabSelected )
    {
        for ( int i = 0; i < tabSelected.length; i++ )
        {
            final int tabId = i;
            WebMarkupContainer tab = new WebMarkupContainer( "tab" + tabId );
            tab.add( new AttributeModifier( "class", true, new Model<String>()
            {
                public String getObject()
                {
                    String tabClass = "tab" + tabId;
                    if ( tabSelected[tabId] )
                    {
                        tabClass += " selected";
                    }

                    if ( tabId == tabSelected.length )
                    {
                        tabClass += " last";
                    }

                    return tabClass;
                }
            } ) );
            add( tab );
        }
    }

    private String renderAbout()
    {
        return new RenderUtil()
        {
            public Panel getPanel()
            {
                return new AboutPanel( RenderUtil.PANEL_ID );
            }
        }.getRenderedContent();
    }
}
