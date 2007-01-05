/*
 * Created on Oct 2, 2003
 *
 * AbstractProxy
 */
package edu.virginia.speclab.ivanhoe.shared;

import java.io.IOException;
import java.io.StreamCorruptedException;
import java.net.SocketTimeoutException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import edu.virginia.speclab.ivanhoe.shared.message.*;

/**
 * AbstractProxy
 * @author lfoster
 * @version
 * 
 * Abstract base class for a proxy
 */
public abstract class AbstractProxy implements Runnable
{
   private CommEndpoint commEndpoint;
   private Vector disconnectListeners;
   private Thread runner;
   private boolean enabled;
   private MessageRouter router;
   private String hostname;
   private int port;
   
   /**
    * Construct an unconnected proxy
    */
   public AbstractProxy() 
   {
      this((CommEndpoint)null);
   }
   
   /**
    * Create a new proxy using the connection from the source
    * @param that
    */
   public AbstractProxy(AbstractProxy src)
   {
      this(src.commEndpoint);
   }
   
   /**
    * Construct a proxy with an existing connection
    * @param endpoint
    */
   public AbstractProxy(CommEndpoint endpoint)
   {
      this.router = new MessageRouter();
      this.disconnectListeners = new Vector();
      this.commEndpoint = endpoint; 
      this.runner = null;
      this.enabled = false;
      this.hostname = "";
      this.port = 0;
   }
   
   /**
    * Connect to the specified host/port
    * @param host
    * @param port
    * @return true if successful connection, false otherwise
    */
   public boolean connect(String host, int port)
   {
      this.hostname = host;
      this.port = port;
       
      // drop a pre-existing connection
      if (isConnected() == true)
      {
         SimpleLogger.logError(getID() + " attempting a connection when already connected. Failed.");
         return false;
      }
      
      //make a new connection
      boolean success = false;
      try
      {
         this.commEndpoint = new CommEndpoint(host, port);
         success  = true;
      }
      catch (IOException e)
      {
         success = false;
      }

      return success;
   }
   
   /**
    * Connect to the specified comm endpoint
    * @param handler
    */
   public void connect(CommEndpoint endpoint)
   {
      // drop a pre-existing connection
      if (isConnected())
      {
         SimpleLogger.logError(getID() + " attempting a connection when already connected. Failed.");
         return;
      }
      
      this.commEndpoint = endpoint;
   }
   
   private void enableProxy()
   {
       SimpleLogger.logInfo("Enabling proxy " + getID() );
       if (this.enabled == false)
       {
          this.enabled = true;
          this.runner = new Thread(this);
          this.runner.start();
       }
       else
       {
          SimpleLogger.logError("Proxy " + getID() + " is already enabled");
       }
   }
   
   private void disableProxy()
   {
       SimpleLogger.logInfo("Disabling proxy " + getID());

       if (this.enabled)
       {
          try
          {
             this.enabled = false;
             this.runner.interrupt();
             this.runner.join(5000);
          }
          catch (InterruptedException e1){}
          finally
          {
             this.runner = null;
          }
       }
   }
   
   /**
    * Enables or disable this proxy. A disabled proxy is still connected,
    * but connot send or receive messages
    */
   public synchronized void setEnabled(boolean enabled)
   {
      if (enabled)
      {
          enableProxy();
      }
      else
      {
          disableProxy();
      }
   }
      
   /** 
    * register a class that will be notified when this proxy diesonnects
    * @param handler 
    */
   public void registerDisconnectHandler(IDisconnectListener handler)
   {
      if (this.disconnectListeners.contains(handler))
      {
         SimpleLogger.logInfo("Handler[" + handler.toString() + 
            "] is already registered for "+this.getID()+" disconnect events");
      }
      else
      {
          SimpleLogger.logInfo("Handler["+handler.getClass().getName()+
                  "] registered for "+this.getID()+" disconnect events");
          this.disconnectListeners.add(handler);
      }
   }
   
   /** 
    * unregister the disconnect handler
    * @param handler
    */
   public void unregisterDisconnectHandler(IDisconnectListener listener)
   {
      this.disconnectListeners.remove(listener);
   }
   
   /**
    * Send a message to the client represented by this proxy
    * @param msg - the message that will be sent to the client
    * @return true if message was successfully sent, false otherwise
    */
   public boolean sendMessage(Message msg)
   {
      // dont send messages when disabled
      if (this.enabled == false)
      {
         SimpleLogger.logError(getID() + " attempted to send message when disabled. Message not sent");
         return false;
      }
      
      // dont send messages if not connected
      if (this.isConnected() == false)
      {
         SimpleLogger.logInfo("Proxy [" + getID() + "] sending message [" +
            msg.toString() + "] when disconnected.Failed");
         return false;
      }
      
      SimpleLogger.logInfo("Proxy [" + getID() + "] sending message [" +
         msg.toString() + "]");
       
      try
      {
         this.commEndpoint.sendMessage(msg);
      }
      catch (IOException e)
      {
         SimpleLogger.logError("Proxy [" + getID() +
            "] unable to send message: " + e);
         disconnect();
         return false;
      }
      
      return true;
   }
   
