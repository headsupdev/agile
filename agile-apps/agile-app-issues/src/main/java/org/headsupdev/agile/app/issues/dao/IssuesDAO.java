package org.headsupdev.agile.app.issues.dao;

import org.headsupdev.agile.storage.dao.HibernateDAO;
import org.headsupdev.agile.storage.issues.Issue;

/**
 * TODO: Document Me 
 * <p/>
 * Created: 07/05/2013
 *
 * @author Andrew Williams
 * @since 1.0
 */
public class IssuesDAO
    extends HibernateDAO<Issue, Long>
{
    @Override
    protected Class<Issue> getPersistentClass()
    {
        return Issue.class;
    }
}
