/*
 * Created on Jul 2, 2004
 *
 * DocumentImporter
 */
package edu.virginia.speclab.ivanhoe.client.game.model.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.text.Element;
import javax.swing.text.ElementIterator;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import edu.virginia.speclab.ivanhoe.client.Ivanhoe;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.DiscourseField;
import edu.virginia.speclab.ivanhoe.client.game.model.document.IvanhoeDocument;
import edu.virginia.speclab.ivanhoe.client.game.model.document.IvanhoeEditorKit;
import edu.virginia.speclab.ivanhoe.client.game.model.util.StatusEventMgr;
import edu.virginia.speclab.ivanhoe.shared.AddDocumentHelper;
import edu.virginia.speclab.ivanhoe.shared.IMessageHandler;
import edu.virginia.speclab.ivanhoe.shared.IvanhoeException;
import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;
import edu.virginia.speclab.ivanhoe.shared.AddDocumentHelper.ImportException;
import edu.virginia.speclab.ivanhoe.shared.data.DocumentInfo;
import edu.virginia.speclab.ivanhoe.shared.message.DocumentCompleteMsg;
import edu.virginia.speclab.ivanhoe.shared.message.DocumentDataMsg;
import edu.virginia.speclab.ivanhoe.shared.message.DocumentErrorMsg;
import edu.virginia.speclab.ivanhoe.shared.message.ImageDataMsg;
import edu.virginia.speclab.ivanhoe.shared.message.Message;
import edu.virginia.speclab.ivanhoe.shared.message.MessageType;

/**
 * @author lfoster
 *
 * This class is responsible for importing documents into an IvanhoeGame.
 * It Accepts files, makes their format ivanhoe-friendly and stores the result in
 * a local working directory. These files are then streamed to the server
 */
public class DocumentImporter implements IMessageHandler
{
    private final DiscourseField discourseField;
    
   private String workingDir;
   private LinkedList imgsImporting;
   
   /**
    * Construct an doc importer that uses the specified directory
    * as the working directory
    * @param workingDir
    */
   public DocumentImporter(String workingDir, DiscourseField discourseField)
   {
       this.discourseField = discourseField;
      this.workingDir = workingDir;
      this.imgsImporting = new LinkedList();
      SimpleLogger.logInfo("DocumentImporter created with working dir [" + workingDir + "]");
   }
   
   
   /**
    * Import a document into the cache
    * @param docInfo
    * @param path
    * @return the File referencing the newly imported & cleaned file
    */
   public File importDocument(DocumentInfo docInfo, File newFile) throws IvanhoeException
   {
      // import the file into the work directory
      // perform all necessary transformations to make the new document
      // format compatible with ivanhoe documents
      File importedDoc = null;
      File importedImage = null;

      try
      {
          	// TODO Make unique filename here
         String fileName = docInfo.getFileName();
         
         SimpleLogger.logInfo("Importing file ["+fileName+"]");
         
         // These files never get deleted, but maybe that's a good thing
         importedDoc = new File(this.workingDir + File.separator + fileName);
         
         // We want to do case insensitive comparisons
         String normalizedNewFileName = newFile.getName().toLowerCase();
         
         if (normalizedNewFileName.endsWith("html"))
         {
            SimpleLogger.logInfo("Importing as HTML");
            importHtmlFile(docInfo, newFile, importedDoc, false);
         }
         else if (normalizedNewFileName.endsWith("txt"))
         {
             SimpleLogger.logInfo("Importing as TXT");
            importTxtFile(docInfo, newFile, importedDoc);
         }
         else if (normalizedNewFileName.endsWith("gif") ||
                 normalizedNewFileName.endsWith("jpg") ||
                 normalizedNewFileName.endsWith("jpeg"))
         {
             SimpleLogger.logInfo("Importing as image");
            // copy image to work dirs and create html wrappe
            String imgFileName = newFile.getName();
            importedImage = new File(this.workingDir + File.separator + imgFileName);
            importImgFile(newFile, docInfo, importedImage, importedDoc);
         }
         else
         {
            SimpleLogger.logError("Unsupported filetype: " + newFile.toString());
            throw new IvanhoeException("Unsupported type for file " + newFile.toString());
         }
      }
      catch (Exception importFail)
      {
         SimpleLogger.logError(
               "Unable to import file " + importFail.getMessage());
         throw new IvanhoeException("Unable to import file: " + importFail.toString());
      }
      finally
      {
          importedDoc.deleteOnExit();
      }
      
      
      
      return importedDoc;
   }
   
