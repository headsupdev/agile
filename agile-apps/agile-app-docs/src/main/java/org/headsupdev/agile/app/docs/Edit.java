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

package org.headsupdev.agile.app.docs;

import org.apache.wicket.PageParameters;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.headsupdev.agile.api.Permission;
import org.headsupdev.agile.api.User;
import org.headsupdev.agile.app.docs.event.CreateDocumentEvent;
import org.headsupdev.agile.app.docs.event.UpdateDocumentEvent;
import org.headsupdev.agile.app.docs.permission.DocEditPermission;
import org.headsupdev.agile.storage.HibernateStorage;
import org.headsupdev.agile.storage.docs.Document;
import org.headsupdev.agile.web.BookmarkableMenuLink;
import org.headsupdev.agile.web.HeadsUpPage;
import org.headsupdev.agile.web.HeadsUpSession;
import org.headsupdev.agile.web.MountPoint;
import org.headsupdev.agile.web.components.NamePatternValidator;
import org.headsupdev.agile.web.components.OnePressSubmitButton;
import org.headsupdev.agile.web.components.history.HistoryPanel;
import org.headsupdev.support.java.StringUtil;
import wicket.contrib.tinymce.TinyMceBehavior;
import wicket.contrib.tinymce.settings.TinyMCESettings;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Date;
import java.util.StringTokenizer;

