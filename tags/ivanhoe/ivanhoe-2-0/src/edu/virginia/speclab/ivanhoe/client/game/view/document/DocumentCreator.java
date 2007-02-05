/*
 * Created on Jun 10, 2004
 *
 * DocumentCreator
 */
package edu.virginia.speclab.ivanhoe.client.game.view.document;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.Date;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;

import edu.virginia.speclab.ivanhoe.client.Ivanhoe;
import edu.virginia.speclab.ivanhoe.client.IvanhoeUIConstants;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.DiscourseField;
import edu.virginia.speclab.ivanhoe.client.game.view.IvanhoeFrame;
import edu.virginia.speclab.ivanhoe.client.game.view.ui.IvanhoeStyleInternalFrame;
import edu.virginia.speclab.ivanhoe.client.game.view.ui.IvanhoeToolBar;
import edu.virginia.speclab.ivanhoe.client.game.view.ui.TextLimit;
import edu.virginia.speclab.ivanhoe.client.util.ResourceHelper;
import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;
import edu.virginia.speclab.ivanhoe.shared.data.DocumentInfo;


/**
 * @author lfoster
 *
 * A simple HTML editor that is used to generate new documents
 */
public class DocumentCreator extends IvanhoeStyleInternalFrame
{    
   private DiscourseField discourseField;
   private JEditorPane editorPane;
   private HeaderPanel headerPanel; 
   private IvanhoeFrame parentFrame;
   
   private static Icon authoringIcon = ResourceHelper.instance.getIcon("res/icons/authoring.jpg");
   
   /**
    * Construct a new editor with a blank document
    */
   public DocumentCreator( DiscourseField discourseField, IvanhoeFrame ivanhoeFrame )
   {
      super("New Document");
      this.parentFrame = ivanhoeFrame;
      this.discourseField = discourseField;
      getContentPane().setLayout(new BorderLayout() );
      
      // create editor
      this.editorPane = new JEditorPane();
      this.editorPane.setEnabled(true);
      this.editorPane.setContentType("text/html");
      this.editorPane.setMargin( new Insets(10,10,10,10));
      
      // make it scrollable
      JScrollPane sp = new JScrollPane(editorPane);
      sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
      sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      this.getContentPane().add(sp, BorderLayout.CENTER);

      headerPanel = new HeaderPanel();
      this.getContentPane().add(headerPanel, BorderLayout.NORTH);
      
      // listen for editor close events
      this.setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE);
      addInternalFrameListener( new InternalFrameAdapter()
         {
            public void internalFrameClosing(InternalFrameEvent e)
            {
               handleClose();
            }
      });
   
