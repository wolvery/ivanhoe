/*
 * Created on Mar 17, 2005
 */
package edu.virginia.speclab.ivanhoe.shared.blist;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * This is an implementation of BList based on the JRE's LinkedList.
 * 
 * @author benc
 */
public class BLinkedList extends LinkedList implements BList
{
    public BLinkedList()
    {
        super();
    }
    
    public BLinkedList(Collection collection)
    {
        super(collection);
    }
    
    public BLinkedList(Object[] array)
    {
        // We could use Arrays.asList(), but then the list elements alias the
        // data in the array, and this would lead to strange side-effects.
        for (int i=0; i<array.length; ++i)
        {
            this.add(array[i]);
        }
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
    
    public Object reduce(BCollectionReducer listReducer)
    {
        return BCollectionAlgorithms.reduce(this,listReducer);
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
        return new BLinkedList();
    }
    
    public BCollection createCopy()
    {
        return (BCollection)this.clone();
    }

    public BCollection createCopy(Collection that)
    {
        return new BLinkedList(that);
    }

    public Iterator reverseIterator() {
        return new BCollectionAlgorithms.ReverseIterator(this);
    }
}