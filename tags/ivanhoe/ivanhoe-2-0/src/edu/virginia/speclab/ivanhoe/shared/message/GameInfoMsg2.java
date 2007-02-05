/*
 * Created on Jun 28, 2004
 *
 * GameInfoMsg
 */
package edu.virginia.speclab.ivanhoe.shared.message;

import edu.virginia.speclab.ivanhoe.shared.data.GameInfo;

/**
 * @author lfoster
 *
 * Game info message to be broadcast to lobby clients
 */
public class GameInfoMsg2 extends Message
{
   private GameInfo info;
   
   public GameInfoMsg2(GameInfo info)
   {
	   super(MessageType.GAME_INFO);
	   this.info = info;
   }
   
   /**
    * @return Returns the info.
    */
   public GameInfo getInfo()
   {
      return info;
   }
}
