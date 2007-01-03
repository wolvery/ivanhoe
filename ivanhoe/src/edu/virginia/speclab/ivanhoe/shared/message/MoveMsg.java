/*
 * Created on Dec 16, 2003
 *
 * MoveMsg
 */
package edu.virginia.speclab.ivanhoe.shared.message;

import edu.virginia.speclab.ivanhoe.shared.data.Move;

/**
 * @author lfoster
 *
 * Message for transmitting moves between client & server
 */
public class MoveMsg extends Message
{
   private final Move move;
   
   public MoveMsg(Move move)
   {
      super(MessageType.MOVE);
      this.move = (move != null) ? new Move(move) : move;
   }
   
   protected MoveMsg(Move move, MessageType msgType)
   {
       super(msgType);
       this.move = (move != null) ? new Move(move) : move;
   }
   
   public Move getMove()
   {
      return this.move;
   }
   
   public String toString()
   {
      String moveMessage;
      
      if( move != null )
      {
          moveMessage = this.move.toString();          
      }
      else
      {
          moveMessage = "null";
      }
      
      return super.toString() + " Move [" + moveMessage + "]";
   }
}
