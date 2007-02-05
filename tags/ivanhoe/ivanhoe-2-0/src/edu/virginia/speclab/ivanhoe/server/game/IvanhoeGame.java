/*
 * Created on Jun 24, 2004
 *
 * IvanhoeGame
 */
package edu.virginia.speclab.ivanhoe.server.game;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import edu.virginia.speclab.ivanhoe.server.*;
import edu.virginia.speclab.ivanhoe.server.exception.*;
import edu.virginia.speclab.ivanhoe.server.mapper.*;
import edu.virginia.speclab.ivanhoe.shared.*;
import edu.virginia.speclab.ivanhoe.shared.data.*;
import edu.virginia.speclab.ivanhoe.shared.database.DBManager;
import edu.virginia.speclab.ivanhoe.shared.message.*;

/**
 * @author lfoster
 *
 * Class representing a game of Ivanhoe
 */
public class IvanhoeGame implements IMessageHandler, IDocumentHandler
{
   private static final int MAX_ROLE_NAME_SIZE = 50;
   
   private  ProxyMgr proxyMgr;
   private  DiscussionMgr discussion;
   private  Messenger messenger;
   private  String discourseFieldDir;
   private  DocumentReceiver receiver;

   private  GameInfo gameInfo;
   
   /**
    * Construct a new game instance
    * @param gameInfo the info describing this game
    * @param server the IvanhoeServer on which this game is hosted
    */
   public IvanhoeGame( int gameID ) throws MapperException  
   {      
	   this.proxyMgr = IvanhoeServer.instance.getProxyMgr();
	   	       
 	   //  if we don't have the game info yet, look it up
	   GameMapper gameMapper = new GameMapper();	   
	   this.gameInfo = gameMapper.get(gameID);
	   
       this.discourseFieldDir = IvanhoeServer.instance.getDiscourseFieldRoot() + File.separator + this.gameInfo.getName();
 	   this.receiver = new DocumentReceiver(discourseFieldDir, gameInfo.getId());
 	   this.receiver.addDocumentHandler(this);		   	     

 	   // create game components
 	   this.discussion = new DiscussionMgr(this);
 	   this.messenger = new Messenger(IvanhoeServer.instance.getName(), this); 	    
 	   
	   // add a game authentication rule if necessary
	      //TODO enable game access restrictions
//	      if (info.isRestricted() == true)
//	      {
//	         this.authenticator.addRule( new GameEntryCheck(this.info.getId(), this.info.getName()) );
//	      }
   }
   
   // TODO still need this?
//   private IvanhoeGame startGame(int gameID) {
//		GameMapper mapper = new GameMapper();
//		IvanhoeGame game = null;
//
//		try {
//			GameInfo info = mapper.get(gameID);
//			if( info == null ) {
//				SimpleLogger.logError("invalid game id "+gameID+" unable to start game.");
//			}
//			else {
//				if (info.isRetired() == false) {
//					game = new IvanhoeGame(info, this);
//					game.configureEmail(this.mailEnabled, this.mailHost,this.mailFrom);
//					game.startup(this.basePort);
//				} else {
//					SimpleLogger.logError("Not staring retired game ["
//							+ info.getName() + "]");
//				}
//			}
//		} catch (MapperException e) {
//			SimpleLogger.logError("Unable to lookup game", e);
//		}
//		return game;
//	}

   /**
    * Broadcast a message to all users in this game
    * @param msg
    */
   public void broadcastMessage(Message msg)
   {
	  msg.setGameID(this.getGameId());
      this.proxyMgr.broadcastMessage(msg);
   }
   

   /**
    * Public method used to send a message to a particular player
    * @param string
    * @param resp
    */
   public void sendMessageTo(String name, Message msg)
   {
      UserProxy player = (UserProxy)this.proxyMgr.getProxyByName( name);
      if (player != null)
      {
         player.sendMessage( msg );
      }
      else
      {
         SimpleLogger.logError("Unable to send message to player " + name +
            ". Not found in proxy mgr");
      } 
   }
   
