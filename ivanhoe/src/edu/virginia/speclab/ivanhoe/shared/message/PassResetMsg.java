/*
 * Created on Nov 17, 2004
 */
package edu.virginia.speclab.ivanhoe.shared.message;

import edu.virginia.speclab.ivanhoe.shared.data.User;

/**
 * @author dgran
 *
 * Message to reset a password for a user.  The User object itself doesn't contain
 * the new password.  It is reset on the server.
 */
public class PassResetMsg extends Message
{
   private User user;
   
   public PassResetMsg(User user)
   {
      super(MessageType.PASS_RESET);
      this.user = user;
   }
   
   public User getUser()
   {
      return user;
   }   
   
   public String toString()
   {
      return super.toString() + " password for [" + user + "]";
   }
}
