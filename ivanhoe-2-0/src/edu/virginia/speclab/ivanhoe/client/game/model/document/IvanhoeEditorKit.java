/*
 * Created on Oct 20, 2003
 *
 */
package edu.virginia.speclab.ivanhoe.client.game.model.document;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.Iterator;

import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.KeyStroke;
import java.awt.datatransfer.DataFlavor;
import javax.swing.text.*;
import javax.swing.text.html.*;

import edu.virginia.speclab.ivanhoe.client.game.model.discourse.DiscourseField;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.DocumentVersionManager;
import edu.virginia.speclab.ivanhoe.client.game.model.util.StatusEventMgr;
import edu.virginia.speclab.ivanhoe.client.util.JavaVersionTester;
import edu.virginia.speclab.ivanhoe.client.util.ResourceHelper;
import edu.virginia.speclab.ivanhoe.shared.GuidGenerator;
import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;
import edu.virginia.speclab.ivanhoe.shared.data.*;

/**
 * @author lfoster
 */
public class IvanhoeEditorKit extends HTMLEditorKit
{
   private final DiscourseField discourseField;
   
   private IvanhoeViewFactory viewFactory;
   private IvanhoeMouseListener mouseListener;
   private Keymap originalKeymap;
   
   public IvanhoeEditorKit(DiscourseField discourseField)
   {
       this.discourseField = discourseField;
   }
   
   /**
    * Create a new instance of an IvanhoeDocument and populate its stylesheet
    */
   public Document createDefaultDocument()
   {
      // get the default HTML stylesheet
      StyleSheet styles = getStyleSheet();
      StyleSheet ss = new StyleSheet();

      // add styles for HTML and Ivanhoe actions
      ss.addStyleSheet(styles);
      loadStyles(ss);

      // create a custom IvanhoeDocument using the IvanhoeStyles
      // and a reference to the tag listener
      DocumentVersionManager dvManager = 
              (discourseField != null ? discourseField.getDocumentVersionManager() : null);
      IvanhoeDocument doc = new IvanhoeDocument(dvManager, ss);
      doc.putProperty("IgnoreCharsetDirective", new Boolean(true));
      doc.setParser(getParser());
      doc.setAsynchronousLoadPriority(4);
      doc.setTokenThreshold(100);
      
      return doc;
   }
   
   /**
    * Create an Ivanhoe document based on the filename passed in
    * @param file Name of the file to read
    * @return The new document
    * @throws IOException
    */
   public IvanhoeDocument createDocumentFromFile(String file)
      throws IOException, BadLocationException
   {
      IvanhoeDocument doc = (IvanhoeDocument)createDefaultDocument();
      FileInputStream fileIn = new FileInputStream(file);      
      this.read(fileIn, doc, 0); 
      fileIn.close();

      return doc;
   }
   
   /**
    * Creates an Ivanhoe document based on html content of a String
    * @param docContent HTML content in a String
    * @return The new document
    */
   public IvanhoeDocument createDocumentFromString(String docContent)
      throws IOException, BadLocationException
   {
      IvanhoeDocument doc = (IvanhoeDocument)createDefaultDocument();
      StringReader sr = new StringReader(docContent);  
      this.read(sr, doc, 0);   
      sr.close();

      return doc;
   }
   
   /**
    * Get an array of all the actions supported by this editor kit
    * IvanhoeEditorKit removes the default handlers for delete and
    * backspace actions and handles them in its own way
    */
   public Action[] getActions()
   {
      Action[] defaultActs = super.getActions();
      Action[] acts = new Action[defaultActs.length-6];
      int cnt = 0;
      
      // add ivanhoe acts
      acts[cnt++] = new IvanhoePasteAction();
      acts[cnt++] = new IvanhoeCutAction(this.discourseField);
      acts[cnt++] = new IvanhoeCopyAction(this.discourseField);
      
      for (int i=0;i<defaultActs.length;i++)
      {
         String name = (String)defaultActs[i].getValue(Action.NAME);
         if ( name.equals("delete-next") || 
              name.equals("delete-previous") ||
              name.equals("default-typed") || 
              name.equals("insert-break") ||
              name.equals("insert-tab") ||
              name.equals("insert-content") || 
              name.equals("cut-to-clipboard") || 
              name.equals("copy-to-clipboard") || 
              name.equals("paste-from-clipboard") )
         {
            continue;
         }
         else
         {
            acts[cnt++] = defaultActs[i];
         }
      }
      
      return acts;
   }
   
