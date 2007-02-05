/*
 * Created on Feb 2, 2004
 *
 * ServerErrorMsg
 */
package edu.virginia.speclab.ivanhoe.shared.message;

/**
 * @author lfoster
 *
 * Message used oo report server error conditions to players
 */
public class ServerErrorMsg extends Message
{
   private String errorTxt;
   private boolean isFatal;

   public ServerErrorMsg(String errorTxt, boolean isFatal)
   {
      super(MessageType.SERVER_ERROR);
      this.errorTxt = errorTxt;
      this.isFatal = isFatal;
   }
   
   public boolean isFatal()
   {
      return this.isFatal;
   }
   
   public String getErrorTxt()
   {
      return this.errorTxt;
   }
}
