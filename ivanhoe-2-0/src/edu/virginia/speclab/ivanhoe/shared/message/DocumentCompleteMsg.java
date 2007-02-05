/*
 * Created on Jun 15, 2004
 *
 * DocumentComplete
 */
package edu.virginia.speclab.ivanhoe.shared.message;

import edu.virginia.speclab.ivanhoe.shared.data.DocumentInfo;

/**
 * @author lfoster
 *
 * Notification from server->client that all files required
 * to open a document have been sent
 */
public class DocumentCompleteMsg extends Message
{
   private DocumentInfo docInfo;

   public DocumentCompleteMsg(DocumentInfo di)
   {
      super(MessageType.DOCUMENT_COMPLETE);
      this.docInfo = di;
   }
   
   public DocumentInfo getDocumentInfo()
   {
      return this.docInfo;
   }
}
