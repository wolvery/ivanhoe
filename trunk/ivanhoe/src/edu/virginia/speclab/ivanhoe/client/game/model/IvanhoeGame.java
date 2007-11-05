/*
 * Created on Sep 22, 2004
 *
 */
package edu.virginia.speclab.ivanhoe.client.game.model;

import java.io.File;

import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import edu.virginia.speclab.ivanhoe.client.Ivanhoe;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.DiscourseField;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.RestoreSessionTransaction;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.RestoreSessionTransactionListener;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.historytree.DocumentHistoryTreeModel;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.historytree.DocumentVersionTreeModel;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.historytree.MoveHistoryTreeModel;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.historytree.RoleHistoryTreeModel;
import edu.virginia.speclab.ivanhoe.client.game.model.metagame.CategoryManager;
import edu.virginia.speclab.ivanhoe.client.game.model.metagame.ReferenceResourceManager;
import edu.virginia.speclab.ivanhoe.client.game.model.metagame.Discussion;
import edu.virginia.speclab.ivanhoe.client.game.model.metagame.Journal;
import edu.virginia.speclab.ivanhoe.client.game.model.metagame.RoleManager;
import edu.virginia.speclab.ivanhoe.client.game.view.Workspace;
import edu.virginia.speclab.ivanhoe.client.game.view.metagame.roleplay.IRoleChooserListener;
import edu.virginia.speclab.ivanhoe.client.game.view.metagame.roleplay.RoleChooser;
import edu.virginia.speclab.ivanhoe.client.network.GameProxy;
import edu.virginia.speclab.ivanhoe.client.network.LobbyProxy;
import edu.virginia.speclab.ivanhoe.client.util.PropertiesManager;
import edu.virginia.speclab.ivanhoe.shared.IMessageHandler;
import edu.virginia.speclab.ivanhoe.shared.IvanhoeException;
import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;
import edu.virginia.speclab.ivanhoe.shared.data.GameInfo;
import edu.virginia.speclab.ivanhoe.shared.data.Role;
import edu.virginia.speclab.ivanhoe.shared.message.Message;
import edu.virginia.speclab.ivanhoe.shared.message.MessageType;
import edu.virginia.speclab.ivanhoe.shared.message.PlayerKickedMsg;

/**
 * @author Nick
 */
public class IvanhoeGame implements IMessageHandler, RestoreSessionTransactionListener, IRoleChooserListener
{
    private final DiscourseField            discourseField;
    private final RoleManager               roleManager;
    private RoleHistoryTreeModel            playerHistoryTreeModel;
    private MoveHistoryTreeModel            moveHistoryTreeModel;
    private DocumentHistoryTreeModel        docHistoryTreeModel;
    private DocumentVersionTreeModel        docVersionTreeModel;
    private final Journal                   journal;
    private final GameInfo                  gameInfo;
    private final Discussion                discussion;
    private final PropertiesManager         propertiesManager;
    private final ReferenceResourceManager  referenceResourceManager;
    private final CategoryManager           categoryManager;
    
    private IvanhoeGameListener             ivanhoeGameListener;
    
    public IvanhoeGame( GameInfo gameInfo, PropertiesManager propertiesManager )
    {
        this.gameInfo = gameInfo;
        this.propertiesManager = propertiesManager;
        
        // create the discourse field, which stores all changes to source docs
        discourseField = new DiscourseField(this);

        // Create the role manager to store player roles
        roleManager = new RoleManager();
        roleManager.addRoleListener(discourseField);
        
        // Create the journal model
        journal = new Journal();
        
        // Create the discussion model
        discussion = new Discussion(roleManager);
        
        // create the book mark manager
        referenceResourceManager = new ReferenceResourceManager();
        
        // create the new category manager
        categoryManager = new CategoryManager();
        
    }
    
    public void setListener( IvanhoeGameListener listener )
    {
        ivanhoeGameListener = listener;
    }
    
    private void fireGameStarted()
    {
        if( ivanhoeGameListener != null )
            ivanhoeGameListener.startGame();
    }
    
