/*
 * Created on Mar 17, 2004
 *
 * Document Cache
 */
package edu.virginia.speclab.ivanhoe.client.game.model.cache;

import java.io.*;
import java.util.*;

import edu.virginia.speclab.ivanhoe.client.Ivanhoe;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.DiscourseField;
import edu.virginia.speclab.ivanhoe.client.game.model.document.IvanhoeDocument;
import edu.virginia.speclab.ivanhoe.client.game.model.document.IvanhoeEditorKit;
import edu.virginia.speclab.ivanhoe.client.game.model.util.StatusEventMgr;
import edu.virginia.speclab.ivanhoe.client.network.GameProxy;
import edu.virginia.speclab.ivanhoe.shared.IMessageHandler;
import edu.virginia.speclab.ivanhoe.shared.IvanhoeException;
import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;
import edu.virginia.speclab.ivanhoe.shared.data.DocumentInfo;
import edu.virginia.speclab.ivanhoe.shared.message.*;

/**
 * @author lfoster
 *
 * Cache to handle documents for ivanhoe.
 * Maintains a list of valid files and tracks if they are local or not.
 * When a file is requested, the local status is checked. Local files
 * are loaded and returned, non-local fils are requested from server.
 */
public class DocumentCache implements IMessageHandler
{
    private final DiscourseField discourseField;
   
    private HashMap cacheMap;
    private String cacheRoot;
    private DocumentImporter importer;

   /**
    * Create a new cache that uses the specified directory as root
    * @param cacheRootDirectory
    */
   public DocumentCache(String cacheRootDirectory, DiscourseField discourseField)
   {
       this.discourseField = discourseField;
      this.cacheMap = new HashMap(5);
      this.cacheRoot = cacheRootDirectory;
      this.importer = new DocumentImporter(this.cacheRoot, discourseField);

      Ivanhoe.registerGameMsgHandler(MessageType.DOCUMENT_INFO, this);
      Ivanhoe.registerGameMsgHandler(MessageType.DOCUMENT_DATA, this);
      Ivanhoe.registerGameMsgHandler(MessageType.DOCUMENT_CHANGED, this);
      Ivanhoe.registerGameMsgHandler(MessageType.IMAGE_DATA, this);
      Ivanhoe.registerGameMsgHandler(MessageType.DOCUMENT_COMPLETE, this);
   }
   
   /**
    * Checks is the specified file is in the process of 
    * downloading from the server
    * @param docTitle
    * @return
    */
   public boolean isLoading(String docTitle)
   {
      CacheRecord rec = (CacheRecord)this.cacheMap.get(docTitle);
      if (rec != null)
      {
         return rec.isLoading;
      }
      
      return false;
   }   
   
   /**
    * Checks if the file is valid and can be used
    * @param docTitle
    * @return
    */
   public boolean isValid(String docTitle)
   {
      CacheRecord rec = (CacheRecord)this.cacheMap.get(docTitle);
      if (rec != null)
      {
         return rec.isValid();
      }
      
      return false;
   } 
   
   /**
    * Request a local instance of the document titled docTitle.
    * If the file is not local, it will begin downloading from server.
    * @param docTitle
    * @return true if the document was requested successfully
    */
   public boolean requestDocument(String docTitle)
   {
      // make sure doc is part of cache
      CacheRecord rec = (CacheRecord)this.cacheMap.get(docTitle);
      if( rec == null )
      {
         return false;
      }
      
      // don't re-request loading docs
      if (rec.isLoading == true)
      {
         SimpleLogger.logInfo("Document " + docTitle + 
         " is already loading");
         return true;
      }
      
      // only request non-local or dirty docs
      if (rec.isLocal == false || rec.isDirty == true)
      {
         rec.isLoading = true;
         rec.isValid = true;
         if (rec.isDirty)
         {
            SimpleLogger.logInfo("Requesting update for document " + docTitle);
            ((GameProxy)Ivanhoe.getProxy()).requestDocumentUpdate(rec.docInfo.getId());
         }
         else
         {
            SimpleLogger.logInfo("Requesting new document " + docTitle);
            ((GameProxy)Ivanhoe.getProxy()).requestDocument(rec.docInfo.getId());      
         }
      } 
      
      return true;
   }