      setSize(500, 650);
      setVisible(true);
   }
   
   /**
    * Notification that document creator is closing
    */
   protected void handleClose()
   {
      Object[] options = {"Add","Discard", "Cancel"};
      int resp = JOptionPane.showOptionDialog(parentFrame,
         "Would you like to add this document to the discourse field or discard it?",
      "Add Document",
      JOptionPane.YES_NO_CANCEL_OPTION,
      JOptionPane.QUESTION_MESSAGE,
      null,
      options,
      options[0]);
      
      if ( resp == 0)
      {
         if (headerPanel.getTitle().length() > 0)
         {
             if( addDocumentToDiscourseField() == true ) dispose();
         }
         else
         {
             Ivanhoe.showErrorMessage("You must enter a document title");
         }
      }
      else if (resp == 1)
      {
         dispose();
      }
   }
   
   protected boolean addDocumentToDiscourseField()
   {
      boolean result = false;
      String docTitle = headerPanel.getTitle();
      String fileName = DocumentInfo.createFileNameFromTitle(docTitle);
      String author = headerPanel.getAuthor();
      String source = headerPanel.getSource();       
      Date createDate = Ivanhoe.getDate();       
      int roleID = discourseField.getCurrentMove().getCurrentRole().getId();
      String roleName = discourseField.getCurrentMove().getCurrentRole().getName();

      DocumentInfo info = new DocumentInfo(fileName,docTitle,author,source,
              							   roleName,roleID,createDate);
      
      // write to local file
      FileWriter writer = null;
      File newFile = new File(info.getFileName());
      try
      {
         
         writer = new FileWriter(newFile, false);
         DocumentCreator.this.editorPane.getEditorKit().write(
            writer,DocumentCreator.this.editorPane.getDocument(),
            0,DocumentCreator.this.editorPane.getDocument().getLength());
      }
      catch (Exception e)
      {
          SimpleLogger.logError("Unable to add new document", e);
          Ivanhoe.showErrorMessage("<html><b>Unable to add new document</b><br>"
                  + "Reason: " + e);
          result = false;
      }
      finally
      {
         try
         {
            writer.close();
         }
         catch (IOException e1){ result = false; }
      }
      
      result = discourseField.addNewDocument(info, newFile);
      
      // document was successfully added, close the editor
      if( result == true )
      {
          newFile.delete();
          DocumentCreator.this.dispose();
      }
      
      return result;
   }
   
   /**
    * Action implemnation for Cancel Creation
    */
   private class CancelAction extends javax.swing.AbstractAction
   {
      public CancelAction()
      {
         super("Cancel",
            ResourceHelper.instance.getIcon("res/icons/smalltrash.gif"));
         putValue(Action.SHORT_DESCRIPTION,"Cancel creation and discard this document");
      }

      public void actionPerformed(ActionEvent arg0)
      {
         int resp = JOptionPane.showConfirmDialog(parentFrame, 
            "Cancel creation of this document and discard all data?", 
            "Confirm Cancel",
            JOptionPane.OK_CANCEL_OPTION);
         if (resp == JOptionPane.OK_OPTION)
         {
            DocumentCreator.this.dispose();
         }
      }
   }
   
   /**
    * Action to handle Paste in New Documents.
    * Extract the text from the clipboard, and strip it of
    * unnecessary stuff. Add it to IvanhoeDoc as a new add action.
    */
   public static class NewDocumentPasteAction extends HTMLEditorKit.HTMLTextAction
   {
      public NewDocumentPasteAction() 
      {
         super(HTMLEditorKit.pasteAction);
         putValue(Action.SHORT_DESCRIPTION,"Paste Text from Clipboard");
         putValue(Action.SMALL_ICON, ResourceHelper.instance.getIcon("res/icons/smallpaste.gif"));
      }

      public void actionPerformed(ActionEvent e) 
      {
         JEditorPane target = (JEditorPane)getTextComponent(e);
         if (target != null) 
         {
            Document doc = target.getDocument();

            // check for required deletion of current sel
	        int start = target.getSelectionStart();
	        int end = target.getSelectionEnd();
	        if (end > start)
	        {
                try {
                    doc.remove(start, end-start);
                } catch (BadLocationException e1) {
                    e1.printStackTrace();
                }    	              
	        }
               
            // grab clipboard and its transferrable data
            Clipboard clip = target.getToolkit().getSystemClipboard();
            Transferable data = clip.getContents(this);
              
            // Check if the data is IvanhoeData or Text data
            if( data.isDataFlavorSupported( DataFlavor.stringFlavor) )
            {
               pasteTextData(doc, start, data);
            }
            else
            {
               SimpleLogger.logError("Unsupported clipboard contents");
               Ivanhoe.showErrorMessage("The type of data you are trying to paste is not supported");
            }
         }
         else
         {
            Toolkit.getDefaultToolkit().beep();
         }
      }
      
      private void pasteTextData( Document doc, int start, Transferable data)
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
                  
                 doc.insertString(start, content.toString(), null);
              }
              catch (Exception e1)
              {
                 SimpleLogger.logError("Unable to paste text data: " + e1);
                 Ivanhoe.showErrorMessage("Unable to paste data at this location");
              }
           }
   }

   
   /**
    * Action implemnation for Complete Creation
    */
   private class AcceptAction extends javax.swing.AbstractAction
   {
      public AcceptAction()
      {
         super("Accept",
            ResourceHelper.instance.getIcon("res/icons/accept.gif"));
         putValue(Action.SHORT_DESCRIPTION, "Accept this document into the discourse field");
      }

      public void actionPerformed(ActionEvent arg0)
      {
          if (headerPanel.getTitle().length() == 0) {
              Ivanhoe.showErrorMessage("You must enter a document title");
          }
          else
          {
              addDocumentToDiscourseField();     
          }
      }
   }

    private class HeaderPanel extends JPanel
    {
        private JTextField titleField, authorField, sourceField;
        
        public HeaderPanel()
        {
            setLayout(new BorderLayout());
            add(createInfoPanel(), BorderLayout.NORTH);
            add(createToolbar(), BorderLayout.SOUTH);
        }

        private void addFieldLabel(JPanel panel, String line)
        {
            JLabel label = new JLabel(line, SwingConstants.LEFT);
            label.setFont(IvanhoeUIConstants.TINY_BOLD_FONT);
            label.setForeground(IvanhoeUIConstants.WHITE);
            panel.add(label);
        }

        /**
         * Creates a new textfield in the document editor and adds it to the display
         * 
         * @param panel The JPanel in which to add the text field
         * @param line Any initial text in which to populate the text field
         * @param columns Limit on the length (in characters) for the field
         * 
         * @return New JTextField object
         */
        private JTextField addFieldData(JPanel panel, String line, int columns)
        {
            JTextField field = new JTextField(line);
            field.addKeyListener(new TextLimit(columns));
            field.setFont(IvanhoeUIConstants.TINY_FONT);
            field.setForeground(IvanhoeUIConstants.BLACK);
            panel.add(field);
            return field;
        }
        
        private void addToolbarButton(JToolBar bar, Action act)
        {
           JButton btn = new JButton(act);
        
        	btn.setText(null);
        	bar.add(btn);
        	btn.setSize(17,17);
        	btn.setOpaque(false);
        	btn.setBorder(new EmptyBorder(3,3,3,3));
        }

        private JPanel createInfoPanel()
        {
            JPanel pnl = new JPanel(new BorderLayout());
            pnl.setOpaque(false);

            JPanel labels = new JPanel(new GridLayout(3, 1));
            labels.setOpaque(false);

            addFieldLabel(labels, "Title: ");
            addFieldLabel(labels, "Author: ");
            addFieldLabel(labels, "Source: ");
            pnl.add(labels, BorderLayout.WEST);

            JPanel disp = new JPanel(new GridLayout(3, 1));
            disp.setOpaque(false);

            // create the controls
            titleField = addFieldData( disp, "", 50);            
            authorField = addFieldData( disp, "", 255);      
            sourceField = addFieldData( disp, "", 100);

            pnl.add(disp, BorderLayout.CENTER);
            pnl.add(Box.createHorizontalStrut(10), BorderLayout.EAST);
            pnl.setBorder(new EmptyBorder(0, 7, 0, 0));

            JPanel infoPnl = new JPanel(new BorderLayout());
            infoPnl.setBackground(IvanhoeUIConstants.DARKEST_GRAY);

            infoPnl.add(pnl, BorderLayout.CENTER);

            JLabel docIcon = new JLabel(authoringIcon);

            docIcon.setBackground(IvanhoeUIConstants.BLACK);
            infoPnl.add(docIcon, BorderLayout.WEST);
            infoPnl.setBorder(new EmptyBorder(3, 3, 3, 3));

            return infoPnl;
        }
        
        public String getTitle()
        {
            return titleField.getText();
        }
        
        public String getAuthor()
        {
            return authorField.getText();
        }
        
        public String getSource()
        {
            return sourceField.getText();
        }

        private IvanhoeToolBar createToolbar()
        {
            // Create the document toolbar
            IvanhoeToolBar toolbar = new IvanhoeToolBar();
            toolbar.setBackground(IvanhoeUIConstants.DARK_GRAY);
            
            // clipboard stuff
            Action act = editorPane.getActionMap().get(DefaultEditorKit.cutAction);
            act.putValue(AbstractAction.SMALL_ICON, ResourceHelper.instance.getIcon("res/icons/smallcut.gif"));
            addToolbarButton(toolbar, act);
            act = editorPane.getActionMap().get(DefaultEditorKit.copyAction);
            act.putValue(AbstractAction.SMALL_ICON, ResourceHelper.instance.getIcon("res/icons/smallcopy.gif"));
            addToolbarButton(toolbar, act);
            
            NewDocumentPasteAction newPasteAction = new NewDocumentPasteAction();
            editorPane.getActionMap().put(DefaultEditorKit.pasteAction,newPasteAction);
            addToolbarButton(toolbar, newPasteAction);
            toolbar.addSeparator();
            
            // styles
            act = editorPane.getActionMap().get("font-bold");
            act.putValue(Action.SMALL_ICON, ResourceHelper.instance.getIcon("res/icons/bold.gif"));
            addToolbarButton(toolbar, act);
            act = editorPane.getActionMap().get("font-italic");
            act.putValue(Action.SMALL_ICON, ResourceHelper.instance.getIcon("res/icons/italic.gif"));
            addToolbarButton(toolbar, act);
            act = editorPane.getActionMap().get("font-underline");
            act.putValue(Action.SMALL_ICON, ResourceHelper.instance.getIcon("res/icons/underline.gif"));
            addToolbarButton(toolbar, act);
            toolbar.addSeparator();
            
            // justify
            act = editorPane.getActionMap().get("left-justify");
            act.putValue(Action.SMALL_ICON, ResourceHelper.instance.getIcon("res/icons/leftJustify.gif"));
            addToolbarButton(toolbar, act);
            act = editorPane.getActionMap().get("center-justify");
            act.putValue(Action.SMALL_ICON, ResourceHelper.instance.getIcon("res/icons/centerJustify.gif"));
            addToolbarButton(toolbar, act);
            act = editorPane.getActionMap().get("right-justify");
            act.putValue(Action.SMALL_ICON, ResourceHelper.instance.getIcon("res/icons/rightJustify.gif"));
            addToolbarButton(toolbar, act);
            
            // control
            toolbar.add(Box.createHorizontalGlue());
            addToolbarButton(toolbar, new CancelAction() );
            addToolbarButton(toolbar, new AcceptAction() );
            
            toolbar.setFloatable(false);

            return toolbar;
        }

    }
}