/*
 * Created on May 5, 2005
 */
package edu.virginia.speclab.ivanhoe.shared.blist.lambda;

import edu.virginia.speclab.ivanhoe.shared.blist.BCollectionFilter;

/**
 * This filters out all elements that are not castable to the specified type.
 * 
 * @author benc
 */
public class CastableFilter implements BCollectionFilter
{
    private final Class type;
    
    /**
     * Create filter that accepts only objects of this type
     * 
     * @param type
     *      The type to select for
     */
    public CastableFilter(Class type)
    {
        if (type == null)
        {
            throw new IllegalArgumentException("type cannot be null");
        }
        
        this.type = type;
    }
    
    public boolean accept(Object o)
    {
        return type.isInstance(o);
    }

}
