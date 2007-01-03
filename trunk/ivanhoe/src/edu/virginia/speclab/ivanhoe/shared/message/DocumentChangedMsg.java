/*
 * Created on Jul 23, 2004
 *
 * DocumentChangedMsg
 */
package edu.virginia.speclab.ivanhoe.shared.message;

import edu.virginia.speclab.ivanhoe.shared.data.DocumentInfo;

/**
 * @author lfoster
 *
 * This message is broadcast from the server to indicate that a document
 * has changed (images added, generally)
 */
public class DocumentChangedMsg extends Message
{
   private final DocumentInfo changedDocument;
   
   public DocumentChangedMsg(DocumentInfo changedDoc)
   {
      super(MessageType.DOCUMENT_CHANGED);
      this.changedDocument = changedDoc;
   }
  
   public DocumentInfo getChangedDocument()
   {
      return this.changedDocument;
   }
}
