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

import javax.persistence.*;
import java.util.Set;
import java.util.HashSet;
import java.io.*;

/**
 * TODO document me
 *
 * @author Andrew Williams
 * @since 1.0
 */
@Entity
@Table( name = "BuildTestResultSets" )
public class TestResultSet
    implements Serializable
{
    @Id
    @GeneratedValue
    private long id;

    private String name;

    private long duration;

    private String outputFile;

    private int tests = 0;
    private int failures = 0;
    private int errors = 0;

    @OneToMany
    private Set<TestResult> results = new HashSet<TestResult>();

    TestResultSet()
    {
    }

    public TestResultSet( String name, String outputFile )
    {
        this.name = name;
        this.outputFile = outputFile;
    }

    public long getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public long getDuration()
    {
        return duration;
    }

    public void setDuration( long duration )
    {
        this.duration = duration;
    }

    public int getTests()
    {
        return tests;
    }

    public void setTests( Integer tests )
    {
        this.tests = tests;
    }

    public int getFailures()
    {
        return failures;
    }

    public void setFailures( Integer failures )
    {
        this.failures = failures;
    }

    public int getErrors()
    {
        return errors;
    }

    public void setErrors( Integer errors )
    {
        this.errors = errors;
    }

    public Set<TestResult> getResults()
    {
        return results;
    }

    public String getOutput()
    {
        if ( outputFile == null || outputFile.length() == 0 || !( new File( outputFile ) ).exists() )
        {
            return null;
        }
        StringBuffer str = new StringBuffer();

        BufferedReader in;
        try
        {
            in = new BufferedReader( new FileReader( outputFile ) );

            String line;
            while ( ( line = in.readLine() ) != null )
            {
                str.append( line );
                str.append( '\n' );
            }
        }
        catch ( IOException e )
        {
            return "Error reading log file";
        }
        return str.toString();
    }

    public String toString()
    {
        return "TestResultSet: " + name;
    }

    @Override
    public int hashCode()
    {
        return String.valueOf( id ).hashCode();
    }

    @Override
    public boolean equals( Object obj )
    {
        return obj instanceof TestResultSet && equals( (TestResultSet) obj );
    }

    public boolean equals( TestResultSet set )
    {
        return set.getId() == id;
    }
}
