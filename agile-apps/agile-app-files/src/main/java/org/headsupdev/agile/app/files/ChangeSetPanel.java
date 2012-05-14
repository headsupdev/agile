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

import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.api.User;
import org.headsupdev.agile.api.service.Change;
import org.headsupdev.agile.api.service.ChangeSet;
import org.headsupdev.agile.storage.ScmChange;
import org.headsupdev.agile.web.HeadsUpSession;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.model.Model;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.PageParameters;
import org.headsupdev.agile.web.components.FormattedDateModel;
import org.headsupdev.agile.web.components.MarkedUpTextModel;
import org.headsupdev.agile.web.RenderUtil;
import org.headsupdev.agile.storage.TransactionalScmChangeSet;
import org.headsupdev.agile.api.Project;

import java.io.File;
import java.util.List;
import java.util.LinkedList;

/**
 * Browse page for viewing changesets
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 *
 */
public class ChangeSetPanel
    extends Panel
{
    public ChangeSetPanel( String id, ChangeSet changeSet, final String stripPrefix )
    {
        super( id );
        if ( changeSet instanceof TransactionalScmChangeSet)
        {
            add( new Label( "revision", ( (TransactionalScmChangeSet) changeSet ).getRevision() ) );
        }
        else
        {
            add( new WebMarkupContainer( "revision" ).setVisible( false ) );
        }

        Label authorLabel = new Label( "author-label", changeSet.getAuthor() );
        Link authorLink;
        User user = Manager.getSecurityInstance().getUserByUsernameEmailOrFullname( changeSet.getAuthor() );
        if ( user != null )
        {
            PageParameters params = new PageParameters();
            params.add( "username", user.getUsername() );
            authorLink = new BookmarkablePageLink( "author-link", RenderUtil.getPageClass( "account" ), params );

            authorLabel = new Label( "author-label", user.getFullnameOrUsername() );
            authorLink.add( authorLabel );
            add( authorLink );
            add( new WebMarkupContainer( "author-label" ).setVisible( false ) );
        }
        else
        {
            add( new WebMarkupContainer( "author-link" ).setVisible( false ) );
            add( authorLabel );
        }

        add( new Label( "date", new FormattedDateModel( changeSet.getDate(),
                ( (HeadsUpSession) getSession() ).getTimeZone() ) ) );
        add( new Label( "comment", new MarkedUpTextModel( changeSet.getComment(), changeSet.getProject() ) )
            .setEscapeModelStrings( false ) );

        List<Change> changes = new LinkedList<Change>( changeSet.getChanges() );
        add( new ListView<Change>( "files", changes ) {
            protected void populateItem( ListItem<Change> listItem )
            {
                final ScmChange file = (ScmChange) listItem.getModelObject();

                if ( !file.getName().startsWith( stripPrefix ) )
                {
                    listItem.setVisible( false );
                    return;
                }

                WebMarkupContainer type = new WebMarkupContainer( "type" );
                type.add( new AttributeModifier( "class", new Model<String>() {
                    public String getObject()
                    {
                        return "type type" + file.getType();
                    }
                } ) );
                listItem.add( type );

                ExternalLink link = new ExternalLink( "link", "#" +
                    file.getName().substring( stripPrefix.length() ).replace( File.separatorChar, ':' ) );
                link.add( new Label( "name", file.getName().substring( stripPrefix.length() ) ) );
                listItem.add( link );

                if ( file.getRevision() == null )
                {
                    listItem.add( new WebMarkupContainer( "revision" ).setVisible( false ) );
                }
                else
                {
                    listItem.add( new Label( "revision", file.getRevision() ) );
                }
            }
        });

        DiffModel model = new DiffModel( changeSet, stripPrefix );
        add( new Label( "diffs", model ).setEscapeModelStrings( false ) );

        int othersCount = model.getHiddenCount();
        if ( othersCount > 0 )
        {
            Project parent = changeSet.getProject();
            while ( parent.getParent() != null )
            {
                parent = parent.getParent();
            }
            PageParameters params = new PageParameters();
            params.add( "project", parent.getId() );
            params.add( "id", changeSet.getId() );
            BookmarkablePageLink others = new BookmarkablePageLink( "others-link",
                RenderUtil.getPageClass( "files/change" ), params );
            others.add( new Label( "othercount", String.valueOf( othersCount ) ) );
            add( others );
        }
        else
        {
            add( new WebMarkupContainer( "others-link" ).setVisible( false ) );
        }
    }
}

