/*
 * Created on Mar 1, 2004
 *
 * RestoreMsg
 */
package edu.virginia.speclab.ivanhoe.shared.message;

import edu.virginia.speclab.ivanhoe.shared.data.Move;

/**
 * @author lfoster
 *
 * Message sent from server->client when restoring a saved game
 */
public class RestoreMsg extends MoveMsg
{
   public RestoreMsg(Move move)
   {
      super(move, MessageType.RESTORE);
   }
}
