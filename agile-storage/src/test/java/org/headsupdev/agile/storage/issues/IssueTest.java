/*
 * HeadsUp Agile
 * Copyright 2014 Heads Up Development.
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

package org.headsupdev.agile.storage.issues;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.headsupdev.agile.storage.StoredFileProject;

/**
 * Tests for Issues
 *
 * @author gordonedwards
 * @since 2.1
 */
public class IssueTest
        extends TestCase
{

    private Issue issue1;
    private Issue issue2;

    public void setUp()
            throws Exception
    {
        super.setUp();
        StoredFileProject project1 = new StoredFileProject( "proj1", "proj1" );
        StoredFileProject project2 = new StoredFileProject( "proj2", "proj2" );
        issue1 = new UniqueIssue( project1 );
        issue2 = new UniqueIssue( project2 );
    }

    public void testDetectDuplicateLinkedRelationships()
    {
        IssueRelationship relationship1 = new IssueRelationship( issue1, issue2, IssueRelationship.TYPE_LINKED );

        Assert.assertFalse( issue1.hasRelationship( relationship1 ) );
        issue1.getRelationships().add( relationship1 );
        Assert.assertTrue( issue1.hasRelationship( relationship1 ) );
    }

    public void testEquivalenceLinkedRelationships()
    {
        IssueRelationship relationship1 = new IssueRelationship( issue1, issue2, IssueRelationship.TYPE_LINKED );
        IssueRelationship relationship2 = new IssueRelationship( issue1, issue2, IssueRelationship.TYPE_LINKED );

        Assert.assertTrue( relationship1.isEquivalent( relationship2 ) );
    }

    public void testInvertEquivalenceLinkedRelationships()
    {
        IssueRelationship relationship1 = new IssueRelationship( issue1, issue2, IssueRelationship.TYPE_LINKED );
        IssueRelationship relationship2 = new IssueRelationship( issue2, issue1, IssueRelationship.TYPE_LINKED );

        Assert.assertTrue( relationship1.isEquivalent( relationship2 ) );
    }

    public void testNonEquivalenceLinkedRelationships()
    {
        StoredFileProject project3 = new StoredFileProject( "proj3", "proj3" );
        Issue issue3 = new Issue( project3 );

        IssueRelationship relationship1 = new IssueRelationship( issue1, issue2, IssueRelationship.TYPE_LINKED );
        IssueRelationship relationship2 = new IssueRelationship( issue3, issue1, IssueRelationship.TYPE_LINKED );

        Assert.assertFalse( relationship1.isEquivalent( relationship2 ) );
    }

    public void testEquivalenceBlocksRelationships()
    {
        IssueRelationship relationship1 = new IssueRelationship( issue1, issue2, IssueRelationship.TYPE_BLOCKS );
        IssueRelationship relationship2 = new IssueRelationship( issue1, issue2, IssueRelationship.TYPE_BLOCKS );

        Assert.assertTrue( relationship1.isEquivalent( relationship2 ) );
    }

    public void testInvertEquivalenceBlockRelationships()
    {
        IssueRelationship relationship1 = new IssueRelationship( issue1, issue2, IssueRelationship.TYPE_BLOCKS );
        IssueRelationship relationship2 = new IssueRelationship( issue2, issue1, IssueRelationship.TYPE_BLOCKS + IssueRelationship.REVERSE_RELATIONSHIP );

        Assert.assertTrue( relationship1.isEquivalent( relationship2 ) );
    }

    public void testDoubleInvertedIsSameLinkedRelationships()
    {
        IssueRelationship relationship = new IssueRelationship( issue1, issue2, IssueRelationship.TYPE_LINKED );
        IssueRelationship invertedRelationship = relationship.getInverseRelationship();
        Assert.assertTrue( relationship.equals( invertedRelationship.getInverseRelationship() ) );
    }

    public void testDoubleInvertedIsSameBlocksRelationships()
    {
        IssueRelationship relationship = new IssueRelationship( issue1, issue2, IssueRelationship.TYPE_BLOCKS );
        IssueRelationship invertedRelationship = relationship.getInverseRelationship();
        Assert.assertTrue( relationship.equals( invertedRelationship.getInverseRelationship() ) );
    }

    public void testDoubleInvertedIsSameBlocksFromInvertedRelationships()
    {
        IssueRelationship invertedRelationship = new IssueRelationship( issue1, issue2, IssueRelationship.TYPE_BLOCKS + IssueRelationship.REVERSE_RELATIONSHIP );
        IssueRelationship relationship = invertedRelationship.getInverseRelationship();
        Assert.assertTrue( relationship.equals( invertedRelationship.getInverseRelationship() ) );
    }

    public void testHasRelationshipLinked()
    {
        IssueRelationship relationship = new IssueRelationship( issue1, issue2, IssueRelationship.TYPE_LINKED );
        issue1.addRelationship( relationship );
        assertTrue( issue1.hasRelationship( relationship ) );
    }

    public void testNotHasRelationshipBlocks()
    {
        StoredFileProject project3 = new StoredFileProject( "proj3", "proj3" );
        Issue issue3 = new UniqueIssue( project3 );
        IssueRelationship relationship1 = new IssueRelationship( issue1, issue2, IssueRelationship.TYPE_BLOCKS );
        IssueRelationship relationship2 = new IssueRelationship( issue1, issue3, IssueRelationship.TYPE_BLOCKS );
        issue1.addRelationship( relationship1 );
        assertFalse( issue1.hasRelationship( relationship2 ) );
    }

    public void testNotHasRelationshipLinked()
    {
        StoredFileProject project3 = new StoredFileProject( "proj3", "proj3" );
        Issue issue3 = new UniqueIssue( project3 );
        IssueRelationship relationship1 = new IssueRelationship( issue1, issue2, IssueRelationship.TYPE_LINKED );
        IssueRelationship relationship2 = new IssueRelationship( issue2, issue3, IssueRelationship.TYPE_LINKED );
        issue1.addRelationship( relationship1 );
        assertFalse( issue1.hasRelationship( relationship2 ) );
    }

    public void testHasRelationshipReversedLinked()
    {
        IssueRelationship relationship1 = new IssueRelationship( issue1, issue2, IssueRelationship.TYPE_LINKED );
        IssueRelationship relationship2 = new IssueRelationship( issue2, issue1, IssueRelationship.TYPE_LINKED );
        issue1.addRelationship( relationship1 );
        assertTrue( issue2.hasRelationship( relationship2 ) );
    }

    public void testHasRelationshipReversedBlocks()
    {
        IssueRelationship relationship1 = new IssueRelationship( issue1, issue2, IssueRelationship.TYPE_BLOCKS );
        IssueRelationship relationship2 = new IssueRelationship( issue2, issue1, IssueRelationship.TYPE_BLOCKS + IssueRelationship.REVERSE_RELATIONSHIP );
        issue1.addRelationship( relationship1 );
        assertTrue( issue2.hasRelationship( relationship2 ) );
    }

    public void testHasRelationshipIncorrectReversedBlocks()
    {
        IssueRelationship relationship1 = new IssueRelationship( issue1, issue2, IssueRelationship.TYPE_BLOCKS );
        IssueRelationship relationship2 = new IssueRelationship( issue2, issue1, IssueRelationship.TYPE_BLOCKS );
        issue1.addRelationship( relationship1 );
        assertFalse( issue2.hasRelationship( relationship2 ) );
    }

}
