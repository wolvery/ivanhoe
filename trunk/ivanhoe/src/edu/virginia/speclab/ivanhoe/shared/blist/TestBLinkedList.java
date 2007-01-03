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
public class TestBLinkedList extends TestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(TestBLinkedList.class);
    }
    
    public void testContentsEqual()
    {
        BList listA = BListTests.populateListA(new BLinkedList());
        BList listB = BListTests.populateListB(new BLinkedList());
        BList listBB = BListTests.populateListBB(new BLinkedList());
        BList listC = BListTests.populateListC(new BLinkedList());
        
        BLinkedList listOfLists = new BLinkedList();
        listOfLists.add(listA);
        listOfLists.add(listB);
        listOfLists.add(listBB);
        listOfLists.add(listC);
        
        for (Iterator i=listOfLists.iterator(); i.hasNext(); )
        {
            BList list = (BList)i.next();
            BList ooList = new BLinkedList(list); // out of order list
            Collections.rotate(ooList, 2);
            
            // identity tests
            assertTrue("BLinkedList failed the identity test with list "+list,
                    BListTests.testContentsEqual(list, list, true));
            assertFalse("BLinkedList failed the in-order identity test with list "+list,
                    BListTests.testContentsEqual(list, ooList, true));
            assertTrue("BLinkedList failed the out-of-order identity test with list "+list,
                    BListTests.testContentsEqual(list, ooList, false));
        }
        
        assertFalse("BLinkedList failed to identify differences",
                BListTests.testContentsEqual(listA, listB, false));
    }
    
    public void testIntersection()
    {
        BList listA = BListTests.populateListA(new BLinkedList());
        BList listB = BListTests.populateListB(new BLinkedList());
        BList listC = BListTests.populateListC(new BLinkedList());
        BList listD = BListTests.populateListD(new BLinkedList());
        BList listAB = BListTests.populateListAintersectB(new BLinkedList());
        BList listCD = BListTests.populateListCintersectD(new BLinkedList());
        BList blankList = new BLinkedList();
        
        BList listAoo = new BLinkedList(listA); // out of order list
        Collections.rotate(listAoo, 2);
            
        // identity tests
        assertTrue("BLinkedList failed the identity test for intersections with listA",
                BListTests.testIntersection(listA, listA, listA));
        assertTrue("BLinkedList failed the out-of-order identity test for intersections with listA",
                BListTests.testIntersection(listA, listAoo, listA));
        
        // intersection tests
        assertTrue("BLinkedList failed the orderable intersection test",
                BListTests.testIntersection(listA, listB, listAB));
        assertTrue("BLinkedList failed the unorderable intersection test",
                BListTests.testIntersection(listC, listD, listCD));
        assertTrue("BLinkedList returned results where there should be none",
                BListTests.testIntersection(listA, listD, blankList));
        assertTrue("BLinkedList failed to correctly deal with a null value",
                BListTests.testIntersection(listC, null, blankList)); 
    }
 
    // TODO: Complete the unit tests.
}
