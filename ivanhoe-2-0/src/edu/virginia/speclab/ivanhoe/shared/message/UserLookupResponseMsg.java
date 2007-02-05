/*
 * Created on Nov 17, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package edu.virginia.speclab.ivanhoe.shared.message;

import java.util.List;

/**
 * @author dgran
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class UserLookupResponseMsg extends Message
{
   private List users;
   
   public UserLookupResponseMsg(List users)
   {
      super(MessageType.USER_LOOKUP_RESPONSE);
      this.users = users;
   }
   
   public List getUsers()
   {
      return users;
   }
   
   public String toString()
   {
      return super.toString() + " users in message [" + users + "]";
   }
}
