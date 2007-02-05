//
// ProxyMgr
// Manage a list of proxies connected to the server
// Author: Lou Foster
// Date  : 10/01/03 
//
package edu.virginia.speclab.ivanhoe.server;

import java.util.*;

import edu.virginia.speclab.ivanhoe.server.game.UserProxy;
import edu.virginia.speclab.ivanhoe.shared.*;
import edu.virginia.speclab.ivanhoe.shared.data.Role;
import edu.virginia.speclab.ivanhoe.shared.message.*;

public class ProxyMgr implements IDisconnectListener 
{
   private List proxies;
   
   public ProxyMgr()
   {
      this.proxies = Collections.synchronizedList(new ArrayList());
   }   
   
   /**
    * Send a message to every proxy currently managed
    * @param msg
    */
   public void broadcastMessage( Message msg )
   {
      synchronized ( this.proxies ) {
          for (Iterator itr = this.proxies.iterator(); itr.hasNext(); )
          {
             // send the message to each
             UserProxy proxy = (UserProxy)itr.next();
             if( proxy.getGameID() == msg.getGameID() ) {
                 proxy.sendMessage(msg);            	 
             }
          }
      }
   }
   
   /**
    * Send a message to every proxy currently managed, except the one named
    * @param skipName The name of the proxy to skip
    * @param msg Message to broadcast
    */
   public void broadcastMessageToOthers( String skipName, Message msg )
   {
      synchronized ( this.proxies ) {
          for (Iterator itr = this.proxies.iterator(); itr.hasNext(); )
          {
             // send the message to each
             UserProxy proxy = (UserProxy)itr.next();
             if (proxy.getID().equals(skipName) == false)
             {
            	 if( proxy.getGameID() == msg.getGameID() ) {
                     proxy.sendMessage(msg);            	 
                 }
             }
          }
      }
   }
   
   /**
    * Send a message to a specific proxy identified by targetName
    * @param targetName
    * @param msg
    * @return
    */
   public boolean sendTo(String targetName, Message msg)
   {
      boolean found = false;
      boolean success = false;
      
      synchronized ( this.proxies ) {
          for (Iterator itr = this.proxies.iterator(); itr.hasNext(); )
          {
             // send the message to each
             UserProxy proxy = (UserProxy)itr.next();
             if ( proxy != null && proxy.getID().equals(targetName)  )
             {
                success = proxy.sendMessage(msg);
                found = true;
                break;
             }
          }
      }
      
      return (found && success);
   }
   
   /**
    * Get a list of names that are currently managed
    * @return the list of names
    */
   public List getNames(int gameID)
   {
      List list = new ArrayList();
      synchronized ( this.proxies ) {
          for (Iterator itr = this.proxies.iterator(); itr.hasNext(); )
          {
             // send the message to each
             UserProxy proxy = (UserProxy)itr.next();
             if( proxy.getGameID() == gameID ) {
                 list.add(proxy.getID());
             }
          }
      }
      
      return list; 
   }
   
   /** 
    * Test if the proxy identifed by proxyName is managed
    * @param proxyName
    * @return true if proxy is managed, false otherwise
    */
   public UserProxy getProxyByName(String proxyName)
   {
	  UserProxy proxy = null;
      synchronized ( this.proxies ) {
          for (Iterator itr = this.proxies.iterator(); itr.hasNext();)
          {
        	  UserProxy testProxy = (UserProxy)itr.next();
             if (testProxy.getID().equals(proxyName))
             {
                proxy = testProxy;
                break;
             }
          }
      }
      return proxy;
   }
   
   public void notifyDisconnect( AbstractProxy proxy)
   {
       if( proxy instanceof UserProxy )
       {
           UserProxy userProxy = (UserProxy) proxy;
           RoleLeftMsg roleLeftMessage = new RoleLeftMsg();
           
           Role role = userProxy.getCurrentRole();            
           if( role != null )
           {
               roleLeftMessage.setRoleName(role.getName());
               roleLeftMessage.setGameID(userProxy.getGameID());
               broadcastMessage(roleLeftMessage);
           }
           
           SimpleLogger.logInfo("Proxy [" + proxy.getID() + 
    	   "] has disconnected; removing from manager");
    	   proxy.unregisterDisconnectHandler(this);
    	   removeProxy(userProxy);  
       }    
    }
   
   /**
    * Add a new proxy to the manager. If a proxy with the same name 
    * already exists, it will be replaced by the new proxy.
    * @param proxy
    */
   public boolean addProxy(UserProxy proxy)
   {
      if ( proxy == null )
      {
         SimpleLogger.logError("addProxy called with null proxy"); 
         return false;  
      }
      
      SimpleLogger.logInfo("Adding proxy [" + proxy.getID() + "]"); 
      this.proxies.add(proxy);  
      
      return true;
   }
   
   /**
    * Remove a proxy from the manager
    * @param proxy
    * @return true if proxy was successfully removed, false otherwise
    */
   public boolean removeProxy(UserProxy proxy)
   { 
      if (proxy == null)
      {
         SimpleLogger.logError("removeProxy called with null proxy"); 
         return false ; 
      }
      
      SimpleLogger.logInfo("Removing proxy [" + proxy.getID() + "]");
      proxy.unregisterDisconnectHandler(this);
      if (this.proxies.remove( proxy ) )
      {
         if (proxy.isConnected())
         {
             proxy.disconnect();
         }
            
         
         return true;
      }
      else
      {
         SimpleLogger.logError("Unable to remove proxy");
      }
      
	  return false;      
   }
   
   /**
    * Disconnect all proxies and clear the managed list
    */
   public synchronized void removeAllProxies()
   {
       SimpleLogger.logInfo("Removing all proxies");
      
      synchronized ( this.proxies ) {
          for (Iterator itr = this.proxies.iterator(); itr.hasNext();)
          {
             UserProxy proxy = (UserProxy)itr.next();
             
             proxy.unregisterDisconnectHandler(this);
             SimpleLogger.logInfo("   Removing [" + proxy.getID() + "]");
             if (proxy.isConnected())
             {
                SimpleLogger.logInfo("   Disconnect required");
                proxy.disconnect();
             }
      }
      }
      
      //TODO this is wrong
      this.proxies.clear();
   }
   
   /**
    * Disconnect all proxies in the specified game
    */
   public synchronized void removeAllProxies( int gameID )
   {
      SimpleLogger.logInfo("Removing all proxies");
      
      synchronized ( this.proxies ) {
          for (Iterator itr = this.proxies.iterator(); itr.hasNext();)
          {
             UserProxy proxy = (UserProxy)itr.next();
             
             if( gameID == proxy.getGameID() ) {            	 
	             proxy.unregisterDisconnectHandler(this);
	             SimpleLogger.logInfo("   Removing [" + proxy.getID() + "]");
	             if (proxy.isConnected()) {
	                SimpleLogger.logInfo("   Disconnect required");
	                proxy.disconnect();
	             }
	             this.proxies.remove(proxy);
             }
	      }
      }      
   }



   /**
    * Get number of proxies in the manager
    * @return
    */
   public int getNumProxies(int gameID)
   {
	  int count = 0;
      synchronized ( this.proxies ) {
          for (Iterator itr = this.proxies.iterator(); itr.hasNext();)
          {
        	  UserProxy testProxy = (UserProxy)itr.next();
             if (testProxy.getGameID()==gameID)
             {
            	 count++;
             }
          }
      }
      return count;
   }

public List getProxies() {
	return proxies;
}
}
