package org.headsupdev.agile.app.milestones;

import org.hibernate.criterion.Criterion;

import java.io.Serializable;

/**
 * TODO: Document Me
 * <p/>
 * Created: 29/11/2012
 *
 * @author Andrew Williams
 * @since 2.0
 */
public interface MilestoneFilter
    extends Serializable
{
    public Criterion getCompletedCriterion();

    public Criterion getDueCriterion();
}
