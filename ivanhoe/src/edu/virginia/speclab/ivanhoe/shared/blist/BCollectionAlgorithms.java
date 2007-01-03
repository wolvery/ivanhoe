/*
 * Created on Jun 1, 2005
 */
package edu.virginia.speclab.ivanhoe.shared.blist;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

/**
 * @author benc
 */
public final class BCollectionAlgorithms
{
    /**
     * Order agnostic content equality checker
     */
    public static boolean contentsEqual(BCollection thisCollection, Collection thatCollection)
    {
        return thisCollection.size() == thatCollection.size() 
                && thisCollection.containsAll(thatCollection);
    }
    
    public static BCollection combine(BCollection thisCollection, Collection thatCollection, BCollectionElementOperator op)
    {
        return inplaceCombine(thisCollection.createCopy(), thatCollection, op);
    }
    
    public static BCollection inplaceCombine(BCollection thisCollection, Collection thatCollection, BCollectionElementOperator op)
    {
        if (thisCollection == null || thatCollection == null || thisCollection.size() == 0 || thatCollection.size() == 0)
        {
            return thisCollection;
        }
        
        if (thisCollection instanceof BList)
        {
            // If it's a list, we can do a real inplace combine
            BList thisList = (BList)thisCollection;
            
            Iterator j = thatCollection.iterator();
            for (ListIterator i=thisList.listIterator(); i.hasNext(); )
            {
                if (j.hasNext())
                {
                    Object o = i.next();
                    i.set(op.operator(o, j.next()));
                }
                else
                {
                    while (i.hasNext())
                    {
                        i.next();
                        i.remove();
                    }
                }
            }
        }
        else
        {
            // If it's not a list, we'll have to fake the inplace combine
            BList results = new BArrayList(Math.min(thisCollection.size(), thatCollection.size()));
            
            for (Iterator i = thisCollection.iterator(), j = thatCollection.iterator() ; i.hasNext() && j.hasNext(); )
            {
                results.add(op.operator(i.next(), j.next()));
            }
            
            thisCollection.clear();
            thisCollection.addAll(results);
        }
        return thisCollection;
    }
    
    public static BCollection intersection(BCollection thisCollection, Collection thatCollection)
    {
        if (thisCollection.isEmpty() || thatCollection == null || thatCollection.isEmpty())
        {
            return thisCollection.createCollection();
        }
        
        if (thisCollection instanceof BSet && thatCollection instanceof Set)
        {
            BSet resultSet = (BSet)thisCollection.createCopy();
            resultSet.retainAll(thatCollection);
            return resultSet;
        }
        
        BCollection resultList;
        
        BList thisListCopy = new BArrayList(thisCollection);
        BList thatListCopy = new BArrayList(thatCollection);
        try
        {
            // If the elements are comparable, this intersection is O(nlog(n))
            Collections.sort(thisListCopy);
            Collections.sort(thatListCopy);
            resultList = sortedIntersection(thisListCopy, thatListCopy);
        }
        catch (Exception e)
        {
            // Otherwise, we'll use a O(n^2) algorithm
            resultList = unsortedIntersection(thisListCopy, thatListCopy);
        }
        
        return thisCollection.createCopy(resultList);
    }

    public static boolean contentsEqual(List thisList, List thatList, boolean inOrder)
    {
        if (inOrder)
        {
            boolean result = true;
            if (thisList != null && thisList.size() == thatList.size())
            {
                Iterator i = thisList.iterator();
                Iterator j = thatList.iterator();
                while (i.hasNext() && j.hasNext())
                {
                    Object thisObj = i.next();
                    Object thatObj = j.next();
                    if (thisObj != null)
                    {
                        if (!thisObj.equals(thatObj))
                        {
                            result = false;
                            break;
                        }
                    }
                    else if (thisObj != thatObj)
                    {
                        result = false;
                        break;
                    }
                }
            }
            else
            {
                result = false;
            }
            
            return result;
        }
        else // not in order
        {
            if ((thisList == null || thatList == null) && thisList != thatList)
            {
                return false;
            }
            
            return thisList.containsAll(thatList) && thisList.size() == thatList.size();
        }
    }

    private static BList sortedIntersection(BCollection thisCopy, Collection thatCopy)
    {
        BList results = new BLinkedList();
        
        Iterator i = thisCopy.iterator(), j = thatCopy.iterator();
        Comparable thisElement = (Comparable)i.next();
        Comparable thatElement = (Comparable)j.next();
        while (true)
        {
            int comparison = thisElement.compareTo(thatElement);
            
            if (comparison == 0)
            {
                results.add(thisElement);
            }
            
            if (comparison >= 0)
            {
                if (!j.hasNext())
                {
                    break;
                }
                thatElement = (Comparable)j.next();

            }
            
            if (comparison <= 0)
            {
                if (!i.hasNext())
                {
                    break;
                }
                thisElement = (Comparable)i.next();
            }
        }
        
        return results;
    }
    
