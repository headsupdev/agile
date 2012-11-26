package org.headsupdev.agile.web.components.issues;

import org.apache.wicket.markup.html.form.DropDownChoice;
import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.storage.HibernateStorage;
import org.headsupdev.agile.storage.issues.Milestone;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

/**
 * A drop down choice component that displays milestones for a project and can include a specific milestone if not in the current project.
 * <p/>
 * Created: 26/11/2012
 *
 * @author Andrew Williams
 * @since 2.0
 */
public class MilestoneDropDownChoice
        extends DropDownChoice<Milestone>
{
    public MilestoneDropDownChoice( String id, Project project )
    {
        this( id, project, null );
    }

    public MilestoneDropDownChoice( String id, Project project, Milestone includeMilestone )
    {
        super( id, getMilestonesForProjectOrParent( project, includeMilestone ) );
    }

    private static List<Milestone> getMilestonesForProjectOrParent( Project project, Milestone includeMilestone )
    {
        List<Milestone> milestones = getMilestonesForProjectOrParent( project );

        if ( includeMilestone != null && !milestones.contains( includeMilestone ) )
        {
            milestones.add( includeMilestone );
        }

        return milestones;
    }

    private static List<Milestone> getMilestonesForProjectOrParent( Project project )
    {
        StringBuffer projectIds = new StringBuffer( "(" );
        Project p = project;
        while ( p != null )
        {
            projectIds.append( "'" );
            projectIds.append( p.getId() );
            projectIds.append( "'" );

            p = p.getParent();
            if ( p != null )
            {
                projectIds.append( "," );
            }
        }
        projectIds.append( ")" );

        Session session = ( (HibernateStorage) Manager.getStorageInstance() ).getHibernateSession();

        Transaction tx = session.beginTransaction();
        Query q = session.createQuery( "from Milestone m where id.project.id in " + projectIds.toString() +
                " and completed is null" );
        List<Milestone> list = q.list();
        tx.commit();

        return list;
    }
}