   /**
    * Called when the kit is associated with an editor pane
    */
   public void install(JEditorPane pane)
   {
      super.install(pane);
      
      // install a new keymap that allows addions and deletions
      // to be trapped and tagged with ivanhoe action tags
      installNewKeymap(pane);
      
      // create/register a new mouse listener that reacts to 
      // moves and clicks on ivanhoe tags
      this.mouseListener = new IvanhoeMouseListener();
      pane.addMouseMotionListener(  this.mouseListener ); 
      pane.addMouseListener( this.mouseListener );
      
   }
   
   /**
    * called as the kit is removed from the pane
    */
   public void deinstall(JEditorPane pane)
   {
      // clean up all the stuff that was previously installed
      pane.removeMouseMotionListener( this.mouseListener );
      this.mouseListener = null;
      
      if (this.originalKeymap != null)
      {
         pane.setKeymap(this.originalKeymap);
      }
      
      super.deinstall(pane);
   }

   /**
    * Create an ivanhoe action and register it with a new keymap
    * so it will be used for all keypresses. Associate this keymap
    * with the passed in editor pane. This allows ivanhoe to track
    * keypresses and generate the appropriate tags.
    * @param pane
    */
   private void installNewKeymap(JEditorPane pane)
   {
      // create a new KeyMap for Ivanhoe that has no bindings or ties
      // to other keymaps in the heirarchy. This is a blank keymap.
      this.originalKeymap = pane.getKeymap();
      Keymap newMap = JEditorPane.addKeymap("IvanhoeKeymap", null);
      
      // Create a new keystroke action and set it as the 
      // default handler for all keys. This is used to catch keystrokes
      // and create ivanhoe actions.
      IvanhoeKeyAction keyAction = new IvanhoeKeyAction();
      newMap.setDefaultAction( keyAction );
      
      // apparently the backspace key is in the default action table for 1.5 but not 1.4
      if( JavaVersionTester.isVersionJavaFiveOrHigher() == false )
      {
          // Set the keyAction to handle backspace key
          KeyStroke backspaceKs = KeyStroke.getKeyStroke("BACK_SPACE");
          newMap.addActionForKeyStroke(backspaceKs, keyAction);
      }
          
      // set the new keymap for the editor pane
      pane.setKeymap(newMap);
   }

   /**
    * Load styles from ivanhoe.css and add them to the stylsheet class 
    * If the file is unavailable, add some defaults
    * @param ss The styleSheet 
    */
   private void loadStyles(StyleSheet ss)
   {
      InputStream fileIn = null;
      InputStreamReader in = null;
      try
      {
         fileIn = ResourceHelper.getInputStream("res/ivanhoe.css");
         in = new InputStreamReader(fileIn);
         ss.loadRules(in, null);
      }
      catch (Exception e)
      {
         StatusEventMgr.fireErrorMsg("Missing Ivanhoe stylesheet! Documents will not be styled correctly.");    
      }
      finally
      {
         try
         {
            if (in != null)
               in.close();
            if (fileIn != null)
               fileIn.close();
         }
         catch (IOException e1)
         {
         }
      }
   }
   
   /**
    * Get the factory used to create views for text elements
    */
   public ViewFactory getViewFactory()
   {         
      if ( viewFactory == null )
      {
         viewFactory = new IvanhoeViewFactory();
      } 
      
      return viewFactory;
   }
   