   /**
    * Helper method to import plain text files.
    * A simple html wrapper is added to the file
    * @param srcPath
    * @param destPath
    * @throws FileNotFoundException
    * @throws IOException
 * @throws IvanhoeException 
    */
   private void importTxtFile(DocumentInfo docInfo, File srcFile, File destFile)
      throws FileNotFoundException, IOException, AddDocumentHelper.ImportException, IvanhoeException
   {
      // create streams used to add html wrapper around txt file
      File tempHtml = File.createTempFile("tmp", "html");
      FileOutputStream htmlDestStream = new FileOutputStream(tempHtml);

      // wrap the text in html
      AddDocumentHelper.txtToHtml(srcFile, htmlDestStream);

      // standardize the html
      importHtmlFile(docInfo, tempHtml, destFile, false);

      // clean up
      tempHtml.delete();
   }
   
   /**
    * Helper method to import html files.
    * The file is tidied and any images are copied
    * @param srcPath
    * @param destPath
    * @throws IOException
    * @throws FileNotFoundException
 * @throws IvanhoeException 
    */
   private boolean importHtmlFile(DocumentInfo docInfo, File srcFile, File destFile, boolean normalize )
      throws IOException, FileNotFoundException, AddDocumentHelper.ImportException, IvanhoeException
   {
       if( normalize )
       {
          // attempt to normalize file on import 
           normalizeExternalHTML( srcFile, destFile );
       }
       else
       {
           // just take the file as is
           copyFile(srcFile, destFile);
       }
          
       // set the length of the newly imported doc
       docInfo.setDocumentLength(calculateDocumentLength(destFile));
      
       // send the data to server
       sendDocumentToServer(docInfo, destFile);
      
       // import the associated image files
       if( !importHTMLFileImages(docInfo,srcFile,destFile) )
       {
           return false;
       }
       
       return true;
   }
   
   private void normalizeExternalHTML( File srcFile, File destFile ) throws IOException, ImportException
   {
       File tempXhtml  = File.createTempFile("tmp_xhtml", "xml");
       File tempNoHdr  = File.createTempFile("tmp_no_hdr", "xml");

       // convert doc from html to xhtml, placing result in temp directory
       FileInputStream source = new FileInputStream(srcFile);
       FileOutputStream dest = new FileOutputStream(tempXhtml);
       AddDocumentHelper.htmlToXhtml(source, dest);
     
       // remove the head tag
       AddDocumentHelper.stripHeader(
          new FileInputStream(tempXhtml),
          new FileOutputStream(tempNoHdr));

       // clean the html, placing result in work directory
       dest = new FileOutputStream(destFile);
       AddDocumentHelper.cleanHtml(new FileInputStream(tempNoHdr), dest);

       // clean tmp files
       tempXhtml.delete();
       tempNoHdr.delete();
   }
   
   private boolean importHTMLFileImages( DocumentInfo docInfo, File srcFile, File destFile )
   {
       // scan document for images and copy them to cache
       HTMLEditorKit kit = new HTMLEditorKit();
       HTMLDocument doc = (HTMLDocument) kit.createDefaultDocument();
      
      try
      {
         kit.read( new FileInputStream(destFile), doc, 0);
      }
      catch (Exception e)
      {
         SimpleLogger.logError("Unable to parse file for images tags! " + e.toString());
         return false;
      }
         
      ElementIterator eleItr = new ElementIterator(doc.getDefaultRootElement());
      Element ele;
      boolean containsErrors = false;
      while (true)
      {
         ele = eleItr.next();
         if (ele != null)
         {
            Object nameAttr = ele.getAttributes().getAttribute(
               StyleConstants.NameAttribute);
            if (nameAttr != null && nameAttr.toString().equalsIgnoreCase("img"))
            {
               // build absolute path to remote image
               String imgFile = (String)ele.getAttributes().getAttribute(HTML.Attribute.SRC);
               
               // check for http, file or relative image
               if (imgFile.regionMatches(true,0,"http:",0,5))
               {
                  // skip http referenced images
                  SimpleLogger.logInfo("This file contains HTTP image reference " +
                     imgFile + "... no attempt will be made to import this image");
               }
               else if (imgFile.regionMatches(true,0,"file:",0,5))
               {
                  // absolute path image, skip
                  SimpleLogger.logError("This document contains an invalid absoulte path specification"
                     + " to image " + imgFile + ". It will be skipped, and image broken");
                  containsErrors = true;
               }
               else
               {
                  // this is an image specified with a relative path
                  String base = srcFile.getParent();
                  File remoteFile = new File(base + File.separator + imgFile);
                  
                  // create cache destination file, and copy image to cache
                  File cacheFile = new File(this.workingDir + File.separator + imgFile);
                  try
                  {
                     copyFile(remoteFile, cacheFile);
                  }
                  catch (IvanhoeException e1)
                  {
                     containsErrors = true;
                     SimpleLogger.logError("Unable to copy referenced image " 
                        + imgFile + " to cache, image will be broken");
                  }
                  
                  // send image data to server
                  sendImageToServer(docInfo, cacheFile);
               }
            }
         }
         else
         {
            break;
         }
      }
      
      // notify user of broken images
      if (containsErrors)
      {
         StatusEventMgr.fireWarningMsg("<html>Document '" + 
            docInfo.getTitle() + "' was added, but broken image links were detected "
            + "during the import process.");
      }
      
      return true;
   }
   
