/*
 * Created on Feb 23, 2004
 *
 * PlayerListMsg
 */
package edu.virginia.speclab.ivanhoe.shared.message;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lfoster
 *
 * Message containing a list of players that are participating the game
 */
public class PlayerListMsg extends Message
{
   private List playerList;
   
   public PlayerListMsg(List players)
   {
      super(MessageType.PLAYER_LIST);
      this.playerList = new ArrayList();
      this.playerList.addAll( players );
   }
   
   public List getNames()
   {
      return playerList;
   }
}