   /**
    * IvanhoeViewFactory
    * @author lfoster
    *
    * Custom view factory for ivanhoe.. used to control tag visibility
    */
   private class IvanhoeViewFactory extends HTMLEditorKit.HTMLFactory
   {
      public View create(Element elem) 
      {
         View elementView = null;
         
         Object o = elem.getAttributes().getAttribute(StyleConstants.NameAttribute);
         if (o instanceof HTML.Tag) 
         {
            HTML.Tag kind = (HTML.Tag) o;
            if (kind.equals(HTML.Tag.CONTENT))
            {
               elementView = new IvanhoeInlineView(elem);
            }
            else if (kind.equals(HTML.Tag.IMG))
            {
                elementView = new IvanhoeImageView(elem);
            }
         }
         
         // if a view has not been created, use the default
         if (elementView == null)
         {
            elementView = super.create(elem);
         }
         
         return elementView;
      }
   }
   
   public static Action getPasteAction()
   {
      return new IvanhoePasteAction();
   }
   
   public static Action getCopyAction( DiscourseField discourseField )
   {
      return new IvanhoeCopyAction(discourseField);
   }
   
   public static Action getCutAction( DiscourseField discourseField )
   {
      return new IvanhoeCutAction(discourseField);
   }
   
   /**
    * Action to handle Paste in Ivanhoe.
    * Extract the text from the clipboard, and strip it of
    * unnecessary stuff. Add it to IvanhoeDoc as a new add action.
    */
   public static class IvanhoePasteAction extends HTMLEditorKit.HTMLTextAction
   {
      public IvanhoePasteAction() 
      {
         super(pasteAction);
         putValue(Action.SHORT_DESCRIPTION,"Paste Text from Clipboard");
         putValue(Action.SMALL_ICON, ResourceHelper.instance.getIcon("res/icons/smallpaste.gif"));
      }

      public void actionPerformed(ActionEvent e) 
      {
         JEditorPane target = (JEditorPane)getTextComponent(e);
         if (target != null) 
         {
            IvanhoeDocument doc = (IvanhoeDocument)target.getDocument();
            if (doc.isReadOnly() == false)
            {
               // check for required deletion of current sel
               int start = target.getSelectionStart();
               int end = target.getSelectionEnd();
               if (end > start)
               {
                  doc.deleteText(start, end);  
               }
               
               // grab clipboard and its transferrable data
               Clipboard clip = target.getToolkit().getSystemClipboard();
               Transferable data = clip.getContents(this);
              
               // Check if the data is IvanhoeData or Text data
               DataFlavor ivanFlavor = IvanhoeTransferable.ivanhoeFlavor;
               if (data.isDataFlavorSupported( ivanFlavor ))
               {
                  pasteIvanhoeData(doc, start, data);
               }
               else if (data.isDataFlavorSupported( DataFlavor.stringFlavor))
               {
                  pasteTextData(doc, start, data);
               }
               else
               {
                  SimpleLogger.logError("Unsupported clipboard contents");
                  StatusEventMgr.fireErrorMsg("The type of data you are trying to paste is not supported");
               }
            }
            else
            {
               Toolkit.getDefaultToolkit().beep();
            }
         }
      }

      private void pasteTextData(
         IvanhoeDocument doc, int start, Transferable data)
      {
         try
         {
            DataFlavor textFlavor = DataFlavor.stringFlavor;
            Reader rdr = textFlavor.getReaderForText(data);
            BufferedReader br = new BufferedReader(rdr);
            String line = "";
            StringBuffer content = new StringBuffer();
            while (true)
            {
               line = br.readLine();
               if (line == null)
               {
                  break;
               }
               else
               {
                  if (content.length() > 0)
                     content.append("\n");
                  content.append( line );
               }
            }
             
            doc.addNewText(start, content.toString());
         }
         catch (Exception e1)
         {
            SimpleLogger.logError("Unable to paste text data: " + e1);
            StatusEventMgr.fireErrorMsg("Unable to paste data at this location");
         }
      }

