/*
 * Created on Dec 16, 2003
 *
 * MoveResponseMsg
 */
package edu.virginia.speclab.ivanhoe.shared.message;

/**
 * @author lfoster
 *
 * Success flag and text sent in response to a move submission
 */
public class MoveResponseMsg extends Message
{
   private boolean success;
   private String message;
   
   public MoveResponseMsg(boolean success, String msg)
   {
      super(MessageType.MOVE_RESPONSE);
      this.success = success;
      this.message = msg;
   }
      
   public String getMessage()
   {
      return message;
   }

   public boolean isSuccess()
   {
      return success;
   }
   
   public String toString()
   {
      return super.toString() + " success [" + this.success +
         "] message [" + this.message + "]";
   }
}
