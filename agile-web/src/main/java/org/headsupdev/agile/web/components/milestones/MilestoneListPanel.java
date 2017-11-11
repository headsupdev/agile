/*
 * HeadsUp Agile
 * Copyright 2009-2014 Heads Up Development.
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

package org.headsupdev.agile.web.components.milestones;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.OrderByBorder;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.api.User;
import org.headsupdev.agile.storage.HibernateStorage;
import org.headsupdev.agile.storage.dao.MilestonesDAO;
import org.headsupdev.agile.storage.hibernate.NameProjectId;
import org.headsupdev.agile.storage.issues.Milestone;
import org.headsupdev.agile.storage.issues.MilestoneGroup;
import org.headsupdev.agile.web.HeadsUpPage;
import org.headsupdev.agile.web.HeadsUpSession;
import org.headsupdev.agile.web.components.*;
import org.headsupdev.agile.web.wicket.StyledPagingNavigator;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.wicketstuff.animator.Animator;
import org.wicketstuff.animator.IAnimatorSubject;
import org.wicketstuff.animator.MarkupIdModel;

import java.util.Date;

/**
 * A panel that displays a formatted, coloured list of the milestones passed in
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 2.0
 */
public class MilestoneListPanel
        extends Panel
{
    private static final int ITEMS_PER_PAGE = 25;
    private final WebMarkupContainer rowAdd;
    private boolean hideProject;
    private MilestoneGroup group;
    private String groupReference;
    private StyledPagingNavigator pagingHeader, pagingFooter;
    private Milestone quickMilestone;
    private HeadsUpPage page;
    private WebMarkupContainer quickAdd;
    private WebMarkupContainer icon;
    private static final int UNGROUPED_NUMBER = -1;

    public MilestoneListPanel( String id, final SortableDataProvider<Milestone> provider, final HeadsUpPage page,
                               final boolean hideProject, MilestoneGroup group )
    {
        this( id, provider, page, hideProject, group, UNGROUPED_NUMBER );
    }

    public MilestoneListPanel( String id, final SortableDataProvider<Milestone> provider, final HeadsUpPage page,
                               final boolean hideProject, MilestoneGroup group, int groupNumber )
    {
        super( id );

        add( CSSPackageResource.getHeaderContribution( getClass(), "milestone.css" ) );
        this.page = page;
        this.group = group;
        this.hideProject = hideProject;

        if ( groupNumber == UNGROUPED_NUMBER )
        {
            this.groupReference = "group";
        }
        else
        {
            this.groupReference = "group" + groupNumber;
        }

        rowAdd = new WebMarkupContainer( "rowAddMilestone" );
        rowAdd.setMarkupId( "rowAddMilestone" + groupNumber );

        Form<Milestone> inlineForm = getInlineForm();
        add( inlineForm );

        final DataView dataView;
        inlineForm.add( dataView = new StripedDataView<Milestone>( "milestones", provider, ITEMS_PER_PAGE )
        {
            protected void populateItem( Item<Milestone> listItem )
            {
                super.populateItem( listItem );

                Milestone milestone = listItem.getModelObject();
                PageParameters params = new PageParameters();
                params.add( "project", milestone.getProject().getId() );
                params.add( "id", milestone.getName() );

                WebMarkupContainer cell = new WebMarkupContainer( "id-cell" );
                Link idLink = new BookmarkablePageLink( "id-link", page.getPageClass( "milestones/view" ), params );
                idLink.add( new Label( "id-label", milestone.getName() ) );
                cell.add( idLink );
                listItem.add( cell );

                double part = milestone.getCompleteness();
                int percent = (int) ( part * 100 );
                Panel panel = new PercentagePanel( "bar", percent );
                listItem.add( panel );

                int total = milestone.getIssues().size();
                int open = milestone.getOpenIssues().size();
                Label label = new Label( "issues", String.valueOf( total ) );
                listItem.add( label );

                label = new Label( "open", String.valueOf( open ) );
                listItem.add( label );

                label = new Label( "project", milestone.getProject().toString() );
                listItem.add( label.setVisible( !hideProject ) );
                label = new Label( "due", new FormattedDateModel( milestone.getDueDate(),
                        ( (HeadsUpSession) getSession() ).getTimeZone() ) );
                label.add( new MilestoneStatusModifier( "due", milestone ) );
                listItem.add( label );
            }
        } );

        AttributeModifier colspanModifier = new AttributeModifier( "colspan", true, new Model<Integer>()
        {
            @Override
            public Integer getObject()
            {
                int cols = 7;
                if ( hideProject )
                {
                    cols--;
                }

                return cols;
            }
        } );
        inlineForm.add( quickAdd );
        inlineForm.add( new OrderByBorder( "orderByName", "name.name", provider ) );
        inlineForm.add( new OrderByBorder( "orderByDue", "due", provider ) );
        inlineForm.add( new OrderByBorder( "orderByProject", "name.project.id", provider ).setVisible( !hideProject ) );

        pagingFooter = new StyledPagingNavigator( "footerPaging", dataView );
        pagingFooter.setOutputMarkupPlaceholderTag( true );
        inlineForm.add( pagingFooter.add( colspanModifier ) );
        pagingHeader = new StyledPagingNavigator( "headerPaging", dataView );
        pagingHeader.setOutputMarkupPlaceholderTag( true );
        inlineForm.add( pagingHeader.add( colspanModifier ) );

        final WebMarkupContainer allCell = new WebMarkupContainer( "allCell" );
        inlineForm.add( allCell.add( colspanModifier ).setVisible( provider.size() > ITEMS_PER_PAGE ) );
        allCell.add( new Link( "allLink" )
        {
            @Override
            public void onClick()
            {
                dataView.setItemsPerPage( Integer.MAX_VALUE );

                setVisible( false );
                allCell.setVisible( false );
                pagingFooter.setVisible( false );
                pagingHeader.setVisible( false );
            }
        } );
    }

    private Form<Milestone> getInlineForm()
    {
        quickMilestone = createMilestone();
        CompoundPropertyModel<Milestone> formPropertyModel = new CompoundPropertyModel<Milestone>( quickMilestone );
        Form<Milestone> inlineForm = new Form<Milestone>( "milestoneInlineForm", formPropertyModel )
        {
            @Override
            protected void onSubmit()
            {
                MilestonesDAO dao = new MilestonesDAO();
                boolean alreadyExists = dao.find( quickMilestone.getName(), quickMilestone.getProject() ) != null;
                if ( alreadyExists )
                {
                    warn( "Cannot create milestone. A milestone with that name already exists." );
                    return;
                }
                quickMilestone.setUpdated( new Date() );
                saveMilestone( quickMilestone );
                //TODO: problem with dependency below
                User currentUser = ( (HeadsUpSession) getSession() ).getUser();
//                page.getHeadsUpApplication().addEvent( new CreateMilestoneEvent( quickMilestone, quickMilestone.getProject(), currentUser ) );
                quickMilestone = createMilestone();
            }
        };

        Component[] rowAddComponents = new Component[7];
        rowAddComponents[0] = new WebMarkupContainer( "submit" );
        rowAddComponents[1] = new TextField<NameProjectId>( "name" );
        rowAddComponents[2] = new PercentagePanel( "completeness", 0 );
        rowAddComponents[3] = new Label( "issues", "0" );
        rowAddComponents[4] = new Label( "open", "0" );
        rowAddComponents[5] = new Label( "project", page.getProject().toString() );
        rowAddComponents[5].setVisible( !hideProject );
        DateTimeWithTimeZoneField due = new DateTimeWithTimeZoneField( "due", new Model<Date>()
        {
            @Override
            public void setObject( Date object )
            {
                quickMilestone.setDueDate( object );
            }

            @Override
            public Date getObject()
            {
                return quickMilestone.getDueDate();
            }
        } );
        rowAddComponents[6] = due;

        addAnimatorToForm( rowAddComponents );
        inlineForm.add( rowAdd );
        return inlineForm;
    }

    private void addAnimatorToForm( Component[] rowAddComponents )
    {
        User currentUser = ( (HeadsUpSession) getSession() ).getUser();
        quickAdd = new WebMarkupContainer( "quickAdd" );
        quickAdd.add( new HeadsUpTooltip( "Quick-add a milestone" ) );
        quickAdd.setVisible( Permissions.canEditDoc( currentUser, page.getProject() ) );

        icon = new WebMarkupContainer( "icon" );
        icon.setMarkupId( "icon" + groupReference ).setOutputMarkupId( true );
        Label iconToggleScript = new Label( "iconToggleScript", "function moveIconBackground" + groupReference + "( value ) {" +
                "Wicket.$('icon" + groupReference + "').style.backgroundPosition = '0px ' + ( 0 + ( value * 16 ) ) + 'px';}"
        );

        iconToggleScript.setEscapeModelStrings( false );
        quickAdd.add( iconToggleScript );

        Animator animator = new Animator( "" + groupReference + "Animator" );

        animator.addCssStyleSubject( new MarkupIdModel( rowAdd.setOutputMarkupId( true ) ), "rowhidden", "rowshown" );

        for ( Component rowAddComponent : rowAddComponents )
        {
            rowAdd.add( rowAddComponent );
            if ( rowAddComponent.isVisible() )
            {
                animator.addCssStyleSubject( new MarkupIdModel( rowAddComponent.setOutputMarkupId( true ) ), "hidden", "shown" );
            }
        }

        animator.addSubject( new IAnimatorSubject()
        {
            public String getJavaScript()
            {
                return "moveIconBackground" + groupReference;
            }

        } );

        animator.attachTo( quickAdd, "onclick", Animator.Action.toggle() );
        quickAdd.add( icon );
    }

    private Milestone createMilestone()
    {
        Project project = page.getProject();
        Milestone milestone = new Milestone( "", project );
        milestone.setGroup( group );
        return milestone;
    }

    private void saveMilestone( Milestone milestone )
    {
        Session session = ( (HibernateStorage) Manager.getStorageInstance() ).getHibernateSession();

        Transaction tx = session.beginTransaction();
        session.save( milestone );
        tx.commit();
    }
}