/**
 * Documents edit page - edit the content of a page and show a preview if requested
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
@MountPoint("edit")
public class Edit
        extends HeadsUpPage
{
    private String name, content;
    boolean canEditTitle = false;

    public Permission getRequiredPermission()
    {
        return new DocEditPermission();
    }

    public void layout()
    {
        super.layout();
        add( CSSPackageResource.getHeaderContribution( getClass(), "doc.css" ) );

        name = getPageParameters().getString( "page" );
        if ( name == null || name.length() == 0 )
        {
            canEditTitle = true;
        }
        else if ( !NamePatternValidator.isValidName( name ) )
        {
            userError( "Invalid document name" );
            return;
        }

        addLink( new BookmarkableMenuLink( getPageClass( "docs/" ), getPageParameters(), "view" ) );

        Document doc = DocsApplication.getDocument( name, getProject() );
        boolean create = false;

        if ( doc == null )
        {

            doc = new Document( name, getProject() );
            String sanitisedName;
            if ( StringUtil.isEmpty( name ) )
            {
                sanitisedName = "Untitled";
            }
            else
            {
                sanitisedName = name;
            }
            doc.setContent( "<html><body><h1>" + sanitisedName + "</h1><p>This is a new page</p></body></html>" );
            create = true;
        }
        content = doc.getContent();

        add( new WebMarkupContainer( "error" ).setVisible( false ) );
        add( new EditForm( "edit", doc, create ) );
    }

    @Override
    public String getTitle()
    {
        return null;
    }

    protected String getVerb()
    {
        return "Edit";
    }

    class EditForm
            extends Form<Document>
    {
        private Document doc;
        private boolean create;
        private boolean save = true;

        public EditForm( String id, Document d, boolean create )
        {
            super( id );
            this.doc = d;
            this.create = create;

            setModel( new CompoundPropertyModel( Edit.this ) );
            add( new Label( "label", doc.getName() ).setVisible( !canEditTitle ) );
            add( new TextField<String>( "name" ).add( new NamePatternValidator() ).setRequired( true ).setVisible( canEditTitle ) );

            add( new Label( "verb", getVerb() ) );
            final TextArea contentArea = new TextArea<String>( "content" )
            {
                protected boolean shouldTrimInput()
                {
                    return false;
                }
            };
            add( contentArea.setOutputMarkupId( true ) );
            add( new OnePressSubmitButton( "submitDoc" ) );

            TinyMCESettings settings = new TinyMCESettings( TinyMCESettings.Theme.advanced );
            settings.setToolbarAlign( TinyMCESettings.Align.left );
            settings.setToolbarLocation( TinyMCESettings.Location.top );
            settings.setStatusbarLocation( TinyMCESettings.Location.bottom );
            settings.setContentCss( new ResourceReference( HeadsUpPage.class, "common.css" ) );
            settings.setRelativeUrls( false );
            settings.setResizing( true );

            // specify this manually as the API does not allow for the list of css contributions
            settings.addCustomSetting( "content_css : \"" +
                    urlFor( new ResourceReference( getClass(), "doc.css" ) ) + "," +
                    urlFor( new ResourceReference( HeadsUpPage.class, "common.css" ) ) + "\"" );

            settings.addCustomSetting( "plugins : \"safari,spellchecker,iespell,style,pagebreak,table,advimage,advlink,inlinepopups,insertdatetime,media,searchreplace,print,contextmenu,fullscreen,noneditable,visualchars,nonbreaking,xhtmlxtras\"" );
            settings.addCustomSetting( "theme_advanced_buttons1 : \"bold,italic,underline,|,justifyleft,justifycenter,justifyright,justifyfull,|,forecolor,backcolor,formatselect,fontselect,visualchars,|,fullscreen,help\"" );
            settings.addCustomSetting( "theme_advanced_buttons2 : \"search,replace,|,bullist,numlist,|,outdent,indent,blockquote,|,undo,redo,|,link,unlink,anchor,image,styleprops,code,|,insertdate,inserttime,spellchecker,iespell\"" );
            settings.addCustomSetting( "theme_advanced_buttons3 : \"tablecontrols,|,hr,removeformat,visualaid,|,sub,sup,|,charmap,print\"" );
            settings.addCustomSetting( "external_image_list_url : \"/" + getProject().getId() +
                    "/docs/imagelist/page/" + d.getName() + "/\"" );
            settings.addCustomSetting( "external_link_list_url : \"/" + getProject().getId() +
                    "/docs/linklist/page/" + d.getName() + "/\"" );
            settings.addCustomSetting( "entity_encoding : 'named+numeric'" );
            contentArea.add( new TinyMceBehavior( settings ) );
        }

        protected void onSubmit()
        {
            if ( !save )
            {
                save = true;
                return;
            }

            User user = ( (HeadsUpSession) getSession() ).getUser();
            if ( create )
            {
                doc = new Document( name, doc.getProject() );
                boolean alreadyExists = DocsApplication.getDocument( doc.getName(), doc.getProject() ) != null;
                if ( alreadyExists )
                {
                    warn( "Cannot create document. A document with that name already exists." );
                    return;
                }

            }
            else
            {
                doc = (Document) ( (HibernateStorage) getStorage() ).getHibernateSession().merge( doc );
            }
            doc.setContent( content );

            if ( create )
            {
                doc.setCreator( user );
                doc.getWatchers().add( user );
                doc.setCreated( new Date() );

                ( (DocsApplication) getHeadsUpApplication() ).addDocument( doc );

                getHeadsUpApplication().addEvent( new CreateDocumentEvent( doc, getContentSummary( doc ) ) );
            }
            else
            {
                doc.setUpdated( new Date() );
                getHeadsUpApplication().addEvent( new UpdateDocumentEvent( doc, user, getContentSummary( doc ) ) );
            }

            PageParameters params = getProjectPageParameters();
            params.add( "page", name );
            setResponsePage( getPageClass( "docs/" ), params );
        }
    }

    static String getContentSummary( Document doc )
    {
        return getContentSummary( doc, HistoryPanel.SUMMARY_LENGTH + 10 );
    }

    static String getContentSummary( Document doc, final int length )
    {
        String complete = doc.getContent();

        try
        {
            final StringWriter text = new StringWriter();

            HTMLEditorKit.ParserCallback callback = new HTMLEditorKit.ParserCallback()
            {
                int chars = 0;
                boolean full = false;
                String currentTag;

                public void handleStartTag( HTML.Tag t, MutableAttributeSet a, int pos )
                {
                    currentTag = t.toString();
                }

                public void handleText( char[] data, int pos )
                {
                    StringTokenizer tokens = new StringTokenizer( new String( data ), " ", true );
                    while ( tokens.hasMoreElements() && !full )
                    {

                        String token = (String) tokens.nextElement();
                        /* weird that the parser should return these brackets... */
                        if ( token.startsWith( ">" ) )
                        {
                            if ( token.length() == 1 )
                            {
                                continue;
                            }
                            else
                            {
                                token = token.substring( 1 );
                            }
                        }
                        if ( chars > length )
                        {
                            text.write( "..." );
                            full = true;
                            return;
                        }

                        text.write( token );
                        chars += token.length();

                        text.write( ' ' );
                        chars++;
                    }
                }
            };

            new ParserDelegator().parse( new StringReader( complete ), callback, false );

            return text.toString();
        }
        catch ( IOException e )
        {
            return "(unable to parse document)";
        }
    }
}