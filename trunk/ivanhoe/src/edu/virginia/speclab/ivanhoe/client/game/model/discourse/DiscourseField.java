/*
 * Created on Oct 27, 2003
 *
 * DiscourseField
 */
package edu.virginia.speclab.ivanhoe.client.game.model.discourse;

import java.io.File;
import java.util.*;

import edu.virginia.speclab.ivanhoe.shared.IMessageHandler;
import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;
import edu.virginia.speclab.ivanhoe.shared.blist.BArrayList;
import edu.virginia.speclab.ivanhoe.shared.blist.BCollection;
import edu.virginia.speclab.ivanhoe.shared.blist.BLinkedList;
import edu.virginia.speclab.ivanhoe.shared.blist.BList;
import edu.virginia.speclab.ivanhoe.shared.data.*;
import edu.virginia.speclab.ivanhoe.shared.message.*;
import edu.virginia.speclab.ivanhoe.client.Ivanhoe;
import edu.virginia.speclab.ivanhoe.client.game.model.IvanhoeGame;
import edu.virginia.speclab.ivanhoe.client.game.model.cache.DocumentCache;
import edu.virginia.speclab.ivanhoe.client.game.model.cache.DocumentLoader;
import edu.virginia.speclab.ivanhoe.client.game.model.cache.IDocumentLoaderListener;
import edu.virginia.speclab.ivanhoe.client.game.model.document.IvanhoeDocument;
import edu.virginia.speclab.ivanhoe.client.game.model.metagame.IRoleListener;
import edu.virginia.speclab.ivanhoe.client.game.model.metagame.RoleManager;
import edu.virginia.speclab.ivanhoe.client.game.view.Workspace;

/**
 * @author lfoster
 *
 * <code>DiscourseField</code> is a map of all the documents and
 * moves by all the players in the current game.
 */
public class DiscourseField implements IMessageHandler, IRoleListener
{
   private final IvanhoeGame game;
   private DiscourseFieldTimeline dfTimeline;
   private DiscourseFieldTime dfCurrentTime;
   private final DocumentVersionManager documentVersionManager;
   private List discourseFieldListeners, currentMoveListeners, permissionListeners;
   private CurrentMove currentMove;
   private BList moveHistory;
   private DocumentCache documentCache;
   private List openDocuments;
   private boolean writable;
   
   // state flags
   private boolean initialized;
   
   private class PlayerMoveHistory
   {
       public String playerName;
       public BList playerMoveHistory;
       
       public PlayerMoveHistory( String player )
       {
           playerName = player;
           playerMoveHistory = new BArrayList();
       }
   }
   
   private static class OpenDocumentRecord
   {
      public final IvanhoeDocument document;
      private int referenceCount;
      
      public OpenDocumentRecord(IvanhoeDocument doc)
      {
         this.document = doc;
         this.referenceCount = 1;
      }
      
      public boolean isOpen()
      {
         return (this.referenceCount > 0);
      }
      
      public void addRef()
      {
         SimpleLogger.logInfo(
            "Adding reference to " + this.document.getVersion().toString() );
         this.referenceCount++;
      }
      
      public void release()
      {
         SimpleLogger.logInfo(
            "Reference to " + this.document.getVersion().toString() + " released");
         this.referenceCount--;
      }
   }

   public DiscourseField(IvanhoeGame ivanhoeGame)
   {
      this.initialized = false;
      this.game = ivanhoeGame;
      this.moveHistory = new BArrayList(5);
      this.openDocuments = new ArrayList(5);
      this.discourseFieldListeners = new LinkedList();
      this.currentMoveListeners = new LinkedList();
      this.permissionListeners = new LinkedList();
      this.writable = false;
      this.documentVersionManager = new DocumentVersionManager(this);
   }
   
