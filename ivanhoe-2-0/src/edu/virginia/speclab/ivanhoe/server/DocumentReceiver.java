/*
 * Created on Jul 7, 2004
 *
 * DocumentReceiver
 */
package edu.virginia.speclab.ivanhoe.server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.virginia.speclab.ivanhoe.server.exception.MapperException;
import edu.virginia.speclab.ivanhoe.server.mapper.DocumentMapper;
import edu.virginia.speclab.ivanhoe.shared.IMessageHandler;
import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;
import edu.virginia.speclab.ivanhoe.shared.data.DocumentInfo;
import edu.virginia.speclab.ivanhoe.shared.message.DocumentDataMsg;
import edu.virginia.speclab.ivanhoe.shared.message.ImageDataMsg;
import edu.virginia.speclab.ivanhoe.shared.message.Message;
import edu.virginia.speclab.ivanhoe.shared.message.MessageType;

/**
 * @author lfoster
 *
 * This class is responsible for any document data message. It assembles
 * the document, makes entries into doc table, and notifes listener that
 * a doc is available
 */
public class DocumentReceiver implements IMessageHandler
{
   private String workDir;
   private List documentHandlers;
   private final int gameId;
   
   public DocumentReceiver(String workDir)
   {
       this(workDir, -1);
   }
   
   public DocumentReceiver(String workDir, int gameId)
   {
      this.workDir = workDir;
      this.documentHandlers = new ArrayList();
      this.gameId = gameId;
   }
   
   /**
    * Add a handler to be notified when a new document is ready
    * @param handler
    */
   public void addDocumentHandler(IDocumentHandler handler)
   {
      if (this.documentHandlers.contains(handler) == false)
      {
         SimpleLogger.logInfo("Registering document handler ["+handler.toString()+"]");
         this.documentHandlers.add(handler);
      }
   }
   
   /**
    * Remove a document handler
    * @param handler
    */
   public void removeDocumentHandler(IDocumentHandler handler)
   {
       if (this.documentHandlers.remove(handler))
       {
           SimpleLogger.logInfo("Removed document handler ["+handler.toString()+"]");
       }
   }

   /**
    * Listen for doc data messages
    */
   public void handleMessage(Message msg)
   {
      if (msg.getType().equals(MessageType.DOCUMENT_DATA))
      {
         handleDocumentData((DocumentDataMsg)msg);
      }
      else if (msg.getType().equals(MessageType.IMAGE_DATA))
      {
         handleImageData((ImageDataMsg)msg);
      }
   }

   /**
    * Receive data for a new image file from a player.
    * Copy the data into the discourse field and add entry into DF database
    * @param msg
    */
   private void handleImageData(ImageDataMsg msg)
   {
      DocumentInfo parentInfo = msg.getParentDocumentInfo();
      FileOutputStream writer = null;
      SimpleLogger.logInfo("Receiving data for image " + msg.getImageFileName());
      
      if (msg.getBufferSize() == 0)
      {
         SimpleLogger.logError("Data for " + msg.getImageFileName() + " is invalid!");
         return;
      }
      
      // make sure the directory structure exists
      File destFile = new File(this.workDir + File.separator + msg.getImageFileName());
      File parentFile = destFile.getParentFile();
      if (parentFile.exists() == false)
      {
         if (parentFile.mkdirs() == false)
         {
            SimpleLogger.logError("Unable to make directories for file " + destFile);
            return;
         }
      }
      
      try
      {
         // write the chunk of data to a local file
         writer = new FileOutputStream(destFile, true);
         writer.write(msg.getDataBufer(),0,msg.getBufferSize());
      }
      catch (IOException e)
      {
         SimpleLogger.logError("Unable to receive doc data for [" +
            msg.getImageFileName() + "]: " + e);
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
            catch (IOException e2 ) {}
         }
      }
      
