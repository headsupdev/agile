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

package org.headsupdev.agile.app.ci;

import org.headsupdev.agile.web.HeadsUpPage;
import org.headsupdev.agile.web.BookmarkableMenuLink;
import org.headsupdev.agile.web.MountPoint;
import org.headsupdev.agile.web.components.FormattedDurationModel;
import org.headsupdev.agile.api.Permission;
import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.app.ci.permission.BuildViewPermission;
import org.headsupdev.agile.storage.ci.Build;
import org.headsupdev.agile.storage.ci.TestResultSet;
import org.headsupdev.agile.storage.ci.TestResult;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.Model;

import java.util.LinkedList;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;

/**
 * A page that renders test results for a build that supports result parsing
 *
 * @author Andrew Williams
 * @since 1.0
 */
@MountPoint( "tests" )
public class Tests
    extends HeadsUpPage
{
    private long buildId;

    public Permission getRequiredPermission() {
        return new BuildViewPermission();
    }

    public void layout() {
        super.layout();
        add( CSSPackageResource.getHeaderContribution( getClass(), "ci.css" ) );

        Project project = getProject();
        long id = getPageParameters().getLong("id");
        if ( project == null || id < 0 )
        {
            notFoundError();
            return;
        }

        Build build = CIApplication.getBuild( id, getProject() );
        addLink( new BookmarkableMenuLink( getPageClass( "builds/" ), getProjectPageParameters(), "history" ) );
        addLink( new BookmarkableMenuLink( getPageClass( "builds/view" ), getPageParameters(), "view" ) );

        buildId = build.getId();
        add( new Label( "project", build.getProject().getAlias() ) );
        add( new Label( "tests", String.valueOf( build.getTests() ) ) );
        add( new Label( "failures", String.valueOf( build.getFailures() ) ) );
        add( new Label( "errors", String.valueOf( build.getErrors() ) ) );

        List<TestResultSet> sets = new LinkedList<TestResultSet>( build.getTestResults() );
        Collections.sort( sets, new Comparator<TestResultSet>()
        {
            public int compare( TestResultSet t1, TestResultSet t2 )
            {
                return t1.getName().compareToIgnoreCase( t2.getName() );
            }
        } );
        add( new ListView<TestResultSet>( "set", sets )
        {
            protected void populateItem( ListItem<TestResultSet> listItem )
            {
                final TestResultSet set = listItem.getModelObject();

                listItem.add( new Label( "name", set.getName() ) );
                String icon;
                if ( set.getErrors() > 0 )
                {
                    icon = "error.png";
                }
                else if ( set.getFailures() > 0 )
                {
                    icon = "failed.png";
                }
                else
                {
                    icon = "passed.png";
                }
                listItem.add( new Image( "status", new ResourceReference( View.class, icon ) ) );

                listItem.add( new Label( "tests", String.valueOf( set.getTests() ) ) );
                listItem.add( new Label( "failures", String.valueOf( set.getFailures() ) ) );
                listItem.add( new Label( "errors", String.valueOf( set.getErrors() ) ) );

                final WebMarkupContainer log = new WebMarkupContainer( "log" );
                log.add( new Label( "output", new Model<String>()
                {
                    String output = null;
                    @Override
                    public String getObject() {
                        if ( output == null )
                        {
                            output = set.getOutput();
                        }

                        return output;
                    }
                } ) );
                listItem.add( log.setOutputMarkupId( true ).setVisible( false ).setOutputMarkupPlaceholderTag( true ) );

                AjaxLink logLink = new AjaxLink( "log-link" )
                {
                    public void onClick( AjaxRequestTarget target )
                    {
                        log.setVisible( !log.isVisible() );
                        target.addComponent( log );
                    }
                };
                logLink.add( new Image( "log-icon", new ResourceReference( Tests.class, "log.png" ) ) );
                listItem.add( logLink.setVisible( set.getOutput() != null ) );

                listItem.add( new Label( "time", new FormattedDurationModel( set.getDuration() ) ) );

                List<TestResult> results = new LinkedList<TestResult>( set.getResults() );
                Collections.sort( results, new Comparator<TestResult>()
                {
                    public int compare( TestResult r1, TestResult r2 )
                    {
                        if ( r1.getStatus() == r2.getStatus() )
                        {
                            return r1.getName().compareToIgnoreCase( r2.getName() );
                        }

                        return r2.getStatus() - r1.getStatus();
                    }
                } );
                listItem.add( new ListView<TestResult>( "test", results )
                {
                    protected void populateItem( final ListItem<TestResult> listItem )
                    {
                        AttributeModifier rowColor = new AttributeModifier( "class", true, new Model<String>()
                        {
                            public String getObject()
                            {
                                if ( listItem.getIndex() % 2 == 1 )
                                {
                                    return "odd";
                                }

                                return "even";
                            }
                        } );
                        final TestResult result = listItem.getModelObject();

                        WebMarkupContainer row1 = new WebMarkupContainer( "row1" );
                        row1.add( rowColor );
                        listItem.add( row1 );

                        String icon;
                        switch ( result.getStatus() )
                        {
                            case TestResult.STATUS_ERROR:
                                icon = "error.png";
                                break;
                            case TestResult.STATUS_FAILED:
                                icon = "failed.png";
                                break;
                            default:
                                icon = "passed.png";
                        }
                        row1.add( new Image( "status", new ResourceReference( View.class, icon ) ) );

                        if ( result.getMessage() != null && result.getMessage().length() > 0 )
                        {
                            if ( result.getStatus() == TestResult.STATUS_FAILED )
                            {
                                row1.add( new Label( "name", result.getName() + " (failed: " + result.getMessage() + ")" ) );
                            }
                            else
                            {
                                row1.add( new Label( "name", result.getName() + " (error: " + result.getMessage() + ")" ) );
                            }
                        }
                        else
                        {
                            row1.add( new Label( "name", result.getName() ) );
                        }

                        boolean showOutput = result.getStatus() == TestResult.STATUS_ERROR ||
                            result.getStatus() == TestResult.STATUS_FAILED;
                        showOutput = showOutput && result.getOutput() != null;

                        final WebMarkupContainer row2 = new WebMarkupContainer( "row2" );
                        row2.add( rowColor );
                        row2.add( new Label( "output", new Model<String>()
                        {
                            @Override
                            public String getObject()
                            {
                                return result.getOutput();
                            }
                        } ) );
                        listItem.add( row2.setVisible( false ).setOutputMarkupPlaceholderTag( true ) );

                        AjaxLink logLink = new AjaxLink( "log-link" )
                        {
                            public void onClick( AjaxRequestTarget target )
                            {
                                row2.setVisible( !row2.isVisible() );
                                target.addComponent( row2 );
                            }
                        };
                        logLink.add( new Image( "log-icon", new ResourceReference( Tests.class, "log.png" ) ) );
                        row1.add( logLink.setVisible( showOutput ) );
                        row1.add( new Label( "time", new FormattedDurationModel( result.getDuration() ) ) );
                    }
                } );
            }
        } );
    }

    @Override
    public String getTitle()
    {
        return "Test results for build " + buildId;
    }

}