   /**
    * Calc the length of an html File
    * @param destFile
    * @return
    */
   private int calculateDocumentLength(File htmlFile)
   {
      int length = 0; 
      try
      {
          IvanhoeEditorKit kit = new IvanhoeEditorKit(discourseField);
          IvanhoeDocument doc =
             kit.createDocumentFromFile(htmlFile.getAbsolutePath());
          length = doc.getLength();
      }
      catch( Exception e )
      {
          SimpleLogger.logError("Unable to determine document length: "+ e);
      }
      
      return length;
   }

   /**
    * Import an image into the cache
    * @param srcPath Source of the image
    * @param imgInfo Document Info for the image
    * @param destImg Local destination file for the image
    * @param htmlWrapperFile Local destination file for html image wrapper
    * @throws FileNotFoundException
    * @throws IOException
    * @throws AddDocumentHelper.ImportException
    */
   private void importImgFile(File srcImage, DocumentInfo imgInfo, 
      File destImg, File htmlWrapperFile)
      throws FileNotFoundException, IOException, IvanhoeException
   {
      FileOutputStream htmlWrapperStream = new FileOutputStream(htmlWrapperFile);
      copyFile(srcImage, destImg);
      
      // generate an html wrapper
      StringBuffer content = new StringBuffer();
      content.append("<html><body>&nbsp;<table>");
      content.append("<tr><th><center>").append(imgInfo.getTitle());
      content.append("</center></th></tr>");
      content.append("<tr><td><center>");
      content.append("<img src=\"").append(destImg.getName());
      content.append("\" alt=\"").append(imgInfo.getTitle());
      content.append("\"></center></td></tr></table>&nbsp;</body></html>");
      htmlWrapperStream.write(content.toString().getBytes());
    
      // set the length of the wrapper
      imgInfo.setDocumentLength(calculateDocumentLength(htmlWrapperFile));
     
      Ivanhoe.getProxy().registerMsgHandler(MessageType.DOCUMENT_COMPLETE, this);
      Ivanhoe.getProxy().registerMsgHandler(MessageType.DOCUMENT_ERROR, this);
      
      // Register the info we're waiting to complete
      DocumentInfo imgFileInfo = new DocumentInfo(
              imgInfo.getId().intValue(),
              destImg.getName(),
              imgInfo.getTitle(),
              imgInfo.getAuthor(),
              imgInfo.getSource(),
              imgInfo.getContributor(),
              imgInfo.getContributorID(),
              imgInfo.getCreateTime());
      imgsImporting.add(imgFileInfo);
      
      // send document to server
      sendDocumentToServer(imgInfo, htmlWrapperFile);
   }
   /**
    * Public helper method to copy 
    * <code>srcFile</code> to <code>destFile</code>
    * @param srcFile
    * @param destFile
    * @return
    */
   public static void copyFile(File srcFile, File destFile) throws IvanhoeException
   {
      File testFile = destFile.getParentFile();
      if (testFile.exists() == false)
      {
         if (testFile.mkdirs() == false)
         {
            SimpleLogger.logError("Unable to copy file to " + destFile);
            throw new IvanhoeException("Unable to create destination file " +
                  destFile.toString() + " for copy");
         }
      };
       
      FileInputStream srcStream = null;
      FileOutputStream destStream = null;
      try
      {
         srcStream = new FileInputStream(srcFile);
         destStream = new FileOutputStream(destFile);
      }
      catch (FileNotFoundException e1)
      {
         SimpleLogger.logError("Unable to copy file to destination", e1);
         throw new IvanhoeException("Unable to open file for copy: " + e1.toString());
      }
      
      byte[] buf = new byte[65535];
      int bytesRead;
      
      while (true)
      {
         try
         {
            bytesRead = srcStream.read(buf);
            if (bytesRead == -1)
            {
               break;
            }
            else
            {
               destStream.write(buf,0,bytesRead);
            }
         }
         catch (IOException e)
         {
            SimpleLogger.logError("Error copying file to cache " + e.toString());
            throw new IvanhoeException("Error copying file: " + e.toString());
         }
      }
   }