    private void fireChooseRole()
    {
        while (ivanhoeGameListener == null)
        {
            try 
            {
                Thread.sleep(1);
            }
            catch (InterruptedException ie) {}
        }
        
        InternalFrameAdapter roleChooserListener = new InternalFrameAdapter()
        {
            public void internalFrameClosed(InternalFrameEvent e) 
            {
                if (!((RoleChooser)e.getSource()).isRoleSelected())
                {
                    Workspace.instance.exitGame(false);
                }
            }
        };
        
        RoleChooser roleChooser = ivanhoeGameListener.chooseRole();
        roleChooser.addListener(this);
        roleChooser.addInternalFrameListener(roleChooserListener);
        
        Workspace.instance.add(roleChooser);
	    roleChooser.pack();
	    roleChooser.setSize(roleChooser.getPreferredSize());

	    // TODO: fix this so that the role panel actually comes up centered.
//    	int x = (Workspace.instance.getWidth() - roleChooser.getWidth()) / 2;
//    	int y = (Workspace.instance.getHeight() - roleChooser.getHeight()) / 2;
	    
//	    x = (x > 0) ? x : 0;
//	    y = (y > 0) ? y : 0;
	    
	    int x = 0;
	    int y = 0;
	    
	    roleChooser.setLocation(x, y);
	    roleChooser.show();
    }
    
    public void joinGame() throws IvanhoeException
    {        
        SimpleLogger.logInfo("Joining game");
        SimpleLogger.logInfo("Ivanhoe Time:[" + Ivanhoe.getDate().toString() + "]");
        
        // listen for the game ready message
        Ivanhoe.registerGameMsgHandler(MessageType.READY, this);
        Ivanhoe.registerGameMsgHandler(MessageType.PLAYER_KICKED, this);
        
        GameProxy gameProxy = (GameProxy) Ivanhoe.getProxy(); 

        gameProxy.populateGameProxyListeners();
        
        String baseDir = System.getProperty("user.home") 
                + File.separator + propertiesManager.getProperty("workingDir");
        String workingDir = createGameDirectory( baseDir,
                gameProxy.getHostname(), gameProxy.getPort(),
                gameInfo.getName());
        
        // create user/game work directory
        if ( workingDir == null )
        {
           SimpleLogger.logError("Unable to create working directory. Exiting");
           Ivanhoe.showErrorMessage("Unable to join game; couldn't create required directories");
           Ivanhoe.shutdown();
        }
        
        // Initialize the DiscourseField
        discourseField.initialize(workingDir);
        
        // Now start receiving data
        Ivanhoe.getProxy().goOnline();
    }
    
    /**
     * Create working directory for this session
     * @param serverInfo
     * @param player
     * @return
     */
    private String createGameDirectory( String baseDir, String hostname,
            int port, String gameName)
    {       
      String safeName = gameName.toLowerCase().replace('"', '_');
    	
       // create game dir
       String workingDir = 
               baseDir + File.separator
               + hostname + "-" + port + File.separator
               + safeName + File.separator;
       if (Ivanhoe.createDirectory(workingDir) == false)
       {
          return null;
       }
       
       SimpleLogger.logInfo("Using working dir [" + workingDir + "]");
       return workingDir;
    }
    
    /**
     * handle messages from the server
     */
    public void handleMessage(Message msg)
    {
       if (msg.getType().equals(MessageType.READY))
       {
          handleReadyMsg();
       }
       else if (msg.getType().equals(MessageType.LOGIN_RESPONSE))
       {
           handleLoginResponse();
       }
       else if (msg.getType().equals(MessageType.PLAYER_KICKED))
       {
           handleKicked((PlayerKickedMsg)msg);
       }
    }
    
