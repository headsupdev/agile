package org.headsupdev.agile.framework;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.web.MountPoint;
import org.headsupdev.agile.web.WebManager;

/**
 * A little page designed to be embedded elsewhere...
 * <p/>
 * Copyright Heads Up Development 2012
 * Created: 13/08/2012
 *
 * @author Andrew Williams
 * @since 2.0
 */
@MountPoint( "hosted" )
public class Hosted
    extends WebPage
{
    private String baseUrl;
    private String downloadUrl;

    public Hosted( PageParameters params )
    {
        String projectId = (String) params.get( "project" );
        add( CSSPackageResource.getHeaderContribution( getClass(), "hosted.css" ) );

        baseUrl = Manager.getStorageInstance().getGlobalConfiguration().getBaseUrl() + projectId + "/";
        downloadUrl = Manager.getStorageInstance().getGlobalConfiguration().getProductUrl();

        add( new WebMarkupContainer( "activity" ).add( new AttributeModifier( "href", true, new Model<String>(
                baseUrl + "activity/" ) ) ) );
        add( new WebMarkupContainer( "docs" ).add( new AttributeModifier( "href", true, new Model<String>(
                baseUrl + "docs/" ) ) ) );
        add( new WebMarkupContainer( "issues" ).add( new AttributeModifier( "href", true, new Model<String>(
                baseUrl + "issues/" ) ) ) );
        add( new WebMarkupContainer( "files" ).add( new AttributeModifier( "href", true, new Model<String>(
                baseUrl + "files/" ) ) ) );

        add( new WebMarkupContainer( "download" ).add( new AttributeModifier( "href", true, new Model<String>(
                Manager.getStorageInstance().getGlobalConfiguration().getProductUrl() ) ) ) );

        add( new WebMarkupContainer( "logo" ).add( new AttributeModifier( "src", true,
                new PropertyModel<String>( WebManager.getInstance(), "lozengeLogo" ) ) ) );
        add( new Label( "name", Manager.getStorageInstance().getGlobalConfiguration().getProductName() ) );

    }
}