   /**
    * Transmit a local cachefile to server
    * @param docInfo DocumentInfo for the document
    * @param htmlFile Cache file that will be transmitted
    */
   public void sendDocumentToServer(DocumentInfo docInfo, File htmlFile)
   {    
      FileInputStream source = null;
      try
      {
         // open the file requested and read data in MAX_DATA_SIZE
         // chunks. Send each chunk back to the server.
         // When the last bit of data is read, set the complete flag
         source = new FileInputStream(htmlFile);
         DocumentDataMsg dataMsg;
         boolean done = false;
         int bytesRead;
         while (!done)
         {
            dataMsg = new DocumentDataMsg(docInfo);
            bytesRead = source.read(dataMsg.getDataBufer());
            if (bytesRead == -1 || bytesRead < DocumentDataMsg.MAX_DATA_SIZE)
            {
               dataMsg.setComplete();
               done = true;
            }

            dataMsg.setBufferSize(bytesRead);

            // send the info to the server
            Ivanhoe.getProxy().sendMessage(dataMsg);
         }
      }
      catch (IOException e)
      {
         SimpleLogger.logError(
            "Error sending doc to server: " + e);
      }
      finally
      {
         if (source != null)
         {
            try
            {
               source.close();
            }
            catch (IOException e1) {}
         }
      }
   }
   
   /**
    * Transmit a local image to server
    * @param docInfo DocumentInfo for the new document file
    */
   public void sendImageToServer(DocumentInfo parentDoc, File imageFile)
   {    
      FileInputStream source = null;
      
      // strip off work dir 
      String img = imageFile.getAbsolutePath();
      SimpleLogger.logInfo("Full image path: " + img);
      SimpleLogger.logInfo("Work Dir: " + this.workingDir);
      int pos = img.lastIndexOf(this.workingDir);
      if (pos > -1)
      {
         img = img.substring(pos + this.workingDir.length());
      }
      SimpleLogger.logInfo("Stripped image path: " + img);
      
      try
      {
         source = new FileInputStream(imageFile);
         ImageDataMsg dataMsg;
         boolean done = false;
         int bytesRead;
         while (!done)
         {
            dataMsg = new ImageDataMsg(img,parentDoc);
            bytesRead = source.read(dataMsg.getDataBufer());
            if (bytesRead == -1 || bytesRead < ImageDataMsg.MAX_DATA_SIZE)
            {
               dataMsg.setComplete();
               done = true;
            }

            dataMsg.setBufferSize(bytesRead);

            // send the info to the server
            Ivanhoe.getProxy().sendMessage(dataMsg);
         }
      }
      catch (IOException e)
      {
         SimpleLogger.logError(
            "Error sending doc to server: " + e);
      }
      finally
      {
         if (source != null)
         {
            try
            {
               source.close();
            }
            catch (IOException e1) {}
         }
      }
   }


    public void handleMessage(Message msg)
    {
        if (msg.getType().equals(MessageType.DOCUMENT_ERROR))
        {
            handleDocumentError((DocumentErrorMsg)msg);
        }
        else if (msg.getType().equals(MessageType.DOCUMENT_COMPLETE))
        {
            handleDocumentComplete((DocumentCompleteMsg)msg);
        }
        else
        {
            return;
        }
        
        Ivanhoe.getProxy().unregisterMsgHandler(MessageType.DOCUMENT_COMPLETE, this);
        Ivanhoe.getProxy().unregisterMsgHandler(MessageType.DOCUMENT_ERROR, this);
    }

    private void handleDocumentComplete(DocumentCompleteMsg msg)
    {
        DocumentInfo docInfo = msg.getDocumentInfo();
        
        for (Iterator i=imgsImporting.iterator(); i.hasNext(); )
        {
            DocumentInfo imgInfo = (DocumentInfo)i.next();
            if (msg.getDocumentInfo().getTitle().equals(imgInfo.getTitle()))
            {
                i.remove();
                
                imgInfo = new DocumentInfo(
                        docInfo.getId().intValue(),
                        imgInfo.getFileName(),
                        docInfo.getTitle(),
                        docInfo.getAuthor(),
                        docInfo.getSource(),
                        docInfo.getContributor(),
                        docInfo.getContributorID(),
                        docInfo.getCreateTime());
                File destImg = new File(this.workingDir + File.separator + imgInfo.getFileName());
                if (destImg.exists())
                {
                    sendImageToServer(msg.getDocumentInfo(), destImg);
                    break;
                }
            }
        }
    }
    
    private void handleDocumentError(DocumentErrorMsg msg)
    {
        for (Iterator i=imgsImporting.iterator(); i.hasNext(); )
        {
            DocumentInfo imgInfo = (DocumentInfo)i.next();
            if (msg.getDocumentInfo().getTitle().equals(imgInfo.getTitle()))
            {
                File destImg = new File(this.workingDir + File.separator + imgInfo.getFileName());
                if (destImg.exists())
                {
                    destImg.delete();
                    break;
                }
            }
        }
        
    }
}
