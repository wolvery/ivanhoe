/*
 * Created on Apr 19, 2004
 * 
 * Gutter
 */

package edu.virginia.speclab.ivanhoe.client.game.view.document;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.html.HTML.Tag;

import edu.virginia.speclab.ivanhoe.client.Ivanhoe;
import edu.virginia.speclab.ivanhoe.client.IvanhoeUIConstants;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.DiscourseField;
import edu.virginia.speclab.ivanhoe.client.game.model.document.IDocumentActionListener;
import edu.virginia.speclab.ivanhoe.client.game.model.document.IvanhoeDocument;
import edu.virginia.speclab.ivanhoe.client.game.view.Workspace;
import edu.virginia.speclab.ivanhoe.client.util.ResourceHelper;
import edu.virginia.speclab.ivanhoe.shared.IMessageHandler;
import edu.virginia.speclab.ivanhoe.shared.SimpleLogger;
import edu.virginia.speclab.ivanhoe.shared.data.DocumentVersion;
import edu.virginia.speclab.ivanhoe.shared.data.IvanhoeAction;
import edu.virginia.speclab.ivanhoe.shared.message.Message;
import edu.virginia.speclab.ivanhoe.shared.message.MessageType;
import edu.virginia.speclab.ivanhoe.shared.message.MoveResponseMsg;

/**
 * @author lfoster
 * 
 * DocumentGutter shows a series of markings in a document editor margin. These
 * markings are used to jump to places in the text where an action has been made
 */
