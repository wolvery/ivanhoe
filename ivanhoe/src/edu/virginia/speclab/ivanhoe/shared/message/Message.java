//
// Message
// Base message class for ivanhoe
// Author: Lou Foster
// Date  : 10/01/03
//
package edu.virginia.speclab.ivanhoe.shared.message;

import java.io.*;


public abstract class Message implements Serializable
{
   private final MessageType msgType;
   protected String      sender;
   protected String 	 password;
   protected int		 gameID;
   
   protected Message(MessageType msgType)
   {
       this.msgType = msgType;
       this.sender = "SYSTEM";
   }
   
   public MessageType getType()
   {
      //assert (msgType.equals(MessageType.UNDEFINED));
      return this.msgType;
   }
   
   public void setSender(String sender)
   {
      this.sender = sender;
   }
   
   public String getSender()
   {
      return this.sender;
   }
   
   public String toString()
   {
      return this.msgType.toString() + " from [" + this.sender + "]";
   }

	public int getGameID() {
		return gameID;
	}
	
	public void setGameID(int gameID) {
		this.gameID = gameID;
	}
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
}
