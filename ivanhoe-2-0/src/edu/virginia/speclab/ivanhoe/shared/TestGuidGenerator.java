/*
 * Created on Apr 6, 2004
 * 
 * edu.virginia.speclab.ivanhoe.shared 
 * TestGuidGenerator.java
 */
package edu.virginia.speclab.ivanhoe.shared;

import java.util.Vector;

import junit.framework.TestCase;

/**
 * @author lfoster
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class TestGuidGenerator extends TestCase
{
   /**
    * Constructor for IvanhoeDocumentTimelineTest.
    * @param arg0
    */
   public TestGuidGenerator(String arg0)
   {
      super(arg0);
   }

   /*
    * @see TestCase#setUp()
    */
   protected void setUp() throws Exception
   {
      super.setUp();
   }
   
   public void testGuid()
   {
      Vector ids = new Vector();
      int numTests = 1000;
      
      String id;
      for (int i=0;i<numTests;i++)
      {
         id = GuidGenerator.generateID();
         assertFalse("ID List should not contain " + id, ids.contains(id));
         ids.add(id);  
         //System.out.println(id);
      }
      
      assertTrue("Size should be " + numTests, ids.size()==numTests);
     
      
   }
}
