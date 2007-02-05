/*
 * Created on May 27, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package edu.virginia.speclab.ivanhoe.shared;

import edu.virginia.speclab.ivanhoe.shared.message.ChatMsg;
import edu.virginia.speclab.ivanhoe.shared.message.Message;
import edu.virginia.speclab.ivanhoe.shared.message.MessageType;
import edu.virginia.speclab.ivanhoe.shared.message.ReadyMsg;
import junit.framework.TestCase;

/**
 * @author lfoster
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class TestMessageRouter extends TestCase
{
   public TestMessageRouter(String arg0)
   {
      super(arg0);
   }

   
   public void testRegister()
   {
      MessageRouter router = new MessageRouter();
      MockHandler handler = new MockHandler();
      router.registerHandler(MessageType.READY, handler);
      router.routeMessage(new ReadyMsg() );
      assertTrue("Handle count should be 1", (handler.eventCount==1) );
   }
   
   public void testDefault()
   {
      MessageRouter router = new MessageRouter();
      MockHandler handler = new MockHandler();
      router.setDefaultHandler( handler);
      router.routeMessage(new ReadyMsg() );
      assertTrue("Handle count should be 1", (handler.eventCount==1) );
   }
   
   public void testRemove()
   {
      MessageRouter router = new MessageRouter();
      MockHandler handler = new MockHandler();
      router.registerHandler(MessageType.READY, handler);
      router.registerHandler(MessageType.CHAT, handler);
      router.routeMessage(new ReadyMsg() );
      router.routeMessage(new ChatMsg("tester","Chat") );
      assertTrue("Handle count should be 2", (handler.eventCount==2) );
      handler.eventCount = 0;
      router.removeHandler(handler);
      router.routeMessage(new ReadyMsg() );
      router.routeMessage(new ChatMsg("tester","Chat") );
      assertTrue("Handle count should be 0", (handler.eventCount==0) );
   }
   
   public void testRemoveDefault()
   {
      MessageRouter router = new MessageRouter();
      MockHandler handler = new MockHandler();
      router.setDefaultHandler(handler);
      router.routeMessage(new ReadyMsg() );
      assertTrue("Handle count should be 1", (handler.eventCount==1) );
      handler.eventCount = 0;
      router.removeDefaultHandler();
      router.routeMessage(new ReadyMsg() );
      assertTrue("Handle count should be 0", (handler.eventCount==0) );
   }
   
   public void testUnregister()
   {
      MessageRouter router = new MessageRouter();
      MockHandler handler = new MockHandler();
      router.registerHandler(MessageType.READY, handler);
      router.registerHandler(MessageType.CHAT, handler);
      router.routeMessage(new ReadyMsg() );
      router.routeMessage(new ChatMsg("tester","Chat") );
      assertTrue("Handle count should be 2", (handler.eventCount==2) );
      handler.eventCount = 0;
      router.unregisterHandler(MessageType.READY, handler);
      router.routeMessage(new ReadyMsg() );
      router.routeMessage(new ChatMsg("tester","Chat") );
      assertTrue("Handle count should be 1", (handler.eventCount==1) );
   }

   public void testUnregister2()
   {
      MessageRouter router = new MessageRouter();
      MockHandler handler = new MockHandler();
      MockUnregisterHandler handler2 = 
         new MockUnregisterHandler(MessageType.READY, router);
      router.registerHandler(MessageType.READY, handler2);
      router.registerHandler(MessageType.READY, handler);
      router.routeMessage(new ReadyMsg() );
      assertTrue("Handle count should be 1", (handler.eventCount==1) );
      assertTrue("Handler2 count should be 1", (handler2.eventCount==1) );
      handler.eventCount = 0;
      handler2.eventCount = 0;
      router.routeMessage(new ReadyMsg() );
      assertTrue("Handle count should be 1", (handler.eventCount==1) );
      assertTrue("Handler2 count should be 0", (handler2.eventCount==0) );
   }

   public void testRemoveAll()
   {
      MessageRouter router = new MessageRouter();
      MockHandler handler = new MockHandler();
      MockHandler handler2 = new MockHandler();
      router.registerHandler(MessageType.READY, handler);
      router.registerHandler(MessageType.READY, handler2);
      router.routeMessage(new ReadyMsg() );
      assertTrue("Handler count should be 1", (handler.eventCount==1) );
      assertTrue("Handler2 count should be 1", (handler2.eventCount==1) );
      handler.eventCount = 0;
      handler2.eventCount = 0;
      router.removelAllHandlers();
      router.routeMessage(new ReadyMsg() );
      assertTrue("Handler count should be 0", (handler.eventCount==0) );
      assertTrue("Handler2 count should be 0", (handler2.eventCount==0) );
   }
   
   private class MockHandler implements IMessageHandler
   {
      public int eventCount;
      
      public void handleMessage(Message msg)
      {
         eventCount++;
      }
   }
   
   private class MockUnregisterHandler implements IMessageHandler
   {
      public int eventCount;
      public MessageRouter router;
      public MessageType msgType;
      public MockUnregisterHandler(MessageType t, MessageRouter r)
      {
         router = r;
         msgType = t;
      }
      public void handleMessage(Message msg)
      {
         eventCount++;
         router.unregisterHandler(msgType, this);
      }
   }
}