   /**
    * handle gameplay messages
    */
   public void handleMessage(Message msg)
   {
	   if(msg.getType().equals(MessageType.LOGIN)) {
		   handleLogin(msg.getSender());
	   } 
	   else if (msg.getType().equals(MessageType.USER_ONLINE))
       {
          handleUserOnline(msg.getSender());
       }
       else if (msg.getType().equals(MessageType.CHAT))
       {
          handleChat((ChatMsg)msg);
       }
       else if (msg.getType().equals(MessageType.DOCUMENT_REQUEST))
       {
          handleDocumentRequest( (DocumentRequestMsg)msg);
       }
       else if (msg.getType().equals(MessageType.MOVE_SUBMIT))
       {
          handleMoveSubmission((MoveSubmitMsg)msg);
       }
       else if (msg.getType().equals(MessageType.CANCEL_MOVE))
       {
          handleMoveCancel((CancelMoveMsg)msg);
       }
       else if (msg.getType().equals(MessageType.DELETE_DOCUMENT))
       {
          handleDeleteDocument((DeleteDocumentMsg)msg);
       }
       else if (msg.getType().equals(MessageType.PLAYER_LIST_REQUEST))
       {
          handlePlayerListRequest((PlayerListRequestMsg)msg);
       }
       else if (msg.getType().equals(MessageType.ROLE_UPDATE))
       {
           handlePlayerRole((RoleUpdateMsg)msg);
       }
       else if( msg.getType().equals(MessageType.NEW_ROLE_REQUEST))
       {
           handleNewRoleRequest((NewRoleRequest)msg);
       }
       else if( msg.getType().equals(MessageType.SELECT_ROLE))
       {
           handleSelectRole((SelectRoleMsg)msg);
       }
       else if( msg.getType().equals(MessageType.REFERENCE_LIST))
       {
           handleReferenceList((ReferenceListMsg)msg);
       }
       else if( msg.getType().equals(MessageType.CLIENT_ERROR))
       {
           handleClientError((ClientErrorMsg)msg);
       }           
   }

   private void handleLogin(String sender) {
	
	  UserProxy user = this.proxyMgr.getProxyByName(sender); 
	   
	  // respond to the client in the manner it expects
      LoginResponseMsg resp = new LoginResponseMsg();
      resp.setSuccess(true);
      user.sendMessage(resp);
      
	   // give them the time of day
	   TimeSynch timeSynchMsg = new TimeSynch();
	   user.sendMessage(timeSynchMsg);
	     
       // tell the client details about this game
       user.sendMessage(new GameInfoMsg2(this.getInfo()));	   
    }

// log an error message from the client.
   private void handleClientError(ClientErrorMsg msg)
   {
       String message = msg.getErrorMessage();
       SimpleLogger.logError("Error from client: "+msg.getSender()+" ["+message+"]");
   }

    /**
     * Update the database with the latest bookmarks from the client.
	 * @param msg
	 */
	private void handleReferenceList(ReferenceListMsg msg)
	{
	    List referenceList = msg.getResources();
	    
	    try 
	    {
            ReferenceResourceMapper.updateReferences(referenceList,msg.getGameID());
            
            // broadcast new bookmark list 
            broadcastMessage(msg);   
        } 
	    catch (MapperException e) 
	    {
            SimpleLogger.logError(e.toString());
        }	    
	}

    /**
	 * @param msg
	 */
	private void handleSelectRole(SelectRoleMsg msg)
	{
	    UserProxy user = (UserProxy)this.proxyMgr.getProxyByName(msg.getSender());
	    
	    // look up the role
	    Role role = null;
        try 
        {
            role = RoleMapper.get(msg.getRoleID());
        } 
        catch (MapperException e) 
        {
            SimpleLogger.logError(e.toString());
        }
        
        if( role != null )
        {
            Role oldRole = user.getCurrentRole();
            if( oldRole != null )
            {
                RoleLeftMsg roleLeftMessage = new RoleLeftMsg();
                roleLeftMessage.setRoleName(role.getName());
                broadcastMessage(roleLeftMessage);
            }
            
            // set the current role
            user.setCurrentRole(role);

		    // broadcast game entry to all users
		    RoleArrivedMsg arriveMsg = new RoleArrivedMsg();	   
		    arriveMsg.setRoleName(role.getName());
		    broadcastMessage(arriveMsg);
            
            
            // Send the data now that the user has a role
            sendDocumentData(user);
            sendMoveData(user);
            sendDocumentVersionsToPlayer(msg.getSender(),role.getId());
            
            // send the move in progress 
            Move move = MoveMapper.getPendingMove( msg.getGameID(), role.getId() );
            RestoreMsg restore = new RestoreMsg(move);
            user.sendMessage(restore);
        }
        
	}

