/*
 * Created on Mar 18, 2005
 */
package edu.virginia.speclab.ivanhoe.shared.blist;

/**
 * BCollectionTransform defines an algorithm to be run on each element in a list
 * that generates a new object based on the original element.  Despite the
 * name, the transform() method should generally not actually alter its
 * argument, but return a new object.  Alteration of the original list's
 * objects may significantly aid performance or be required for objects that
 * cannot be duplicated, so it is not strictly disallowed.  Just be careful.  
 * 
 * @author benc
 */
public interface BCollectionTransform
{
    /**
     * The transformation method itself.
     * 
     * @param o
     *          The original element in the source list
     * @return
     *          The new or transformed element
     */
    public Object transform(Object o);
}
