//
// UserProxy
// Server-side Proxy to Ivanhoe game client
// Author: Lou Foster
// Date  : 10/01/03 
//
package edu.virginia.speclab.ivanhoe.server.game;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

import edu.virginia.speclab.ivanhoe.server.Authenticator;
import edu.virginia.speclab.ivanhoe.server.exception.MapperException;
import edu.virginia.speclab.ivanhoe.server.mapper.MoveMapper;
import edu.virginia.speclab.ivanhoe.server.mapper.UserMapper;
import edu.virginia.speclab.ivanhoe.shared.*;
import edu.virginia.speclab.ivanhoe.shared.data.Move;
import edu.virginia.speclab.ivanhoe.shared.data.Role;
import edu.virginia.speclab.ivanhoe.shared.data.User;
import edu.virginia.speclab.ivanhoe.shared.message.*;

public class UserProxy extends AbstractProxy
{
   private File         journalFile;
   private File         tempFile;
   protected User       userData;
   protected int        gameId;
   private Role			currentRole;
   private Authenticator authenticator;
   private IvanhoeGame ivanhoeGame;
   
   public UserProxy() 
   	throws MapperException
   {
	   // used to authenticate incoming messages
	   this.authenticator = new Authenticator();
	}
   
   /**
    * Get the identity of this proxy
    * @return The string representation of the proxy identity (userName)
    */
   public String getID()
   {
      if (this.userData != null)
      {
         return this.userData.getUserName();
      }
 
      SimpleLogger.logError("Requesting ID of proxy with invalid user data");
      return "Undefined";
   }
   
   public int getGameID() {
	   return this.gameId;
   }
   
   public Role getCurrentRole()
   {
       return currentRole;
   }
   
   public void setCurrentRole( Role role )
   {
       currentRole = role;
   }
   
   /**
    * Handles messages arriving from the client associated 
    * with this proxy
    * @param msg - the incoming message
    */
   public synchronized void receiveMessage(Message msg)
   {
      SimpleLogger.logInfo("UserProxy [" + getID() +
            "] got [" + msg.toString() + "]" );
      
      try
      {
	      // first authenticate the sender of this message, drop it if auth fails.
	      if( this.authenticator.performAuthentication(msg.getSender(), msg.getPassword(), true).length() != 0 ) {
	    	  disconnect();
	    	  return;
	      }
    	  
		   // if this is the first message recieved on this proxy, initialize 
	       if( this.userData == null ) {   	   
			   try {	
				   
				   // first disconnect any existing proxies with this username 
				   UserProxy existingProxy = IvanhoeServer.instance.getProxyMgr().getProxyByName(msg.getSender());
				   if ( existingProxy != null)
				   {
			         SimpleLogger.logInfo("Proxy [" + msg.getSender() +
			             "] already exists, bumping."); 
			             
			         // if problem removing proxy, disconnect
			         if (IvanhoeServer.instance.getProxyMgr().removeProxy(existingProxy) == false)
			         {
			            SimpleLogger.logError("Unable to remove duplicate proxy"); 
			            disconnect();
			            return;
			         }    
				   }
				    
				   // load more information about this user 
				   this.userData = UserMapper.getByName(msg.getSender());
				   
				   // the game this user is playing in
				   this.gameId = msg.getGameID();
				   
				   // the ivanhoe game object will process most messages
				   this.ivanhoeGame = new IvanhoeGame(this.gameId);
				   setDefaultMsgHandler( ivanhoeGame );
				   
				   // register the receiver for all document & image data from each player
			       registerMsgHandler(MessageType.DOCUMENT_DATA, ivanhoeGame.getReceiver());
			       registerMsgHandler(MessageType.IMAGE_DATA, ivanhoeGame.getReceiver());
	
				   SimpleLogger.logInfo("Successfully loaded [" + 
				      this.userData.toString() + "");
		
				   // init journal files
				   this.journalFile = new File(this.getID() + "-game" + 
				      this.gameId + "-journal.html");
				   this.tempFile = new File(this.getID() + "-game" + 
				      this.gameId + "-scratch.html");
			   }
			   catch( MapperException e ) {
				   disconnect();
				   return;
			   }
	      }
       
          // user proxy gets first shot at handling incomming msgs
          if ( msg.getType().equals(MessageType.LOGOUT))
          {
             receiveLogout();
          }
          else if ( msg.getType().equals(MessageType.USER_ONLINE))
          {
             receiveUserOnline();
          }
          else if ( msg.getType().equals(MessageType.JOURNAL_DATA))
          {
             receiveJournalData((JournalDataMsg)msg);
          }
          else if ( msg.getType().equals(MessageType.SAVE))
          {
             receiveSaveGame( (SaveMsg)msg );
          }
          
          // give other handlers a chance to handle the msg
          routeMessage(msg); 
      }
      // something this player did resulted in an exception, stay alive but let them go
      catch( RuntimeException e )
      {       
           String player = null;
           if( msg != null )
           {
               player = msg.getSender();
           }
           
           handleUncaughtException(player,e);
      }
   }

