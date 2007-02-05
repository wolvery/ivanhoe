/*
 * Created on Jul 6, 2004
 *
 * CreateGameResponseMsg
 */
package edu.virginia.speclab.ivanhoe.shared.message;

/**
 * @author lfoster
 *
 * Message sent from server->clent to indicate results of create game attempt
 */
public class CreateGameResponseMsg extends Message
{
   private boolean success;
   private String message;

   public CreateGameResponseMsg(boolean success, String msg)
   {
      super(MessageType.CREATE_GAME_RESPONSE);
      this.success = success;
      this.message = msg;
   }
   
   /**
    * @return Returns the message.
    */
   public String getMessage()
   {
      return message;
   }
   
   /**
    * @return Returns the success.
    */
   public boolean isSuccess()
   {
      return success;
   }

}
