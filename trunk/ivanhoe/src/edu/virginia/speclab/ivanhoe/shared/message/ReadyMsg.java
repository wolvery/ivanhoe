/*
 * Created on Dec 4, 2003
 * ReadyMsg
 */
package edu.virginia.speclab.ivanhoe.shared.message;

/**
 * @author lfoster
 *
 * Notification sent from Server -> client to indicate that the game
 * is ready to be played. 
 */
public class ReadyMsg extends Message
{
   public ReadyMsg()
   {
      super(MessageType.READY);
   }
}