    private void handleLoginResponse()
    {
        GameProxy gameProxy = ((LobbyProxy)Ivanhoe.getProxy()).getGameProxy(); 
        
        // This is the big proxy switch from lobby to game proxy
        Ivanhoe.setProxy(gameProxy);
        
        String baseDir = System.getProperty("user.home") 
                + File.separator + propertiesManager.getProperty("workingDir");
        String workingDir = createGameDirectory( baseDir,
                gameProxy.getHostname(), gameProxy.getPort(),
                gameInfo.getName());
        
        // create user/game work directory
        if ( workingDir == null )
        {
           SimpleLogger.logError("Unable to create working directory. Exiting");
           Ivanhoe.showErrorMessage("Unable to join game; couldn't create required directories");
           Ivanhoe.shutdown();
        }
        
        // Initialize the DiscourseField
        discourseField.initialize(workingDir);
        
        // Now start receiving data
        Ivanhoe.getProxy().goOnline();
    }

    // the READY message is the cue to start the game
    private void handleReadyMsg()
    {
        // determine the role the player is playing
        fireChooseRole();
    }
    
    private void handleKicked(PlayerKickedMsg msg)
    {
        Workspace.instance.exitGame(false);
    }
    
    public void roleChosen(Role chosenRole)
    {
        // set the current role
        roleManager.setCurrentRole(chosenRole);

        // begin the restore session transaction  
        new RestoreSessionTransaction(chosenRole,discourseField.getCurrentMove(),this);
    }
    
    /**
     * @return Returns the historyTreeModel.
     */
    public RoleHistoryTreeModel getPlayerHistoryTreeModel()
    {
        return playerHistoryTreeModel;
    }
    /**
     * @return Returns the journal.
     */
    public Journal getJournal()
    {
        return journal;
    }
    /**
     * @return Returns the gameInfo.
     */
    public GameInfo getGameInfo()
    {
        return gameInfo;
    }
    /**
     * @return Returns the discussion.
     */
    public Discussion getDiscussion()
    {
        return discussion;
    }
    /**
     * @return Returns the moveHistoryTreeModel.
     */
    public MoveHistoryTreeModel getMoveHistoryTreeModel()
    {
        return moveHistoryTreeModel;
    }

    /**
     * @return Returns the propertiesManager.
     */
    public PropertiesManager getPropertiesManager()
    {
        return propertiesManager;
    }
    
    /**
     * @return Returns the discourseField.
     */
    public DiscourseField getDiscourseField()
    {
        return discourseField;
    }
        
    /**
     * @return Returns the roleManager.
     */
    public RoleManager getRoleManager()
    {
        return roleManager;
    }

    /* (non-Javadoc)
     * @see edu.virginia.speclab.ivanhoe.client.game.model.discourse.RestoreSessionTransactionListener#sessionRestored()
     */
    public void sessionRestored()
    {        
        // let the discourse field know we are done recieving the pre-game initialization
        discourseField.gameReady();

        // initialize the history tree models
        moveHistoryTreeModel = MoveHistoryTreeModel.createMoveHistoryTreeModel(discourseField);
        playerHistoryTreeModel = RoleHistoryTreeModel.createRoleHistoryTreeModel(discourseField);
        docHistoryTreeModel = DocumentHistoryTreeModel.createDocumentHistoryTreeModel(discourseField);
        docVersionTreeModel = DocumentVersionTreeModel.createDocumentVersionTreeModel(discourseField);
        
        SimpleLogger.logInfo("History tree models loaded.");
        
        // notify listeners that the game is ready
        fireGameStarted();
    }
       
    /**
     * @return Returns the bookmarkManager.
     */
    public ReferenceResourceManager getBookmarkManager()
    {
        return referenceResourceManager;
    }
    
    /**
     * @return Returns the categoryManager.
     */
    public CategoryManager getCategoryManager()
    {
        return categoryManager;
    }
    public DocumentHistoryTreeModel getDocHistoryTreeModel()
    {
        return docHistoryTreeModel;
    }
    
    public DocumentVersionTreeModel getDocVersionTreeModel()
    {
        return docVersionTreeModel;
    }
    
    public boolean isWritable()
    {
        return discourseField.isWritable();
    }
}
