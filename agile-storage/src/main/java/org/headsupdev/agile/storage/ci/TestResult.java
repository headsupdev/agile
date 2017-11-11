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

package org.headsupdev.agile.storage.ci;

import org.hibernate.annotations.Type;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * TODO document me
 *
 * @author Andrew Williams
 * @since 1.0
 */
@Entity
@Table( name = "BuildTestResults" )
public class TestResult
    implements Serializable
{
    public static final int STATUS_PASSED = 1;
    public static final int STATUS_FAILED = 2;
    public static final int STATUS_ERROR = 3;

    @Id
    @GeneratedValue
    private long id;

    private String name;

    private int status;

    private long duration;

    private String message;

    @Type( type = "text" )
    private String output;

    TestResult()
    {
    }

    public TestResult( String name, int status, long duration, String message, String output )
    {
        this.name = name;
        this.status = status;
        this.duration = duration;
        this.message = message;
        this.output = output;
    }

    public long getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public int getStatus()
    {
        return status;
    }

    public long getDuration()
    {
        return duration;
    }

    public String getMessage()
    {
        return message;
    }

    public String getOutput()
    {
        return output;
    }

    @Override
    public String toString()
    {
        String ret = "Test ";
        switch ( status )
        {
            case STATUS_FAILED:
                ret += "failed";
                break;
            case STATUS_ERROR:
                ret += "error";
                break;
            default:
                ret += "passed";
        }

        return ret + ": " + name;
    }

    @Override
    public int hashCode()
    {
        return String.valueOf( id ).hashCode();
    }

    @Override
    public boolean equals( Object obj )
    {
        return obj instanceof TestResult && equals( (TestResult) obj );
    }

    public boolean equals( TestResult result )
    {
        return result.getId() == id;
    }
}
