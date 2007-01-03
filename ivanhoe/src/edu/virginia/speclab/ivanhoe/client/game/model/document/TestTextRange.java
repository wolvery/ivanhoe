/*
 * Created on May 5, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package edu.virginia.speclab.ivanhoe.client.game.model.document;

import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author lfoster
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class TestTextRange extends TestCase
{
   public TestTextRange(String name)
   {
      super(name);
   }

   public void testBasics()
   {
      TextRange tr = new TextRange(1,10);
      assertEquals(
         "Length should be 9", tr.getLength(), 9);
   }
   
   public void testEquals()
   {
      // test overlap of non-overlapping ranges
      TextRange tr1 = new TextRange(1,5);
      TextRange tr2 = new TextRange(1,5);
      assertTrue("Equals should be true", tr1.equals(tr2));    
   }
   
   public void testOverlaps()
   {
      // test overlap of non-overlapping ranges
      TextRange tr1 = new TextRange(10,15);
      TextRange tr2 = new TextRange(1,5);
      assertFalse("Overlaps should be false", tr1.overlaps(tr2)); 
      
      TextRange tr3 = new TextRange(10,15);
      TextRange tr4 = new TextRange(8,12);
      assertTrue("Overlaps should be true", tr3.overlaps(tr4));
   }
   
   public void testFullOverlap()
   {
      // test overlap of non-overlapping ranges
      TextRange tr1 = new TextRange(1,5);
      TextRange tr2 = new TextRange(1,5);
      TextRange overlap = tr1.getOverlap(tr2);
      assertEquals("Overlap should be 4", overlap.getLength(), 4);
      assertEquals("Overlap start should be 1", overlap.start, 1);
      assertEquals("Overlap end should be 5", overlap.end, 5);
   }
   
   public void testNonOverlap()
   {
      // test overlap of non-overlapping ranges
      TextRange tr1 = new TextRange(1,5);
      TextRange tr2 = new TextRange(11,15);
      TextRange overlap = tr1.getOverlap(tr2);
      assertEquals("Overlap of non-overlapping ranges should be 0",
         overlap.getLength(), 0);    
   }
   
   public void testPreOverlap()
   {
      // test overlap of non-overlapping ranges
      TextRange tr1 = new TextRange(10,20);
      TextRange tr2 = new TextRange(5,15);
      TextRange overlap = tr1.getOverlap(tr2);
      assertEquals("Overlap should be 5", overlap.getLength(), 5);
      assertEquals("Overlap start should be 10", overlap.start, 10);
      assertEquals("Overlap end should be 15", overlap.end, 15);
   }
   
   public void testPostOverlap()
   {
      // test overlap of non-overlapping ranges
      TextRange tr1 = new TextRange(10,20);
      TextRange tr2 = new TextRange(15,25);
      TextRange overlap = tr1.getOverlap(tr2);
      assertEquals("Overlap should be 5", overlap.getLength(), 5);
      assertEquals("Overlap start should be 15", overlap.start, 15);
      assertEquals("Overlap end should be 20", overlap.end, 20);
   }
   
   public void testMidOverlap()
   {
      // test overlap of non-overlapping ranges
      TextRange tr1 = new TextRange(10,20);
      TextRange tr2 = new TextRange(12,18);
      TextRange overlap = tr1.getOverlap(tr2);
      assertEquals("Overlap should be 6", overlap.getLength(), 6);
      assertEquals("Overlap start should be 12", overlap.start, 12);
      assertEquals("Overlap end should be 18", overlap.end, 18);
   }
   
   public void testAdjacent()
   {
      // test overlap of non-overlapping ranges
      TextRange tr1 = new TextRange(10,20);
      TextRange tr2 = new TextRange(0,10);
      TextRange overlap = tr1.getOverlap(tr2);
      assertEquals("Overlap should be 0", overlap.getLength(), 0);
      assertEquals("Overlap start should be 0", overlap.start, 0);
      assertEquals("Overlap end should be 0", overlap.end, 0);
   }

   public static void main(String[] args)
   {
      junit.textui.TestRunner.run(new TestSuite(TestTextRange.class));
   }
}