    private static BList unsortedIntersection(List thisCopy, List thatCopy)
    {
        BList results = new BLinkedList();
        UnequalObject fillerObject = new UnequalObject();
        
        for (ListIterator i=thisCopy.listIterator(); i.hasNext(); )
        {
            Object thisElement = i.next();
            for (ListIterator j=thatCopy.listIterator(); j.hasNext(); )
            {
                boolean matched;
                Object thatElement = j.next();
                
                if (thisElement == null)
                {
                    matched = (thisElement == thatElement);
                }
                else
                {
                    matched = thisElement.equals(thatElement);
                }
                
                if (matched)
                {
                    results.add(thisElement);
                    j.set(fillerObject);
                    break;
                }
            }
        }
        return results;
    }
    
    public static BCollection filter(BCollection thisCollection, BCollectionFilter collectionFilter)
    {
        BCollection copy = thisCollection.createCopy();
        return inplaceFilter(copy, collectionFilter);
    }
    
    public static BCollection inplaceFilter(BCollection thisCollection, BCollectionFilter collectionFilter)
    {
        for (Iterator i=thisCollection.iterator(); i.hasNext();)
        {
            Object o = i.next();
            if (!collectionFilter.accept(o))
            {
                i.remove();
            }
        }
        return thisCollection;
    }
    
    public static BCollection transform(BCollection thisCollection, BCollectionTransform collectionTransform)
    {
        return inplaceTransform(thisCollection.createCopy(), collectionTransform);
    }
    
    public static BCollection inplaceTransform(BCollection thisCollection, BCollectionTransform collectionTransform)
    {
        if (thisCollection instanceof AbstractList)
        {
            for (ListIterator i=((AbstractList)thisCollection).listIterator(); i.hasNext(); )
            {
                Object o = i.next();
                i.set(collectionTransform.transform(o));
            }            
        }
        else
        {
            BArrayList results = new BArrayList(thisCollection.size());
            for (Iterator i=thisCollection.iterator(); i.hasNext(); )
            {
                Object o = i.next();
                results.add(collectionTransform.transform(o));
            }
            thisCollection.clear();
            thisCollection.addAll(results);
        }

        return thisCollection;
    }
    
    public static Object reduce(BCollection thisCollection, BCollectionReducer collectionReducer)
    {
        for (Iterator i=thisCollection.iterator(); i.hasNext(); )
        {
            if (collectionReducer.reduce(i.next()))
            {
                break;
            }
        }
        
        return collectionReducer.getReduction();
    }
    
    public static BList list(Enumeration e)
    {
        BList l = new BArrayList();
        while (e.hasMoreElements())
        {
            l.add(e.nextElement());
        }
        return l;
    }
    
    public static BSet set(Enumeration e)
    {
        BSet s = new BHashSet();
        while (e.hasMoreElements())
        {
            s.add(e.nextElement());
        }
        return s;
    }
    
    /**
     * Inverts the meaning of a filter
     * 
     * @param filter
     *      Filter whose .accept() method is to be inverted
     * @return
     *      A
     */
    public static BCollectionFilter invertFilter(BCollectionFilter filter)
    {
        // Two negatives make a positive
        if (filter instanceof FilterOut)
        {
            return ((FilterOut)filter).filter;
        }

        return new FilterOut(filter);
    }
    
    private final static class UnequalObject
    {
        public final boolean equals(Object o) { return false; }
    }
    
    /**
     * This filter is a wrapper to invert other filters.
     * 
     * @author benc
     */
    private static final class FilterOut implements BCollectionFilter
    {
        private final BCollectionFilter filter;
       
        /**
         * @param filter
         *      Filter to be inverted
         */
        public FilterOut(BCollectionFilter filter)
        {
            if (filter == null)
            {
                throw new IllegalArgumentException("filter must be non-null");
            }
            
            this.filter = filter;
        }
        
        public boolean accept(Object o)
        {
            return !filter.accept(o);
        }
    }

    static class ReverseIterator implements Iterator
    {
        private final ListIterator itr;
        
        public ReverseIterator(List list)
        {
            this.itr = list.listIterator(list.size());
        }

        public void remove() {
            itr.remove();
        }

        public boolean hasNext() {
            return itr.hasPrevious();
        }

        public Object next() {
            return itr.previous();
        }
    }
}
