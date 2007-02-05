/*
 * Created on Oct 2, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package edu.virginia.speclab.ivanhoe.shared.message;


/**
 * @author lfoster
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class LoginResponseMsg extends Message
{
   private String  reason;
   private boolean success;
   
   public LoginResponseMsg()
   {
      super(MessageType.LOGIN_RESPONSE);
   }
      
   public String getReason()
   {
      return reason;
   }

   public boolean isSuccess()
   {
      return success;
   }

   public void setReason(String string)
   {
      reason = string;
   }

   public void setSuccess(boolean b)
   {
      success = b;
   }
   
   public String toString()
   {
      return super.toString() + " Success [" + success + 
         "] Reason [" + reason + "]";
   }

}
