

package org.headsupdev.agile.web.components;

import org.wicketstuff.jwicket.tooltip.WalterZornTips;

/**
 * Custom WalterZorn tooltip
 *
 * @author Gordon Edwards
 * @version $Id$
 * @since 2.1
 */
public class HeadsUpTooltip extends WalterZornTips
{
    public HeadsUpTooltip( String tooltipText )
    {
        super( tooltipText );
        setBgColor( "#E6E5BA" );
        setDelay( 0 );
        setShadow( true );
        setShadowWidth( 2 );
        setJumphorz( true );
    }
}