   /**
    * @return returns true if connected, false otherwise
    */
   public synchronized boolean isConnected()
   {
      if (this.commEndpoint == null)
         return false;
         
      return this.commEndpoint.isConnected();
   }
   
   /**
    * @return Returns true if the proxy is enabled
    */
   public synchronized boolean isEnabled()
   {
      return this.enabled;
   }
   
   /**
    * Close the connection maintained by this proxy
    * and notify registered disconnect listener
    */
   public synchronized void disconnect()
   {
      if (isConnected())
      {
         // interrupt socket & thread 
         if ( this.runner != null )
         {
             disableProxy();
         }
         
         // disconnect the endpoint
         if (this.commEndpoint != null)
         {
            this.commEndpoint.disconnect();
            this.commEndpoint = null;
         }
         
         // notify all
         notifyDisconnectListeners();
         this.notifyAll();
      }      
   }
   
   /**
    * Notify all listeners that this proxy has disconnected
    */
   private void notifyDisconnectListeners()
   {
      Iterator itr = ((Vector)this.disconnectListeners.clone()).iterator();
      while (itr.hasNext())
      {
         ((IDisconnectListener)itr.next()).notifyDisconnect(this);
      }
   }

   /**
    * Loop to await input on the endpoint owned by the proxy
    */
   public void run()
   {
      // input handling loop
      while ( isConnected() && isEnabled() )
      {
         try
         {
            Message msg = this.commEndpoint.getMessage();
            if (msg != null && isEnabled())
            {               
               receiveMessage(msg);               
            }
            else
            {
               SimpleLogger.logError("Proxy [" + getID() + 
                  "] got null input; ignoring");
            }
         }
         catch (SocketTimeoutException ste)
         {
             SimpleLogger.logError("Socket timed out for " + getID() );    
         }
         catch (StreamCorruptedException sce)
         {
            String message = "Stream corrupted exception for " + getID();
            Throwable cause = sce.getCause();
            if( cause != null )
            {
                message += cause.toString();
            }
            	
            else
            SimpleLogger.logError(message);
            disconnect(); 
         }
         catch (IOException ioe)
         {
            SimpleLogger.logInfo(
               "End of stream for [" + getID() + "] " + ioe.toString());
            SimpleLogger.logInfo("Cause: " + 
               ioe.getLocalizedMessage());
            disconnect();
         }
      }
      
      this.runner = null;
      this.enabled = false;
      SimpleLogger.logInfo("Thread for proxy [" + getID() + "] has terminated");
   }
   
   public abstract void receiveMessage(Message msg);
   public abstract String getID();
   
   /**
    * Routes a message to registerd handlers
    * @param msg
    */
   protected void routeMessage(Message msg)
   {
      if (isEnabled() == false)
      {
         SimpleLogger.logInfo("Proxy [" + getID() + 
            "] routed message when disabled");
      }
      this.router.routeMessage(msg);
   }
   
   /**
    * Sets the default message handler. This handler is called for everry type
    * of message received
    * @param handler
    * @return
    */
   public void setDefaultMsgHandler(IMessageHandler handler)
   {
      this.router.setDefaultHandler(handler);
   }
   
   /**
    * Removes the default message handler
    * @param handler
    * @return
    */
   public void removeDefaultMsgHandler()
   {
      this.router.removeDefaultHandler();
   }
   
   /**
    * register a handler the specified message type
    * @param msgType - The type of the message
    * @param handler - Handler for the above type
    * @return true if successfully registerd, false otherwise
    */
   public boolean registerMsgHandler(MessageType msgType, IMessageHandler handler)
   {
      return router.registerHandler(msgType, handler);
   }
   
   /**
    * Unregister a a handler for the specified message type.
    * @param msgType
    * @param handler
    * @return
    */
   public boolean unregisterMsgHandler(
      MessageType msgType,
      IMessageHandler handler)
   {
      return router.unregisterHandler(msgType, handler);
   }

   /**
    * remove all message handling for the specifed handler; If this
    * handler was registerd for several different event type, all 
    * would be unregisterd by this method
    * @param handler
    * @return
    */
   public boolean removeMsgHandler(IMessageHandler handler)
   {
      return router.removeHandler(handler);
   }
   
   
   /**
    * Removes all disconnect and message handlers from this proxy
    */
   public void removeAllHandlers()
   {
      this.router.removelAllHandlers();
      this.disconnectListeners.removeAllElements();
   }
   
   protected Collection getDisconnectListeners()
   {
       return (Collection)disconnectListeners.clone();
   }
   
    public String getHostname()
    {
        return hostname;
    }
    public int getPort()
    {
        return port;
    }
}
