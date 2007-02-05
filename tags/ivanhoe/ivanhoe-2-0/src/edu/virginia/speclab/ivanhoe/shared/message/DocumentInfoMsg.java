/*
 * Created on Dec 4, 2003
 *
 * DocumentInfoMsg
 */
package edu.virginia.speclab.ivanhoe.shared.message;

import edu.virginia.speclab.ivanhoe.shared.data.DocumentInfo;

/**
 * @author lfoster
 *
 * Message contains info about a Document contained in the DF
 */
public class DocumentInfoMsg extends Message
{
   private DocumentInfo docInfo;
   
   public DocumentInfoMsg(DocumentInfo docInfo)
   {
      super(MessageType.DOCUMENT_INFO);
      this.docInfo = docInfo;
   }
   
   public DocumentInfo getDocumentInfo()
   {
      return this.docInfo;
   }
}
