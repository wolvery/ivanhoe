/*
 * Created on Jul 6, 2004
 *
 * PlayerListRequestMsg
 */
package edu.virginia.speclab.ivanhoe.shared.message;

/**
 * @author lfoster
 *
 * Message used to request a list of ivanhoe players
 */
public class PlayerListRequestMsg extends Message
{
   private int requestType;
   
   public static final int ONLINE_PLAYERS = 0;
   public static final int ALL_PLAYERS = 1;
   
   public PlayerListRequestMsg(int requestType)
   {
      super(MessageType.PLAYER_LIST_REQUEST);
      
      if (requestType == ONLINE_PLAYERS || requestType == ALL_PLAYERS)
      {
         this.requestType = requestType;
      }
      else
      {
         throw new IllegalArgumentException("requestType");
      }
   }
   
   public int getRequestType()
   {
      return this.requestType;
   }

}
