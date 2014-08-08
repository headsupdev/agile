package org.headsupdev.agile.web.components;

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.model.IModel;

/**
 * Created by Gordon Edwards on 06/08/2014.
 */
public class OnePressButton
        extends Button
{
    public OnePressButton( String id )
    {
        super( id );
    }

    public OnePressButton( String id, IModel<String> model )
    {
        super( id, model );
    }

    @Override
    protected String getOnClickScript()
    {
        return "this.disabled=true; this.form.submit();";
    }
}
