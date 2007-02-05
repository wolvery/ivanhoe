/*
 * Created on Nov 26, 2003
 */
package edu.virginia.speclab.ivanhoe.client.game.view.document;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Highlighter;

import edu.virginia.speclab.ivanhoe.client.IvanhoeUIConstants;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.DiscourseField;
import edu.virginia.speclab.ivanhoe.client.game.model.document.IDocumentMouseListener;
import edu.virginia.speclab.ivanhoe.client.game.model.document.IvanhoeEditorKit;
import edu.virginia.speclab.ivanhoe.client.game.view.Workspace;
import edu.virginia.speclab.ivanhoe.client.game.view.ui.IvanhoeStyleInternalFrame;
import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;
import edu.virginia.speclab.ivanhoe.shared.data.Link;
import edu.virginia.speclab.ivanhoe.shared.data.Role;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.text.*;

/**
 * @author lfoster
 */
public class AnimatedEditorPane extends JEditorPane
{
   private LinkPopUp popup;
   private Highlighter highlighter;
   private boolean isLoading = false;
   private int dotCount;
   private Vector mouseListeners;
   private Object highlightTag;

   	private static final DefaultHighlighter.DefaultHighlightPainter 
   		highlightPainter =
            new DefaultHighlighter.DefaultHighlightPainter(
            	new Color(10,250,100) );

   public AnimatedEditorPane( DiscourseField discourseField )
   {
      super();
      IvanhoeEditorKit editorKit = new IvanhoeEditorKit(discourseField);
      setEditorKit(editorKit);
      highlighter = getHighlighter();
      dotCount = 0;
      setDragEnabled(false);
      
      this.mouseListeners = new Vector();
      this.addMouseListener( new MouseAdapter()
       {
          public void mousePressed(MouseEvent e)
          {
             if (isEnabled())
             {
                clearHighlight();
             }
          }
       });
      this.addCaretListener( new CaretListener()
              {
		          public void caretUpdate(CaretEvent ce)
		          {
		              if (ce.getSource() == AnimatedEditorPane.this && ce.getDot() == 0)
		              {
		                  try
		                  {
		                      AnimatedEditorPane.this.setCaretPosition(1);
		                  }
		                  catch (IllegalArgumentException iae)
		                  {
		                      // TODO: fix this hack.  I've yet to find a case
		                      // that it breaks under, but one is probably out
		                      // there.
		                      //
		                      // Whenever caret position 1 isn't legal, the bug
		                      // that this fixes doesn't occur. 
		                  }
		              }
		          }
              });
   }
   
   /**
    * Add a listener for mouse events geberated by IvanhoeTags
    * @param listener
    */
   public void addMouseListener(IDocumentMouseListener listener)
   {
      if (this.mouseListeners.contains(listener) == false)
      {
         this.mouseListeners.add(listener);
      }
   }
   
   /**
    * Send notification that the mouse has entered an ivanhoe tag
    * to all registered listsners
    * @param tagInfo
    */
   public void fireTagEntered(String tagDetails)
   {
      Enumeration enumerator = this.mouseListeners.elements();
      while (enumerator.hasMoreElements())
         ((IDocumentMouseListener)enumerator.nextElement()).tagEntered(tagDetails);
   }
   
   /**
    * Send notification that the mouse has exited an ivanhoe tag
    * to all registered listsners
    * @param tagInfo
    */
   public void fireTagExited()
   {
      Enumeration enumerator = this.mouseListeners.elements();
      while (enumerator.hasMoreElements())
         ((IDocumentMouseListener)enumerator.nextElement()).tagExited();
   }
   
   /**
    * Send notification an ivanhoe tag has been clicked
    * to all registered listsners
    * @param tagInfo
    */
   public void fireLinkActivated(Link linkTarget)
   {
      Enumeration enumerator = this.mouseListeners.elements();
      while (enumerator.hasMoreElements())
         ((IDocumentMouseListener)enumerator.nextElement()).linkActivated(linkTarget);
   }
   
   /**
    * Remove a mouse listener
    * @param listener
    */
   public void removeMouseListener(IDocumentMouseListener listener)
   {
      this.mouseListeners.remove(listener);
   }

   public void showPop(Link link, Role role)
   {
      if( popup != null )
      {
          popup.setVisible(false);
          Workspace.instance.remove(popup);
      }
       
      this.popup = new LinkPopUp(link,role);
      Workspace.instance.add(this.popup);
      Workspace.instance.moveToFront(popup);
      Workspace.instance.centerWindowOnWorkspace(popup);
   }
   