public class DocumentGutter extends JPanel implements 
   IDocumentActionListener, IMessageHandler
{
   private Vector           marks;
   private Marker           currentMark;
   private int              actionNum;
   private DocumentEditor   editor;
   private RemoveAction     removeAct;
   private RemoveAllAction  removeAllAct;
   private NextMarkAction   nextMarkAct;
   private PriorMarkAction  priorMarkAct;
   private InfoPanel        infoPanel;
   
   private static final int GUTTER_WIDTH = 20;
   private boolean initialized;

   public DocumentGutter(DocumentEditor editor)
   {
      super();
      
      // create all gutter actions. All are disabled until data arrives
      this.removeAct = new RemoveAction();
      this.removeAllAct = new RemoveAllAction();
      this.nextMarkAct = new NextMarkAction();
      this.priorMarkAct = new PriorMarkAction();

      // setup the look of the gutter
      setBorder(new BevelBorder(BevelBorder.LOWERED));      
      setBackground(IvanhoeUIConstants.LIGHT_GRAY);
      setMinimumSize(new Dimension(GUTTER_WIDTH, 0));
      setMaximumSize(new Dimension(GUTTER_WIDTH, 0));
      setPreferredSize(new Dimension(GUTTER_WIDTH, 0));
      
      // set up data
      this.editor = editor;
      this.marks = new Vector();
      
      this.addComponentListener( new ComponentAdapter() 
         {
            public void componentResized(ComponentEvent e) 
            {
               updateMarks();
            }
         });

      // register a mouse motion listener to handle showing the
      // hand cursor when over a mark
      this.addMouseMotionListener(new MouseMotionAdapter()
         {
            public void mouseMoved(MouseEvent e)
            {
               checkMouseMove(e.getPoint().x, e.getPoint().y);
            }
         });

      // register a mouse click listener to handle highlighting
      // when a mark is clicked
      this.addMouseListener(new MouseAdapter()
         {
            public void mouseExited(MouseEvent e)
            {
                hideInfoPanel();
            }
            public void mouseClicked(MouseEvent e)
            {
               checkMouseClick(e.getPoint().x, e.getPoint().y);
            }
         });
      
      Ivanhoe.registerGameMsgHandler(MessageType.MOVE_RESPONSE, this);
   }
   
   private void hideInfoPanel()
   {
       if( infoPanel != null )
           infoPanel.setVisible(false);
   }
   
   /**
    * Notification that the editor to which the gutter belongs is closing
    * Remove message handling! 
    */
   public void handleClose()
   {
      SimpleLogger.logInfo("Editor closing; gutter unregistering for response msgs");
      Ivanhoe.getProxy().unregisterMsgHandler(MessageType.MOVE_RESPONSE, this);
   }

   /**
    * If a mark is clicked, highlight the appropriate text
    * @param x
    * @param y
    */
   public void checkMouseClick(int x, int y)
   {
      IvanhoeAction act = null;
      Marker mark = getMarkerAtPosition(x, y);
      if (mark != null)
      {
         if (this.currentMark == null)
         {
            this.currentMark = mark;
            this.actionNum = 0;
         }
         else if (this.currentMark.equals(mark))
         {
            this.actionNum++;
            if (this.actionNum >= this.currentMark.getActionCount())
            {
               this.actionNum = 0;
            }
         }
         else
         {
            this.currentMark.setSelected(false);
            this.currentMark = mark;
            this.actionNum = 0;
         }
         
         act = editor.getDiscourseField().lookupAction(
            this.currentMark.getActionId(this.actionNum));
      }
      else
      {
          this.clearSelection();
      }
      Workspace.instance.getNavigator().getActionSelection().changeSelection(act);
   }

   /**
    * Check if cursor is over a mark. Set hand cursor as needed.
    * @param x
    * @param y
    */
   public void checkMouseMove(int x, int y)
   {
      Marker mark = getMarkerAtPosition(x, y);
      
      if ( mark != null && infoPanel != null )
      {
         editor.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
         infoPanel.setMsg(mark.getDescription());
 
         Point p1 = SwingUtilities.convertPoint(this, x,y, Workspace.instance);
         int right = Workspace.instance.getLocation().x + Workspace.instance.getWidth();
         int popRight = p1.x + infoPanel.getWidth();
         if ( popRight > right)
         {
            p1.x -= (popRight-right);
            infoPanel.setLocation(p1.x,p1.y);
         }
         else
         {
             infoPanel.setLocation(p1.x+10,p1.y);
         }
         
         infoPanel.setVisible(true);
      }
      else
      {
         editor.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
         infoPanel.setVisible(false);
      }
   }

   /**
    * Find the marker at the given x,y position - or null if none
    * @param x
    * @param y
    * @return
    */
   private Marker getMarkerAtPosition(int x, int y)
   {
      Marker hit = null;
      Marker test;
      Enumeration enumerator = this.marks.elements();
      while (enumerator.hasMoreElements())
      {
         test = (Marker) enumerator.nextElement();
         if (test.hit(x, y))
         {
            hit = test;
            hit.setHighlighted(true);
         }
         else 
         {
            test.setHighlighted(false);
         }
      }
      repaint();

      return hit;
   }
   
   /**
    * Find the mark for <code>action</code> and set it as current.
    * This method does not attempt to highlight the text of the action;
    * it is assumed that an external method has selected the text. This just
    * keeps the gutter in sync
    * @param action
    */
    public void selectAction(String actionId)
    {
        this.clearSelection();
        
        Marker mark;
        Enumeration enumerator = this.marks.elements();
        while (enumerator.hasMoreElements())
        {
            mark = (Marker) enumerator.nextElement();
            if (mark.containsAction(actionId))
            {
                this.currentMark = mark;
                this.currentMark.setSelected(true);
                repaint();
                break;
            }
        }
    }
   
   /**
    * After a submission, update all marks in the gutter to show new state
    */
   public void handleMessage(Message msg)
   {
      SimpleLogger.logInfo("Gutter got submit response - updating marks");
      if (msg.getType().equals(MessageType.MOVE_RESPONSE))
      {
         MoveResponseMsg resp = (MoveResponseMsg)msg;
         if (resp.isSuccess())
         {
            updateMarks();
         }
      }
   }
   
   /**
    * Remove active mark selection
    */
   public void clearSelection()
   {
      if (this.currentMark != null)
      {
         this.currentMark.setSelected(false);
         this.currentMark = null;
         repaint();
      }
   }
   
   /**
    * Walk the marks in document order
    */
   public void nextMark()
   {
      if (this.marks.size() == 0)
      {
         return;
      }
      
      if ( this.currentMark == null)
      {
         this.currentMark = (Marker)this.marks.firstElement();
         this.actionNum = 0;
      }
      else
      {
         this.actionNum++;
         
         if ( this.actionNum >= this.currentMark.getActionCount())
         {
            int nextIndex = this.marks.indexOf(this.currentMark) + 1;
            if (nextIndex >= this.marks.size()) nextIndex = 0;
            this.currentMark.setSelected(false);
            this.currentMark = (Marker)this.marks.elementAt(nextIndex);
            this.actionNum = 0;
         }
      }
      
      IvanhoeAction act = editor.getDiscourseField().lookupAction(
         this.currentMark.getActionId(this.actionNum));
      Workspace.instance.getNavigator().getActionSelection().changeSelection(act);
      repaint();
   }
   
   /**
    * Walk the marks in reverse document order
    */
   public void previousMark()
   {
      if (this.marks.size() == 0)
      {
         return;
      }
      
      if ( this.currentMark == null)
      {
         this.currentMark = (Marker)this.marks.lastElement();
         this.actionNum = this.currentMark.getActionCount()-1;
      }
      else
      {
         this.actionNum--;
         
         if ( this.actionNum < 0)
         {
            int nextIndex = this.marks.indexOf(this.currentMark) - 1;
            if (nextIndex < 0)
               	nextIndex = this.marks.size() - 1;
            this.currentMark.setSelected(false);
            this.currentMark = (Marker)this.marks.elementAt(nextIndex);
            this.actionNum = this.currentMark.getActionCount()-1;
         }
      }
      
      IvanhoeAction act = editor.getDiscourseField().lookupAction(
         this.currentMark.getActionId(this.actionNum));
      Workspace.instance.getNavigator().getActionSelection().changeSelection(act);
      repaint();
   }
   
   /**
    * Initialize the gutter with a document. Grabs the timeline and creates a
    * series of marks for each action
    * @param document
    */
   public void initialize()
   {
      IvanhoeDocument document = this.editor.getDocument();
      document.addActionListener(this);
      this.initialized = true;
      updateMarks();
   }
   
   public void updateMarks()
   {
      if (this.initialized == false)
         return;
      
      this.marks.clear();
      this.currentMark = null;
      IvanhoeDocument document = this.editor.getDocument();
      
      float scaleFactor = calculateScale(document);  
      Marker marker;
      int location;

      // mark each action in the document 
      Iterator actionItr = document.getActionHistory().iterator();
      String actionId;
      int offset;
      while (actionItr.hasNext())
      {
         actionId = (String)actionItr.next();
         offset = document.getActionOffset(actionId);
         location = (int)( offset * scaleFactor);
         
         // is there already a mark at this location?
         marker = findExistingMark(location);
         if (marker != null )
         {
            // add this act to it
            marker.addAction(actionId);
         }
         else
         {
            // create a new marker
            marker = new Marker( editor.getDiscourseField(), actionId, location);
            this.marks.add(marker);
         }         
      }
  
      // sort the marks in document order
      sortMarks();
  
      repaint();
   }

   /**
    * Sort marks in increasing document order
    * @param marker
    */
   private void sortMarks()
   {
      Marker mark1, mark2;
      for (int i = 0; i < this.marks.size(); i++)
      {
         for (int j = i + 1; j <  this.marks.size(); j++)
         {
            mark1 = (Marker)this.marks.elementAt(i);
            mark2 = (Marker)this.marks.elementAt(j);
            if ( mark1.getLocation() > mark2.getLocation())
            {
               this.marks.set(i, mark2);
               this.marks.set(j, mark1);
            }
         }
      }
   }

   /**
    * @param location
    * @return
    */
   private Marker findExistingMark(int location)
   {
      Marker match = null;
      Marker test;
      Enumeration enumerator = this.marks.elements();
      while (enumerator.hasMoreElements())
      {
         test = (Marker)enumerator.nextElement();
         if ( Math.abs(test.getLocation() -location) <= Marker.MARK_HEIGHT/2 )
         {
            match = test;
            break;
         }
      }
      return match;
   }

   /**
    * Paint the gutter. Iterate over all marks and draw them
    */
   public void paint(Graphics g)
   {
      super.paint(g);
      
      // paint all the markers
      Enumeration enumerator = this.marks.elements();
      while (enumerator.hasMoreElements())
      {
         ((Marker) enumerator.nextElement()).paint(g);
      }
   }
   
   private float calculateScale(IvanhoeDocument document)
   {
      return (float) (getHeight()-Marker.MARK_HEIGHT-Marker.MARK_BORDER*2) / 
         (float) document.getLength();
   }

   /**
    * Add a new action to the gutter
    */
   public void actionAdded(DocumentVersion version, String actionId, Tag type)
   {
      IvanhoeDocument document = this.editor.getDocument();
      float scaleFactor = calculateScale(document);  
      int location = (int)(document.getActionOffset(actionId) * scaleFactor);
      
      // is there already a mark at this location?
      Marker marker = findExistingMark(location);
      if (marker != null )
      {
         // add this act to it
         marker.addAction(actionId);
      }
      else
      {
         // create a new marker
         marker = new Marker( editor.getDiscourseField(), actionId, location);
         this.marks.add(marker);
      }
      
      repaint();
   }

   /**
    * Action has been deleted; remove its marker
    */
   public void actionDeleted(String actionId)
   {
      Marker marker;
      Iterator itr = this.marks.iterator();
      while (itr.hasNext())
      {
         marker = (Marker)itr.next();
         if (marker.containsAction(actionId))
         {
            if (marker.getActionCount() == 1)
            {
               this.marks.remove(marker);
            }
            else
            {
               marker.removeAction(actionId);
            }
            break;
         }
      }
      
      repaint();
   }
   
   /**
    * get the action that allows deletion of the currently selected act
    * @return
    */
   public RemoveAction getDeleteActAction()
   {
      return this.removeAct;
   }
   
   /**
    * Get the action that allows removal of all current actions
    * @return
    */
   public RemoveAllAction getDeleteAllAction()
   {
      return this.removeAllAct;
   }

   /**
    * Get the action for stepping thru all available acts
    * @return
    */
   public NextMarkAction getNextActAction()
   {
      return this.nextMarkAct;
   }
   
   /**
    * Get the action for stepping thu available acts in reverse order.
    * @return
    */
   public PriorMarkAction getPreviousActAction()
   {
      return this.priorMarkAct;
   }
   
   /**
    * This class represents a mark in the gutter
    */
   private static class Marker
   {
      private Vector     	actions;
      private boolean       isCurrent;
      private int           top;
      private Rectangle		rect;
      private boolean 		highlight;
      private boolean 		selected;
      
      private DiscourseField discourseField;

      private static final Color oldMarkColor     = new Color(70, 40, 150,125);
      private static final Color currMarkColor    = new Color(0, 150, 255,125);
      
      private static final int MARK_WIDTH = 12;
      private static final int MARK_HEIGHT = 4;
      private static final int MARK_BORDER = 3;

      /**
       * All markers must be construtcted with at least one action.
       * @param act
       */
      public Marker( DiscourseField discourseField, String actionId, int location)
      {
         this.discourseField = discourseField;
         this.actions = new Vector();
         this.actions.add( actionId );
         setLocation(location);
         
         // set flag to indicate actions in-progress
         if (discourseField.getCurrentMove().containsAction(actionId) )
         {
            this.isCurrent = true;
         }
      }

      /**
       * @param actionId
       */
      public void removeAction(String actionId)
      {
         this.actions.remove(actionId);
         this.isCurrent = false;
         for (Iterator itr = this.actions.iterator(); itr.hasNext();)
         {
            if (discourseField.getCurrentMove().containsAction(
               (String)itr.next()) )
            {
               this.isCurrent = true;
               break;
            }
         }
      }

      /**
       * @param action
       * @return
       */
      public boolean containsAction(String actionId)
      {
         return this.actions.contains(actionId);
      }

      /**
       * Check if this mark contains any current acts
       * @return
       */
      public boolean isCurrent()
      {
         return this.isCurrent;
      }

      /**
       * Get the screen location of the top of this mark
       * @return
       */
      public int getLocation()
      {
         return this.top;
      }
      
      /**
       * Get a desctiption of the action(s) at this mark
       * @return
       */
      public String getDescription()
      {
         final StringBuffer buf = new StringBuffer("<html><center><b>Actions</b></center>");
         
         for (Iterator i = this.actions.iterator(); i.hasNext();)
         {
            final String actionID = (String) i.next(); 
            final IvanhoeAction act = discourseField.lookupAction(actionID);
            final DocumentVersion docVersion =
                discourseField.getDocumentVersionManager().getDocumentVersion(act);
            
            if (docVersion != null)
            {
                buf
                    .append("<br>")
                    .append(act.getDescription(docVersion, isCurrent(), 25));
            }
            else
            {
                SimpleLogger.logError("Could not find document version for action ["+act+"]", new Exception());
            }
         }
            
         return buf.toString();
      }
      
      /**
       * Add an action to this list of managed actions located at
       * this marker position
       * @param act
       */
      public void addAction(String actId)
      {
         if (this.actions.contains(actId) == false)
         {
            this.actions.add(actId);
            sortActions();
            
            // if any actions are in-progress, this mark is in-progress
            if (discourseField.getCurrentMove().containsAction(actId) )
            {
               this.isCurrent = true;
            }
         }
      }
      
      private void sortActions()
      {
         String id1, id2;
         IvanhoeAction act1, act2;
         for (int i = 0; i < this.actions.size(); i++)
         {
            for (int j = i + 1; j <  this.actions.size(); j++)
            {
               id1 = (String)this.actions.elementAt(i);
               id2 = (String)this.actions.elementAt(j);
               act1 = discourseField.lookupAction(id1);
               act2 = discourseField.lookupAction(id2);
               if ( act1.getOffset() > act2.getOffset())
               {
                  this.actions.set(i, id2);
                  this.actions.set(j, id1);
               }
            }
         }
      }
      
      /**
       * Get the number of actions contained by this mark
       * @return
       */
      public int getActionCount()
      {
         return this.actions.size();
      }
      
      /**
       * Get the specified action
       * @return
       */
      public String getActionId(int index)
      {
         index = Math.max(0,index);
         index = Math.min(index, this.actions.size()-1);
         return (String)this.actions.elementAt(index);
      }

      /**
       * Check if the x,y corrdinates hit this mark
       * @param x
       * @param y
       * @return true if hit, false otherwise
       */
      public boolean hit(int x, int y)
      {
         return this.rect.contains(x,y);
      }
      
      /**
       * Set the y coordinate of the top of this mark
       * @param y
       */
      private void setLocation(int y)
      {
         this.top = y;
         this.top = Math.max(MARK_BORDER,this.top);
         this.rect = new Rectangle(MARK_BORDER,this.top,MARK_WIDTH,MARK_HEIGHT);
      }
      
      /**
       * Toggle highlighting of this mark
       * @param lit
       */
      public void setHighlighted(boolean lit)
      {
         this.highlight = lit;
      }
      
      /**
       * Toggle selection of this mark
       * @param sel
       */
      public void setSelected(boolean sel)
      {
         this.selected = sel;
      }

      /**
       * Render the mark
       * 
       * @param g
       */
      public void paint(Graphics g)
      {  
         Color color ;
         if ( this.isCurrent)
            color = Marker.currMarkColor;
         else
            color = Marker.oldMarkColor;
         
         if (this.selected == true)
         {
            int rgb = color.getRGB();
            rgb = rgb | 0x00ff00;
            rgb = rgb & 0xffffff11;
            color = new Color(rgb);
         }
         
         if (this.highlight == true)
         {
            color = color.brighter();
         }
         
         // fill the main body of the mark
         g.setColor(color);
         g.fillRect(this.rect.x, this.rect.y, 
            this.rect.width, this.rect.height );
         
         // draw the border based
      	   g.setColor(color.darker().darker());
      	   g.drawRect(this.rect.x, this.rect.y, 
      	      this.rect.width, this.rect.height);
         
         // draw an indicator that multiple actions live here
         if (this.actions.size() > 1)
         {
            g.setColor( Color.WHITE );
            int centerX = rect.x + rect.width/2;
            int centerY = rect.y + rect.height/2;
            g.drawLine(centerX-2, centerY, centerX+2,centerY);
            g.drawLine(centerX, centerY-1, centerX, centerY+1);
         }
      }
   }
   
   /**
    * AbstractAction implementation to facilitate walking thru all marks
    */
   private class NextMarkAction extends javax.swing.AbstractAction
   {
      public NextMarkAction()
      {
         super("Next", ResourceHelper.instance.getIcon("res/icons/smalldown.gif"));
         putValue(Action.SHORT_DESCRIPTION,"Go to next action");
      }

      /**
       * Action poerformed just calls gutter.nextMark to advance to the next mark
       */
      public void actionPerformed(ActionEvent arg0)
      {
         DocumentGutter.this.nextMark();
      }
   }
   
    /**
    * AbstractAction implementation to facilitate walking thru all marks
    */
   public class PriorMarkAction extends javax.swing.AbstractAction
   {
      public PriorMarkAction()
      {
         super("Previuos", ResourceHelper.instance.getIcon("res/icons/smallup.gif"));
         putValue(Action.SHORT_DESCRIPTION,"Go to previous action");
      }

      /**
       * Action poerformed just calls gutter.previosMark to move to the prior mark
       */
      public void actionPerformed(ActionEvent arg0)
      {
         DocumentGutter.this.previousMark();
      }
   }
   
   /**
    * Action implemnation for local doc searches
    */
   private class RemoveAction extends javax.swing.AbstractAction
   {
      public RemoveAction()
      {
			super("Remove Action",
				ResourceHelper.instance.getIcon("res/icons/deleteone.gif"));
			putValue(Action.SHORT_DESCRIPTION,"Remove the current action");
      }

      public void actionPerformed(ActionEvent arg0)
      {
         if (DocumentGutter.this.marks.size() == 0)
         {
             Ivanhoe.showErrorMessage("<html><b>Remove action failed</b> - " +
            "There are no actions to be removed from this document.");
            return;
         }
         if (DocumentGutter.this.currentMark == null)
         {
             Ivanhoe.showErrorMessage("<html><b>Remove action failed</b> - No action is currently selected.<br>" +
            "Select an action for removal by first clicking on a mark in <br>" +
            "the right margin, then click the remove action button.");
            return;
         }
         String actId = DocumentGutter.this.currentMark.getActionId(
            DocumentGutter.this.actionNum);
        
         if (editor.getDiscourseField().getCurrentMove().removeAction(actId))
         {
            DocumentGutter.this.editor.clearHighlights();
            DocumentGutter.this.currentMark.setSelected(false);
            DocumentGutter.this.currentMark = null;
         }
         else
         {
             Ivanhoe.showErrorMessage("Unable to remove actions from a prior move.");
         }
      }
   }
   
   /**
    * Action implemnation for removing all doc actions
    */
   private class RemoveAllAction extends javax.swing.AbstractAction
   {
      public RemoveAllAction()
      {
			super("Remove All",
				ResourceHelper.instance.getIcon("res/icons/deleteall.gif"));
			putValue(Action.SHORT_DESCRIPTION,"Remove all actions");
      }

      public void actionPerformed(ActionEvent arg0)
      { 
         if (DocumentGutter.this.marks.size() == 0)
         {
             Ivanhoe.showErrorMessage("<html><b>Remove all failed</b> - " +
            "There are no actions to be removed from this document.");
            return;
         }
         int resp = JOptionPane.showConfirmDialog(null, 
            "This will discard all current actions in this document. Continue?", 
            "Confirm Remove All",
            JOptionPane.OK_CANCEL_OPTION);
         if (resp == JOptionPane.OK_OPTION)
         {
            String title = DocumentGutter.this.editor.getDocument().getTitle();
            editor.getDiscourseField().getCurrentMove().removeAllActionsInDocument(title);
            DocumentGutter.this.editor.clearHighlights();
            if (DocumentGutter.this.currentMark != null)
            {
               DocumentGutter.this.currentMark.setSelected(false);
               DocumentGutter.this.currentMark = null;
            }
         }
      }
   }

/**
 * @param infoPanel The infoPanel to set.
 */
public void setInfoPanel(InfoPanel infoPanel)
{
    this.infoPanel = infoPanel;
}
}