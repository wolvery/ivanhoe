/*
 * Created on Jan 7, 2004
 *
 * AnnotationDialog
 */
package edu.virginia.speclab.ivanhoe.client.game.view.document;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.text.BadLocationException;

import edu.virginia.speclab.ivanhoe.client.Ivanhoe;
import edu.virginia.speclab.ivanhoe.client.IvanhoeUIConstants;
import edu.virginia.speclab.ivanhoe.client.game.view.ui.IvanhoeStyleInternalFrame;
import edu.virginia.speclab.ivanhoe.shared.data.LinkTag;
import edu.virginia.speclab.ivanhoe.shared.data.LinkType;

/**
 * @author lfoster
 *
 * Dialog to handle the entry of annotation information
 */
public class AnnotationWindow extends IvanhoeStyleInternalFrame implements ActionListener
{
   private JButton okBtn;
   private JButton cancelBtn;
   private JEditorPane annotationTxt;
   private JTextField titleTxt;  
   private DocumentEditor parentEditor;
   private int start, end;
//   private String briefSrc;
   
   public AnnotationWindow(DocumentEditor parentEditor, int start, int end)
   {
      super("comment");

      this.parentEditor = parentEditor;
      this.start = start;
      this.end = end;
      
//      try
//      {
//         IvanhoeDocument srcDocument = parentEditor.getDocument();
//         String txt = srcDocument.getText(this.start, this.end-this.start);
//         
//         if( txt.length() > 25 )
//             this.briefSrc = txt.substring(0,25);
//         else
//             this.briefSrc = txt;
//             
//      }
//      catch (BadLocationException e){}

      createEntryUI();
   }

   /**
    * Create the UI components necessary for annotation entry
    */
   private void createEntryUI()
   {
      getContentPane().setLayout( new BorderLayout(1,5) );
      JPanel mainPnl = new JPanel(new BorderLayout(1,5));
      
      JPanel headerPnl = new JPanel( new BorderLayout() );
      JPanel labels = new JPanel( new GridLayout(2,1) );
      JLabel titleLabel = new JLabel("title: ", SwingConstants.RIGHT);
      titleLabel.setFont(IvanhoeUIConstants.BOLD_FONT);
      labels.add(titleLabel);
      headerPnl.add(labels, BorderLayout.WEST);
      
      JPanel fields = new JPanel( new GridLayout(2,1) );
      this.titleTxt = new JTextField();
      fields.add( this.titleTxt );
      
      headerPnl.add(fields, BorderLayout.CENTER);
      
      
      JPanel anoPnl = new JPanel( new BorderLayout() );
      JLabel annotationLabel = new JLabel("commentary: ");
      annotationLabel.setFont(IvanhoeUIConstants.BOLD_FONT);
      anoPnl.add( annotationLabel, BorderLayout.NORTH);
      anoPnl.setFont(IvanhoeUIConstants.SMALL_FONT);
      this.annotationTxt = new JEditorPane();
      this.annotationTxt.setContentType("text/html");
      this.annotationTxt.setMargin(new Insets(10, 10, 10, 10));
      
      JScrollPane sp = new JScrollPane( this.annotationTxt );
      sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
      sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
      anoPnl.add( sp, BorderLayout.CENTER );
      
      mainPnl.add(headerPnl, BorderLayout.NORTH);
      mainPnl.add(anoPnl, BorderLayout.CENTER);

      getContentPane().add(Box.createVerticalStrut(5), BorderLayout.NORTH);
      getContentPane().add(mainPnl,BorderLayout.CENTER);
      getContentPane().add(createControlPanel(),BorderLayout.SOUTH);
      getContentPane().add(Box.createHorizontalStrut(10), BorderLayout.EAST);
      getContentPane().add(Box.createHorizontalStrut(10), BorderLayout.WEST);   
   }
   
   /**
    * Create the panel containing commentary controls
    * @return
    */
   private Component createControlPanel()
   {
      JPanel pnl = new JPanel( new BorderLayout());
      
      JPanel btns = new JPanel();

      this.okBtn = new JButton("ok");
      this.okBtn.setFont(IvanhoeUIConstants.BOLD_FONT);
      this.okBtn.addActionListener(this);
      btns.add(this.okBtn);

      this.cancelBtn = new JButton("cancel");
      this.cancelBtn.setFont(IvanhoeUIConstants.BOLD_FONT);
      this.cancelBtn.addActionListener(this);
      btns.add(this.cancelBtn);
      
      pnl.add(btns, BorderLayout.NORTH);

      return pnl;
   }
   
   public String getAnnotation()
   {
      String txt = "";
      try
      {
         txt = this.annotationTxt.getText(0,
            this.annotationTxt.getDocument().getLength());
      }
      catch (BadLocationException e){}
      return txt;
   }
   
   public String getCaption()
   {
      return this.titleTxt.getText();
   }

   public void actionPerformed(ActionEvent e)
   {
      if (e.getSource().equals(this.okBtn))
      {
         if (this.titleTxt.getText().length() < 1)
         {
             Ivanhoe.showErrorMessage("Please enter a title for this commentary");
            return;
         }
         if (this.annotationTxt.getDocument().getLength() < 1)
         {
             Ivanhoe.showErrorMessage("Please enter a body for this commentary");
            return;
         }
         if (this.titleTxt.getText().length() > 20 )
         {
             Ivanhoe.showErrorMessage("Title too long! Please limit your title to 20 characters");
            return;
         }
       
        // commit the change and close the window
        LinkTag linkData = new LinkTag(getAnnotation());
	       
        // set the annotation type and add the link
        parentEditor.addLink(start,end,LinkType.COMMENT, getCaption(), linkData );
        
        closeWindow();        
      }
      else if (e.getSource().equals(this.cancelBtn))
      {
        closeWindow();
      }
   }
      
}
