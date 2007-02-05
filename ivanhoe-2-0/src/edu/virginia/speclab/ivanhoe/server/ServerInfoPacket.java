/*
 * Created on Jul 8, 2004
 *
 * ServerInfoPacket
 */
package edu.virginia.speclab.ivanhoe.server;

import java.io.Serializable;

/**
 * @author lfoster
 *
 * Datagram used to identify a gameserver. A gamserver can hos many games
 */
public class ServerInfoPacket implements InfoPacket, Serializable
{
   private String serverName;
   private int basePort;
   private boolean valid;
   
   public ServerInfoPacket(String name, int port)
   {
      this.serverName = name;
      this.basePort = port;
      this.valid = true;
   }
   
   /**
    * @return Returns the basePort.
    */
   public int getBasePort()
   {
      return basePort;
   }
   
   /**
    * @return Returns the serverName.
    */
   public String getServerName()
   {
      return serverName;
   }
   
   /**
    * @return Returns the valid.
    */
   public boolean isValid()
   {
      return valid;
   }
}