   /**
    * Attempt to load a local file. If the file is non-local or
    * invalid, NULL will be returned
    * @param docInfo DocInfo for the local file
    * @return IvanhoeDocument or Null
    */
   public IvanhoeDocument loadDocument(String docTitle)
   {
      CacheRecord rec = (CacheRecord)this.cacheMap.get(docTitle);
      if (rec != null && rec.isValid() )
      {
         String filename = rec.docInfo.getFileName();
         try
         {
            // create doc
            IvanhoeEditorKit kit = new IvanhoeEditorKit(discourseField);
            IvanhoeDocument doc =
               kit.createDocumentFromFile(rec.cacheFile.getAbsolutePath());                       
            doc.setDocumentInfo(rec.docInfo);
            doc.setBase(rec.cacheFile.toURL());
            return doc;
         }
         catch (Exception e1)
         {
            SimpleLogger.logError(
               "Error loading local document " + filename + ": " + e1);
            return null;
         }
      }
      
      return null;
   }

   /**
    * Handle message from server.
    * The cache handles DOCUMENT_LIST and DOCUMENT_DATA messages
    */
   public void handleMessage(Message msg)
   {
      if (msg.getType().equals(MessageType.DOCUMENT_INFO))
      {
         handleDocumentInfo((DocumentInfoMsg) msg);
      }
      else if (msg.getType().equals(MessageType.DOCUMENT_DATA))
      {
         handleDocumentData((DocumentDataMsg) msg);
      }
      else if (msg.getType().equals(MessageType.IMAGE_DATA))
      {
         handleImageData((ImageDataMsg) msg);
      }
      else if (msg.getType().equals(MessageType.DOCUMENT_COMPLETE))
      {
         handleDocumentComplete((DocumentCompleteMsg) msg);
      }
      else if (msg.getType().equals(MessageType.DOCUMENT_CHANGED))
      {
         handleDocumentChanged( (DocumentChangedMsg)msg);
      }
   }

   /**
    * Mark changed document as dirty
    * @param msg
    */
   private void handleDocumentChanged(DocumentChangedMsg msg)
   {
      CacheRecord rec = (CacheRecord)this.cacheMap.get(msg.getChangedDocument().getTitle());
      if (rec != null)
      {
         if (rec.docInfo.getId().intValue() > 0)
         {
            SimpleLogger.logInfo("Marking " + msg.getChangedDocument() + " as dirty");
            rec.isDirty = true;
         }
      }
   }

   /**
    * Handle notification that a document and all of its supporting images
    * areavailable on client-side
    * @param msg
    */
   private void handleDocumentComplete(DocumentCompleteMsg msg)
   {
      // get the cache rec for this doc
      CacheRecord rec = (CacheRecord)this.cacheMap.get(msg.getDocumentInfo().getTitle());
      if (rec == null)
      {
          SimpleLogger.logError("DocumentCompleteMsg received for document with no CacheRecord");
          return;
      }
      
      CacheRecord newRec = new CacheRecord(msg.getDocumentInfo(), rec.cacheFile);
    
      if (rec.isValid)
      {
         newRec.isLocal = true;
         newRec.isDirty = false;
      }
      newRec.isLoading = false; 
      
      if (rec.cacheFile.exists() == false)
      {
         SimpleLogger.logInfo("Cache is marking missing document " 
            + rec.docInfo.getTitle() + " as invalid");
         rec.invalidate();
      }
      else
      {
          this.cacheMap.put(newRec.docInfo.getTitle(), newRec);
      }
   }