      private void pasteIvanhoeData(
         IvanhoeDocument doc, int start, Transferable data)
      {
         try
         {
            IvanhoeClipboard content = 
               (IvanhoeClipboard)data.getTransferData(
               IvanhoeTransferable.ivanhoeFlavor);
            
            // now add the text
            SimpleLogger.logInfo("Pasting content adopted from player " + 
                    content.getSourcePlayer());
            doc.addNewText(start, content.getCopiedText());
            
            // add copied links
            Iterator acts = content.getCopiedActions();
            IvanhoeAction act;
            while (acts.hasNext())
            {
               act = (IvanhoeAction)acts.next();
               if (act.getType().equals(ActionType.LINK_ACTION) )
               {
                  Link srcTgt = (Link)act.getContent();
                  String guid = GuidGenerator.generateID();
                  Link tgt = new Link(guid, srcTgt.getType(),
                     srcTgt.getLinkTag(), srcTgt.getLabel());
                  doc.createLink(
                     start+act.getOffset(), start+act.getOffset()+act.getLength(), tgt );  
                }
            }
         }
         catch (Exception e)
         {
            SimpleLogger.logError("Unable to paste ivanhoe data: " + e);
            StatusEventMgr.fireErrorMsg("Unable to paste data at this location");
         }
      }
   }
   
   /**
    * Action to handle Cuts in Ivanhoe.
    * It copies the selected text to the clipboard, then marks 
    * the range as a new deletion.
    */
   public static class IvanhoeCutAction extends HTMLEditorKit.HTMLTextAction
      implements ClipboardOwner
   {
      private final DiscourseField discourseField; 

      public IvanhoeCutAction(DiscourseField discourseField) 
      {
         super(cutAction);
           this.discourseField = discourseField;
		   putValue(Action.SHORT_DESCRIPTION,"Cut Text to Clipboard");
		   putValue(Action.SMALL_ICON, ResourceHelper.instance.getIcon("res/icons/smallcut.gif"));
      }

      public void actionPerformed(ActionEvent e) 
      {
         JEditorPane target = (JEditorPane)getTextComponent(e);
         if (target != null) 
         {
            // dont cut and remove text; just copy it
            IvanhoeDocument ivanDoc = (IvanhoeDocument)target.getDocument();
            IvanhoeClipboard content = ivanDoc.copyContent( discourseField,
               target.getSelectionStart(), target.getSelectionEnd());
           
            // stick it on clipboard
            Clipboard cb = target.getToolkit().getSystemClipboard();
            cb.setContents( new IvanhoeTransferable(content), this );
               
            // .. and mark it as deleted if editable
            if (ivanDoc.isReadOnly() == false)
            {
               ivanDoc.deleteText( target.getSelectionStart(),
                  target.getSelectionEnd());
            }
            else
            {
               Toolkit.getDefaultToolkit().beep();
            }   
         }
      }

      public void lostOwnership(Clipboard clipboard, Transferable contents)
      {  
      }
   }
   
   /**
    * Action to handle Copy in Ivanhoe.
    * It copies the selected text to the clipboard, skipping over
    * text that is deleted. Actions are also copied
    */
   public static class IvanhoeCopyAction extends HTMLEditorKit.HTMLTextAction
      implements ClipboardOwner
   {
      private final DiscourseField discourseField;
      
      public IvanhoeCopyAction( DiscourseField discourseField ) 
      {
         super(copyAction);
         this.discourseField = discourseField;
         putValue(Action.SHORT_DESCRIPTION,"Copy Text to Clipboard");
         putValue(Action.SMALL_ICON, ResourceHelper.instance.getIcon("res/icons/smallcopy.gif"));
      }

      public void actionPerformed(ActionEvent e) 
      {
         JEditorPane target = (JEditorPane)getTextComponent(e);
         if (target != null) 
         {
            if (target.isEditable())
            {
               // copy non-deleted content and actions from document
               IvanhoeDocument ivanDoc = (IvanhoeDocument)target.getDocument();
               IvanhoeClipboard content = ivanDoc.copyContent( discourseField,
                  target.getSelectionStart(), target.getSelectionEnd());
              
               // stick it on clipboard
               Clipboard cb = target.getToolkit().getSystemClipboard();
               cb.setContents( new IvanhoeTransferable(content), this );
            } 
         }
      }

      public void lostOwnership(Clipboard clipboard, Transferable contents)
      { 
      }
   }
}