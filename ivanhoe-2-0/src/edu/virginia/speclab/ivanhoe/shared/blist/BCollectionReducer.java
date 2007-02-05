/*
 * Created on Mar 18, 2005
 */
package edu.virginia.speclab.ivanhoe.shared.blist;

/**
 * A BCollectionReducer defines an algorithm to be run on each element in a list
 * that, as its end result, generates a single value.
 * 
 * @author benc
 */
public interface BCollectionReducer
{
    /**
     * This method is called on every element in a list in order to compute
     * some single value (the reduction)
     * 
     * @param o
     *          An element in the list
     * @return
     *          true if this terminates early
     */
    public boolean reduce(Object o);
    
    /**
     * Get the result of the reduction.
     * 
     * @return
     *          the result of the reduction
     */
    public Object getReduction();
}
