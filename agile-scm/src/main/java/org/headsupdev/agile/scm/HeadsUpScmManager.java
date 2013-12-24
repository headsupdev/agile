/*
 * HeadsUp Agile
 * Copyright 2009-2012 Heads Up Development Ltd.
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

import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.manager.BasicScmManager;
import org.apache.maven.scm.provider.git.gitexe.GitExeScmProvider;
import org.apache.maven.scm.provider.svn.svnexe.SvnExeScmProvider;
import org.apache.maven.scm.provider.hg.HgScmProvider;

import java.util.LinkedList;
import java.util.List;

/**
 * Interface for the main source control management ApI
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class HeadsUpScmManager extends BasicScmManager
{
    private static HeadsUpScmManager instance = new HeadsUpScmManager();

    private static final List<String> SCM_IDS = new LinkedList<String>();

    static
    {
        SCM_IDS.add( "git" );
        SCM_IDS.add( "hg" );
        SCM_IDS.add( "svn" );
    }

    public static HeadsUpScmManager getInstance()
    {
        return instance;
    }

    public HeadsUpScmManager()
    {
        setScmProvider( "svn", new SvnExeScmProvider() );
        setScmProvider( "git", new GitExeScmProvider() );
        setScmProvider( "hg", new HgScmProvider() );
    }

    @Override
    protected ScmLogger getScmLogger()
    {
        return new ScmLoggerAdapter();
    }

    public List<String> getScmIds()
    {
        return SCM_IDS;
    }

    /**
     * Get the variant that describes the behaviour of an scm variant.
     * The parameter can be either the scm code or an scm url string
     *
     * @param scm the scm code (i.e. svn,hg) or an scm url (i.e. scm:git:file:///tmp/repo)
     * @return the ScmVariant for the requested scm type or null if not recognised
     */
    public ScmVariant getScmVariant( String scm )
    {
        String scmCode = scm;
        if ( scm.startsWith( "scm:" ) )
        {
            String tmp = scm.substring( 4 );
            int end = tmp.indexOf( ':' );
            scmCode = tmp.substring( 0, end );
        }

        if ( scmCode.equals( "svn" ) )
        {
            return new SvnScmVariant();
        }
        else if ( scmCode.equals( "git" ) )
        {
            return new GitScmVariant();
        }
        else if ( scmCode.equals( "hg" ) )
        {
            return new HgScmVariant();
        }

        return null;
    }
}
