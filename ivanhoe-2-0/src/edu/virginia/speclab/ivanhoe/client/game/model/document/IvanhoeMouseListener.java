/*
 * Created on Nov 19, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package edu.virginia.speclab.ivanhoe.client.game.model.document;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;
import javax.swing.text.html.*;

import edu.virginia.speclab.ivanhoe.client.IvanhoeUIConstants;
import edu.virginia.speclab.ivanhoe.client.game.view.document.AnimatedEditorPane;
import edu.virginia.speclab.ivanhoe.client.util.ResourceHelper;
import edu.virginia.speclab.ivanhoe.shared.data.*;

/**
 * MouseListener
 * @author lfoster
 *
 * Mouse listener that will check for moves over IvanhoeLinks
 * and change the cursor. This will also listen for clicks
 * and activate the links.
 */
public class IvanhoeMouseListener extends MouseAdapter implements MouseMotionListener
{
   private static final Cursor handCursor = 
      Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
   private static final Cursor arrowCursor = 
      Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
   private Position.Bias[] bias = new Position.Bias[1];

   /**
    * Check if the click was on an ilink element. handle 
    * link if necessary
    */
   public void mouseClicked(MouseEvent e)
   {
      // get reference to the document 
      AnimatedEditorPane pane = (AnimatedEditorPane)e.getSource();
      IvanhoeDocument doc = (IvanhoeDocument)pane.getDocument();

      // convert the x,y coord to a document position, and
      // find the character element at that position
      Point pt = new Point(e.getX(), e.getY());
      Element charElem = null;
      int pos = pane.getUI().viewToModel(pane, pt, bias);
      if (pos >= 0)
      {
         charElem = doc.getCharacterElement(pos);
      }

      if (charElem != null)
      {
         // handle the click based on the type of tag
         AttributeSet atts = charElem.getAttributes();
         if (atts.isDefined(IvanhoeTag.ILINK))
         {
            handleLinkClick(e, atts);
         }
      }
   }

   /**
    * Handle mouse click events on ilink tags
    * @param e
    * @param atts
    */
   private void handleLinkClick(MouseEvent e, AttributeSet atts)
   { 
      // grab the id of the ilink element that has been clicked
      AttributeSet linkAtts = (AttributeSet)atts.getAttribute(IvanhoeTag.ILINK);
      String linkId = (String)linkAtts.getAttribute(HTML.Attribute.ID);

      AnimatedEditorPane pane = (AnimatedEditorPane)e.getSource();
      IvanhoeDocument doc = (IvanhoeDocument)pane.getDocument();
      List targets = doc.getLinkManager().getLinks(linkId);
      
      JPopupMenu menu = createLinkPopupMenu(targets,pane);
      menu.show(pane, e.getX(), e.getY());
   }

   /**
    * Create a popup menu listing available link choices
    */
   private JPopupMenu createLinkPopupMenu(List targets, AnimatedEditorPane pane)
   {
      JPopupMenu menu = new JPopupMenu();
      Iterator itr = targets.iterator();
      while (itr.hasNext())
      {
         Link linkTarget = (Link)itr.next();
         JMenuItem menuItem = new JMenuItem(new LinkMenuItem(linkTarget, pane));
         menuItem.setFont(IvanhoeUIConstants.SMALL_FONT);
         menu.add(menuItem);
      }

      menu.setBorder(
         new TitledBorder(
            new LineBorder(new Color(0,128,0)),
            "Available Links",
            TitledBorder.CENTER,
            TitledBorder.DEFAULT_POSITION,
            IvanhoeUIConstants.SMALL_FONT));
      return menu;
   }

   public void mouseDragged(MouseEvent e)
   {
      // No action for dragging 
   }

   /**
    * Check for ivanhoe tags under current pos. If one is found,
    * set status message and change cursor.
    */
   public void mouseMoved(MouseEvent e)
   {
      // get reference to the editor and document    
      AnimatedEditorPane pane = (AnimatedEditorPane)e.getSource();      
      IvanhoeDocument doc = (IvanhoeDocument)pane.getDocument();

      // convert the x,y coord to a document position, and
      // find the character element at that position
      Point pt = new Point(e.getX(), e.getY());
      Element charElem = null;
      int pos = pane.getUI().viewToModel(pane, pt, bias);
      if (pos >= 0)
      {
         charElem = doc.getCharacterElement(pos);
      }

      if (charElem == null)
      {
         // nothing to do
         return;
      }
      
      AttributeSet atts = charElem.getAttributes();
      if (atts.isDefined(IvanhoeTag.ILINK) )
      {
         pane.setCursor(IvanhoeMouseListener.handCursor);
      }
      else
      {
         pane.setCursor(IvanhoeMouseListener.arrowCursor);
      }
   }
   
   /**
    * Simple extension of a menu item that contains a target
    */
   private class LinkMenuItem extends AbstractAction
   {
      private Link target;
      private AnimatedEditorPane pane;

      public LinkMenuItem(Link tgt, AnimatedEditorPane pane)
      {
         super(tgt.getLabel());
         this.target = tgt;
         this.pane = pane;

         if (tgt.getType().equals(LinkType.URL))
         {           
            this.putValue(Action.SMALL_ICON, ResourceHelper.instance.getIcon("res/icons/url.gif"));
         }
         else if (tgt.getType().equals(LinkType.COMMENT))
         {
            this.putValue(Action.SMALL_ICON, ResourceHelper.instance.getIcon("res/icons/comment.gif"));
         }
         else
         {
            this.putValue(Action.SMALL_ICON, ResourceHelper.instance.getIcon("res/icons/doc.gif"));
         }
      }

      public void actionPerformed(ActionEvent e)
      {
         this.pane.fireLinkActivated(this.target);
      }
   }
}
