/*
 * HeadsUp Agile
 * Copyright 2009-2012 Heads Up Development.
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

package org.headsupdev.agile.scm;

import org.apache.maven.scm.ScmRevision;

import java.util.HashSet;
import java.util.Set;

/**
 * A basic abstraction for git behaviour
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class GitScmVariant
    implements ScmVariant
{
    private static final Set<String> IGNORED_FILE_NAMES = new HashSet<String>();

    static
    {
        IGNORED_FILE_NAMES.add( ".git" );
    }

    public boolean isTransactional()
    {
        return true;
    }

    public boolean isLogOldestFirst()
    {
        return false;
    }

    public boolean useDiffForFileListing()
    {
        return true;
    }

    public Set<String> getIgnoredFileNames()
    {
        return IGNORED_FILE_NAMES;
    }

    public ScmRevision getStartRevisionForDiff( String start, String end )
    {
        return new ScmRevision( end + "~1" );
    }

    public ScmRevision getEndRevisionForDiff( String start, String end )
    {
        return new ScmRevision( end );
    }
}
