/*
 * Created on Dec 5, 2003
 *
 * DocumentRequestMsg
 */
package edu.virginia.speclab.ivanhoe.shared.message;

/**
 * @author lfoster
 *
 * This message is sent by the client to request data for
 * the document named docName
 */
public class DocumentRequestMsg extends Message
{
   private final Integer docId;
   private final boolean fullUpdate;
   
   public DocumentRequestMsg(Integer docId, boolean fullUpdate)
   {
      super(MessageType.DOCUMENT_REQUEST);
      this.docId = docId;
      this.fullUpdate = fullUpdate;
   }

   public Integer getDocumentId()
   {
      return this.docId;
   }
   
   public boolean isFullUpdate()
   {
      return this.fullUpdate;
   }
   
   public String toString()
   {
      return super.toString() + " documentId [" + this.docId + "]";
   }
}
