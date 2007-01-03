/*
 * Created on Oct 3, 2003
 *
 * ChatMsg
 */
package edu.virginia.speclab.ivanhoe.shared.message;


/**
 * @author lfoster
 *
 * This message is used to send direct or general chat throught Ivanhoe
 */
public class ChatMsg extends Message
{
   private String chatText;
   private String targetPlayer;
   private String speakerRole;
   private boolean privateMsg;
   
   public ChatMsg( String speakerRole, String targetPlayer, String msg)
   {
      super(MessageType.CHAT);
      this.chatText = msg;
      this.privateMsg = true;
      this.targetPlayer = targetPlayer;
      this.speakerRole = speakerRole;
   }
   
   public ChatMsg( String speakerRole, String msg)
   {
      super(MessageType.CHAT);
      this.chatText = msg;
      this.privateMsg = false;
      this.speakerRole = speakerRole;
   }
   
   public boolean isPrivate()
   {
      return this.privateMsg;
   }
   
   public String getTargetPlayer()
   {
      return this.targetPlayer;
   }
   
   public String getChatText()
   {
      return chatText;
   }

   public String getSpeakerRole()
   {
	  return speakerRole;
   }
}
