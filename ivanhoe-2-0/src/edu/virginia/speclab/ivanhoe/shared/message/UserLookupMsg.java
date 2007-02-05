/*
 * Created on Nov 17, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package edu.virginia.speclab.ivanhoe.shared.message;

/**
 * @author dgran
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class UserLookupMsg extends Message
{
   private String userId;
   
   public UserLookupMsg(String userId)
   {
      super(MessageType.USER_LOOKUP);
      this.userId = userId;
   }
   
   public String getIdText()
   {
      return userId;
   }
   
   public String toString()
   {
      return super.toString() + " user [" + userId + "]";
   }
}
