/*
 * Created on Jun 15, 2004
 *
 * ImageDataMsg
 */
package edu.virginia.speclab.ivanhoe.shared.message;

import edu.virginia.speclab.ivanhoe.shared.data.DocumentInfo;

/**
 * @author lfoster
 *
 * Message for sending image data to/from server
 */
public class ImageDataMsg extends DataMsg
{
   private String fileName;
   private DocumentInfo parentDocument;

   public ImageDataMsg(String fileName, DocumentInfo parent)
   {
      super(MessageType.IMAGE_DATA);
      this.fileName = fileName;
      this.parentDocument = parent;
   }
   
   public DocumentInfo getParentDocumentInfo()
   {
      return this.parentDocument;
   }
   
   public String getImageFileName()
   {
      return this.fileName;
   }
}
