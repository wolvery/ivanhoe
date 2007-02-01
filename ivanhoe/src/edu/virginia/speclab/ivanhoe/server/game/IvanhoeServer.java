//
// IvanhoeServer
// Main server process for Ivanhoe 
// Author: Lou Foster
// Date  : 10/01/03 
//

package edu.virginia.speclab.ivanhoe.server.game;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

import java.util.Properties;

import edu.virginia.speclab.ivanhoe.shared.*;
import edu.virginia.speclab.ivanhoe.shared.database.DBManager;
import edu.virginia.speclab.ivanhoe.shared.message.*;
import edu.virginia.speclab.ivanhoe.server.AdminListener;
import edu.virginia.speclab.ivanhoe.server.ProxyMgr;
import edu.virginia.speclab.ivanhoe.server.exception.MapperException;

/**
 * @author lfoster
 *
 * This is the main server process for Ivanhoe. Its porpose is to
 * configure and manage one or many games of Ivanhoe. It will
 * also broadcast all of the games it has available using UDP
 */
public class IvanhoeServer
{
   private AdminListener adminListener;
   private String discourseFieldRoot;
   private ProxyMgr proxyMgr;

   private String name;
   private int basePort;
   private boolean mailEnabled;
   private String mailHost;
   private String mailFrom;
   
   private ServerSocket socket;
   private volatile boolean running;
   
   private static final String PROPERTIES_FILE = System.getProperty("IVANHOE_DIR",".")+"/ivanhoe.properties";
   
   public static IvanhoeServer instance = null;

   /**
    * MAIN - start point for the IvanhoeServer
    * @param args
    */
   public static void main(String[] args)
   {
      IvanhoeServer.instance = new IvanhoeServer();
      if (IvanhoeServer.instance.init() == false)
      {
         System.err.println("Unable to start server, see log for details");
         System.exit(-1);
      }
   
      SimpleLogger.logInfo("Server is ready");
      System.out.println("Ivanhoe game server is running");
      
      IvanhoeServer.instance.mainLoop();
   }

   /**
    * GameServer constructor. Initialize logging and
    * construct all components required to run the IvanhoeServer
    */
   private IvanhoeServer()
   {
      initLogging(false);
      SimpleLogger.logInfo("IvanhoeServer constructed");
   }
   
   /**
    * Initialize logging.
    * @param logToConsole log to console if true, otherwise log to file
    */
   public static void initLogging( boolean logToConsole )
   {       
       if( logToConsole ) 
       {
           SimpleLogger.initConsoleLogging();
       }
       else
       {
	       try
	       {
	          SimpleLogger.initFileLogging("IvanhoeServer.log");
	          
	       }
	       catch (IOException e)
	       {
	          System.err.println("Unable to init file logging, use console");
	          SimpleLogger.initConsoleLogging();
	       }  
       }       
   }

   /**
    * Initialize the server.
    * - Read all parameter from the properties file
    * - Init DB connections
    * - Start connection listening
    * - Initialize the discourse field
    * Any failures here mean that the server will terminate
    * @return True if all initializations completed successfully
    */
   private boolean init()
   {      
      SimpleLogger.logInfo("IvanhoeServer initializing...");

      Properties props = loadIvanhoeProperties();
      
      // error loading properties
      if( props == null ) return false;
      
      // grab the name and base port of this server
      this.name = props.getProperty("gameServerName");
      this.basePort = Integer.parseInt(props.getProperty("gameServerBasePort"));

      // initialize the root directory for the discourse field
      this.discourseFieldRoot = props.getProperty("discourseFieldRoot");
      
      // connect DB
      if (DBManager.instance.connect(
            props.getProperty("dbHost"),
            props.getProperty("dbUser"),
            props.getProperty("dbPass"),
            props.getProperty("dbName") ) == false)
      {
         SimpleLogger.logError("Unable to connect DB");
         return false;
      }
      
      // grab the email messenger configuration
      String enableString = props.getProperty("mailEnabled");
      if (enableString.equalsIgnoreCase("true"))
         this.mailEnabled =  true;
      else
         this.mailEnabled = false;
      this.mailHost = props.getProperty("mailHost");
      this.mailFrom = props.getProperty("mailFromAddress");
      
      // create the server wide proxy manager
      this.proxyMgr = new ProxyMgr();
     
      // listen for admin connections
      try
      {
         this.adminListener = new AdminListener(this, basePort+1);
      }
      catch (IOException e2)
      {
         SimpleLogger.logError("Unable to start admin listener", e2);
         return false;
      }
      
      try {
  		this.socket = new ServerSocket(getBasePort());
	  } 
      catch (IOException e1) {
          SimpleLogger.logError("Unable to create server socket", e1);
          return false;
	  }
      
      return true;
   }
   
