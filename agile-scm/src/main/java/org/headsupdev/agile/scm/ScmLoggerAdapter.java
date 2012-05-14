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

import org.headsupdev.agile.api.HeadsUpConfiguration;
import org.headsupdev.agile.api.Manager;
import org.headsupdev.agile.api.logging.Logger;
import org.apache.maven.scm.log.ScmLogger;

/**
 * A logger adapter that allows us to pass control from the maven-scm system into our own logger.
 *
 * @author Andrew Williams
 * @version $Id: ScmLoggerAdapter.java 772 2010-04-06 22:26:35Z handyande $
 * @since 1.0
 */
public class ScmLoggerAdapter
    implements ScmLogger
{
    private Logger delegate = Manager.getLogger( "MavenSCM" );

    public boolean isDebugEnabled()
    {
        return HeadsUpConfiguration.isDebug();
    }

    public void debug( String message )
    {
        delegate.debug( message );
    }

    public void debug( String message, Throwable throwable )
    {
        delegate.debug( message );
    }

    public void debug( Throwable throwable )
    {
        delegate.debug( throwable.getMessage() );
    }

    public boolean isInfoEnabled()
    {
        return HeadsUpConfiguration.isVerbose();
    }

    public void info( String message )
    {
        delegate.info( message );
    }

    public void info( String message, Throwable throwable )
    {
        delegate.info( message );
    }

    public void info( Throwable throwable )
    {
        delegate.info( throwable.getMessage() );
    }

    public boolean isWarnEnabled()
    {
        return HeadsUpConfiguration.isVerbose();
    }

    public void warn( String message )
    {
        delegate.warn( message );
    }

    public void warn( String message, Throwable throwable )
    {
        delegate.warn( message );
    }

    public void warn( Throwable throwable )
    {
        delegate.warn( throwable.getMessage() );
    }

    public boolean isErrorEnabled()
    {
        return true;
    }

    public void error( String message )
    {
        delegate.error( message );
    }

    public void error( String message, Throwable throwable )
    {
        delegate.error( message, throwable );
    }

    public void error( Throwable throwable )
    {
        delegate.error( throwable.getMessage(), throwable );
    }
}
