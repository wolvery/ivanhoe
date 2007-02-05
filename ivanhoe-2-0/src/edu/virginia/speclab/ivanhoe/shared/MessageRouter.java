//
// MessageRouter
// Receive messages and deal out to registered handlers
// Author: Lou Foster
// Date  : 10/03/03 
//

package edu.virginia.speclab.ivanhoe.shared;

import java.util.*;

import edu.virginia.speclab.ivanhoe.shared.message.*;

/**
 * MessageRouter
 * @author lfoster
 *
 * MessageRouter is a helper clas that provides a mechanism for 
 * messages to be handed off to the currect handlers
 */
public final class MessageRouter
{
   private HashMap handlerMap;
   private IMessageHandler defaultHandler;
   
   public MessageRouter()
   {
      this.handlerMap = new HashMap(10);
   }
   
   /**
    * Register a default handler that will be called when 
    * no other handlers can process a message
    * @param handler
    */
   public void setDefaultHandler(IMessageHandler handler)
   {
      this.defaultHandler = handler;
   }
   
   /**
    * Remove the default message handler
    */
   public void removeDefaultHandler()
   {
      this.defaultHandler = null;
   }
   
   /**
    * Register a handler for messages of type msgType
    * @param msgType
    * @param handler
    * @return true if successfully registered
    */
   public boolean registerHandler(MessageType msgType, IMessageHandler handler)
   {
       if (handler == null)
       {
           return false;
       }
       
      boolean success = false;
      
      if (this.handlerMap.containsKey(msgType))
      {
         Vector handlerList = (Vector)this.handlerMap.get(msgType);  
         if (handlerList.contains( handler ) )
         {
            success = false;
         }
         else
         {
            handlerList.addElement(handler);
            success = true;
         }
      }
      else
      {
         Vector handlerList = new Vector();
         handlerList.addElement(handler);
         this.handlerMap.put(msgType, handlerList);
         success = true;
      }
      
      return success;
   }
   
   /**
    * Unregister the specified handler for the given message type
    * @param msgType
    * @param handler
    * @return
    */
   public boolean unregisterHandler(MessageType msgType, IMessageHandler handler)
   {
      Vector handlers = findHandlers(msgType);
      if (handlers != null)
      {
         // remove the handler from the list, then reset the handler list
         handlers.removeElement(handler);
         this.handlerMap.put(msgType, handlers);
         return true;
      }
      
      return false;
   }
   
   /**
    * Remove handler for ALL types of messages
    * @param handler
    * @return
    */
   public boolean removeHandler(IMessageHandler handler)
   {
      Set keySet = this.handlerMap.keySet();
      MessageType key = null;
      Iterator itr = keySet.iterator();
      Vector handlers = null;
      while (itr.hasNext())
      {
         key = (MessageType)itr.next();
         handlers = (Vector)this.handlerMap.get(key);
         handlers.removeElement(handler);
      }
      return true;
   }
   
   /**
    * Send the message out to all handlers registerd for that message type
    * @param msg
    */
   public void routeMessage(Message msg)
   {
      Vector handlerList = findHandlers(msg.getType());
      if (handlerList != null)
      {
         Iterator itr2 = handlerList.iterator();
         while (itr2.hasNext())
         {
            // pass message to registerd handler
            ((IMessageHandler)itr2.next()).handleMessage(msg);
         }
      }
      
      // try the default handler 
      if (this.defaultHandler != null)
      {
         SimpleLogger.logInfo("Sending " 
            + msg.getType().toString() + " to default msg handler");
         this.defaultHandler.handleMessage( msg );
      }
   }
   
   /**
    * Find a list of handlers registerd for the given message type
    * @param msgType
    * @return Vector of registered handlers
    */
   private Vector findHandlers(MessageType msgType)
   {
      Vector result = null;
          
      // iterate over keys til a match is found
      Set keySet = this.handlerMap.keySet();
      MessageType key = null;
      Iterator itr = keySet.iterator();
      while (itr.hasNext())
      {
         key = (MessageType)itr.next();
         if ( msgType.equals(key) )
         {
            // Return a cloned list of handlers. The close is necessary
            // so a handler can unregister itself during handling
            result = (Vector)((Vector)this.handlerMap.get(key)).clone();
            break;
         }
      }
      
      return result;
   }

   public synchronized void removelAllHandlers()
   {
      this.defaultHandler = null;
      this.handlerMap.clear();
   }
} 
