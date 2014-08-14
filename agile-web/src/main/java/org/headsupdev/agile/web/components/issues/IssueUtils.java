/*
 * HeadsUp Agile
 * Copyright 2009-2014 Heads Up Development Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.headsupdev.agile.web.components.issues;

import org.headsupdev.agile.api.User;
import org.headsupdev.agile.storage.issues.Issue;
import org.headsupdev.agile.storage.issues.IssueRelationship;

import java.util.LinkedList;
import java.util.List;

/**
 * Utilities for issues
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class IssueUtils
{
    private static List<Integer> types = new LinkedList<Integer>();
    private static List<Integer> priorities = new LinkedList<Integer>();
    private static List<Integer> resolutions = new LinkedList<Integer>();
    private static List<Integer> relationships = new LinkedList<Integer>();

    static
    {
        types.add( Issue.TYPE_BUG );
        types.add( Issue.TYPE_FEATURE );
        types.add( Issue.TYPE_ENHANCEMENT );
        types.add( Issue.TYPE_TASK );
        types.add( Issue.TYPE_SPEC );
        types.add( Issue.TYPE_ENQUIRY );

        priorities.add( Issue.PRIORITY_BLOCKER );
        priorities.add( Issue.PRIORITY_CRITICAL );
        priorities.add( Issue.PRIORITY_MAJOR );
        priorities.add( Issue.PRIORITY_MINOR );
        priorities.add( Issue.PRIORITY_TRIVIAL );

        resolutions.add( Issue.RESOLUTION_FIXED );
        resolutions.add( Issue.RESOLUTION_DUPLICATE );
        resolutions.add( Issue.RESOLUTION_CANNOT_REPRODUCE );
        resolutions.add( Issue.RESOLUTION_INVALID );
        resolutions.add( Issue.RESOLUTION_WONTFIX );

        relationships.add( IssueRelationship.TYPE_LINKED );
        relationships.add( IssueRelationship.TYPE_DUPLICATE );
        relationships.add( IssueRelationship.TYPE_REQUIRES );
        relationships.add( IssueRelationship.TYPE_BLOCKS );

        relationships.add( IssueRelationship.TYPE_DUPLICATE + IssueRelationship.REVERSE_RELATIONSHIP );
        relationships.add( IssueRelationship.TYPE_REQUIRES + IssueRelationship.REVERSE_RELATIONSHIP );
        relationships.add( IssueRelationship.TYPE_BLOCKS + IssueRelationship.REVERSE_RELATIONSHIP );
    }

    public static String getTypeName( int type )
    {
        switch ( type )
        {
            case Issue.TYPE_BUG:
                return "bug";
            case Issue.TYPE_FEATURE:
                return "feature";
            case Issue.TYPE_ENHANCEMENT:
                return "enhancement";
            case Issue.TYPE_TASK:
                return "task";
            case Issue.TYPE_SPEC:
                return "spec";
            case Issue.TYPE_ENQUIRY:
                return "enquiry";
            default:
                return "";
        }
    }

    public static String getPriorityName( int priority )
    {
        switch ( priority )
        {
            case Issue.PRIORITY_BLOCKER:
                return "blocker";
            case Issue.PRIORITY_CRITICAL:
                return "critical";
            case Issue.PRIORITY_MAJOR:
                return "major";
            case Issue.PRIORITY_MINOR:
                return "minor";
            case Issue.PRIORITY_TRIVIAL:
                return "trivial";
            default:
                return "";
        }
    }

    public static String getStatusName( int status )
    {
        switch ( status )
        {
            case Issue.STATUS_NEW:
                return "new";
            case Issue.STATUS_FEEDBACK:
                return "feedback";
            case Issue.STATUS_ASSIGNED:
                return "assigned";
            case Issue.STATUS_REOPENED:
                return "reopened";
            case Issue.STATUS_INPROGRESS:
                return "in progress";
            case Issue.STATUS_RESOLVED:
                return "resolved";
            case Issue.STATUS_CLOSED:
                return "closed";
            default:
                return "";
        }
    }

    public static String getStatusDescription( Issue issue )
    {
        String desc;
        if ( issue.getStatus() >= Issue.STATUS_RESOLVED && issue.getResolution() != 0 )
        {
            desc = IssueUtils.getStatusName( issue.getStatus() ) + " (" +
                    IssueUtils.getResolutionName( issue.getResolution() ) + ")";
        }
        else
        {
            desc = IssueUtils.getStatusName( issue.getStatus() );
        }
        if ( issue.getReopened() > 0 )
        {
            desc += " (" + issue.getReopened() + ")";
        }

        return desc;
    }

    public static String getResolutionName( int resolution )
    {
        switch ( resolution )
        {
            case Issue.RESOLUTION_FIXED:
                return "fixed";
            case Issue.RESOLUTION_INVALID:
                return "invalid";
            case Issue.RESOLUTION_WONTFIX:
                return "won't fix";
            case Issue.RESOLUTION_DUPLICATE:
                return "duplicate";
            case Issue.RESOLUTION_CANNOT_REPRODUCE:
                return "cannot reproduce";
            default:
                return "";
        }
    }

    public static String getRelationshipName( int relationship )
    {
        if ( relationship > IssueRelationship.REVERSE_RELATIONSHIP )
        {
            return getReverseRelationshipName( relationship );
        }

        switch ( relationship )
        {
            case IssueRelationship.TYPE_DUPLICATE:
                return "duplicates";
            case IssueRelationship.TYPE_REQUIRES:
                return "requires";
            case IssueRelationship.TYPE_BLOCKS:
                return "blocks";
            default:
                return "related to";
        }
    }

    public static String getReverseRelationshipName( int relationship )
    {
        if ( relationship > IssueRelationship.REVERSE_RELATIONSHIP )
        {
            relationship -= IssueRelationship.REVERSE_RELATIONSHIP;
        }

        switch ( relationship )
        {
            case IssueRelationship.TYPE_DUPLICATE:
                return "duplicated by";
            case IssueRelationship.TYPE_REQUIRES:
                return "required by";
            case IssueRelationship.TYPE_BLOCKS:
                return "blocked by";
            default:
                return "related to";
        }
    }

    public static List<Integer> getTypes()
    {
        return types;
    }

    public static List<Integer> getPriorities()
    {
        return priorities;
    }

    public static List<Integer> getResolutions()
    {
        return resolutions;
    }

    public static List<Integer> getRelationships()
    {
        return relationships;
    }


    public static String getWatchersDescription( Issue issue, User user )
    {
        if ( issue.getWatchers() == null || issue.getWatchers().size() == 0 )
        {
            return "Nobody";
        }

        int watchers = issue.getWatchers().size();
        if ( issue.getWatchers().contains( user ) )
        {
            if ( watchers == 1 )
            {
                return "Myself";
            }
            else if ( watchers == 2 )
            {
                return "Myself and 1 other account";
            }
            return "Myself and " + ( watchers - 1 ) + " other accounts";
        }
        if ( watchers == 1 )
        {
            return "1 account";
        }
        return watchers + " accounts";
    }
}
