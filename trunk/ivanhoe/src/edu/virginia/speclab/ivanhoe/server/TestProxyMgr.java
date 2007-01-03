/*
 * Created on Oct 3, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package edu.virginia.speclab.ivanhoe.server;

import java.util.List;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import edu.virginia.speclab.ivanhoe.shared.*;
import edu.virginia.speclab.ivanhoe.shared.message.*;

/**
 * @author lfoster
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class TestProxyMgr extends TestCase
{
   private class MockProxy extends AbstractProxy
   {
      private int numMsgsSent = 0;
      private boolean connected = true;
      private String name;
      
      public MockProxy(String name)
      {
         this.name = name;
      }
      
      public String getID()
      {
         return this.name;
      }
            
      public boolean sendMessage(Message msg)
      {
         numMsgsSent++;
         return true;
      }

      public void receiveMessage(Message msg)
      {
      }

      public boolean isConnected()
      {
         return connected;
      }

      public void disconnect()
      {
         connected = false;
      }
   }
   
   public TestProxyMgr(String arg0)
   {
      super(arg0);
   }
   
   final public void testAddProxy()
   {
      ProxyMgr mgr = new ProxyMgr();
      MockProxy proxy = new MockProxy("Proxy1");
      
      assertNull("Proxy1 should not be managed", 
         mgr.getProxyByName("Proxy1"));
      mgr.addProxy(proxy);
      assertNotNull("Proxy1 should be managed",
         mgr.getProxyByName("Proxy1"));
         
      assertFalse("Null proxy add should fail",
         mgr.addProxy(null));
   }
   
   final public void testAddDuplicateProxy()
   {
      ProxyMgr mgr = new ProxyMgr();
      MockProxy proxyOrig = new MockProxy("Proxy2");
      MockProxy proxyDup  = new MockProxy("Proxy2");
      
      mgr.addProxy(proxyOrig);
      mgr.addProxy(proxyDup);
      
      assertFalse("Original proxy should not be connected",
         proxyOrig.isConnected());
      assertTrue("Duplicate proxy should be connected",
         proxyDup.isConnected());
   }
   
   final public void testRemoveProxy()
   {
      ProxyMgr mgr = new ProxyMgr();
      MockProxy proxy = new MockProxy("Proxy3");
      
      mgr.addProxy(proxy);
      mgr.removeProxy(proxy);
      assertNull("Proxy3 should not be managed",
         mgr.getProxyByName("Proxy3"));
         
      MockProxy unmanagedProxy = new MockProxy("BadProxy");
      assertFalse("BadProxy should fail removal",
         mgr.removeProxy(unmanagedProxy));
         
      assertFalse("BadProxy should fail removal",
         mgr.removeProxy(null));
   }
   
   final public void testBroadcast()
   {
      ProxyMgr mgr = new ProxyMgr();
      MockProxy proxy1 = new MockProxy("Proxy4");
      MockProxy proxy2  = new MockProxy("Proxy5");
      
      mgr.addProxy(proxy1);
      mgr.addProxy(proxy2);
      
      ChatMsg msg = new ChatMsg("tester","Test");
      msg.setSender("Proxy4");
      mgr.broadcastMessage(msg);
      
      assertTrue( "Proxy4 should send 1 message",
         proxy1.numMsgsSent == 1 );
      assertTrue( "Proxy5 should send 1 message",
         proxy2.numMsgsSent == 1 );
   }
   
   final public void testGetNames()
   {
      ProxyMgr mgr = new ProxyMgr();
      MockProxy proxy1 = new MockProxy("Proxy8");
      MockProxy proxy2  = new MockProxy("Proxy9");
      
      mgr.addProxy(proxy1);
      mgr.addProxy(proxy2);
      
      List names = mgr.getNames();
      assertTrue("Name list size should be 2",
         names.size() == 2);
         
      mgr.removeProxy(proxy2);
      names = mgr.getNames();
      assertTrue("Name list size should be 1",
         names.size() == 1);
   }
   
   final public void testRemoveAll()
   {
      ProxyMgr mgr = new ProxyMgr();
      MockProxy proxy1 = new MockProxy("Proxy10");
      MockProxy proxy2  = new MockProxy("Proxy11");
      
      mgr.addProxy(proxy1);
      mgr.addProxy(proxy2);
      
      mgr.removeAllProxies();
      
      assertFalse("Proxy10 should be disconnected",
         proxy1.connected );
      assertFalse("Proxy11 should be disconnected",
         proxy2.connected );
   }

   public static void main(String[] args)
   {
      junit.textui.TestRunner.run(new TestSuite(TestProxyMgr.class));
   }
}
