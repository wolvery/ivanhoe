/*
 * Created on Mar 22, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package edu.virginia.speclab.ivanhoe.shared.message;

import edu.virginia.speclab.ivanhoe.shared.data.DocumentInfo;

/**
 * @author lfoster
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class DeleteDocumentMsg extends Message
{
   private DocumentInfo docInfo;
   
   public DeleteDocumentMsg(DocumentInfo delDocInfo)
   {
      super(MessageType.DELETE_DOCUMENT);
      this.docInfo = delDocInfo;
   }
   
   public DocumentInfo getInfo()
   {
      return this.docInfo;
   }
}
