/*
 * HeadsUp Agile
 * Copyright 2009-2014 Heads Up Development Ltd.
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

package org.headsupdev.agile.web;

import org.apache.wicket.markup.html.CSSPackageResource;
import org.headsupdev.agile.api.logging.Logger;
import org.headsupdev.agile.web.auth.WebLoginManager;
import org.headsupdev.agile.web.components.*;
import org.headsupdev.agile.web.dialogs.LoginDialog;
import org.headsupdev.agile.web.dialogs.LogoutDialog;
import org.apache.wicket.*;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.util.time.Duration;
import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.headsupdev.agile.api.*;
import org.headsupdev.agile.api.SecurityManager;
import org.headsupdev.agile.api.Application;
import org.headsupdev.agile.api.Page;
import org.headsupdev.agile.security.permission.ProjectListPermission;
import org.headsupdev.agile.storage.StoredProject;
import org.headsupdev.agile.core.PrivateConfiguration;
import org.wicketstuff.animator.Animator;
import org.wicketstuff.animator.IAnimatorSubject;
import org.wicketstuff.animator.MarkupIdModel;

import java.util.*;
import java.util.regex.Pattern;
import java.io.Serializable;

/**
 * The parent to all HeadsUp styled pages
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public abstract class HeadsUpPage
    extends Page
    implements Serializable
{
    public static final Pattern ID_PATTERN = Pattern.compile( "[a-zA-Z0-9-_\\.]*" );

    protected static final String DIALOG_PANEL_ID = "agile-dialog";
    protected static final String PAGE_TITLE_SEPARATOR = " :: ";

    private Application application;
    private Project project;
    private List<Link> links = new LinkedList<Link>();

    private PageParameters parameters;
    private WebMarkupContainer dialog, submenu;
    private String searchQuery;
    private ArrayList<MenuLink> menuLinks = new ArrayList<MenuLink>();
    private boolean linksRendered = false;

    private Logger log = Manager.getLogger( getClass().getName() );

    public HeadsUpPage()
    {
        parameters = new PageParameters();
    }

    public PageParameters getPageParameters()
    {
        return parameters;
    }

    public void setPageParameters( PageParameters parameters )
    {
        this.parameters = parameters;
    }

    protected void setHeaders( WebResponse response )
    {
        response.setHeader( "Pragma", "no-cache" );
        response.setHeader( "Cache-Control", "no-store" );
    }

    public void layout()
    {
        WebManager.getInstance().checkPermissions( this );

        User user = WebLoginManager.getInstance().getLoggedInUser( ( (WebRequest) getRequest() ).getHttpServletRequest() );
        if ( user == null )
        {
            user = HeadsUpSession.ANONYMOUS_USER;
        }

        /* security check */
        if ( getRequiredPermission() != null )
        {
            requirePermission( getRequiredPermission() );
        }

        if ( !isErrorPage() )
        {
            getSession().setProject( getProject() );
        }

        for ( MenuLink link : application.getLinks( getProject() ) )
        {
            addLink( link );
        }

        add( CSSPackageResource.getHeaderContribution( HeadsUpPage.class, "agile.css" ) );
        add( CSSPackageResource.getHeaderContribution( HeadsUpPage.class, "mobile.css", "handheld, only screen and (max-width: 767px)" ) );

        add( new WebMarkupContainer( "headerlogo" ).add( new AttributeModifier( "src", true,
            new PropertyModel<String>( WebManager.getInstance(), "headerLogo" ) ) ) );

        WebMarkupContainer mainmenu = new WebMarkupContainer( "mainmenu-container" );
        mainmenu.setMarkupId( "mainmenu" );
        mainmenu.add( new ListView<Application>( "mainmenu",
                ApplicationPageMapper.get().getApplications( getSession().getUser() ) ) {
            protected void populateItem( ListItem<Application> listItem )
            {
                Project project = getProject();
                final Application app = listItem.getModelObject();
                if ( "home".equals( app.getApplicationId() ) )
                {
                    listItem.setVisible( false );
                    return;
                }

                String link = "/" + project.getId() + "/" + app.getApplicationId() + "/";
                if ( ApplicationPageMapper.isHomeApp( app ) ) {
                    if ( project.equals( StoredProject.getDefault() ) )
                    {
                        link  = "/";
                    }
                    else
                    {
                        link = "/" + project.getId() + "/show/";
                    }
                }
                ExternalLink applink = new ExternalLink( "mainmenu-link", link );
                applink.add( new Label( "mainmenu-label", app.getName() ) );
                listItem.add( applink );

                listItem.add( new AttributeModifier( "class", new Model<String>()
                {
                    public String getObject() {
                        if ( app.equals( getHeadsUpApplication() ) )
                        {
                            return "mainmenu-item-selected";
                        }
                        else
                        {
                            return "mainmenu-item";
                        }
                    }
                } ) );
            }
        } );

        addAnimatedSelect( "mainlink", getHeadsUpApplication().getName() + "   \u25bc", mainmenu );
        add( mainmenu );

        submenu = new WebMarkupContainer( "submenu-container" );
        submenu.setMarkupId( "submenu" );
        addAnimatedSelect( "sublink", "\u2699", submenu );
        add( submenu );

        submenu.add( new ListView<Link>( "submenu", links ) {
            protected void populateItem( ListItem<Link> listItem )
            {
                Link link = listItem.getModelObject();
                listItem.add( link );

                listItem.add( new AttributeModifier( "class", new Model<String>()
                {
                    public String getObject() {
                        if ( getClass().equals( /* TODO find class of link */ null ) )
                        {
                            return "submenu-item-selected";
                        }
                        else
                        {
                            return "submenu-item";
                        }
                    }
                } ) );
            }
        } );

        WebMarkupContainer projectmenu = createProjectMenu( user );
        add( projectmenu );

        if ( !getClass().getName().endsWith( "Login" ) && !getClass().getName().endsWith( "Logout" ) )
        {
            getSession().setPreviousPageClass( getClass() );
            getSession().setPreviousPageParameters( getPageParameters() );
        }
        WebMarkupContainer userpanel = new WebMarkupContainer( "userpanel" );
        userpanel.setOutputMarkupId( true );
        userpanel.setMarkupId( "userpanel" );
        if ( !PrivateConfiguration.isInstalled() || user.equals( HeadsUpSession.ANONYMOUS_USER ) )
        {
            userpanel.add( new Label( "username", "you are not logged in" ) );
            userpanel.add( new WebMarkupContainer( "account" ).setVisible( false ) );

            Link login = new AjaxFallbackLink( "login-link" )
            {
                @Override
                public void onClick( AjaxRequestTarget target )
                {
                    if ( target == null )
                    {
                        setResponsePage( getPageClass( "login" ), getProjectPageParameters() );
                    }
                    else
                    {
                        target.appendJavascript("document.getElementById('userpanelbutton').childNodes[1].click()");
                        showDialog( new LoginDialog( DIALOG_PANEL_ID, true, HeadsUpPage.this ), target );
                    }
                }
            };
            login.add( new Label( "login-label", "login" ) );
            userpanel.add( login );
        }
        else
        {
            userpanel.add( new Label( "username", "logged in as " + user.getFullnameOrUsername() ) );
            Class<? extends Page> userLink = getPageClass( "account" );
            if ( userLink != null )
            {
                userpanel.add( new BookmarkablePageLink( "account", userLink, getProjectPageParameters() ) );
            }
            else
            {
                userpanel.add( new WebMarkupContainer( "account" ).setVisible( false ) );
            }

            Link login = new AjaxFallbackLink( "login-link" )
            {
                @Override
                public void onClick( AjaxRequestTarget target )
                {
                    if ( target == null )
                    {
                        setResponsePage( getPageClass( "logout" ), getProjectPageParameters() );
                    }
                    else
                    {
                        target.appendJavascript("document.getElementById('userpanelbutton').childNodes[1].click()");
                        showDialog( new LogoutDialog( DIALOG_PANEL_ID, true, HeadsUpPage.this ), target );
                    }
                }
            };
            login.add( new Label( "login-label", "logout" ) );
            userpanel.add( login );
        }

        if ( ApplicationPageMapper.get().getSearchApp() != null )
        {
            userpanel.add( new BookmarkablePageLink( "search", getPageClass( "search" ), getProjectPageParameters() ) );
        }
        else
        {
            userpanel.add( new WebMarkupContainer( "search" ).setVisible( false ) );
        }
        if ( ApplicationPageMapper.get().getSupportApp() != null )
        {
            userpanel.add( new BookmarkablePageLink( "support", ApplicationPageMapper.get().getSupportApp().getHomePage(),
                    getProjectPageParameters() ) );
        }
        else
        {
            userpanel.add( new WebMarkupContainer( "support" ).setVisible( false ) );
        }

        WebMarkupContainer userpanelButton = new WebMarkupContainer( "userpanelbutton" );
        userpanelButton.add( new Label( "label", "\u25bc" ) );
        add( userpanelButton );

        Animator animator = new Animator();
        animator.addCssStyleSubject( new MarkupIdModel( userpanel ), "up", "down" );
        animator.attachTo( userpanelButton, "onclick", Animator.Action.toggle() );

        boolean showUserTools = getSession().getUser() != null && !getSession().getUser().equals( HeadsUpSession.ANONYMOUS_USER );
        if ( showUserTools )
        {
            int userIssues = AccountSummaryPanel.getIssuesAssignedTo( getSession().getUser() ).size();

            WebMarkupContainer userDashbutton = new WebMarkupContainer( "userdashbutton" );
            userpanel.add( userDashbutton.setVisible( userIssues > 0 ) );

            Label totals = new Label( "totals", String.valueOf( userIssues ) );
            totals.add( new AttributeModifier( "class", true, new Model<String>()
            {
                @Override
                public String getObject()
                {
                    if ( AccountSummaryPanel.userHasOverdueMilestones( getSession().getUser() ) )
                    {
                        return "totals overdue";
                    }

                    if ( AccountSummaryPanel.userHasDueSoonMilestones(getSession().getUser()) )
                    {
                        return "totals duesoon";
                    }

                    return "totals";
                }
            } ) );
            userDashbutton.add( totals );

            UserDashboard dash = new UserDashboard( "userdashboard", this );
            dash.setOutputMarkupId( true );
            dash.setMarkupId( "userdashboard" );
            add( dash );
            WebMarkupContainer dashBack = new WebMarkupContainer( "userdashboardbackground" );
            dashBack.setOutputMarkupId( true );
            dashBack.setMarkupId( "userdashboardbackground" );
            add( dashBack );

            animator = new Animator();
            animator.withEaseInOutTransition();
            animator.addCssStyleSubject( new MarkupIdModel( dash ), "up", "down" );
            animator.addCssStyleSubject( new MarkupIdModel( dashBack ), "up", "down" );
            animator.addSubject( new IAnimatorSubject() {
                public String getJavaScript() {
                    return "function showBackground() {" +
                            "   var background = Wicket.$('userdashboardbackground');" +
                            "   if (userdashbuttonAnimator.state > 0) {" +
                            "       background.style.display=\"block\";" +
                            "       document.body.style.overflow=\"hidden\";" +
                            "   } else {" +
                            "       background.style.display=\"none\";" +
                            "       document.body.style.overflow=\"auto\";" +
                            "   }" +
                            "}";
                }
            } );
            animator.attachTo( userDashbutton, "onclick", Animator.Action.toggle() );
        }
        else
        {
            WebMarkupContainer userDashbutton = new WebMarkupContainer( "userdashbutton" );
            userpanel.add( userDashbutton.setVisible( false ) );

            add( new WebMarkupContainer( "userdashboard" ).setVisible( false ) );
            add( new WebMarkupContainer( "userdashboardbackground" ).setVisible( false ) );
        }
        add( userpanel );

        Form form = new Form( "quicksearch" ) {

            protected void onSubmit() {
                if ( searchQuery == null )
                {
                    return;
                }

                super.onSubmit();

                String quickLink = getQuickSearchResponse( searchQuery );
                if ( quickLink != null )
                {
                    getRequestCycle().getResponse().redirect( quickLink );

                    throw new RedirectToUrlException( quickLink );
                }

                PageParameters params = getProjectPageParameters();
                params.add( "query", searchQuery );
                setResponsePage( getPageClass( "search" ), params );
            }
        };
        form.add( new OnePressButton( "searchSubmit" ) );
        add( form.setVisible( ApplicationPageMapper.get().getSearchApp() != null ) );
        form.add( new TextField<String>( "query", new PropertyModel<String>( this, "searchQuery" ) ) );

        addAnimatedSelect( "projectlink", getProject().getAlias() + "   \u25bc", projectmenu );

        WebMarkupContainer taskpanel = new WebMarkupContainer( "taskpanel" );
        Link taskLink = new BookmarkablePageLink( "tasklink", getPageClass( "tasks" ), getProjectPageParameters() );
        taskpanel.add( taskLink );
        final Image spinner = new Image( "spinner", new ResourceReference( HeadsUpPage.class, "images/ajax-loader.gif" ) );
        spinner.add( new AttributeModifier( "style", new Model<String>()
        {
            public String getObject()
            {
                if ( getManager().getTasks() == null || getManager().getTasks().size() == 0 )
                {
                    return "display:none";
                }

                return "";
            }
        } ) );
        spinner.setOutputMarkupId( true );
        spinner.add( new AbstractAjaxTimerBehavior( Duration.seconds( 10 ) ) {
            {
                onlyTargetActivePage();
            }

            protected void onTimer( AjaxRequestTarget target )
            {
                target.addComponent( spinner );
            }
        });
        taskLink.add( spinner );
        add( taskpanel );

