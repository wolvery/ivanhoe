/*
 * Created on Jan 13, 2004
 *
 */
package edu.virginia.speclab.ivanhoe.client.test;

import edu.virginia.speclab.ivanhoe.client.game.model.util.IStatusListener;
import edu.virginia.speclab.ivanhoe.client.game.model.util.StatusEventMgr;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author lfoster
 *
 * Unit test for the status monitor class
 */
public class TestStatusEventMgr extends TestCase
{
   /**
    * Mock class to hold reveal internal workings of a class that might implement
    * the IDocumentMsgListener interface.
    */
   private class MockDocumentMessage implements IStatusListener
   {
      public String err = null;
      public String warn = null;

      
      public void warningMsgNotification(String message)
      {
         warn = message;
      }
      
      public void errorMsgNotification(String message)
      {
         err = message;
      }
   }
   
   public static void main(String[] args)
   {
      junit.textui.TestRunner.run(new TestSuite(TestStatusEventMgr.class));
   }
   
   public TestStatusEventMgr(String name)
   {
      super(name);
   }
   
   public void testMessageFire()
   {
      MockDocumentMessage mdm = new MockDocumentMessage();

      StatusEventMgr.addListener(mdm);
      StatusEventMgr.fireErrorMsg("err_test");

      assertEquals(
         "Value of error message should be set to err_test",
         "err_test",
         mdm.err);
   }
   
   public void testRemoveOneListener()
   {
      MockDocumentMessage mdm = new MockDocumentMessage();
      MockDocumentMessage mdm2 = new MockDocumentMessage();

      StatusEventMgr.addListener(mdm2);
      StatusEventMgr.addListener(mdm);
      StatusEventMgr.removeListener(mdm);
   }
   
   public void testRemoveAllListeners()
   {
      MockDocumentMessage mdm = new MockDocumentMessage();
      MockDocumentMessage mdm2 = new MockDocumentMessage();

      StatusEventMgr.addListener(mdm);
      StatusEventMgr.addListener(mdm2);
      StatusEventMgr.removeAllListeners();
   }
}
