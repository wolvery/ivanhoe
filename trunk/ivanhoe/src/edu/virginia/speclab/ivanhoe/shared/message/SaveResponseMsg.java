/*
 * Created on Mar 1, 2004
 *
 * SaveResponseMsg
 */
package edu.virginia.speclab.ivanhoe.shared.message;

/**
 * @author lfoster
 *
 * REsponse to a players save message
 */
public class SaveResponseMsg extends Message
{
   private boolean success;

   public SaveResponseMsg(boolean success)
   {
      super(MessageType.SAVE_RESPONSE);
      this.success = success;
   }
  
   public boolean isSuccess()
   {
      return success;
   }

}
