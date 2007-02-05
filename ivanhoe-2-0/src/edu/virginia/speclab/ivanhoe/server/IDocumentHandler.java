/*
 * Created on Jul 7, 2004
 *
 * IDocumentHandler
 */
package edu.virginia.speclab.ivanhoe.server;

import edu.virginia.speclab.ivanhoe.shared.data.DocumentInfo;

/**
 * @author lfoster
 *
 * this interface is called when the documentReceiver has successfully
 * received a new document
 */
public interface IDocumentHandler
{
   public void documentReady(DocumentInfo newDocInfo);
   public void documentChanged(DocumentInfo changedDocInfo);
   public void documentError(DocumentInfo badInfo, String errorMsg);
}
