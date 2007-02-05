/*
 * Created on Nov 18, 2004
 */
package edu.virginia.speclab.ivanhoe.shared.message;

import edu.virginia.speclab.ivanhoe.shared.data.User;

/**
 * @author dgran
 *
 * Message to indicate the success or failure of the password reset operation
 */
public class PassResetResponseMsg extends Message
{
   /** if reset was successful, the following variable is true */
   private boolean success;
   
   /** the User object for which the password was reset */
   private User user;
   
   public PassResetResponseMsg(boolean success, User user)
   {
      super(MessageType.PASS_RESET_RESPONSE);
      this.success = success;
      this.user = user;
   }
   
   public boolean passwordResetSuccessfully()
   {
      return success;
   }
   
   public String toString()
   {
      return super.toString() + "Password reset for " + user.getUserName() + 
         " status: " + success;
   }
}
