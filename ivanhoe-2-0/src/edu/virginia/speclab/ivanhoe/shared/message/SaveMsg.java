/*
 * Created on Mar 1, 2004
 *
 * SaveMsg
 */
package edu.virginia.speclab.ivanhoe.shared.message;

import java.util.Map;

import edu.virginia.speclab.ivanhoe.shared.data.Move;

/**
 * @author lfoster
 *
 * Request to save current player state
 */
public class SaveMsg extends MoveSubmitMsg
{
   public SaveMsg(Move move, Map documentVersionOriginsMap)
   {       
      super(move, documentVersionOriginsMap, MessageType.SAVE );
   }
}