   /**
    * @param msg
    */
   private void handleImageData(ImageDataMsg msg)
   {
      // make sure the directory structure exists
      File destFile = new File(this.cacheRoot + File.separator + msg.getImageFileName());
      File parentFile = destFile.getParentFile();
      if (parentFile.exists() == false)
      {
         if (parentFile.mkdirs() == false)
         {
            SimpleLogger.logError("Unable to make directories for file " + destFile);
            return;
         }
      }
      
      // Assemble the data chunks into a complete file
      FileOutputStream writer = null;
      try
      {
         // write the chunk of data to a local file
         writer = new FileOutputStream(destFile, true);
         writer.write(msg.getDataBufer(), 0, msg.getBufferSize());
      }
      catch (IOException e)
      {
         SimpleLogger.logError("Error receiving data for " + 
            msg.getImageFileName() + " " + e);
      }
      finally
      {
         if (writer != null)
         {
            try
            {
               writer.flush();
               writer.close();
            }
            catch (IOException e2)
            {
            }
         }
      }
   }

   /**
    * Assemble data into complete files, and notify listeners
    * @param msg
    */
   private void handleDocumentData(DocumentDataMsg msg)
   {
      DocumentInfo docInfo = msg.getDocumentInfo();
      
      // make sure this is a cache managed document that has been requested
      CacheRecord rec = (CacheRecord)this.cacheMap.get(docInfo.getTitle());
      if (rec != null && rec.isValid == true)
      {
         // Assemble the data chunks into a complete file
         FileWriter writer = null;
         try
         {
            // write the chunk of data to a local file
            writer = new FileWriter(rec.cacheFile, true);
            String s = new String(msg.getDataBufer());
            writer.write(s, 0, msg.getBufferSize());
         }
         catch (IOException e)
         {
            SimpleLogger.logError("Error receiving data for " + docInfo + " " + e);
            if (rec.cacheFile.exists())
            {
               rec.cacheFile.delete();
            }
            rec.invalidate();
         }
         finally
         {
            if (writer != null)
            {
               try
               {
                  writer.flush();
                  writer.close();
               }
               catch (IOException e2)
               {
               }
            }
         }
      }
      else
      {
         SimpleLogger.logError(
            "Cache got datamsg for unknown document " + docInfo.getTitle());
      }
   }

   /**
    * Put all files in the list message into the cacheMap
    * @param msg
    */
   private void handleDocumentInfo(DocumentInfoMsg msg)
   {
      SimpleLogger.logInfo("Cache got document info for " + 
         msg.getDocumentInfo().getTitle());
      
      // Put it in the cache map
      DocumentInfo docInfo = msg.getDocumentInfo();

      String absolutePath = this.cacheRoot + File.separator + docInfo.getFileName();
      File file = new File(absolutePath);
      CacheRecord rec = new CacheRecord(docInfo, file);
      
      // first time files always need an update.. dont mark as local
      // til they've been requested once
      //rec.isLocal = file.exists();
      
      // if the file already exists, purge it so multiple versions
      // of same doc are not appended
      rec.isLocal = false;
      if (file.exists())
      {
         file.delete();
      }
     
      this.cacheMap.put(docInfo.getTitle(),rec);   
   }
   
   /**
    * Get a list of docInfo for documents managed by the cache
    * @return
    */
   public List getDocumentInfoList()
   {
      ArrayList docList = new ArrayList();
      CacheRecord rec;
      for (Iterator i = this.cacheMap.entrySet().iterator(); i.hasNext();)
      {
         Map.Entry entry = (Map.Entry) i.next();
         rec = (CacheRecord)entry.getValue();
         docList.add(rec.docInfo);
      }
      return docList;
   }
   
   /**
    * @param title
    * @return
    */
   public DocumentInfo getDocumentInfo(String title)
   {
      CacheRecord rec = (CacheRecord)this.cacheMap.get(title);
      if (rec == null)
      {
          throw new RuntimeException("No CacheRecord found for \""+title+"\"");
      }
      else
      {
          return rec.docInfo;
      }
   }
   
