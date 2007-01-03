/*
 * Created on Jan 7, 2005
 *
 */
package edu.virginia.speclab.ivanhoe.client.lobby.newgame;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.virginia.speclab.ivanhoe.client.Ivanhoe;
import edu.virginia.speclab.ivanhoe.shared.IMessageHandler;
import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;
import edu.virginia.speclab.ivanhoe.shared.data.GameInfo;
import edu.virginia.speclab.ivanhoe.shared.message.CreateGameMsg;
import edu.virginia.speclab.ivanhoe.shared.message.CreateGameResponseMsg;
import edu.virginia.speclab.ivanhoe.shared.message.DocumentErrorMsg;
import edu.virginia.speclab.ivanhoe.shared.message.Message;
import edu.virginia.speclab.ivanhoe.shared.message.MessageType;

/**
 * @author Nick
 *
 */
class NewGameTransaction implements IMessageHandler
{
    private String gameName;
	private int startingDocumentPrivilege;
	private String desc, obj;	
	private List docList,accessList,categoryList;
	private boolean restrictedAccess;
	
	private INewGameTransactionListener listener;
	private String errorText; 

    public static final int SUCCESS = 0;
    public static final int GAME_NAME_TOO_LONG = 1;
    public static final int DOC_PRIV_OUT_OF_RANGE = 2;
    public static final int NAME_BLANK = 3;
    public static final int DESCRIPTION_BLANK = 4;
    public static final int HOST_ERROR = 5;
    public static final int DOCUMENT_ERROR = 6;
    public static final int GAME_NAME_HAS_INVALID_CHARACTERS = 7;

    public NewGameTransaction( String gameName, int startingDocumentPrivilege, 
            				   String desc, String obj, List docList,
            				   boolean restrictedAccess, List accessList,
            				   List categoryList,
            				   INewGameTransactionListener listener )
    {
        this.startingDocumentPrivilege = startingDocumentPrivilege;
        this.gameName = gameName;
        this.desc = desc;
        this.obj = obj;
        this.docList = docList;
        this.restrictedAccess = restrictedAccess;
        this.accessList = accessList;
        this.categoryList = categoryList;
        
        this.listener = listener;
        
        if( validateParameters() )
        {
    		Ivanhoe.getProxy().registerMsgHandler(MessageType.CREATE_GAME_RESPONSE, this);
    		Ivanhoe.getProxy().registerMsgHandler(MessageType.DOCUMENT_ERROR, this);
            createGame();
        }
    }
    
    private boolean validateParameters()
    {
		if (startingDocumentPrivilege < 1) 
		{
		    fireCreateFailed(DOC_PRIV_OUT_OF_RANGE);
			return false;
		}
		
		if (gameName == null || gameName.length() <= 0 )
		{
		    fireCreateFailed(NAME_BLANK);
			SimpleLogger.logError("Failed attempt to create game with missing name.");
			return false;
		}

		if (gameName.length() >= 50)
		{
		    fireCreateFailed(GAME_NAME_TOO_LONG);
		    return false;
		}
        
        if (gameName.indexOf("?") != -1)
        {
            fireCreateFailed(GAME_NAME_HAS_INVALID_CHARACTERS);
            return false;
        }
		
		if( desc == null || desc.length() <= 0 )
		{
		    fireCreateFailed(DESCRIPTION_BLANK);
		    SimpleLogger.logError("Failed attempt to create game with missing description.");
		    return false;
		}
		
		return true;
    }
    
    /**
	 * create a game and submit it to the server!
	 */
	private void createGame()
	{				
		// build a list of doc titles for the game
		List docTitleList = new ArrayList();
		
		for (Iterator itr = docList.iterator(); itr.hasNext();)
		{
			NewDocumentRecord newRec = (NewDocumentRecord)itr.next();
			docTitleList.add( newRec.getDocumentInfo().getTitle() );
		}
		
		// send the create game message
		GameInfo newGame = new GameInfo(gameName,
				Ivanhoe.getProxy().getUserName(), desc, obj,
				this.restrictedAccess, false, startingDocumentPrivilege);
		
		CreateGameMsg createMsg = null;
		if (this.restrictedAccess)
		{
			createMsg = new CreateGameMsg(newGame, docTitleList, this.accessList, this.categoryList);
		}
		else
		{
			createMsg = new CreateGameMsg(newGame, docTitleList, this.categoryList);
		}
		
		SimpleLogger.logInfo("Creating " + newGame.toString());
		Ivanhoe.getProxy().sendMessage(createMsg);
	}
	
	/**
	 * Handle response message from lobby server
	 */
	public void handleMessage(Message msg)
	{
		if (msg.getType().equals(MessageType.CREATE_GAME_RESPONSE))
		{
			CreateGameResponseMsg resp = (CreateGameResponseMsg)msg;
			if (resp.isSuccess())
			{
				SimpleLogger.logInfo("Game successfully created");
				fireCreateSuccess();
			}
			else
			{
				SimpleLogger.logError("Unable to create game:  " + resp.getMessage());
				fireCreateFailed(HOST_ERROR);
			}
		}
		else if (msg.getType().equals(MessageType.DOCUMENT_ERROR))
		{
			DocumentErrorMsg errMsg = (DocumentErrorMsg)msg;
			
			// record the dead title for future reference
			errorText = errMsg.getDocumentInfo().getTitle();
			
			fireCreateFailed(DOCUMENT_ERROR);			
		}						
	}
	
	private void unregisterListeners()
	{
		Ivanhoe.getProxy().unregisterMsgHandler(MessageType.CREATE_GAME_RESPONSE, this);
		Ivanhoe.getProxy().unregisterMsgHandler(MessageType.DOCUMENT_ERROR, this);    		
	}

    private void fireCreateFailed( int errorCode )
    {        
        listener.createFailed(errorCode);
        unregisterListeners();
    }
    
    /**
     * 
     */
    private void fireCreateSuccess()
    {
        listener.createSuccess();
        unregisterListeners();
    }
    /**
     * @return Returns the error text
     */
    public String getErrorText()
    {
        return errorText;
    }
}