   public void removeDocumentVersions(Collection documentVersions)
   {
       for (Iterator i=documentVersions.iterator(); i.hasNext(); )
       {
           IvanhoeDocument doc = getOpenDocument((DocumentVersion)i.next());
           Workspace.instance.closeDocumentWindow(doc);
       }
   }
   
   public void addListener( IDiscourseFieldListener dfListener )
   {
       discourseFieldListeners.add(dfListener);
   }
   
   public void addPermissionListener( IPermissionListener permissionListener)
   {
       permissionListeners.add(permissionListener);
   }
   
   public void addCurrentMoveListener(ICurrentMoveListener cmListener )
   {
       currentMoveListeners.add(cmListener);
   }
   
   private void fireMoveAddedToHistory( Move move )
   {
       for( Iterator i = discourseFieldListeners.iterator(); i.hasNext(); )
       {
           IDiscourseFieldListener dfListener = (IDiscourseFieldListener) i.next();
           dfListener.moveAddedToHistory(move);
       }
   }

   private void fireAddNewDocument( DocumentInfo docInfo )
   {
       for( Iterator i = discourseFieldListeners.iterator(); i.hasNext(); )
       {
           IDiscourseFieldListener dfListener = (IDiscourseFieldListener) i.next();
           dfListener.addNewDocument(docInfo);
       }
   }

   private void fireRemoveDocument( DocumentInfo docInfo )
   {
       for( Iterator i = discourseFieldListeners.iterator(); i.hasNext(); )
       {
           IDiscourseFieldListener dfListener = (IDiscourseFieldListener) i.next();
           dfListener.removeDocument(docInfo);
       }
   }
   
   private void firePermissionsChanged()
   {
       if (game == null)
       {
           writable = false;
       }
       else
       {
           Role currentRole = game.getRoleManager().getCurrentRole();
           if (currentRole == null || !currentRole.hasWritePermission())
           {
               writable = false;
           }
           else
           {
               writable = !game.getGameInfo().isArchived();
           }
       }
       
       for (Iterator i=permissionListeners.iterator(); i.hasNext(); )
       {
           IPermissionListener permissionListener = (IPermissionListener)i.next();
           permissionListener.writePermissionsChanged(writable);
       }
   }

   /**
    * Initialize the discourse field for the given player
    * @param workDir Working directory for player
    */
   public void initialize(String workDir)
   {            
      this.currentMove = new CurrentMove(this);
      this.documentCache = new DocumentCache(workDir, this);

      for (Iterator i=currentMoveListeners.iterator(); i.hasNext(); )
      {
          currentMove.addListener((ICurrentMoveListener)i.next());
      }
      
      // register message handlers
      Ivanhoe.registerGameMsgHandler(MessageType.JOURNAL_DATA, this);
      Ivanhoe.registerGameMsgHandler(MessageType.MOVE, this);
      
      SimpleLogger.logInfo(
         "Discourse Field initialized using root dir ["
            + workDir
            + "]");
   }
   
   /**
    * Returns a discourse field timeline which contains all moves 
    * by all players sorted chronologically.
    * @return
    */
   public DiscourseFieldTimeline getDiscourseFieldTimeline()
   {
       return dfTimeline;       
   }
   
   /**
    * Returns a list of all players in the discourse field.
    * @return
    */
   public List getPlayerList()
   {
       LinkedList playerList = new LinkedList();
       
       for( Iterator i = moveHistory.iterator(); i.hasNext(); )
       {
           PlayerMoveHistory playerHistory = (PlayerMoveHistory) i.next();
           playerList.add(playerHistory.playerName);
       }
       
       return playerList;       
   }
      
   /**
    * Get an open instance of the document at version <code>docVersion</code>
    * @param docVersion
    * @return
    */
   public IvanhoeDocument getOpenDocument(DocumentVersion docVersion)
   {
      if (docVersion == null) return null;
       
       boolean found = false;
      OpenDocumentRecord docRec = null;
      Iterator itr = this.openDocuments.iterator();
      while (itr.hasNext())
      {
         docRec = (OpenDocumentRecord)itr.next();
         if (docRec.document.getVersion().equals(docVersion))
         {
            found = true;
            break;
         }
      }
      
      if (found)
      {
         return docRec.document;
      }
      
      return null;
   }
   
