/*
 * HeadsUp Agile
 * Copyright 2014 Heads Up Development.
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

package org.headsupdev.agile.web.dialogs;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.headsupdev.agile.web.HeadsUpSession;
import org.headsupdev.agile.web.RenderUtil;

/**
 * A dialog that lets the user cancel the submission of a link or button
 *
 * @author Gordon Edwards
 * @version $Id$
 * @since 2.1
 */
public class ConfirmDialog
        extends Panel
{
    private boolean popup;

    public ConfirmDialog( String id, String title, String message )
    {
        this( id, title, message, true );
    }

    protected ConfirmDialog( String id, String title, String message, boolean isDialog )
    {
        super( id );
        popup = isDialog;
        add( new Label( "title", title ) );
        add( new Label( "message", message ) );
        Form form = new Form( "dialogform" );

        form.add( new Button( "yes" )
        {
            @Override
            public void onSubmit()
            {
                onDialogConfirmed();

                Class previous = ( (HeadsUpSession) getSession() ).getPreviousPageClass();
                if ( previous != null )
                {
                    setResponsePage( previous, ( (HeadsUpSession) getSession() ).getPreviousPageParameters() );
                }
                else
                {
                    setResponsePage( RenderUtil.getPageClass( "" ) );
                }
            }

        }.setDefaultFormProcessing( false ) );
        form.add( new AjaxFallbackButton( "no", form )
        {
            @Override
            protected void onSubmit( AjaxRequestTarget target, Form form )
            {
                if ( target == null || !popup )
                {
                    Class previous = ( (HeadsUpSession) getSession() ).getPreviousPageClass();
                    if ( previous != null )
                    {
                        setResponsePage( previous, ( (HeadsUpSession) getSession() ).getPreviousPageParameters() );
                    }
                    else
                    {
                        setResponsePage( RenderUtil.getPageClass( "" ) );
                    }
                }
                else
                {
                    ConfirmDialog.this.setVisible( false );
                    target.addComponent( ConfirmDialog.this );
                }
            }
        }.setDefaultFormProcessing( false ) );
        add( form );
    }

    public void onDialogConfirmed()
    {
    }
}
