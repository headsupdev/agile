package org.headsupdev.agile.web.components.milestones;

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
    private static Permission editDocPerm = new Permission()
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

    protected static boolean canEditDoc( User user, Project project )
    {
        return Manager.getSecurityInstance().userHasPermission( user,
                editDocPerm, project );
    }
}
