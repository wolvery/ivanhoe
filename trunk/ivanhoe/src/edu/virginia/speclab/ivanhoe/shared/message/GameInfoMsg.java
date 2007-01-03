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
public class GameInfoMsg extends Message
{
   private GameInfo info;
   private String gameServerName;
   private String host;
   private int  port;
   private int playerCnt;
   
   public GameInfoMsg(GameInfo info)
   {
      this("", info, "", 0,0);
   }
   
   public GameInfoMsg(String gameServerName, GameInfo info, String host, int port, int cnt)
   {
      super(MessageType.GAME_INFO);
      this.info = info;
      this.gameServerName = gameServerName;
      this.host = host;
      this.playerCnt = cnt;
      this.port = port;
   }

   /**
    * @return Returns the info.
    */
   public GameInfo getInfo()
   {
      return info;
   }
   
   /**
    * @return Returns the name of the gameserver that is hosting this game
    */
   public String getGameServerName()
   {
      return this.gameServerName;
   }
   
   /**
    * @return Returns the playerCnt.
    */
   public int getPlayerCnt()
   {
      return playerCnt;
   }
   
   /**
    * @return Returns the port.
    */
   public int getPort()
   {
      return port;
   }
   
   /**
    * @return Returns the host.
    */
   public String getHost()
   {
      return host;
   }
}