    /**
     * @param role
     */
    private void sendDocumentData(UserProxy proxy)
    {
        // send a list of all published docs to newPlayer
        DiscourseFieldMapper mapper = new DiscourseFieldMapper();
        Iterator docIter = mapper.getDocumentList(getGameId()).iterator();
        final String roleName = proxy.getCurrentRole().getName();
     
        while (docIter.hasNext())
        {
           this.proxyMgr.sendTo(proxy.getID(), 
              new DocumentInfoMsg( (DocumentInfo)docIter.next() ) );
        }
        
        // send a list of this players pending docs
        docIter = mapper.getPendingDocuments(getGameId(), roleName).iterator();
        while (docIter.hasNext())
        {
           this.proxyMgr.sendTo(proxy.getID(), 
              new DocumentInfoMsg( (DocumentInfo)docIter.next() ) );
        }
    }
    
    private void sendMoveData(UserProxy proxy)
    {
        // send a history of the previously submitted moves
        sendMoveHistoriesToPlayer(proxy.getID());
        
        //init discussion for this player
        proxy.registerMsgHandler(MessageType.DISCUSSION_ENTRY, this.discussion);
        this.discussion.sendEntryToPlayer(proxy);

        // send bookmarks for this game
        sendReferencesToPlayer(proxy);
        
        // send categories for this game
        sendMoveCategoriesToPlayer(proxy);        
    }
    
    /**
	 * @param request
	 */
	private void handleNewRoleRequest(NewRoleRequest request)
	{
	    UserProxy user = (UserProxy)this.proxyMgr.getProxyByName(request.getSender());
	    
	    // attempt to create the new role in the database
	    Role role = null;	    
	    String createFailedReason = null;
        try 
        {
            int gameID = user.gameId;
            
            if( request.getRoleName().length() == 0 )
            {
                createFailedReason = "Please enter a name for your role.";
            }
            else if( request.getRoleName().length() > MAX_ROLE_NAME_SIZE )
            {
                createFailedReason = "Please enter a shorter name.";
            }
            else if( !RoleMapper.isNameUnique(request.getRoleName(),gameID) )
            {
                createFailedReason = "There is already a role with that name.";
            }
            else
            {
                int userID = user.getUserData().getId();	    	   
	            role = RoleMapper.newRole(request.getRoleName(), userID, gameID, user.getUserData().getWritePermission() );
            }
        }
            
        catch (MapperException e) 
        {
            SimpleLogger.logError(e.toString());
            createFailedReason = "Unable to create role.";
        }

        // prepare response
        NewRoleResponse response;
        if( role != null )
        {
            // successful role creation
            response = new NewRoleResponse(role);    
            
            // send the new role out to all players 
            RoleMsg roleMsg = new RoleMsg(user.getUserData().getUserName(), role, false);
            this.broadcastMessage(roleMsg);
        }
        else
        {
            // unable to create requested role
            response = new NewRoleResponse(createFailedReason);
        }
        
        // send response to client
        user.sendMessage(response);
	}

	private void handlePlayerRole( RoleUpdateMsg roleMsg )
	{
       // broadcast role change to all players in game 
       this.broadcastMessage(roleMsg);
       
       try 
       {
           // update the database
           RoleMapper.updateRole(roleMsg.getRole());
       } 
       catch (MapperException e) 
       {
           SimpleLogger.logError(e.toString());
       }
	}
   
   /**
    * @param msg
    */
   private void handlePlayerListRequest(PlayerListRequestMsg msg)
   {
      UserProxy user = (UserProxy)this.proxyMgr.getProxyByName(msg.getSender());
      if (msg.getRequestType() == PlayerListRequestMsg.ALL_PLAYERS)
      { 
         try
         {
            List users = UserMapper.getAllUserNames();
            user.sendMessage( new PlayerListMsg(users));
         }
         catch (MapperException e)
         {
            SimpleLogger.logError(e.toString());
            user.sendMessage( new ServerErrorMsg("Unable to get list of players.", false));
         }
      }
      else
      {
         user.sendMessage( new PlayerListMsg(this.proxyMgr.getNames(msg.getGameID())));
      }
   }

