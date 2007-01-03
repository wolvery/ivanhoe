/*
 * Created on Jun 23, 2004
 *
 * GameInfoPacket
 */
package edu.virginia.speclab.ivanhoe.server;

import java.io.Serializable;

import edu.virginia.speclab.ivanhoe.shared.data.GameInfo;

/**
 * @author lfoster
 *
 * Datagram used to broadcast game availability
 */
public class GameInfoPacket implements InfoPacket, Serializable
{
   private GameInfo info;
   private String gameServerName;
   private int port;
   private int playerCount;
   private boolean valid;
   
   public GameInfoPacket(GameInfo info, String gameServerName, int port, int cnt)
   {
      this.info = info;
      this.gameServerName = gameServerName;
      this.port = port;
      this.playerCount = cnt;
      this.valid = true;
   }
   
   public String toString()
   {
      return "Game '" + getInfo().getName() + "' on " + getGameServerName() + 
         " at port " + getPort() + 
         " with " + getPlayerCount() + " players";
   }
   
   public boolean isValid()
   {
      return this.valid;
   }
   
   public String getGameServerName()
   {
      return this.gameServerName;
   }
   
   public GameInfo getInfo()
   {
      return this.info;
   }
    
   /**
    * @return Returns the playerCount.
    */
   public int getPlayerCount()
   {
      return this.playerCount;
   }
   
   /**
    * @return Returns the port.
    */
   public int getPort()
   {
      return this.port;
   }
}
