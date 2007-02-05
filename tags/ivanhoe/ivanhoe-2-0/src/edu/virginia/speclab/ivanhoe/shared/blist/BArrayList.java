/*
 * Created on Mar 17, 2005
 */
package edu.virginia.speclab.ivanhoe.shared.blist;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * This is an implementation of BList based on the JRE's ArrayList.  This is
 * created to be a reference implementation of a BList while still being
 * performant. 
 * 
 * @author benc
 */
public class BArrayList extends ArrayList implements BList
{
    public BArrayList()
    {
        super();
    }
    
    public BArrayList(int capacity)
    {
        super(capacity);
    }
    
    public BArrayList(Collection collection)
    {
        super(collection);
    }
    
    public BArrayList(Object[] array)
    {
        // We could use Arrays.asList(), but then the list elements alias the
        // data in the array, and this would lead to strange side-effects.
        super(array.length);
        for (int i=0; i<array.length; ++i)
        {
            this.add(i, array[i]);
        }
    }
    
    
    public BCollection filter(BCollectionFilter collectionFilter)
    {
        return BCollectionAlgorithms.filter(this,collectionFilter);
    }

    public BCollection inplaceFilter(BCollectionFilter collectionFilter)
    {
        return BCollectionAlgorithms.inplaceFilter(this,collectionFilter);
    }

    public BCollection transform(BCollectionTransform collectionTransform)
    {
        BArrayList outList = new BArrayList(this.size());
        for (int i=0; i < this.size(); ++i)
        {
            Object o = this.get(i);
            outList.add(collectionTransform.transform(o));
        }
        return outList;
    }
    
    public BCollection inplaceTransform(BCollectionTransform collectionTransform)
    {
        for (int i=0; i < this.size(); ++i)
        {
            Object o = this.get(i);
            this.set(i, collectionTransform.transform(o));
        }
        return this;
    }

    
    public Object reduce(BCollectionReducer listReducer)
    {
        return BCollectionAlgorithms.reduce(this, listReducer);
    }

    public BCollection combine(BCollection that, BCollectionElementOperator operator)
    {
        return BCollectionAlgorithms.combine(this, that, operator);
    }
    
    public BCollection inplaceCombine(BCollection that, BCollectionElementOperator operator)
    {
        return BCollectionAlgorithms.inplaceCombine(this, that, operator);
    }

    public boolean contentsEqual(Collection that)
    {
        return BCollectionAlgorithms.contentsEqual(this, that);
    }

    public boolean contentsEqual(List that, boolean inOrder)
    {
        return BCollectionAlgorithms.contentsEqual(this, that, inOrder);
    }

    public BCollection createCollection()
    {
        return new BArrayList();
    }

    public BCollection createCopy()
    {
        return (BCollection)this.clone();
    }

    public BCollection createCopy(Collection that)
    {
        return new BArrayList(that);
    }

    public BCollection intersection(Collection that)
    {
        return BCollectionAlgorithms.intersection(this, that);
    }
    
    public Iterator reverseIterator()
    {
        return new BCollectionAlgorithms.ReverseIterator(this);
    }
}