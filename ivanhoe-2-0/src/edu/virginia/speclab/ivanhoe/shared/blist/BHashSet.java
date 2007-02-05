/*
 * Created on Jun 1, 2005
 */
package edu.virginia.speclab.ivanhoe.shared.blist;

import java.util.Collection;
import java.util.HashSet;

/**
 * @author benc
 */
public class BHashSet
        extends HashSet
        implements BSet
{
    public BHashSet()
    {
        super();
    }
    
    public BHashSet(Collection that)
    {
        super(that);
    }
    
    public BCollection intersection(Collection that)
    {
        return BCollectionAlgorithms.intersection(this, that);
    }

    public BCollection filter(BCollectionFilter collectionFilter)
    {
        return BCollectionAlgorithms.filter(this, collectionFilter);
    }

    public BCollection inplaceFilter(BCollectionFilter collectionFilter)
    {
        return BCollectionAlgorithms.inplaceFilter(this, collectionFilter);
    }

    public BCollection transform(BCollectionTransform collectionTransform)
    {
        return BCollectionAlgorithms.transform(this, collectionTransform);
    }

    public BCollection inplaceTransform(BCollectionTransform collectionTransform)
    {
        return BCollectionAlgorithms.inplaceTransform(this, collectionTransform);
    }

    public Object reduce(BCollectionReducer collectionReducer)
    {
        return BCollectionAlgorithms.reduce(this,collectionReducer);
    }

    public BCollection combine(BCollection that, BCollectionElementOperator operator)
    {
        return BCollectionAlgorithms.combine(this,that,operator);
    }
    
    public BCollection inplaceCombine(BCollection that, BCollectionElementOperator operator)
    {
        return BCollectionAlgorithms.inplaceCombine(this,that,operator);
    }

    public boolean contentsEqual(Collection that)
    {
        return BCollectionAlgorithms.contentsEqual(this,that);
    }

    public BCollection createCollection()
    {
        return new BHashSet();
    }

    public BCollection createCopy()
    {
        return (BCollection)this.clone();
    }

    public BCollection createCopy(Collection that)
    {
        return new BHashSet(that);
    }
}
