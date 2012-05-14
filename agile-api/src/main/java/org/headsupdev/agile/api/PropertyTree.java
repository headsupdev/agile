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

package org.headsupdev.agile.api;

import java.util.*;
import java.io.PrintWriter;
import java.io.Serializable;

/**
 * A heirarchical storage system for simple string key -&gt; value structures that can be dumped to a DB table.
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class PropertyTree
    implements Serializable
{
    private String prefix = "";
    private PropertyTree parent;

    private Map<String, String> properties = new HashMap<String, String>();
    private Map<String, PropertyTree> subTrees = new HashMap<String, PropertyTree>();

    /**
     * Create a new root tree node containing the provided properties.
     * Nodes with a '.' in the name will be parsed into the heirarchy.
     *
     * @param properties the properties to load into the tree.
     */
    public PropertyTree( Map<String, String> properties )
    {
        Map<String, Map<String, String>> trees = new HashMap<String, Map<String, String>>();

        for ( String name : properties.keySet() )
        {
            int dotPos = name.indexOf( '.' );
            if ( dotPos > 0 )
            {
                String treeName = name.substring( 0, dotPos );
                String subName = name.substring( dotPos + 1 );

                Map<String, String> tree = trees.get( treeName );
                if ( tree == null )
                {
                    tree = new HashMap<String, String>();
                    trees.put( treeName, tree );
                }

                tree.put( subName, properties.get( name ) );
            }
            else
            {
                this.properties.put( name, properties.get( name ) );
            }
        }

        for ( String treeId : trees.keySet() )
        {
            addSubTree( treeId, new PropertyTree( trees.get( treeId ) ) );
        }
    }

    public PropertyTree()
    {
    }

    public String getProperty( String name )
    {
        int dotPos = name.indexOf( '.' );
        if ( dotPos > 0 )
        {
            String treeName = name.substring( 0, dotPos );
            String subName = name.substring( dotPos + 1 );

            PropertyTree tree = subTrees.get( treeName );
            if ( tree == null )
            {
                return null;
            }

            return tree.getProperty( subName );
        }

        return properties.get( name );
    }

    public String getProperty( String name, String deflt )
    {
        String value = getProperty( name );

        if ( value == null )
        {
            return deflt;
        }

        return value;
    }

    public Set<String> getPropertyNames()
    {
        return properties.keySet();
    }

    public void setProperty( String name, String value )
    {
        if ( parent != null )
        {
            parent.setProperty( prefix + "." + name, value );
        }
        else
        {
            doSetProperty( name, value );
        }
    }

    void doSetProperty( String name, String value )
    {
        int dotPos = name.indexOf( '.' );
        if ( dotPos > 0 )
        {
            String treeName = name.substring( 0, dotPos );
            String subName = name.substring( dotPos + 1 );

            PropertyTree tree = subTrees.get( treeName );
            if ( tree == null )
            {
                Map<String, String> properties = new HashMap<String, String>();
                properties.put( subName, value );
                tree = new PropertyTree( properties );
                this.addSubTree( treeName, tree );
            }
            else
            {
                tree.doSetProperty( subName, value );
            }
        }
        else
        {
            properties.put( name, value );
        }
    }

    public String removeProperty( String name )
    {
        if ( parent != null )
        {
            return parent.removeProperty( prefix + "." + name );
        }
        else
        {
            return doRemoveProperty( name );
        }
    }

    String doRemoveProperty( String name )
    {
        int dotPos = name.indexOf( '.' );
        if ( dotPos > 0 )
        {
            String treeName = name.substring( 0, dotPos );
            String subName = name.substring( dotPos + 1 );

            PropertyTree tree = subTrees.get( treeName );
            if ( tree == null )
            {
                return null;
            }
            else
            {
                return tree.doRemoveProperty( subName );
            }
        }
        else
        {
            String ret = getProperty( name );
            properties.remove( name );

            return ret;
        }
    }

    public Set<String> getSubTreeIds()
    {
        return subTrees.keySet();
    }

    public PropertyTree getSubTree( String prefix )
    {
        int dotPos = prefix.indexOf( '.' );
        if ( dotPos > 0 )
        {
            String tree1 = prefix.substring( 0, dotPos );
            String tree2 = prefix.substring( dotPos + 1 );

            return getSubTree( tree1 ).getSubTree( tree2 );
        }

        if ( !subTrees.containsKey( prefix ) )
        {
            addSubTree( prefix, new PropertyTree() );
        }

        return subTrees.get( prefix );
    }

    public void addSubTree( String prefix, PropertyTree tree )
    {
        tree.setPrefix( prefix );
        tree.setParent( this );
        subTrees.put( prefix, tree );

        // this triggers a store for all sub keys
        PropertyTree child = getSubTree( prefix );
        for ( String name : child.getPropertyNames() )
        {
            child.setProperty( name, child.getProperty( name ) );
        }
        for ( String subId : child.getSubTreeIds() )
        {
            child.addSubTree( subId, child.getSubTree( subId ) );
        }
    }

    public PropertyTree removeSubTree( String prefix )
    {
        // copy the tree we are removing...
        PropertyTree removing = getSubTree( prefix );
        Map<String, String> props = new HashMap<String, String>();
        for ( String name : removing.getPropertyNames() )
        {
            props.put( name, removing.getProperty( name ) );
        }

        Enumeration<String> names = new Vector<String>( removing.getPropertyNames() ).elements();
        // this triggers a remove for all sub keys
        while ( names.hasMoreElements() )
        {
            removing.removeProperty( names.nextElement() );
        }

        PropertyTree ret = new PropertyTree( props );
        Enumeration<String> subs = new Vector<String>( removing.getSubTreeIds() ).elements();
        // this triggers a remove for all sub keys
        while ( subs.hasMoreElements() )
        {
            String subId = subs.nextElement();
            ret.addSubTree( subId, removing.removeSubTree( subId ) );
        }

        subTrees.remove( prefix );
        return ret;
    }

    public String getPrefix()
    {
        return prefix;
    }

    public void setPrefix( String prefix )
    {
        this.prefix = prefix;
    }

    public PropertyTree getParent()
    {
        return parent;
    }

    public void setParent( PropertyTree parent )
    {
        this.parent = parent;
    }

    public void list( PrintWriter writer )
    {
        list( writer, "" );
    }

    public void list( PrintWriter writer, String indent )
    {
        for ( String treeId : subTrees.keySet() )
        {
            writer.print( indent );
            writer.println( treeId );
            subTrees.get( treeId ).list( writer, indent + "  " );
        }

        for ( String name : properties.keySet() )
        {
            writer.print( indent );
            writer.print( name );
            writer.print( " => " );
            writer.println( properties.get( name ) );
        }

        writer.flush();
    }
}