   public static Properties loadIvanhoeProperties()
   {
       Properties props = null;

       // Open the server proprties file 
       try
       {
          File f = new File(PROPERTIES_FILE);
          FileInputStream is = new FileInputStream(f);
          if (is == null)
          {
             SimpleLogger.logError("Unable to open properties file");
             return null;
          }
          else
          {
             props = new Properties();
             props.load(is);
          }
       }
       catch (IOException e1)
       {
          SimpleLogger.logError(
             "Unable to load properties file: " + e1.toString());
          return null;
       }

       return props;
   }
   
       
   /**
    * @return Returns the name of this server
    */
   public String getName()
   {
      return this.name;
   }
   
   /**
    * @return Returns the base port for this server
    */
   public int getBasePort()
   {
      return this.basePort;
   }
   
   /**
    * @return Returns the diesourse field directory
    */
   public String getDiscourseFieldRoot()
   {
      return this.discourseFieldRoot;
   }
      
   /**
    * Accept connections and add them to a proxy list
    */
   private void mainLoop()
   {
	  
      Socket client = null;
      this.running = true;
      while (this.running == true)
      {
         client = null;
         
         try
         {
        	// when a new connection is formed, spawn a game object which will handle
        	// this communication session with the client.
            client = this.socket.accept();
            
            UserProxy userProxy = null;
            try
            {
               userProxy = new UserProxy();
               userProxy.connect(new CommEndpoint(client));
               this.proxyMgr.addProxy( userProxy );
               userProxy.registerDisconnectHandler( this.proxyMgr );
               
               // enable the proxy
               userProxy.setEnabled(true);       
            }
            catch (MapperException e)
            {
          	  userProxy.disconnect();
            }   
         }
         catch (IOException e)
         {
            SimpleLogger.logInfo("Socket closed");
         }
      }
   }
   
   /**
    * Shutdown the server
    */
   public void shutdown()
   {
      SimpleLogger.logInfo("Server shutting down...");
      
      // stop admin stuff
      this.adminListener.stopListening();
      
      this.proxyMgr.removeAllProxies();
      
      // close DB
      DBManager.instance.disconnect();
      SimpleLogger.logInfo("Shutdown complete");
      System.exit(0);
   }

   /**
    * Broadcast a message to all users in all games
    * @param msg
    */
   public void broadcastMessage(Message msg)
   {
	   this.proxyMgr.broadcastMessage(msg);   	
   }
   
   /**
    * AdminHandler interface implementation of systemAnnouncement
    * allows remote administrator broadcast a message to all online players
    */
   public void systemAnnouncement(String message)
   {
      SimpleLogger.logInfo("Administrator is broadcasting a message");
      ChatMsg chat = new ChatMsg("Game Admin", message);
      chat.setSender("ADMIN");
      broadcastMessage(chat);
   }

   /**
    * Retire a game
    */
   public boolean retireGame()
   {
      // now mark as retired
    // GameMapper mapper = new GameMapper();
      
      //TODO refactor this to not rely on state
      //boolean success = mapper.retireGame(ivanhoeGame.getInfo().getName());

//      // shut down the server
//      if( success ) shutdown();
//      
//      return success;
      
      return false;
   }
   
   /**
    * delete a game
    */
   public boolean deleteGame()
   {
     // // now mark as deleted
   //   GameMapper mapper = new GameMapper();

      //TODO refactor this to not rely on state
      //boolean success = mapper.deleteGame(ivanhoeGame.getInfo().getName());

//      // shut down the server
//      if( success ) shutdown();
//      
//      return success;
      
      return false;
   }
   
   public void kickAllPlayers()
   {
	   //TODO fix
//	   ivanhoeGame.kickAllPlayers();
   }
   
   /**
    * kick a player out of the system
    */
   public boolean kickPlayer(String kickedPlayer)
   {
	   	//TODO fix
//      SimpleLogger.logInfo("Got kickout request for ["+kickedPlayer+"]");
//	     if (ivanhoeGame.isPlayer(kickedPlayer))
//	     {
//	    	 ivanhoeGame.kickPlayer(kickedPlayer);
//	        return true;
//	     }
      return false;    
   }
	
	public ProxyMgr getProxyMgr() {
		return proxyMgr;
	}

}