   /**
    * Add a reference to an open document
    * @param docVersion
    */
   private void addReference(DocumentVersion docVersion)
   {
      OpenDocumentRecord docRec = null;
      Iterator itr = this.openDocuments.iterator();
      while (itr.hasNext())
      {
         docRec = (OpenDocumentRecord)itr.next();
         if (docRec.document.getVersion().equals(docVersion))
         {
            SimpleLogger.logInfo("Adding reference to open documment " + docVersion.toString());
            docRec.addRef();
            break;
         }
      }
   }

   /**
    * Open a doc that is already part of the DF for a given user.
    * @param docTitle The document to open.
    * @param userName The owner of the move history to use.
    * @param listener A listener for when the document is done loading.
    */
   public void requestDocument(DocumentVersion version, IDocumentLoaderListener listener )
   {
      SimpleLogger.logInfo("Opening document " + version.toString());
      
      // don't try to reload doc if its already loaded
      IvanhoeDocument doc = getOpenDocument(version);
      if ( doc != null )
      {
         SimpleLogger.logInfo("Document already loaded, using existing document.");
         addReference(version);
         listener.documentLoaded( doc );
         return;
      }

      DocumentLoader loader =
         new DocumentLoader(this, this.documentCache, version, listener, isReadOnly(version) );
      
      loader.start();
   }
   
   /**
    * Check if a version is read-only for this player.
    * Rules: 
    * 1) If the DiscourseField is read-only, all docs are read-only
    * 2) Otherwise, it's writable if the DocumentVersion is newly created   
    * @param version
    * @return
    */
   public boolean isReadOnly(DocumentVersion version)
   {
      if (!this.isWritable())
      {
          return true;
      }
       
      return version.isPublished();
   }

   /**
    * Delete a document from the discourse field
    * @param filename
    */
   public boolean deleteDocument(String docTitle)
   {
   		this.fireRemoveDocument(getDocumentInfo(docTitle));
		return this.documentCache.deleteDocument(docTitle);      
   }

   /**
    * Obtain a list of all the documents currently
    * available in the discourse field, including documents that have
    * not yet been opened by the player in this game.
    * @return a list of <code>DocumentInfo</code> objects.
    */
   public List getDocumentInfoList()
   {
      return this.documentCache.getDocumentInfoList();
   }
   
   /**
    * @param string
    * @return
    */
   public DocumentInfo getDocumentInfo(String title)
   {
      return this.documentCache.getDocumentInfo(title);
   }
   
   /**
    * Adds an image as a supporting file to the specified document.
    * The call is passed through to the cache
    * @param info Document that will own the image
    * @param imageFile Image file
    */
   public void addImage(DocumentInfo info, File imageFile)
   {
      this.documentCache.addImage(info, imageFile);
   }

   /**
    * Adds a local document to the discourse field. 
    * @param fileName The name of the file to add
    * @param path the full path (including filename) to the new file
    * @return reference to the newly added document, null if failure
    */
   public boolean addNewDocument(DocumentInfo docInfo, File newFile)
   {
      if (this.documentCache.importDocument(docInfo, newFile) == true)
      {
         this.currentMove.documentAdded(docInfo);
         fireAddNewDocument(docInfo);
         return true;
      }
      return false;
   }

   /**
    * Accessor for move-in-progress
    * @return The current move
    */
   public CurrentMove getCurrentMove()
   {
      return this.currentMove;
   }
  
   /**
    * handle messages from the server
    * This is used to get the list of documents and their data
    */
   public void handleMessage(Message msg)
   {
      if (msg.getType().equals(MessageType.MOVE))
      {
         handleMove((MoveMsg) msg);
      }
   }

