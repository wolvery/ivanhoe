/*
 * Created on Apr 26, 2005
 */
package edu.virginia.speclab.ivanhoe.shared.blist;

import java.io.File;

/**
 * @author benc
 */
public class BListTests
{   
    public static BList populateListA(BList list)
    {
        list.clear();
        
        list.add("bca");
        list.add("abc");
        list.add("abd");
        list.add("");
        list.add("abc");
        list.add("bcd");
        
        return list;
    }
    
    public static BList populateListB(BList list)
    {
        list.clear();
        
        list.add("bca");
        list.add("abc");
        list.add("zxy"); // not in A
        //list.add("abd");
        //list.add("");
        list.add("abc");
        list.add("bcd");

        return list;
    }
    
    public static BList populateListAintersectB(BList list)
    {
        list.clear();
        
        list.add("bca");
        list.add("abc");
        list.add("abc");
        list.add("bcd");
        
        return list;
    }
    
    public static BList populateListC(BList list)
    {
        list.clear();
        
        list.add(new File("/bad/path/name"));
        list.add(null);
        list.add(new Integer(3));
        list.add("foo");
        
        list.addAll(list);
        list.addAll(list);
        
        return list;
    }
    
    public static BList populateListD(BList list)
    {
        list.clear();
        
        list.add(new File("/worse/path/name"));
        list.add(null);
        list.add(new Integer(4));
        list.add("foo");
        
        list.addAll(list);
        
        return list;
    }
    
    public static BList populateListCintersectD(BList list)
    {
        list.clear();
        
        list.add(null);
        list.add("foo");
        list.add(null);
        list.add("foo");
        
        return list;
    }
    
    public static BList populateListBB(BList list)
    {
        list.clear();
        
        list = populateListB(list);
        list.addAll(list);
        
        return list;
    }
    
    public static boolean testContentsEqual(BList thisList, BList thatList, boolean inOrder)
    {
        return thisList.contentsEqual(thatList, inOrder);
    }
    
    public static boolean testIntersection(BList thisList, BList thatList, BList resultList)
    {
        BList intersection = (BList)thisList.intersection(thatList);
        return intersection.contentsEqual(resultList, false);
    }
    
    public static boolean testFilter(BList thisList, BCollectionFilter filter, BList resultList)
    {
        BList filteredList = (BList)thisList.filter(filter);
        return filteredList.contentsEqual(resultList);
    }
    
    public static boolean testReduce(BList thisList, BCollectionReducer reducer, Object result)
    {
        Object actualResult = thisList.reduce(reducer);
        
        if (result == null)
        {
            return actualResult == null;
        }
        else
        {
            return result.equals(actualResult);
        }
    }
    
    public static boolean testTransform(BList thisList, BCollectionTransform transform, BList resultList)
    {
        BList transformedList = (BList)thisList.transform(transform);
        return transformedList.contentsEqual(resultList);
    }
    
    public static boolean testCombine(BList thisList, BList thatList, BCollectionElementOperator op, BList resultList)
    {
        BList combinedList = (BList)thisList.combine(thatList, op);
        return combinedList.contentsEqual(resultList);
    }
}
