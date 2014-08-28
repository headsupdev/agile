package org.headsupdev.agile.storage;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

/**
 * Interface that Issues, Milestones and Documents extend as they can all be commented on
 *
 * Created: 27/08/2014
 *
 * @author Gordon Edwards
 * @since 2.1
 */
public interface CommentableEntity
{
    public Set<Comment> getComments();
    public void setUpdated( Date date );
}
