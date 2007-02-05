/*
 * Created on Jun 1, 2005
 */
package edu.virginia.speclab.ivanhoe.shared.blist;

import java.util.Collection;

/**
 * @author benc
 */
public interface BCollection extends Collection
{
    /**
     * Creates a collection containing all elements that exist in both this and that.
     * Elements that are contained multiple times in both collections will be
     * reproduced in the returned collection the number of times they occur in the
     * collection that contains them the least number of times.
     * 
     * @param that 
     *          the collection to intersect against this collection
     * @return
     *          a new collection containing all elements contained in both collections
     * @throws UnsupportedOperationException
     *          In the case that intersections aren't supported on this collection type
     */
    public BCollection intersection(Collection that);

    /**
     * Creates a new collection that contains the elements from this collection where
     * collectionFilter.filter() returns true on that element.  All other elements
     * are omitted.
     * 
     * @param collectionFilter
     *          the filter function.
     * @return 
     *          a new collection that is filtered
     */
    public BCollection filter(BCollectionFilter collectionFilter);
    
    /**
     * Modifies this collection so that it contains only the elements where
     * collectionFilter.filter() returns true.  All other elements are omitted.
     * 
     * @param collectionFilter
     *          the filter function.
     * @return 
     *          reference to this
     */
    public BCollection inplaceFilter(BCollectionFilter collectionFilter);

    /**
     * Runs the collectionTransform.transform() function on each element of the collection
     * and populates a new collection with the results.
     * 
     * @param collectionTransform 
     *          the transformation function
     * @return 
     *          the new collection containing the transformations
     */
    public BCollection transform(BCollectionTransform collectionTransform);

    /**
     * Runs the collectionTransform.transform() function on each element of the collection
     * and repopulates this collection with the results.
     * 
     * @param collectionTransform 
     *          the transformation function
     * @return 
     *          reference to this
     */
    public BCollection inplaceTransform(BCollectionTransform collectionTransform);
    
    /**
     * Computes a reduction to a single value for the elements on this collection.
     * Examples include finding the minimum or maximum element, summing a
     * collection of numbers, or creating a text summary of objects in the collection.
     *  
     * @param collectionReducer
     *          The object that determines the type of reduction
     * @return
     *          The reduction
     */
    public Object reduce(BCollectionReducer collectionReducer);
    
    /**
     * Takes this and that collections and returns a collection such that element n in the
     * returned collection is set to operator.operator(this[n], that[n]).  The size
     * of the returned collection is the minimum of the sizes of the two collections.
     * 
     * @param that
     *          The collection with which to combine this collection's elements
     * @param operator
     *          The operator object that combines each two elements
     * @return
     *          The collection of the combined list of the same type as that
     */
    public BCollection combine(BCollection that, BCollectionElementOperator operator);
    
    /**
     * Takes this and that collections and alters this collection such that
     * element n is set to operator.operator(this[n], that[n]).  The size
     * of this collection is altered to be the minimum of the sizes of the two
     * collections.
     * 
     * @param that
     *          The collection with which to combine this collection's elements
     * @param operator
     *          The operator object that combines each two elements
     * @return
     *          reference to this
     */
    public BCollection inplaceCombine(BCollection that, BCollectionElementOperator operator);
    
    /**
     * Determines if the contents of this collection are equal to the contents of
     * that collection.  Order doesn't matter.
     * 
     * @param that
     *          The collection being compared to.  May be null.
     * @return
     *          Whether the two collections are equal
     */
    public boolean contentsEqual(Collection that);

    /**
     * Create new object of this type of collection
     * 
     * @return
     *      New instance of this type of collection 
     */
    public BCollection createCollection();
    
    /**
     * Create a copy of this collection
     * 
     * @return
     *      A copy of this object.  Usually alias for "(BCollection)this.clone()"
     */
    public BCollection createCopy();
    
    /**
     * Create a copy of that collection with this type
     * 
     * @param that
     *      Data to copy
     * @return
     *      Object of this type, with that data
     */
    public BCollection createCopy(Collection that);
}