//        add( new Label( "producttitle", HeadsUpConfiguration.getProductName() ) );
        add( new Label( "title", new PropertyModel( this, "title" ) )
        {
            @Override
            protected void onBeforeRender()
            {
                super.onBeforeRender();
                setVisible( getTitle() != null );
            }
        });
        add( new Label( "pagetitle", new PropertyModel( this, "pageTitle" ) ) );
        add( new FeedbackPanel( "messages" ) );

        add( new Label( "footer-description", WebManager.getInstance().getFooterDescriptionHTML( getSession().getTimeZone() ) ).setEscapeModelStrings( false ) );
        add( new Label( "footer-copyright", WebManager.getInstance().getFooterCopyrightHTML() ).setEscapeModelStrings( false ) );

        String noteString = WebManager.getInstance().getFooterNoteHTML();
        add( new Label( "footer-note", noteString ).setVisible( noteString != null ) );

        add( new BookmarkablePageLink( "footer-update", getPageClass( "updates" ) )
                .setVisible(getManager().isUpdateAvailable() ) );

        dialog = new WebMarkupContainer( DIALOG_PANEL_ID );
        dialog.setVisible( false );
        dialog.setOutputMarkupId( true );
        dialog.setOutputMarkupPlaceholderTag( true );
        add( dialog );
    }

    public String getTitle()
    {
        return null;
    }

    public String getPageTitle()
    {
        String appProductTitle = getHeadsUpApplication().getName() + PAGE_TITLE_SEPARATOR +
                getStorage().getGlobalConfiguration().getProductName();

        if ( getTitle() == null || getTitle().trim().length() == 0 )
        {
            return appProductTitle;
        }

        return getTitle() + PAGE_TITLE_SEPARATOR + appProductTitle;
    }

    @Override
    protected void onBeforeRender()
    {
        renderMenuLinks();
        super.onBeforeRender();
    }

    public void addLink( MenuLink menuLink )
    {
        menuLinks.add( menuLink );
    }

    private void renderMenuLinks()
    {
        if ( linksRendered )
        {
            return;
        }
        linksRendered = true;

        for ( MenuLink link : menuLinks )
        {
            Link convertedLink = convertMenuLinkToLink( link );
            links.add( convertedLink );
        }
    }

    /**
     *
     * @param menuLink if successful returns true
     * @return
     */
    public boolean removeLink( MenuLink menuLink )
    {
        // if we have rendered, we can't remove
        if ( linksRendered )
        {
            return false;
        }

        return menuLinks.remove( menuLink );
    }

    private Link convertMenuLinkToLink( MenuLink menuLink )
    {
        Link link;
        if ( menuLink instanceof SimpleMenuLink )
        {
            String prefix = "";
            if ( !getHeadsUpApplication().getApplicationId().equals( "home" ) )
            {
                prefix = getHeadsUpApplication().getApplicationId() + "/";
            }

            menuLink = new BookmarkableMenuLink( getPageClass( prefix + ( (SimpleMenuLink) menuLink ).getTarget() ),
                    getProjectPageParameters(), menuLink.getLabel() );
        }

        if ( menuLink instanceof BookmarkableMenuLink )
        {
            link = ( (BookmarkableMenuLink) menuLink ).getLink();
        }
        else if ( menuLink instanceof Link )
        {
            link = (Link) menuLink;
        }
        else
        {
            final MenuLink oldLink = menuLink;

            link = new DynamicMenuLink( oldLink.getLabel() )
            {
                public void onClick()
                {
                    oldLink.onClick();
                }
            };
        }

        final MenuLink finalLink = menuLink;
        link.add( new Label( "submenu-label", new Model<String>()
        {
            @Override
            public String getObject()
            {
                return finalLink.getLabel();
            }
        } ).setEscapeModelStrings( false ) );

        return link;
    }

    public void addLinks( List<MenuLink> links )
    {
        for ( MenuLink link : links )
        {
            addLink( link );
        }
    }

    protected String getPageHint()
    {
        if ( ApplicationPageMapper.isHomeApp( getHeadsUpApplication() ) )
        {
            if ( ApplicationPageMapper.get().getApplication( "dashboard" ) != null )
            {
                return "show";
            }
            else
            {
                return "";
            }
        }

        return getHeadsUpApplication().getApplicationId();
    }

    protected WebMarkupContainer createProjectMenu( User user )
    {
        String pageHint = getPageHint();
        WebMarkupContainer projectmenu = new WebMarkupContainer( "projectmenu" );
        projectmenu.setOutputMarkupId( true );
        projectmenu.setMarkupId( "projectmenu" );

        List<Project> recent = getStorage().getRecentRootProjects( user );
        recent = getProjectsPermitted( recent );
        projectmenu.add( new ProjectListPanel( "recent-project-tree", recent, getPageClass( pageHint ), getProject() )
                .setVisible( userHasPermission( user, new ProjectListPermission(), null ) && recent.size() > 0 ) );

        List<Project> active = new ArrayList<Project>( getStorage().getActiveRootProjects() );
        active.removeAll( recent );
        active = getProjectsPermitted( active );
        projectmenu.add( new ProjectListPanel( "active-project-tree", active, getPageClass( pageHint ), getProject() )
                .setVisible( userHasPermission( user, new ProjectListPermission(), null ) && active.size() > 0 ) );

        List<Project> other = new ArrayList<Project>( getStorage().getRootProjects() );
        other.removeAll( recent );
        other.removeAll( active );
        other = getProjectsPermitted( other );
        projectmenu.add( new ProjectListPanel( "other-project-tree", other, getPageClass( pageHint ), getProject() )
                .setVisible( userHasPermission( user, new ProjectListPermission(), null ) && other.size() > 0 ) );


        WebMarkupContainer noProjects = new WebMarkupContainer( "noprojects" );
        noProjects.setVisible( getStorage().getProjects().size() == 0 );
        projectmenu.add( noProjects );

        PageParameters params = new PageParameters();
        params.add( "project", Project.ALL_PROJECT_ID );
        BookmarkablePageLink allProjects = new BookmarkablePageLink( "allprojects-link", getPageClass( pageHint ), params );
        allProjects.add( new AttributeModifier( "class", new Model<String>() {
            public String getObject()
            {
                if ( getProject().equals( StoredProject.getDefault() ) )
                {
                    return "selected";
                }

                return "";
            }
        } ) );
        allProjects.setVisible( getStorage().getProjects().size() > 0 );
        projectmenu.add( allProjects );

        return projectmenu;
    }

    protected void addAnimatedSelect( String id, String title, WebMarkupContainer list )
    {
        WebMarkupContainer select = new WebMarkupContainer( id );
        select.add( new Label( "title", title ) );
        add( select );

        Animator animator = new Animator();
        animator.addCssStyleSubject( new MarkupIdModel( list ), "up", "down" );
        animator.attachTo( select, "onclick", Animator.Action.toggle() );
    }

    public String getQuickSearchResponse( String search )
    {
        if ( searchQuery == null )
        {
            return null;
        }
        String returnURL = null;

        Map<String, LinkProvider> providers = Manager.getInstance().getLinkProviders();
        for ( LinkProvider provider : providers.values() )
        {
            if ( !provider.supportsQuickLink() )
            {
                continue;
            }

            if ( provider.isLinkBroken( search, getProject() ) )
            {
                continue;
            }

            if ( returnURL != null )
            {
                log.debug( "QuickSearch also matched \"" + searchQuery + "\" in " + provider.getId() + ", running search" );
                return null;
            }

            log.debug( "QuickSearch matched \"" + searchQuery + "\" in " + provider.getId() );
            returnURL = provider.getLink( search, getProject() );
        }

        if ( returnURL == null && searchQuery.contains( ":" ) )
        {
            if ( !MarkedUpTextModel.isLinkBroken( searchQuery, getProject(), providers ) )
            {
                returnURL = MarkedUpTextModel.getLink( searchQuery, getProject(), providers );
            }
        }
        return returnURL;
    }

    public void userError( String reason )
    {
        getSession().error( reason );

        PageParameters userError = new PageParameters();
        userError.add( "userError", "true" );
        userError.add( "reason", reason );
        setResponsePage( getPageClass( "error" ), userError );
    }

    public void notFoundError()
    {
        PageParameters page = new PageParameters();
        page.add( "uri", getRequest().getPath() );
        setResponsePage( getPageClass( "filenotfound" ), page );
    }

    public org.apache.wicket.Page getPage( String path )
    {
        Class<? extends Page> page = ApplicationPageMapper.get().getPageClass( path );

        return getApplication().getSessionSettings().getPageFactory().newPage( page );
    }

    public org.apache.wicket.Page getPage( String path, PageParameters params )
    {
        Class<? extends Page> page = ApplicationPageMapper.get().getPageClass( path );

        return getApplication().getSessionSettings().getPageFactory().newPage( page, params );
    }

    public Class<? extends Page> getPageClass( String path )
    {
        return ApplicationPageMapper.get().getPageClass( path );
    }

    public Manager getManager()
    {
        return Manager.getInstance();
    }

    public SecurityManager getSecurityManager()
    {
        return Manager.getSecurityInstance();
    }

    public Storage getStorage()
    {
        return Manager.getStorageInstance();
    }

    public Application getHeadsUpApplication()
    {
        return application;
    }

    public void setApplication( Application application )
    {
        this.application = application;
    }

    public Project getProject()
    {
        if ( project != null )
        {
            return project;
        }

        String projectId = getPageParameters().getString( "project" );

        if ( projectId != null && projectId.length() > 0 && !projectId.equals( StoredProject.ALL_PROJECT_ID ) )
        {
            project = getStorage().getProject( projectId );
        }

        if ( project == null )
        {
            project = StoredProject.getDefault();
        }
        return project;
    }

    public HeadsUpSession getSession()
    {
        return (HeadsUpSession) super.getSession();
    }

    public PageParameters getProjectPageParameters()
    {
        PageParameters params = new PageParameters();
        params.add( "project", getProject().getId() );

        return params;
    }

    public abstract Permission getRequiredPermission();

    public void requirePermission( Permission permission )
    {
        User user = getSession().getUser();

        if ( !userHasPermission( user, permission, getProject() ) )
        {
            if ( user == null || user.equals( HeadsUpSession.ANONYMOUS_USER ) )
            {
                throw new RestartResponseAtInterceptPageException( getPageClass( "login" ) );
            }
            else
            {
                throw new RestartResponseException( getPageClass( "permissiondenied" ) );
            }
        }
    }

    public boolean userHasPermission( User user, Permission permission, Project project )
    {
        return PrivateConfiguration.isInstalled() && getSecurityManager().userHasPermission( user, permission, project );
    }

    public void showDialog( Panel panel, AjaxRequestTarget target )
    {
        if ( !panel.equals( dialog ) )
        {
            panel.setOutputMarkupId( true );
            panel.setOutputMarkupPlaceholderTag( true );
            dialog.replaceWith( panel );

            dialog = panel;
        }

        panel.setVisible( true );

        if ( target != null )
        {
            target.addComponent( panel );
        }
    }

    protected List<Project> getProjectsPermitted( List<Project> projects )
    {
        List<Project> permitted = new ArrayList<Project>( projects.size() );
        for ( Project project : projects )
        {
            if ( getSecurityManager().userCanListProject( getSession().getUser(), project ) )
            {
                permitted.add( project );
            }
        }

        return permitted;
    }
}
