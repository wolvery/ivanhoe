/*
 * Created on Mar 11, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package edu.virginia.speclab.ivanhoe.shared.message;

import edu.virginia.speclab.ivanhoe.shared.data.DiscussionEntry;

/**
 * @author lfoster
 *
 * Message used to transmit discussion data between client/server
 */
public class DiscussionEntryMsg extends Message
{
   private DiscussionEntry entry;
   
   public DiscussionEntryMsg(DiscussionEntry entry)
   {
      super(MessageType.DISCUSSION_ENTRY);
      this.entry = entry;
   }
   
   public DiscussionEntry getEntry()
   {
      return this.entry;
   }
}
