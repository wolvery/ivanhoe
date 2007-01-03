/*
 * Created on Mar 18, 2005
 */
package edu.virginia.speclab.ivanhoe.shared.blist;

import java.util.Iterator;
import java.util.List;
/**
 * BList is an interface for lists that supply certain lambda-like
 * functionality.  It's heavilly inspired by the filter() and map() functions
 * in Python as well as similar and other abilities in many other languages.
 * 
 * @author benc
 */
public interface BList extends List, BCollection
{
    /**
     * Determines if the contents of this list are equal to the contents of
     * that list.
     * 
     * @param that
     *          The list being compared to.  May be null.
     * @param inOrder
     *          Whether or not order matters.
     * @return
     *          Whether the two lists are equal
     */
    public boolean contentsEqual(List that, boolean inOrder);
    
    /**
     * Allows iteration through a list in reverse order without reversing
     * the data structure
     * 
     * @return
     *          An iterator that goes from last to first element
     */
    public Iterator reverseIterator();
}