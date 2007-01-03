/*
 * Created on Mar 15, 2004
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
public class DiscussionResponseMsg extends Message
{
   private boolean success;
   private String message;
   
   public DiscussionResponseMsg(boolean success, String msg)
   {
      super(MessageType.DISCUSSION_RESPONSE);
      this.success = success;
      this.message = msg;
   }
   
   public boolean isSuccess()
   {
      return this.success;
   }
   
   public String getMessage()
   {
      return this.message;
   }
}
