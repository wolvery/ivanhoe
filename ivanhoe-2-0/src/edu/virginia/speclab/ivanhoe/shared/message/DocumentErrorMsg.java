/*
 * Created on Jul 19, 2004
 *
 * DocumentErrorMsg
 */
package edu.virginia.speclab.ivanhoe.shared.message;

import edu.virginia.speclab.ivanhoe.shared.data.DocumentInfo;

/**
 * @author lfoster
 *
 * Document error notification message
 */
public class DocumentErrorMsg extends Message
{
   private DocumentInfo documentInfo;
   private String message;
   
   public DocumentErrorMsg(DocumentInfo badInfo, String errMsg)
   {
      super(MessageType.DOCUMENT_ERROR);
      this.documentInfo = new DocumentInfo(badInfo);
      this.message = errMsg;
   }
   
   public String getErrorMessage()
   {
      return this.message;
   }
   
   public DocumentInfo getDocumentInfo()
   {
      return this.documentInfo;
   }
}
