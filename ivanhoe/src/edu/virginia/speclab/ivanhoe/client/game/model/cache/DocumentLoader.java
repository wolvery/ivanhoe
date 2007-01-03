/*
 * Created on Mar 18, 2004
 */
package edu.virginia.speclab.ivanhoe.client.game.model.cache;

import edu.virginia.speclab.ivanhoe.client.game.model.discourse.CurrentMove;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.DiscourseField;
import edu.virginia.speclab.ivanhoe.client.game.model.document.IvanhoeDocument;
import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;
import edu.virginia.speclab.ivanhoe.shared.data.*;

/**
 * Asynchronously loads a document from the local cache. If the document
 * is not present in the local cache then the document is fetched from the
 * server. Calls the registered callback when complete.
 * @author Nick Laiacona
 */
public class DocumentLoader extends Thread
{
   private boolean readOnly;
   private DocumentCache documentCache;
   private DiscourseField discourseField;
   private IDocumentLoaderListener documentLoaderListener;
   private DocumentVersion version;
   
   
   /**
    * Create a new document loader. Call start() to begin loading the 
    * document in a separate thread. 
    * @param docCache The document cache to use for loading.
    * @param version The version of the document to retrieve
    * @param listener The listener to call back when the document is done loading.
    * @param readOnly Sets whether or not the document will be editable.
    */
   public DocumentLoader(
      DiscourseField discourseField,
      DocumentCache docCache,
      DocumentVersion version,
      IDocumentLoaderListener listener, boolean readOnly)
   {
      super();

      this.version = version;
      this.documentCache = docCache;
      this.documentLoaderListener = listener;
      this.discourseField = discourseField;
      this.readOnly = readOnly;
   }
   
   /**
    * The runnable interface for this thread. Should not be called directly, 
    * call <code>start()</code>.
    */
   public void run()
   {
       SimpleLogger.logInfo("Starting DocumentLoader for ["+version+"]");      

       // request the document
       String docTitle = this.version.getDocumentTitle();
       if (documentCache.requestDocument(docTitle) == false)
       {
          documentLoaderListener.documentLoadError(this.version.getDocumentTitle(),
             "Document is not part of the Discourse Field.");
          return;
       }

       // wait until the document data is all available
       while (documentCache.isLoading(docTitle))
       {
          try
          {
             // just hang out until the document is downloaded from server.
             Thread.sleep(500);
          }
          catch (InterruptedException e)
          {
          }
       }

       // create a new document with the data
       IvanhoeDocument doc = documentCache.loadDocument(docTitle);

       if (doc != null)
       {
          // set readonly flag
          doc.setReadOnly(this.readOnly);
          
          // create a document updater
          DocumentUpdater updater = new DocumentUpdater(discourseField, doc, this.version);
          CurrentMove currentMove = discourseField.getCurrentMove();
          
          // Recurse through the DocumentVersions and add all their actions
          updater.addActions();
          
           // add any cached acts from current move
          updater.addCurrentActions( currentMove.activateCachedActions(this.version) );
          
          // set the doc to the correct state
          updater.updateDocument();
          
          // tag the document version
          doc.setVersion(this.version);
          
          // register current move as listener for changes
          doc.addActionListener(currentMove);   
          
          // Add doc to DF list of open docs 
          discourseField.addOpenDocument(doc);
         
          // send the document to the listener.
          documentLoaderListener.documentLoaded(doc);
       }
       else
       {
          String errorMessage =
             "Error downloading document ["
             + docTitle
             + "] from server.";

          documentLoaderListener.documentLoadError(
             this.version.getDocumentTitle(),
             errorMessage);
       }
   }
}