      // once complete, add img to DF database
      if (msg.isComplete())
      {
         SimpleLogger.logInfo("Image " + msg.getImageFileName() + " is complete");
         
         try
         {                          
            DocumentMapper.addImage(parentInfo, msg.getImageFileName());
            fireDocumentChanged(parentInfo);
         }
         catch (MapperException e1)
         {
            SimpleLogger.logError("Cant add image " + e1);
         }
      }
   }

   /**
    * handle data for a document being added to a new game
    * @param msg
    */
   private void handleDocumentData(DocumentDataMsg msg)
   {
      // This code allows the client to upload two documents with the same
      // filename during game creation, creating document info entries that
      // refer to the same on-disk file.  This isn't really a bug, as this
      // doesn't currently break anything, but it's of note.
       
      DocumentInfo di = msg.getDocumentInfo();
      FileWriter writer = null;
      
      if (gameId >= 0)
      {
          // check for duplicate names
          if (DocumentMapper.exists(di.getTitle(), gameId))
          {
             if (msg.isComplete())
             {
                SimpleLogger.logInfo("Firing duplicate document title error");
                fireDocumentError(di, "A document with the title '"
                   + di.getTitle() 
                   + "' already exists. Please choose another name and try again.");
             }
             return;
          }
          
          if (DocumentMapper.fileExists(di.getFileName(), gameId))
          {
             if (msg.isComplete())
             {
                SimpleLogger.logInfo("Firing duplicate document filename error");
                fireDocumentError(di, "A document with the filename '"
                   + di.getFileName() 
                   + "' already exists. Please choose another name and try again.");
             }
             return;
          }
      }
      
      // chack for a valid buffer
      if (msg.getBufferSize() == 0)
      {
         SimpleLogger.logError("Data for " + di.getFileName() + " is invalid!");
         fireDocumentError(di, "The document file '"
            + di.getFileName()  + "' is invalid or corrupt.");
         return;
      }
      
      try
      {
         File workDirFile = new File(this.workDir);
         if (!workDirFile.exists())
         {
             SimpleLogger.logInfo("Game working directory ["+workDir
                     +"] not found.  Creating it.");
             workDirFile.mkdirs();
         }
          
         // write the chunk of data to a local file
         writer = new FileWriter(this.workDir + File.separator + di.getFileName(), true);
         String s = new String(msg.getDataBufer());
         writer.write(s, 0, msg.getBufferSize());
      }
      catch (IOException e)
      {
         SimpleLogger.logError("Unable to receive doc data for [" +
            di.getFileName() + "]: " + e);
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
            catch (IOException e2 ) {}
         }
      } 
      
      // add the doc to the DF list if its not already there
      if (msg.isComplete())
      {
         SimpleLogger.logInfo("Document [" + di.getFileName() + "] is complete");
         
         try
         {
            // add it to the document table 
            int docID = DocumentMapper.add(di);
            di = DocumentMapper.getDocumentInfo(docID);
            fireDocumentAvailable(di);
            SimpleLogger.logInfo("Added document " + di.getTitle());
         }
         catch (MapperException e)
         {
            SimpleLogger.logError("Failure adding new doc to DB: " + e.toString());
            fireDocumentError(di, "Database Error! Unable to add document '"
               + di.getTitle() + "'. Contact the game adminstrator or try again later.");
            return;
         }
      }
   }

   /**
    * notify all handlers that a document is ready
    * @param di
    */
   private void fireDocumentAvailable(DocumentInfo di)
   {
      List handlerCopy = new ArrayList(this.documentHandlers);
      for (Iterator itr = handlerCopy.iterator(); itr.hasNext();)
      {
         ((IDocumentHandler)itr.next()).documentReady(di);
      }
   }
   
   /**
    * notify all handlers that a document is effed
    * @param di
    */
   private void fireDocumentError(DocumentInfo di, String errorMsg)
   {
      List handlerCopy = new ArrayList(this.documentHandlers);
      for (Iterator itr = handlerCopy.iterator(); itr.hasNext();)
      {
         ((IDocumentHandler)itr.next()).documentError(di, errorMsg);
      }
   }
   
   /**
    * notify all handlers that a document has changed
    * @param changedDocInfo
    */
   private void fireDocumentChanged(DocumentInfo changedDocInfo)
   {
      List handlerCopy = new ArrayList(this.documentHandlers);
      for (Iterator itr = handlerCopy.iterator(); itr.hasNext();)
      {
         ((IDocumentHandler)itr.next()).documentChanged(changedDocInfo);
      }
   }
}
