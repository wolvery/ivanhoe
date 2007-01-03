/*
 * Created on Dec 5, 2003
 *
 * DocumentDataMsg
 */
package edu.virginia.speclab.ivanhoe.shared.message;

import edu.virginia.speclab.ivanhoe.shared.data.DocumentInfo;

/**
 * @author lfoster
 *
 * This message contains a chunk of document data
 */
public class DocumentDataMsg extends DataMsg
{
   private DocumentInfo docInfo;
   
   public DocumentDataMsg(DocumentInfo docInfo)
   {
      super(MessageType.DOCUMENT_DATA);
      this.docInfo = docInfo;
   }

   public DocumentInfo getDocumentInfo()
   {
      return this.docInfo;
   } 
}
