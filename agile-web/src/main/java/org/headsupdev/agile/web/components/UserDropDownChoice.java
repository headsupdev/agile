package org.headsupdev.agile.web.components;

import org.apache.wicket.Session;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.api.User;
import org.headsupdev.agile.web.HeadsUpSession;

import java.util.List;

/**
 * A drop down choice component that displays active users and can include a specific user if not active.
 * Also it can highlight "Myself" at the top of the list
 * <p/>
 * Created: 25/11/2012
 *
 * @author Andrew Williams
 * @since 2.0
 */
public class UserDropDownChoice
    extends DropDownChoice<User>
    implements IChoiceRenderer<User>
{
    public UserDropDownChoice( String id )
    {
        this( id, null );
    }

    public UserDropDownChoice( String id, User includeUser )
    {
        super( id, getUserList( includeUser ) );

        setChoiceRenderer( this );
    }

    private static List<User> getUserList( User includeUser )
    {
        List<User> users;
        if ( includeUser != null )
        {
            users = Manager.getSecurityInstance().getRealUsersIncluding( includeUser );
        }
        else
        {
            users = Manager.getSecurityInstance().getRealUsers();
        }

        User myself = ( (HeadsUpSession) Session.get() ).getUser();
        users.remove( myself );
        users.add( 0, myself );

        return users;
    }

    public Object getDisplayValue( User user )
    {
        if ( user.equals( ( (HeadsUpSession) Session.get() ).getUser() ) )
        {
            return "Myself";
        }

        return user.getFullnameOrUsername();
    }

    public String getIdValue( User user, int i )
    {
        return String.valueOf( i );
    }
}