   /**
    * Remove pending move for the player
    * @param sender
    */
   private void handleMoveCancel(CancelMoveMsg msg)
   {
      SimpleLogger.logInfo("Reverting pending move for " + msg.getSender());
      MoveMapper.cancelPendingMove(msg.getGameID(), msg.getRoleID());
   }

   /**
    * boounce chat to all players in this game
    * @param msg
    */
   private void handleChat(ChatMsg msg)
   {
      if (msg.isPrivate())
      {
         // bounce to target & sender for private chat
         UserProxy tgt = (UserProxy)this.proxyMgr.getProxyByName(msg.getTargetPlayer());
         UserProxy sender = (UserProxy)this.proxyMgr.getProxyByName(msg.getSender());
         if (tgt == null)
         {
             SimpleLogger.logInfo("Bad chat message target received from " + msg.getSender());
             return;
         }
         tgt.sendMessage(msg);
         sender.sendMessage(msg);
      }
      else
      {
         // just broadcast out to all in lobby
    	 broadcastMessage(msg);
      }
   }

   /**
    * Delete the spacified document from the DF directories & DB
    * @param msg
    */
   private void handleDeleteDocument(DeleteDocumentMsg msg)
   {
      DiscourseFieldMapper mapper = new DiscourseFieldMapper();
      mapper.deleteDocument(msg.getInfo().getTitle(), msg.getGameID());
   }

