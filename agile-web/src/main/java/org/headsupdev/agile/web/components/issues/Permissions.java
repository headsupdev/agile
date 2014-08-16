package org.headsupdev.agile.web.components.issues;

import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.api.Permission;
import org.headsupdev.agile.api.Project;
import org.headsupdev.agile.api.User;

import java.util.List;

/**
 * Created by Gordon Edwards on 30/07/2014.
 *
 * Allows
 */
class Permissions
{
    private static Permission editIssuePerm = new Permission()
    {
        public String getId()
        {
            return "ISSUE-EDIT";
        }

        public String getDescription()
        {
            return null;
        }

        public List<String> getDefaultRoles()
        {
            return null;
        }
    };

    protected static boolean canEditIssue( User user, Project project )
    {
        return Manager.getSecurityInstance().userHasPermission( user,
                editIssuePerm, project );
    }
}
