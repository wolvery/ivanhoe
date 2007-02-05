/*
 * Created on Mar 18, 2005
 */
package edu.virginia.speclab.ivanhoe.shared.blist;

/**
 * BCollectionFilter defines an acceptance method that either accepts or rejects each
 * element in a BList.
 * 
 * @author benc
 */
public interface BCollectionFilter
{
    /**
     * This method determines whether each object in a list should be filtered
     * out or kept in the new list. 
     * 
     * @param o
     *          The object to accept or reject
     * @return
     *          Whether to accept or reject this object
     */
    public boolean accept(Object o);
}