   /**
    * Remove highlighted text
    */
   public void clearHighlight()
   {
      if (this.highlightTag != null)
      {
         this.highlighter.removeHighlight(this.highlightTag);
         this.highlightTag = null;
      }
   }
   
   /**
    * @return
    */
   public boolean isHighlightActive()
   {
      return (this.highlightTag != null);
   }
   
   public void highlightSelection( int position, int length)
   {
      int startPosition = position;
      int endPosition = position+length;

      try 
      {       
         // Highlight the selected postion
         if (this.highlightTag == null)
         {
            this.highlightTag = highlighter.addHighlight(
               	startPosition, endPosition, highlightPainter);
         }
         else
         {
            this.highlighter.changeHighlight(this.highlightTag, startPosition, endPosition);
         }
         
      	} 
      catch (BadLocationException e) 
      {
         SimpleLogger.logError("Attempted to highlight invalid region.");
      }
   }

   public void scrollToOffset(int offset)
   {
      try 
      {
         // scroll to the beginning of the doc 
         Rectangle startOfDoc = modelToView(0);
         scrollRectToVisible(startOfDoc);
         
         //scroll to the selected position
         Rectangle targetRect = modelToView(offset);
         Rectangle viewRect = getVisibleRect();
         targetRect.y += viewRect.height / 2;
         scrollRectToVisible(targetRect);
   
      } catch (BadLocationException e) 
      {
         SimpleLogger.logError("Attempted to scroll to invalid offset: "+offset);
      }
   }

   public void setLoading(boolean loading)
   {
      this.isLoading = loading;
   }

   public void paint(Graphics g)
   {
      super.paint(g);       
      
      if( isLoading == true )
      {
         // clear the surface
         g.clearRect(0,0,getWidth(),getHeight());
         String dots = new String();
        
         // prepare the dots
         for( int i=0; i < dotCount; i++ )
         {
            dots = dots + ".";
         }

         // draw the text
         g.setFont(IvanhoeUIConstants.SMALL_FONT);
         g.setColor(Color.BLUE);
         g.drawString("Document Loading"+dots, getWidth()/2-40,getHeight()/2);
      }
   }

   /**
    * Handle a tick.. perform animations, update states.
    */
   public void tick()
   {  
      if (isLoading == true)
      {
         if( dotCount++ >= 5 ) dotCount = 0;
         repaint();  
      }
   }
   
   /**
    * @return Returns the isLoading.
    */
   public boolean isLoading()
   {
       return isLoading;
   }
   
   private class LinkPopUp extends IvanhoeStyleInternalFrame
   {
      public LinkPopUp( Link link, Role author ) 
      {
         super("Commentary");
         
         getContentPane().setLayout(new BorderLayout() );
         
         JEditorPane display = new JEditorPane();
         display.setEditable(false);
         
         JScrollPane sp = new JScrollPane(display);
         sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
         sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);      
         sp.setBorder(new EmptyBorder(0,0,0,0));
         
         JPanel topPanel = new JPanel();
         topPanel.setLayout( new BorderLayout() );
         topPanel.setBackground(IvanhoeUIConstants.titleColor);

         if( author != null )
         {
             JLabel authorLabel = new JLabel("From: " + author.getName());
             authorLabel.setFont(IvanhoeUIConstants.TINY_BOLD_FONT);
             authorLabel.setForeground(Color.WHITE);
             topPanel.add(authorLabel, BorderLayout.NORTH);
             setTitleColor(author.getFillPaint());
         }

         if( link != null )
         {
             JLabel subjectLabel = new JLabel("Subject: " + link.getLabel());
             subjectLabel.setFont(IvanhoeUIConstants.TINY_BOLD_FONT);
             subjectLabel.setForeground(Color.WHITE);
             topPanel.add(subjectLabel, BorderLayout.SOUTH);
         }

         getContentPane().add(topPanel, BorderLayout.NORTH);
         getContentPane().add(sp, BorderLayout.CENTER);

         display.setText(link.getLinkTag().getTagData());
         display.setCaretPosition(0);
         display.setFont(IvanhoeUIConstants.SMALL_FONT);
         
         setSize(300,200);
         setVisible(true);         
      }     
   }
}
