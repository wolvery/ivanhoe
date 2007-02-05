/*
 * Created on Jul 15, 2004
 *
 * CreateAccountMsg
 */
package edu.virginia.speclab.ivanhoe.shared.message;

import edu.virginia.speclab.ivanhoe.shared.data.User;

/**
 * @author lfoster
 *
 * Message is passed from client to server to request the creation
 * of a new user account
 */
public class CreateAccountMsg extends Message
{
   private User newUser;
   
   public CreateAccountMsg(User newUser)
   {
      super(MessageType.CREATE_ACCOUNT);
      this.newUser = new User(newUser);
   }
   
   public User getNewUserData()
   {
      return this.newUser;
   }
}
