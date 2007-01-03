/*
 * Created on Mar 2, 2004
 *
 * JournalDataMsg
 */
package edu.virginia.speclab.ivanhoe.shared.message;

/**
 * @author lfoster
 *
 * Extension of the abstract DataMessage to handle data
 * used by the journal
 */
public class JournalDataMsg extends DataMsg
{
   public JournalDataMsg()
   {
      super(MessageType.JOURNAL_DATA);
   }
}