   // report the error in the log and disconnect this session
   private void handleUncaughtException( String player, Exception e )
   {
       String stackTrace = e.toString() + ":\n";
       StackTraceElement[] trace = e.getStackTrace();
       for( int i=0; i<trace.length; i++ )
       {
           stackTrace += trace[i] + "\n";
       }
       SimpleLogger.logError("Uncaught exception "+stackTrace);
       
       if( player != null )
       {
           SimpleLogger.logError("Disconnecting player: "+player);
           disconnect();
       }
       else
       {
           SimpleLogger.logError("Unknown player cause uncaught exception!");
       }
       
   }
   /**
    * Save user data
    * @param msg
    */
   private void receiveSaveGame(SaveMsg msg)
   {        
      Move moveInProgress = msg.getMove();
      boolean success = false;
      try
      {
          Move move =
              new MoveMapper().insert(this.gameId, moveInProgress, msg.getDocumentVersionOrigins(), false);
          success = (move != null);
      }
      catch (Exception e)
      {
          SimpleLogger.logError("Exception while saving game", e);
          success = false;
      }
      
      if (success)
      {
          SimpleLogger.logInfo("Game state saved for player " + msg.getSender());
      }
      else
      {
         SimpleLogger.logInfo("Unable to save game for player " + msg.getSender());
      }
   }

   private void receiveJournalData(JournalDataMsg msg)
   {   
      // re-assemble file in local filesystem
      FileWriter writer = null;
      try
      {
         // write the chunk of data to a local temp file
         writer = new FileWriter(this.tempFile, true);
         String s = new String(msg.getDataBufer());
         writer.write(s, 0, msg.getBufferSize());     
      }
      catch (IOException e)
      {
         SimpleLogger.logError("Unable to save journal data: " + e);
      }
      finally
      {
         if (writer != null)
         {
            try
            {
               writer.flush();
               writer.close();
            }
            catch (IOException e2 ) {}
         }
      }
      
      // if data is complete, move tmp file to actual file
      if (msg.isComplete())
      {
         SimpleLogger.logInfo("Journal for " + getID() 
               + " is complete; renaming scratch file");
         if (this.journalFile.exists())
            this.journalFile.delete();
         if (this.tempFile.renameTo(this.journalFile) == false)
            SimpleLogger.logError("Cant rename");
         
      }
   }

   private void sendJournalToPlayer()
   {
      if (this.journalFile.exists() == false)
      {
         SimpleLogger.logInfo("No journal for player " + getID() );
         return;
      }
      
      FileInputStream source = null;
      try
      {
         source = new FileInputStream( this.journalFile );
         JournalDataMsg dataMsg;
         boolean done = false;
         int bytesRead;
         while (!done)
         {
            dataMsg = new JournalDataMsg();
            bytesRead = source.read(dataMsg.getDataBufer());
            if (bytesRead == -1 || bytesRead < JournalDataMsg.MAX_DATA_SIZE)
            {
               if (bytesRead < 0)
                  bytesRead = 0;
               dataMsg.setComplete();
               done = true;
            }

            dataMsg.setBufferSize(bytesRead);

            // send the info up to the requestor
            sendMessage(dataMsg);
         }
      }
      catch (IOException e)
      {
         SimpleLogger.logError(
            "Error handling request for journal]: " + e);
      }
      finally
      {
         if (source != null)
         {
            try
            {
               source.close();
            }
            catch (IOException e1)
            {
            }
         }
      }
      
   }

   private void receiveUserOnline()
   {
      sendJournalToPlayer();
   }

   private void receiveLogout()
   {
      SimpleLogger.logInfo("User [" + userData.getUserName() + "] has logged out");
      this.disconnect();
   }
   
   
   public void disconnect()
   {
       super.disconnect();
       
       if (getCurrentRole() != null)
       {
           //TODO this isn't sending a message.. implications?
	       // broadcast left msg to everyone
	       RoleLeftMsg leftMsg = new RoleLeftMsg();
	       leftMsg.setRoleName(getCurrentRole().getName());
       }
   }

  
/**
 * @return Returns the userData.
 */
public User getUserData()
{
    return userData;
}

public IvanhoeGame getIvanhoeGame() {
	return ivanhoeGame;
}
}
