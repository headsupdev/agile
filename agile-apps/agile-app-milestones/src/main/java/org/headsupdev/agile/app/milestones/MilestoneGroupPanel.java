package org.headsupdev.agile.app.milestones;

import org.headsupdev.agile.storage.DurationWorkedUtil;
import org.headsupdev.agile.storage.issues.Issue;
import org.headsupdev.agile.storage.issues.MilestoneGroup;

import java.util.Set;

/**
 * Panel to render the details of a milestone group
 * <p/>
 * Created: 11/12/2012
 *
 * @author Andrew Williams
 * @since 2.0
 */
public class MilestoneGroupPanel
    extends IssueSetPanel
{
    private MilestoneGroup group;

    public MilestoneGroupPanel( String id, MilestoneGroup group )
    {
        super( id );
        this.group = group;

        layout( group.getName(), group.getDescription(), group.getProject(), group.getCreated(), group.getUpdated(),
                group.getStartDate(), group.getDueDate(), group.getCompletedDate() );
    }

    @Override
    protected double getCompleteness()
    {
        return DurationWorkedUtil.getMilestoneGroupCompleteness( group );
    }

    @Override
    protected Set<Issue> getReOpenedIssues()
    {
        return group.getReOpenedIssues();
    }

    @Override
    protected Set<Issue> getOpenIssues()
    {
        return group.getOpenIssues();
    }

    @Override
    protected Set<Issue> getIssues()
    {
        return group.getIssues();
    }
}