   /**
    * Handle notification that game is ready
    */
   public void gameReady()
   {
      // flag init as complete
      this.initialized = true;
      
      // populate the time line
      this.dfTimeline = new DiscourseFieldTimeline(this);
      this.dfCurrentTime = new DiscourseFieldTime(this.dfTimeline); 
      
      this.documentVersionManager.initDocumentVersions();
      
   }
   
   public boolean isStartingDocumentVersion(int docVersionID)
   {
       return documentVersionManager.isStartingDocumentVersion(docVersionID);
   }

   public List getActionList()
   {       
       List actionList = new LinkedList();
       
       for( Iterator i = moveHistory.iterator(); i.hasNext();) 
       {
           PlayerMoveHistory playerHistory = (PlayerMoveHistory)i.next();
           
           for( Iterator j = playerHistory.playerMoveHistory.iterator(); j.hasNext(); )
           {
               Move move = (Move) j.next();
               for( Iterator k = move.getActions(); k.hasNext(); )
               {
                   IvanhoeAction act = (IvanhoeAction) k.next();
                   actionList.add(act);
               }                              
           }           
       }

       return actionList;
   }

   /**
    * Receive move messages
    * @param msg The move message
    */
   private void handleMove(MoveMsg msg)
   {
      addMoveToHistory(msg.getMove());
   }

   /**
    * Submit any pending discourse field changes to the server
    */
   public void submitChanges()
   {
       List unpublishedDocs = this.dfTimeline.getUnpublishedDocuments();
       
       for( Iterator i = unpublishedDocs.iterator(); i.hasNext(); )
       {
           DocumentInfo docInfo = (DocumentInfo) i.next();
           docInfo.setPublishedDocument(true);
       }
       
      this.currentMove.submit();
   }

   /**
    * Add a prior move the vector of moves
    * @param move
    */
   public void addMoveToHistory(Move move)
   {
      String player = move.getRoleName();
      List history = null;
      
      for( Iterator i = moveHistory.iterator(); i.hasNext();) 
      {
          PlayerMoveHistory playerHistory = (PlayerMoveHistory)i.next();
          if( playerHistory.playerName.equals(player) == true )
          {
              history = playerHistory.playerMoveHistory;
              break;
          }
      }
      
      if (history == null)
      {
         // create a new entry with an empty history vector
         SimpleLogger.logInfo("Creating history for player [" + player + "]");
         PlayerMoveHistory playerHistory = new PlayerMoveHistory(player);
         this.moveHistory.add( playerHistory ); 
         history = playerHistory.playerMoveHistory;
      }
    
      // update that player history
      history.add(move);
      SimpleLogger.logInfo("Move added to " + player + "'s history");
      fireMoveAddedToHistory(move);
   }
   
   /**
    * This function is called by the document loader to inform
    * the discourse field of the presence of a new document
    * which must be kept up to date. 
    * @param doc
    */
   public void addOpenDocument( IvanhoeDocument doc )
   {
      SimpleLogger.logInfo("Adding open document record for " + doc.getVersion().toString());
      this.openDocuments.add(new OpenDocumentRecord(doc));    
   }
   
   /**
    * Remove document reference from open document list
    * Notify current move that the document is no longer open
    * @param document
    */
   public void removeReference(IvanhoeDocument document)
   {
      SimpleLogger.logInfo("Document " + document.getTitle() + " closed");
      
      // see if there are any outstanding refs to this doc
      for (Iterator itr = this.openDocuments.iterator(); itr.hasNext();)
      {
         OpenDocumentRecord rec = (OpenDocumentRecord)itr.next();
         if (rec.document.equals(document))
         {
            rec.release();
            if (rec.isOpen() == false)
            {
               SimpleLogger.logInfo("This is the last ref to the doc, removing record.");
               final DocumentVersion docVersion = document.getVersion();
               
               // notify current move that the document is closed
               getCurrentMove().documentClosing(document);
               this.openDocuments.remove(rec);
               
               // Check to see if this document version is removed
               final DocumentVersion parentVersion = docVersion.getParentVersion();
               
               // If there's no parent, then don't remove it
               if (parentVersion != null)
               {
               	  final Collection currentActions = docVersion.getActionIDs();
                  currentActions.removeAll(parentVersion.getActionIDs());
	              if (currentActions.isEmpty())
	              {
	                 documentVersionManager.removeDocumentVersion(docVersion);
	              }
               }
               
               
            }
            break;
         }
      }
   }