   /**
    * Send the requested document to the requeting player
    * @param msg the request message
    */
   private void handleDocumentRequest(DocumentRequestMsg msg)
   {
      SimpleLogger.logInfo("Processing document request");
      Integer documentId = msg.getDocumentId();
      String tgtUser = msg.getSender();
      
      // get the document info associated with this id      
      DocumentInfo di =  DocumentMapper.getDocumentInfo(documentId.intValue());      
      if ( di == null)
      {
         SimpleLogger.logError("Got request for invalid document id [" +
            documentId + "] - ignoring");
         return;
      }
      
      // log the type of request
      if (msg.isFullUpdate())
      {
         SimpleLogger.logInfo("This is a request for a full update");
      }
      else
      {
         SimpleLogger.logInfo("This is a request for an image update");
      }
      
      // only send the document on a full update request
      // otherwise send just images
      if (msg.isFullUpdate())
      {
         FileInputStream source = null;
         try
         {
            // open the file requested and read data in MAX_DATA_SIZE
            // chunks. Send each chunk back to the requesting client.
            // When the last bit of data is read, set the complete flag
            source = new FileInputStream(this.getDiscourseFieldDir() + 
               File.separator + di.getFileName());
            DocumentDataMsg dataMsg;
            boolean done = false;
            int bytesRead;
            while (!done)
            {
               dataMsg = new DocumentDataMsg(di);
               bytesRead = source.read(dataMsg.getDataBufer());
               if (bytesRead == -1 || bytesRead < DocumentDataMsg.MAX_DATA_SIZE)
               {
                  if (bytesRead < 0)
                     bytesRead = 0;
                  dataMsg.setComplete();
                  done = true;
               }
   
               dataMsg.setBufferSize(bytesRead);
   
               // send the info up to the requestor
               this.proxyMgr.sendTo(tgtUser, dataMsg);
            }
         }
         catch (IOException e)
         {
            SimpleLogger.logError(
               "Error handling request for [" + di.getTitle() + "]: " + e);
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
      // send all supporting images
      try
      {
         Iterator imgItr = DocumentMapper.getDocumentImages(di.getId().intValue()).iterator();
         while (imgItr.hasNext())
         {
            sendImageToPlayer(di, (String)imgItr.next(), tgtUser);
         }
         
      }
      catch (MapperException e1)
      {
         SimpleLogger.logError("Unable to send images to " + tgtUser + " " + e1);
      }
      
      // send done flag
      this.proxyMgr.sendTo(tgtUser, new DocumentCompleteMsg(di));
   }

   /**
    * Handles userOnline message
    * This method sends the list of DF documents to the player
    */
   private void handleUserOnline(String newPlayer)
   {   
      // get this user proxy
      UserProxy proxy =
         (UserProxy)this.proxyMgr.getProxyByName(newPlayer);
      
      // send a game info message up to player
      GameInfoMsg gameInfoMsg = new GameInfoMsg(getInfo());
      proxy.sendMessage(gameInfoMsg);
      
      // initialize the player's role, if a new role is created, send it
      // out to all players, and send the current list to the new player      
      sendRoleListToPlayer( proxy );
      Role defaultRole = createDefaultRole(proxy.getUserData());
      if( defaultRole != null )
      {
          RoleMsg roleMsg = new RoleMsg(proxy.getUserData().getUserName(), defaultRole, true);
          this.broadcastMessage(roleMsg);
      }
      
      // listen for updates to the player's role
      proxy.registerMsgHandler(MessageType.ROLE, this );
      
      // lastly, send the ready message 
      // indicating that game is ready to play
      this.proxyMgr.sendTo(newPlayer, new ReadyMsg() );
   }

   private void sendDocumentVersionsToPlayer( String sender, int currentRoleID )
   {
      // send the history of all moves made by all players
      try
      {
         List versions = DocumentVersionMapper.getDocumentVersions(getGameId(),currentRoleID);
         
         if (versions.size() > 0)
         {
            DocumentVersionMsg msg;
            Iterator itr = versions.iterator();
            while (itr.hasNext())
            {
               DocumentVersion docVersion = (DocumentVersion)itr.next();
               if ( docVersion.isPublished() || docVersion.getRoleID() == currentRoleID )
               {
                  msg = new DocumentVersionMsg(docVersion);
                  this.proxyMgr.sendTo(sender, msg);
               }
            }
         }
         else
         {
            SimpleLogger.logInfo("No document versions sent");
         }
      }
      catch (MapperException e)
      {
         SimpleLogger.logError("Unable to get document versions: " + e);
      }
   }
   
   private void broadcastDocumentVersion(int documentVersionID)
   {
       try
       {
          DocumentVersion version = DocumentVersionMapper.getDocumentVersion(documentVersionID);
          
          if (version != null)
          {
             DocumentVersionMsg msg = new DocumentVersionMsg(version);
             broadcastMessage(msg);
          }
          else
          {
             SimpleLogger.logInfo("DocumentVersion ["+documentVersionID+"] not found, so not sent");
          }
       }
       catch (MapperException e)
       {
          SimpleLogger.logError("Unable to get DocumentVersion ["+documentVersionID+"]: " + e);
       }
   }

/**
	 * @param proxy
	 */
	private void sendMoveCategoriesToPlayer(UserProxy player)
	{
        if( player == null )
        {
            SimpleLogger.logError("Invalid user proxy in sendMoveCategoriesToPlayer");
            return;
        }
        
        try 
	    {
            CategoryListMsg msg = new CategoryListMsg();
            List categoryList = CategoryMapper.getCategories(this.getGameId());
            
            if( !categoryList.isEmpty() )
            {                
                for( Iterator i = categoryList.iterator(); i.hasNext(); )
                {
                    Category category = (Category) i.next();
                    msg.addResource(category);
                }
                
                // send the list to the player
                player.sendMessage(msg);
            }
	    } 
	    catch (MapperException e) 
	    {
	         SimpleLogger.logError("Error getting categories: " + e.toString());
	    }	    
	}

	// 	get the list of reference resources for this game and send it to the player
   private void sendReferencesToPlayer(UserProxy player)
	{
        try 
	    {
            ReferenceListMsg msg = new ReferenceListMsg();
            List references = ReferenceResourceMapper.getReferences(this.getGameId());
            
            for( Iterator i = references.iterator(); i.hasNext(); )
            {
                ReferenceResource reference = (ReferenceResource) i.next();
                msg.addResource(reference);
            }
            
            // send the list to the player
            player.sendMessage(msg);            
	    } 
	    catch (MapperException e) 
	    {
	         SimpleLogger.logError("Error getting references: " + e.toString());
	    }
	}

// 	check to see if the player has a role in this game and if not add one
   private Role createDefaultRole( User userData )
   {
       Role role = null;
       
       try 
       {
           int gameId = this.getGameId();
           if( RoleMapper.hasRole(gameId,userData.getId()) == false )
           {               
               // player doesn't have a role, create one and add it to DB
               role = RoleMapper.newRole(userData.getUserName(),userData.getId(),gameId,false);
               SimpleLogger.logInfo("New role added: "+role.getName());
           }
       } 
       catch (MapperException e) 
       {
           SimpleLogger.logError("Error initializing role for player "
              + userData.getUserName() + ": " + e.toString());       
       }
       
       return role;
   }

   // sends all the roles associated with this game
   private void sendRoleListToPlayer( UserProxy userProxy )
   {
         SimpleLogger.logInfo("Sending Role list for game: "+this.getInfo().getName());
         
	     try 
	     {	        
	        List roleList = RoleMapper.getGameRoles(getGameId());
	        if (roleList.size() > 0)
	        {
	           for( Iterator i = roleList.iterator(); i.hasNext(); )
	           {
	               Role role = (Role) i.next();
		           User user = UserMapper.getUserForRole(role.getId());
		           
		           // check to see if the player is online
		           boolean online = false;
		           UserProxy lookupUserProxy = (UserProxy) proxyMgr.getProxyByName(user.getUserName());
		           if( lookupUserProxy != null )
		           {			           
			           Role currentRole = lookupUserProxy.getCurrentRole();
			           if( currentRole != null && currentRole.getId() == role.getId() )
			           {
			               online = true;    
			           }
		           }
		           
		           RoleMsg msg = new RoleMsg(user.getUserName(),role,online );
		           SimpleLogger.logInfo("Sending Role "+role.getName()+" for player "+user.getUserName());
		           userProxy.sendMessage(msg); 
	           }
	        }
	     } 
	     catch (MapperException e) 
	     {
	         SimpleLogger.logError("Error retrieving roles for game: "+this.getInfo().getName());
	         return;
	     }
   }
   
   /**
    * Notification that there was a problem receiving a document
    */
   public void documentError(DocumentInfo badInfo, String errorMsg)
   {
      // log the error and notify contributor
      SimpleLogger.logError("Error receiving document " + badInfo.getTitle() 
         + ": " + errorMsg);
      
      try
      {
          User user = UserMapper.getUserForRole(badInfo.getContributorID());
          UserProxy sender = (UserProxy)this.proxyMgr.getProxyByName(user.getUserName());
          sender.sendMessage( new DocumentErrorMsg(badInfo, errorMsg));
      } 
      catch (Exception e)
      {
          SimpleLogger.logError("Unable to indentify sender of document with error.");
      }      
   }
   
   /**
    * Notification that a document has changed
    */
   public void documentChanged(DocumentInfo changedDocInfo)
   {
      SimpleLogger.logInfo("Document " + changedDocInfo.getTitle() 
         + " has changed. Notifying all players");
      DocumentChangedMsg msg =  new DocumentChangedMsg(changedDocInfo);
      broadcastMessage(msg);
   }

   /**
    * Notification that a document has been received from a user
    * @param msg
    */
   public void documentReady(DocumentInfo newDocInfo)
   {
      SimpleLogger.logInfo("Successfully added " + newDocInfo.getTitle());
      DiscourseFieldMapper dfMapper = new DiscourseFieldMapper();
      try
      {
         dfMapper.addPendingDocument(getInfo().getName(),
                 newDocInfo.getTitle(), newDocInfo.getId().intValue());
         DocumentCompleteMsg dcMsg = new DocumentCompleteMsg(newDocInfo);
         broadcastMessage(dcMsg);
      }
      catch (MapperException e)
      {
         SimpleLogger.logError("Error adding document to DF: " + e.toString());
      }
   }

   /**
    * Handle submission of a move.
    * Writes all data to DB via MoveMapper, and sends result msgs
    * @param moveMsg the submitted move
    */
   private void handleMoveSubmission(MoveSubmitMsg moveMsg)
   {
      // update existing move, or insert a new one
      boolean success;
      Move move = null;
      MoveMapper moveMapper = new MoveMapper();
      
      // TODO: Check to see that sender has permissions to write
      if (this.getInfo().isArchived())
      {
          SimpleLogger.logInfo("["+moveMsg.getSender()+"] attempted to alter"
                  +" game ["+this.getInfo().getName()+"] with insufficient privileges");
          MoveResponseMsg resp = new MoveResponseMsg(false, 
              "You do not have permission to modify this game.");
          this.proxyMgr.sendTo(moveMsg.getSender(), resp);
          
          // Exit early
          return;
      }
      
      try
      {   
          if (moveMsg.getMove().getId() > -1)
          {
             SimpleLogger.logInfo("Updating prior move");
             success = moveMapper.update( moveMsg.getGameID(), moveMsg.getMove(),
                     moveMsg.getDocumentVersionOrigins(), true );
             move = MoveMapper.getMove( moveMsg.getMove().getId() );
          }
          else
          {
             SimpleLogger.logInfo("Adding new move");
             move = moveMapper.insert( moveMsg.getGameID(), moveMsg.getMove(),
                     moveMsg.getDocumentVersionOrigins(), true ) ;
             success = ( move != null ); 
          }
      }
      catch (MapperException me)
      {
          SimpleLogger.logError("Exception in move submission", me);
          success = false;
      }
      
      if ( success )
      {
         MoveMsg broadcastMoveMsg = new MoveMsg(move);
                 
         // move all documents that were added in this move
         // from a pending table to the discourse field
         commitDocumentAdds(move);
                
         for (Iterator i=move.getDocumentVersionIDs().iterator(); i.hasNext(); )
         {
             Integer dvID = (Integer)i.next();
             this.broadcastDocumentVersion(dvID.intValue());
         }

         // broadcast the move to everyone
         broadcastMessage( broadcastMoveMsg );
         
         // send move response to submitting player
         MoveResponseMsg resp = new MoveResponseMsg(true, 
            "Your move was successfully submitted" );
         this.proxyMgr.sendTo(moveMsg.getSender(), resp);
     
         // send out emails to all players
         this.messenger.sendMoveNotification(broadcastMoveMsg);
         
         // broadcast move notification
         ChatMsg chat = new ChatMsg("Ivanhoe", move.getRoleName() + " has submitted a move");
         broadcastMessage(chat );   
      }
      else
      {
         MoveResponseMsg resp = new MoveResponseMsg(false, 
            "Database error; try submitting your move later" );
         this.proxyMgr.sendTo(moveMsg.getSender(), resp);
      } 
   }

   /**
    * @param move
    */
   private void commitDocumentAdds(Move move)
   {
      String playerName = move.getRoleName();
      DiscourseFieldMapper mapper = new DiscourseFieldMapper();
         
      // grab a list of pending documents for this player
      // and remove the pending status
      List pendingAdds = mapper.getPendingDocuments(getGameId(), playerName);
      mapper.commitDocumentAdds(getGameId(), playerName);
       
      // Let everyone else know a doc has been added
      for (Iterator itr = pendingAdds.iterator();itr.hasNext();)
      {
          DocumentInfo docInfo = (DocumentInfo) itr.next();
          docInfo.setPublishedDocument(true);
          ChatMsg chat = new ChatMsg( "Ivanhoe", playerName + " has added document " +
          docInfo.getTitle() + " to the discourse field");
          broadcastMessage( chat );
          broadcastMessage( new DocumentInfoMsg(docInfo)); 
      }
  
   }

   /**
    * Read each entry in the move table and send MoveMessages
    * to the player. This allows the player to update
    * their DF to the current state
    * @param sender
    */
   private void sendMoveHistoriesToPlayer(String sender)
   {
      // send the history of all moves made by all players
           
      try
      {
         List history = MoveMapper.getMoveHistory(getGameId());
         if (history.size() > 0)
         {
            MoveMsg msg;
            Iterator itr = history.iterator();
            while (itr.hasNext())
            {
               msg = new MoveMsg((Move)itr.next());
               this.proxyMgr.sendTo(sender, msg);
            }
         }
         else
         {
            SimpleLogger.logInfo("No history");
         }
      }
      catch (MapperException e)
      {
         SimpleLogger.logError("Unable to get history" + e);
      }     
   }

   /**
    * @param string
    * @param tgtUser
    */
   private void sendImageToPlayer(DocumentInfo parentDoc, 
      String imgFileName, String tgtUser)
   {
      FileInputStream source = null;
      try
      {
         source = new FileInputStream(this.getDiscourseFieldDir() + 
            File.separator + imgFileName);
         ImageDataMsg dataMsg;
         boolean done = false;
         int bytesRead;
         while (!done)
         {
            dataMsg = new ImageDataMsg(imgFileName, parentDoc);
            bytesRead = source.read(dataMsg.getDataBufer());
            if (bytesRead == -1 || bytesRead < DocumentDataMsg.MAX_DATA_SIZE)
            {
               if (bytesRead < 0)
                  bytesRead = 0;
               dataMsg.setComplete();
               done = true;
            }

            dataMsg.setBufferSize(bytesRead);

            // send the info up to the requestor
            this.proxyMgr.sendTo(tgtUser, dataMsg);
         }
      }
      catch (IOException e)
      {
         SimpleLogger.logError(
            "Error handling sending image [" + imgFileName + "]: " + e);
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

   
   /**
    * Authorization rule to determine if player is allowed into this game
    */
   private static class GameEntryCheck extends AuthRule
   {
      private int gameId;
      private String gameName;
      
      public GameEntryCheck(int gameId, String gameName)
      {
         super("GameEntryCheck");
         this.gameId = gameId;
         this.gameName = gameName;
      }
      
      public boolean executeRule(String user, String pass, boolean retry )
      {
         boolean success = false;
         PreparedStatement statement = null;
         ResultSet rs = null;
         try
         {            
            String sqlCommand = "select name from player, player_game, game where " +
               " player_game.fk_game_id = game.id and " +
               " player_game.fk_player_id = player.id and player.playername = ?" +
               " and game.id = ?";
            statement = DBManager.instance.getConnection().prepareStatement(sqlCommand);
            statement.setString(1,user);
            statement.setInt(2,this.gameId);
            rs = statement.executeQuery();
            if (rs.first())
            {
               success = true;
            }
         }
         catch (SQLException e)
         {
            SimpleLogger.logError("auth error: " + e.getMessage());
            if( retry == true )
            {
                SimpleLogger.logError("Retrying...");                
                return executeRule(user,pass,false); 
            }
         } 
         finally
         {
            DBManager.instance.close(rs);
            DBManager.instance.close(statement);
         }
         
         if (success == false)
         {
            setMessage("Not authorized for game " + this.gameName);
            SimpleLogger.logInfo("Player " + user + " is not authorized for game "
               + this.gameName);
         } 
         return success;
      }
   }

   /**
    * get the number of players in this game
    * @return
    */
   public int getPlayerCount()
   {
      return this.proxyMgr.getNumProxies(this.getGameId());
   }

   /**
    * Configure email messaging
    * @param mailEnabled
    * @param mailHost
    * @param mailFrom
    */
   public void configureEmail(boolean mailEnabled, String mailHost, String mailFrom)
   {
      this.messenger.configure(mailEnabled,mailHost,mailFrom);
   }

   /**
    * Determine if a player is in this game
    * @param playerName
    * @return
    */
   public boolean isPlayer(String playerName)
   {
       return (this.proxyMgr.getProxyByName(playerName) != null);
   }
   
   public void kickAllPlayers()
   {
       broadcastMessage(new PlayerKickedMsg("kicking all players") );
       Thread kickingThread = new KickingThread(this.proxyMgr, this.getGameId(), KickingThread.KICK_GRACE_TIME);
       kickingThread.start();
   }
   
   /**
    * Kick a player out of the game
    * @param kickedPlayer
    */
   public void kickPlayer(String kickedPlayer)
   {
      UserProxy kicked = this.proxyMgr.getProxyByName(kickedPlayer);
      kicked.sendMessage(new PlayerKickedMsg("Kicked by admin"));
      
      Thread kickingThread = new KickingThread(this.proxyMgr, kicked, KickingThread.KICK_GRACE_TIME);
      kickingThread.start();
   }

   public String getDiscourseFieldDir()
   {
       return discourseFieldDir;
   }
   
   public GameInfo getInfo() {
	   return this.gameInfo;
   }
   
   public int getGameId() {
	   return (this.gameInfo != null) ? this.gameInfo.getId() : 0;
   }

public DocumentReceiver getReceiver() {
	return receiver;
}

}
