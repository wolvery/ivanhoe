/*
 * Created on Jan 16, 2004
 */
package edu.virginia.speclab.ivanhoe.shared;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import org.w3c.tidy.Tidy;

/**
 * @author dgran
 *
 * Collection of helper methods when adding a document to the discourse field
 */
public class AddDocumentHelper
{
   /**
    * Check if the given file is unicode(utf-16) by examining
    * the first two bytes. Unicode should have 255, 254
    * @param srcFile
    * @return
    */
   private static boolean isUnicode(File srcFile)
   {
      boolean unicode = false;
      FileInputStream srcStream = null;
      try
      {
         srcStream = new FileInputStream(srcFile);
         int byte1 = srcStream.read();
         int byte2 = srcStream.read();
         if (byte1 == 255 && byte2 == 254)
         {
            unicode = true;
         }
      }
      catch (IOException e)
      {
         SimpleLogger.logError("Error testing file: " + 
            srcFile.toString() + " for unicode. " + e.toString());
      }
      finally
      {
         if (srcStream != null)
         {
            try
            {
               srcStream.close();
            }
            catch (IOException e1){}
         }
      }
      return unicode;
   }
   
   public static void txtToHtml(File srcFile, OutputStream out)
   {
      String line;
      BufferedReader br = null;
      BufferedWriter bw = null;
      try
      {
         if (isUnicode(srcFile))
         {
            br = new BufferedReader(new InputStreamReader(
               new FileInputStream(srcFile), "utf-16"));
         }
         else
         {
            br = new BufferedReader(new InputStreamReader(
               new FileInputStream(srcFile)));
         }
         bw = new BufferedWriter(new OutputStreamWriter(out));
         
         bw.write( "<html><body><p>" );
         bw.flush();
            
         while ((line = br.readLine()) != null)
         {
            line = line.replaceAll("\t", "&nbsp;&nbsp;&nbsp;&nbsp;");
            
            bw.write( (line) + "</p><p>" );
            bw.flush();
         }
         
         bw.write( "</p></body></html>" );
         bw.flush();
      }
      catch (IOException e)
      {
         SimpleLogger.logError("Error converting txt to html: " + e.toString());
      }
      finally
      {
         try
         {
           br.close();
           bw.close();
         }
         catch (Exception e) {   }
      }
   }
   
   /**
    * Converts an HTML file into proper XHTML, which is XML compliant.
    * @param in InputStream referring to the HTML source
    * @param out OutputStream for resulting XML
    * @throws ImportException
    */
   public static void htmlToXhtml(InputStream in, OutputStream out) 
      throws ImportException
   {
      Tidy jtidy = new Tidy();
      jtidy.setXHTML(true);
      jtidy.setXmlPi(false);
      jtidy.setTidyMark(false);
      jtidy.parseDOM(in, out) ;
      
      if ( jtidy.getParseErrors() > 0)
      {
         throw new ImportException("Invalid document structure");
      }
   }
   
   /**
    * Takes an XHTML document an removes the HTML HEAD content.
    * This is done to avoid altering the document length with non
    * printing characters.
    * 
    * @param in InputStream referring to the XHTML source
    * @param out OutputStream for resulting XHTML without header
    */
   public static void stripHeader(InputStream in, OutputStream out)
   {
      int bytesRead;
      byte[] buffer;
      String strBuf;
      boolean headOpen = false;
      boolean headerStripped = false;
      try
      {
         while (true)
         {
            buffer = new byte[16884];
            bytesRead = in.read(buffer);
            if (bytesRead == -1 )
            {
               break;
            }
            
            strBuf = new String(buffer);
            strBuf = strBuf.substring(0,bytesRead);
            if (headerStripped == false)
            {
               int headOpenPos, headClosePos;
               if (headOpen == false)
               {
                  headOpenPos = strBuf.indexOf("<head>");
                  if (headOpenPos > -1)
                  {
                     headOpen = true;
                     headClosePos = strBuf.indexOf("</head>");
                     if (headClosePos > -1)
                     {
                        headOpen = false;
                        String tmp;
                        tmp = strBuf.substring(0,headOpenPos);
                        tmp = tmp + strBuf.substring(headClosePos+8);
                        strBuf = tmp;
                        headerStripped = true;
                     }
                     else
                     {
                        strBuf = strBuf.substring(0,headOpenPos);
                     }
                  }  
               }
               else
               {
                  headClosePos = strBuf.indexOf("</head>");
                  if (headClosePos > -1)
                  {
                     headOpen = false;
                     strBuf = strBuf.substring(headClosePos+8);
                     headerStripped = true;
                  }
               }
            }
            // write it back out 
            if (strBuf.length() > 0)
               out.write(strBuf.getBytes(), 0, strBuf.length());
         }
      }
      catch (IOException e)
      {
         SimpleLogger.logError("Unable to strip header! " + e.toString());
      }
   }

   /**
    * Via an XSL transformation, the method adds id attributes to
    * all elements in the source stream.
    * 
    * @param in An InputStream referring to the source XML document
    * @param out An OutputStream for the resulting document with ids
    */
   public static void idTags(InputStream in, OutputStream out)
   {
      // 1. Instantiate a TransformerFactory.
      javax.xml.transform.TransformerFactory tFactory =
         javax.xml.transform.TransformerFactory.newInstance();

      try
      {
         // 2. Use the TransformerFactory to process the stylesheet Source and
         //    generate a Transformer.
         javax.xml.transform.Transformer transformer =
            tFactory.newTransformer(
               new javax.xml.transform.stream.StreamSource(
                  new FileInputStream("res/xsl/generate-ids.xsl")));

         // 3. Use the Transformer to transform an XML Source and send the
         //    output to a Result object.
         transformer.transform(
            new javax.xml.transform.stream.StreamSource(in),
            new javax.xml.transform.stream.StreamResult(out));
      }
      catch (Exception e)
      {
         SimpleLogger.logError("Error creatign tag IDs: " + e.toString());
      }
   }
   
   /**
    * Cleans up a few issues in HTML syntax that break the Java
    * HTMLEditorKit rendering.
    * 
    * @param in An InputStream for the source data
    * @param out
    */
   public static void cleanHtml(InputStream in, OutputStream out)
   {
      String input = "";
      String xmlPat = "<\\?xml.*\\?>";
      BufferedReader bin = new BufferedReader(new InputStreamReader(in));
      BufferedWriter bout = new BufferedWriter(new OutputStreamWriter(out));
      try
      {
         String result = "";
         while ((input = bin.readLine()) != null)
         {
            result = input.replaceAll(xmlPat, "");  
            result = result.replaceAll("/>", ">");
   
            bout.write(result + "\n");
            bout.flush();
         }
      }
      catch (IOException e)
      {
         SimpleLogger.logError("Error cleaning HTML: " + e.toString());
      }
      finally
      {
         try
         {
           bin.close();
           bout.close();
         }
         catch (Exception e) {   }
      }
   }
   
   public static class ImportException extends Exception
   {
      public ImportException(String cause)
      {
         super(cause);
      }
   }
}
