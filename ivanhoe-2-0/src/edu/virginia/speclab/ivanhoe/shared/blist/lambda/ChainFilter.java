/*
 * Created on May 25, 2005
 */
package edu.virginia.speclab.ivanhoe.shared.blist.lambda;

import java.util.Iterator;

import edu.virginia.speclab.ivanhoe.shared.blist.*;

/**
 * @author benc
 */
public class ChainFilter implements BCollectionFilter
{
    private final BList filterList;
    private short chainOperator;
    
    public final static short OR = 0;
    public final static short AND = 1;
    
    /**
     * Constructs an empty ChainFilter.  Default chain operation is AND.
     *
     */
    public ChainFilter()
    {
        filterList = new BLinkedList();
        chainOperator = AND;
    }
    
    /**
     * Constructs an empty ChainFilter with the specified logical chain 
     * operator.
     * 
     * @param chainOperator
     *          A constant representing a chain operator
     */
    public ChainFilter(short chainOperator)
    {
        filterList = new BLinkedList();
        this.chainOperator = chainOperator;
    }
    
    /**
     * Filter chain accept method
     */
    public boolean accept(Object o)
    {
        boolean b;
        switch (chainOperator)
        {
        case OR:
            b = true;
            break;
        case AND:
            b = false;
            break;
        default:
            return false;
        }
        
        for (Iterator i=filterList.iterator(); i.hasNext(); )
        {
            if (((BCollectionFilter)i.next()).accept(o) == b) return b;
        }
        return !b;
    }

    public short getChainOperator()
    {
        return chainOperator;
    }
    
    public void setChainOperator(short chainOperator)
    {
        this.chainOperator = chainOperator;
    }
    
    /**
     * Add all BCollectionFilters from filters to the filter chain.  Any object in
     * filters that is not a BCollectionFilter will be ignored.
     * 
     * @param filters
     *      A collection of BCollectionFilters
     * @return
     *      This chain filter
     */
    public ChainFilter addAllFilters(BCollection filters)
    {
        filters.filter(new CastableFilter(BCollectionFilter.class));
        filterList.addAll(filters);
        return this;
    }
    
    /**
     * Add a filter to this chain
     * 
     * @param filter
     *      The filter to add
     * @return
     *      This chain filter
     */
    public ChainFilter addFilter(BCollectionFilter filter)
    {
        filterList.add(filter);
        return this;
    }
    
    /**
     * Remove a specific filter
     * 
     * @param filter
     *      Filter to remove
     * @return
     *      Whether or not the specified filter was removed from the chain
     */
    public boolean removeFilter(BCollectionFilter filter)
    {
        return filterList.remove(filter);
    }
}