   /**
    * Remove a document from the cache
    * @param docTitle
    * @return
    */
   public boolean deleteDocument(String docTitle)
   {
      if (this.cacheMap.containsKey(docTitle))
      {
         SimpleLogger.logInfo("Removing " + docTitle + " from cache");
         CacheRecord rec = (CacheRecord)this.cacheMap.get(docTitle);
         this.cacheMap.remove(docTitle);
         
         // send delete to server
         DeleteDocumentMsg msg = new DeleteDocumentMsg( rec.docInfo );
         Ivanhoe.getProxy().sendMessage(msg);
      }
      
      return false;
   }
   
   /**
    * Add a supporting image to the specified document
    * @param parentInfo Doc info for the parent document
    * @param imageFile Image file being added
    */
   public void addImage(DocumentInfo parentInfo, File imageFile)
   {
      // make sure this parent doc is part of the DF
      if (this.cacheMap.containsKey(parentInfo.getTitle()) == false)
      {
         SimpleLogger.logError("Attempt to add image to invalid document " 
            + parentInfo);
         StatusEventMgr.fireErrorMsg(
            "Unable to add image to invalid document titled "
               + parentInfo.getTitle());
         return;
      }
      
      try
      {
         File cacheFile = new File(this.cacheRoot + File.separator + imageFile.getName());
         DocumentImporter.copyFile(imageFile, cacheFile);
         this.importer.sendImageToServer(parentInfo, cacheFile);
      }
      catch (IvanhoeException e)
      {
         SimpleLogger.logError("Error copying image to cache", e);
         StatusEventMgr.fireErrorMsg("<html><b>Unable to add image to document</b><br>Reason: " + e.getMessage()); 
      }
   }
   
   /**
    * Import a document into the cache
    * @param docInfo
    * @param path
    * @return
    */
   public boolean importDocument(DocumentInfo docInfo, File newFile)
   {
      // make sure this doc is not already part of the DF
      if (this.cacheMap.containsKey(docInfo.getTitle()))
      {
         SimpleLogger.logError(
            "Unable to add "
               + docInfo.getFileName()
               + " - it is already part of the DF");
         StatusEventMgr.fireErrorMsg(
            "Unable to add '"
               + docInfo.getFileName()
               + "'; it is already part of the Discourse Field");
         return false;
      }

      try
      {
         File importedDoc = this.importer.importDocument(docInfo, newFile);
         CacheRecord newDocRec = new CacheRecord(docInfo,importedDoc);
         newDocRec.isLocal = true;
         this.cacheMap.put(docInfo.getTitle(), newDocRec);
         return true;
      }
      catch (IvanhoeException e)
      {
         SimpleLogger.logError("Unable to import " + docInfo.getTitle() + ": " + e);
      }
      
      return false;
   }
    
   /**
    * CacheRecord collects the docInfo and a flag to indicate 
    * if the file is local
    */
   private static class CacheRecord
   {
      public final DocumentInfo docInfo;
      public final File cacheFile;
      public boolean isLocal;
      public boolean isLoading;
      public boolean isValid;
      public boolean isDirty;
      
      public CacheRecord(DocumentInfo docInfo, File cacheFile)
      {
         if (docInfo == null)
         {
             throw new IllegalArgumentException("CacheRecord() does not take a null docInfo");
         }
         if (cacheFile == null)
         {
             throw new IllegalArgumentException("CacheRecord() does not take a null cacheFile");
         }
         this.docInfo = docInfo;
         this.cacheFile = cacheFile;
         this.isLoading = false;
         this.isLocal = false;
         this.isValid = true;
         this.isDirty = false;
      }
      
      public boolean isValid()
      {
         return (this.isDirty == false && 
                 this.isLocal == true && 
                 this.isValid == true);
      }
      
      public void invalidate()
      {
         this.isLocal = false;
         this.isValid = false;
         this.isDirty = false;
      }
   }
}
