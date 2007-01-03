/*
 * Created on Jul 1, 2004
 *
 * CreateGameMsg
 */
package edu.virginia.speclab.ivanhoe.shared.message;

import java.util.ArrayList;
import java.util.List;

import edu.virginia.speclab.ivanhoe.shared.data.GameInfo;

/**
 * @author lfoster
 *
 * Message to create a new game. Passed from client to LobbyServer
 */
public class CreateGameMsg extends Message
{
   private GameInfo gameInfo;
   private List playerList;
   private List docTitleList;
   private List categoryList;
   
   /**
    * Constructor for Restricted game
    * @param newInfo
    * @param newPlayerList
    */
   public CreateGameMsg(GameInfo newInfo, List docTitleList, List newPlayerList, List categoryList )
   {
      super(MessageType.CREATE_GAME);
      this.gameInfo = new GameInfo(newInfo);
      this.docTitleList = new ArrayList();
      this.docTitleList.addAll( docTitleList);
      this.playerList = new ArrayList();
      this.categoryList = categoryList;
      
      if (newPlayerList != null)
      {
         this.playerList.addAll(newPlayerList);
      }
   }
   
   /**
    * Constructor for Public game
    * @param newInfo
    * @param newPlayerList
    */
   public CreateGameMsg(GameInfo newInfo, List docTitleList, List categoryList)
   {
      this(newInfo, docTitleList, null, categoryList );
   }
   
   /**
    * @return Returns the gameInfo.
    */
   public GameInfo getGameInfo()
   {
      return gameInfo;
   }
   
   /**
    * @return Returns the playerList.
    */
   public List getPlayerList()
   {
      return playerList;
   }
   
   /**
    * @return Returns the list of document titles in this game.
    */
   public List getDocumentList()
   {
      return docTitleList;
   }
   
	/**
	 * @return Returns the categoryList.
	 */
	public List getCategoryList()
	{
	    return categoryList;
	}
}