   /**
    * Gets all moves in the history
    * @return
    *       a list of all moves
    */
   public BList getMoveHistory()
   {
       BList allMoves = new BArrayList();
       for (Iterator i=moveHistory.iterator(); i.hasNext(); )
       {
           PlayerMoveHistory playerHistory = (PlayerMoveHistory)i.next();
           allMoves.addAll(playerHistory.playerMoveHistory);
       }
       
       return allMoves;
   }
   
   /**
    * Gets the history of moves for a given player
    * @param userName
    *       the name of a role
    * @return 
    *       a list of moves by player userName
    */
   public BList getMoveHistory(String userName)
   {
      for( Iterator i = moveHistory.iterator(); i.hasNext();) 
      {
          PlayerMoveHistory playerHistory = (PlayerMoveHistory)i.next();
          if( playerHistory.playerName.equals(userName) == true )
          {
              return (BList) playerHistory.playerMoveHistory.createCopy();
          }
      }
       
      return new BArrayList();
   }
   
   public Move getContainingMove(IvanhoeAction action)
   {
       Role role = game.getRoleManager().getRole(action.getRoleID());
       
       BList moveHistoryList = getMoveHistory(role.getName());
       
       for(Iterator i=moveHistoryList.iterator(); i.hasNext(); )
       {
           Move move = (Move)i.next();
           if (move.getActionList().contains(action))
           {
               return move;
           }
       }
       return null;
   }

   /**
    * Return a cloned instance of the action identified by <code>actionId</code>
    * @param actionId
    * @return
    */
   public IvanhoeAction lookupAction(String actionId)
   {
      IvanhoeAction act = null;
      Iterator itr = moveHistory.iterator();
      List moveList;
      while (itr.hasNext())
      {
         moveList = ((PlayerMoveHistory)itr.next()).playerMoveHistory;
         Iterator moveItr = moveList.iterator();
         Move move;
         while (moveItr.hasNext() && act == null)
         {
            move = (Move) moveItr.next();
            act = move.getAction(actionId);
         }
      }

      // return a cloned instance of this action
      if (act == null)
      {
         act = this.currentMove.getAction(actionId);
      }
      
      if (act == null)
      {
         SimpleLogger.logError("Unable to find action " + actionId + " in DF");
      }
      
      return act;
   }

   /**
    * Check if the discourse field is initialized
    * @return
    */
   public boolean isInitialized()
   {
      return this.initialized;
   }
   
	/**
	 * @return Returns the dfCurrentTime.
	 */
	public DiscourseFieldTime getDiscourseFieldTime()
	{
	    return dfCurrentTime;
	}

    public boolean isWritable()
    {
        return this.writable;
    }

    public void roleChanged()
    {
        firePermissionsChanged();
    }

    public DocumentVersionManager getDocumentVersionManager()
    {
        return documentVersionManager;
    }
    
    public BCollection convertTagsToActions( BCollection actionTags )
    {
        BLinkedList actionTagList = new BLinkedList();
        
        for( Iterator i = actionTags.iterator(); i.hasNext(); )
        {
            String actionTag = (String) i.next();           
            IvanhoeAction action = lookupAction(actionTag);            
            if( action != null ) actionTagList.add(action);
        }
        
        return actionTagList;
    }
    
    /**
     * @return
     */
    public final RoleManager getRoleManager()
    {
        return game.getRoleManager();
    }

}
