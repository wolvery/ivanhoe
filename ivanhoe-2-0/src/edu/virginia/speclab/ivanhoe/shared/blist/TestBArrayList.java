/*
 * Created on Apr 26, 2005
 */
package edu.virginia.speclab.ivanhoe.shared.blist;

import java.util.Collections;
import java.util.Iterator;

import junit.framework.TestCase;

/**
 * @author benc
 */
public class TestBArrayList extends TestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(TestBArrayList.class);
    }
    
    public void testContentsEqual()
    {
        BList listA = BListTests.populateListA(new BArrayList());
        BList listB = BListTests.populateListB(new BArrayList());
        BList listBB = BListTests.populateListBB(new BArrayList());
        BList listC = BListTests.populateListC(new BArrayList());
        
        BArrayList listOfLists = new BArrayList();
        listOfLists.add(listA);
        listOfLists.add(listB);
        listOfLists.add(listBB);
        listOfLists.add(listC);
        
        for (Iterator i=listOfLists.iterator(); i.hasNext(); )
        {
            BList list = (BList)i.next();
            BList ooList = new BArrayList(list); // out of order list
            Collections.rotate(ooList, 2);
            
            // identity tests
            assertTrue("BArrayList failed the identity test with list "+list,
                    BListTests.testContentsEqual(list, list, true));
            assertFalse("BArrayList failed the in-order identity test with list "+list,
                    BListTests.testContentsEqual(list, ooList, true));
            assertTrue("BArrayList failed the out-of-order identity test with list "+list,
                    BListTests.testContentsEqual(list, ooList, false));
        }
        
        assertFalse("BArrayList failed to identify differences",
                BListTests.testContentsEqual(listA, listB, false));
    }
    
    public void testIntersection()
    {
        BList listA = BListTests.populateListA(new BArrayList());
        BList listB = BListTests.populateListB(new BArrayList());
        BList listC = BListTests.populateListC(new BArrayList());
        BList listD = BListTests.populateListD(new BArrayList());
        BList listAB = BListTests.populateListAintersectB(new BArrayList());
        BList listCD = BListTests.populateListCintersectD(new BArrayList());
        BList blankList = new BArrayList();
        
        BList listAoo = new BArrayList(listA); // out of order list
        Collections.rotate(listAoo, 2);
            
        // identity tests
        assertTrue("BArrayList failed the identity test for intersections with listA",
                BListTests.testIntersection(listA, listA, listA));
        assertTrue("BArrayList failed the out-of-order identity test for intersections with listA",
                BListTests.testIntersection(listA, listAoo, listA));
        
        // intersection tests
        assertTrue("BArrayList failed the orderable intersection test",
                BListTests.testIntersection(listA, listB, listAB));
        assertTrue("BArrayList failed the unorderable intersection test",
                BListTests.testIntersection(listC, listD, listCD));
        assertTrue("BArrayList returned results where there should be none",
                BListTests.testIntersection(listA, listD, blankList));
        assertTrue("BArrayList failed to correctly deal with a null value",
                BListTests.testIntersection(listC, null, blankList)); 
    }
    
    // TODO: complete the unit tests
}
