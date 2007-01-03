//
// IvanhoeServer
// Main server process for Ivanhoe 
// Author: Lou Foster
// Date  : 10/01/03 
//

package edu.virginia.speclab.ivanhoe.server.game;

import java.io.*;

import java.util.Properties;

import edu.virginia.speclab.ivanhoe.shared.*;
import edu.virginia.speclab.ivanhoe.shared.data.GameInfo;
import edu.virginia.speclab.ivanhoe.shared.database.DBManager;
import edu.virginia.speclab.ivanhoe.shared.message.*;
import edu.virginia.speclab.ivanhoe.server.AdminListener;
import edu.virginia.speclab.ivanhoe.server.exception.MapperException;
import edu.virginia.speclab.ivanhoe.server.mapper.*;

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
   private IvanhoeGame ivanhoeGame;
  
   private String name;
   private int basePort;
   private boolean mailEnabled;
   private String mailHost;
   private String mailFrom;
   
   private static final String PROPERTIES_FILE = System.getProperty("IVANHOE_DIR",".")+"/ivanhoe.properties";

   /**
    * MAIN - start point for the IvanhoeServer
    * @param args
    */
   public static void main(String[] args)
   {
      IvanhoeServer server = new IvanhoeServer();
      if (server.init() == false)
      {
         System.err.println("Unable to start server, see log for details");
         System.exit(-1);
      }

      SimpleLogger.logInfo("Server is ready");
      System.out.println("Ivanhoe game server is running");
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
     
      // listen for admin connections
      try
      {
         this.adminListener = new AdminListener(this, basePort+1);
      }
      catch (IOException e2)
      {
         SimpleLogger.logError("Unable to start admin listener", e2);
      }

      // start up the actual game
      int gameID = Integer.parseInt(props.getProperty("gameID"));
      this.ivanhoeGame = startGame(gameID);

      return (this.ivanhoeGame != null);
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
    * Shutdown the server
    */
   public void shutdown()
   {
      SimpleLogger.logInfo("Server shutting down...");
      
      // stop admin stuff
      this.adminListener.stopListening();
      
      ivanhoeGame.shutdown();
      
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
         ivanhoeGame.broadcastMessage(msg);
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
      GameMapper mapper = new GameMapper();
      
      boolean success = mapper.retireGame(ivanhoeGame.getInfo().getName());

      // shut down the server
      if( success ) shutdown();
      
      return success;
   }
   
   /**
    * delete a game
    */
   public boolean deleteGame()
   {
      // now mark as deleted
      GameMapper mapper = new GameMapper();
      boolean success = mapper.deleteGame(ivanhoeGame.getInfo().getName());

      // shut down the server
      if( success ) shutdown();
      
      return success;
   }
   
   private IvanhoeGame startGame(int gameID) {
		GameMapper mapper = new GameMapper();
		IvanhoeGame game = null;

		try {
			GameInfo info = mapper.get(gameID);
			if( info == null ) {
				SimpleLogger.logError("invalid game id "+gameID+" unable to start game.");
			}
			else {
				if (info.isRetired() == false) {
					game = new IvanhoeGame(info, this);
					game.configureEmail(this.mailEnabled, this.mailHost,this.mailFrom);
					game.startup(this.basePort);
				} else {
					SimpleLogger.logError("Not staring retired game ["
							+ info.getName() + "]");
				}
			}
		} catch (MapperException e) {
			SimpleLogger.logError("Unable to lookup game", e);
		}
		return game;
	}
   
   public void kickAllPlayers()
   {
	   ivanhoeGame.kickAllPlayers();
   }
   
   /**
    * kick a player out of the system
    */
   public boolean kickPlayer(String kickedPlayer)
   {
      SimpleLogger.logInfo("Got kickout request for ["+kickedPlayer+"]");
	     if (ivanhoeGame.isPlayer(kickedPlayer))
	     {
	    	 ivanhoeGame.kickPlayer(kickedPlayer);
	        return true;
	     }
      return false;    
   }

   
}
