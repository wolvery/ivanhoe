/*
 * Created on Apr 23, 2004
 *
 * Drawer
 */
package edu.virginia.speclab.ivanhoe.client.game.view.ui;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import javax.swing.*;

import edu.virginia.speclab.ivanhoe.client.Ivanhoe;
import edu.virginia.speclab.ivanhoe.client.IvanhoeUIConstants;
import edu.virginia.speclab.ivanhoe.client.game.view.Workspace;

/**
 * @author lfoster
 *
 * Drawer is a panel that contains one other panel. It has a tab that can be used to 
 * pull it out from the edge of the screen
 */
public class Drawer extends JPanel
{
   protected JPanel content;
   protected String label;
   protected int tabLocation;
   protected Rectangle tabRect;
   protected int state;
   protected int delta;
   
   // label alignment stuff
   protected int labelPadding;
   protected int labelOffset;
   
   private static int lastRightTabBottom = 2;
   private static int lastLeftTabBottom = 2;
 
   // tab constants
   public static final int RIGHT_TAB = 0;
   public static final int LEFT_TAB = 1;
   public static final int TAB_THICKNESS = 20;
   
   // Drawers states
   private static final int MOVE_RATE = 200;
   protected static final int OPENING = 0;
   protected static final int CLOSING = 1;
   protected static final int OPENED = 2;
   protected static final int CLOSED = 3;
   
   public Drawer(String label, JPanel content, int tabLocation)
   {
      super();
      setVisible(false);
      setLayout(new BorderLayout() );
      setOpaque(false);
      
      setLabelAlignment(SwingConstants.CENTER);
      
      // drawers are closed by default
      this.label = label;
      this.state = CLOSED;
      this.tabLocation = tabLocation;
      
      // add content
      this.content = content;
      add(content, BorderLayout.CENTER);
      
      // add a spacer to allow for drawing the tab
      switch (tabLocation)
      {
         case RIGHT_TAB:
            add(Box.createHorizontalStrut(TAB_THICKNESS), BorderLayout.EAST);
            break;
         case LEFT_TAB:
            add(Box.createHorizontalStrut(TAB_THICKNESS), BorderLayout.WEST);
            break;
      }
      
      this.addComponentListener( new ComponentAdapter()
         {  
            public void componentResized(ComponentEvent arg0)
            {
               handleResize();
            }
         });
      
      this.addMouseMotionListener( new MouseMotionAdapter() 
         {
            public void mouseMoved(MouseEvent e)
            {
               Point pt = e.getPoint();
               Drawer drawer = Drawer.getDrawerAtLocation(pt.x, pt.y);
               if (drawer != null)
               {
                  drawer.setCursor( Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
               }
               else
               {
                  setCursor(null);
               }
            }
         });
      
      this.addMouseListener( new MouseAdapter() 
         {
            public void mouseClicked(MouseEvent e)
            {
               Point pt = e.getPoint();
               Drawer clickedDrawer = Drawer.getDrawerAtLocation(pt.x, pt.y);
               if (clickedDrawer != null)
               {
                  clickedDrawer.togglePosition();
               }
            }
         });
   }
   
   /**
    * Set the alignment of the tab label. Choices are
    * SwingConstants.LEFT, SwingConstnts.CENTER, SwingConatants.RIGHT
    * @param i
    */
   public void setLabelAlignment(int alignment)
   {
      // default label alignment = centered; label padding is
      // evenly split on left & right of label
      this.labelPadding = 40;
      if (alignment == SwingConstants.LEFT)
      {
         this.labelOffset = this.labelPadding/2 - this.labelPadding/4;
      }
      else if (alignment == SwingConstants.RIGHT)
      {
         this.labelOffset = this.labelPadding/2 + this.labelPadding/4;
      }
      else
      {
         this.labelOffset = this.labelPadding/2;
      }
   }
   
   /**
    * Check if this coordinate is in this drawers' tab
    * @param x
    * @param y
    * @return
    */
   private boolean isPointInTab(int x, int y)
   {
      return this.tabRect.contains(x,y);
   }
   
   /**
    * Find an instance of a drawer with a tab containing 
    * location <code>x</code>, <code>y</code>
    * @param x
    * @param y
    * @return
    */
   public static Drawer getDrawerAtLocation(int x, int y)
   {
      // Look at all tabs
      Drawer match = null;
      Component[] comps = Workspace.instance.getComponents();
      for (int i=0;i<comps.length;i++)
      {
         if (comps[i] instanceof Drawer)
         {
            if ( ((Drawer)comps[i]).isPointInTab(x,y) )
            {
               match = (Drawer)comps[i];
               break;
            }
         }
      }
      return match;
   }
   
   /**
    * Get the contents of the drawer
    * @return
    */
   public JPanel getContents()
   {
      return this.content;
   }
   
   /**
    * Checks if a drawer is open
    * @return
    */
   public boolean isOpen()
   {
      return (this.state == Drawer.OPENED);
   }
   
   /**
    * Called when owner of the drawer is resized.
    * resize the drawer apprioriately
    */
   private void handleResize()
   {
      if (this.content.getWidth() == 0)
      {
         return;
      }
      
      // keep drawer in the correct position
      switch (tabLocation)
      {
         case RIGHT_TAB:
            if (this.state == CLOSED)
            {
               setLocation(-this.content.getWidth()+2,0);
            }
            else if (this.state == OPENED)
            {
               setLocation(0,0);
            }
            break;
         case LEFT_TAB:
            if (this.state == CLOSED)
            {
               setLocation(Workspace.instance.getWidth()-TAB_THICKNESS-2,0);
            }
            else if (this.state == OPENED)
            {
               setLocation(Workspace.instance.getWidth()-
                  this.content.getWidth() - TAB_THICKNESS-2,0);
            }
            break;
      }
      setVisible(true);
   }
      
   /**
    * Toggle drawer between open and closed
    */
   private void togglePosition()
   {
      if (this.state == CLOSED)
      {
         this.state = OPENING;
      }
      else
      {
         this.state = CLOSING;
      }
      
      Workspace.instance.moveToFront(this);
      this.delta = getWidth()-TAB_THICKNESS;
   }
   
   /**
    * Is the drawer in the process of opening or closing?
    * @return
    */
   public boolean isAnimating()
   {
      return (this.state == OPENING || this.state == CLOSING);
   }
   
   /**
    * Handle a tick from the main app thread.
    * Animate the drawer opening or closing
    */
   public void tick()
   {
      if (isAnimating())
      {
         int sign;
         if (this.state == OPENING)
            sign = 1;
         else
            sign = -1;
         
         // adjust sign for side
         if (this.tabLocation == Drawer.LEFT_TAB)
            sign *= -1;
         
         int positionDelta = MOVE_RATE;
         this.delta -= MOVE_RATE;
         if (this.delta < 0)
         {
            positionDelta += this.delta;
            if (this.state == OPENING)
            {
               this.state = OPENED;
            }
            else
            {
               this.state = CLOSED;
            }
         }
         
         Point pos = getLocation();
         setLocation(pos.x + sign*positionDelta, pos.y);
      }
   }
   
   public void paint(Graphics g)
   {  
      if (isVisible() == false)
         return;
      
      Graphics2D g2d = Ivanhoe.getGraphics2D(g);
      
      switch (this.tabLocation)
      {
         case RIGHT_TAB:
            drawRightTab(g2d);
            break;
         case LEFT_TAB:
            drawLeftTab(g2d);
            break;
      }
      
      this.paintChildren(g);
   }

   private void drawLeftTab(Graphics2D g2d)
   {  
      // set front for label drawing and calculate the tab size
      g2d.setFont(IvanhoeUIConstants.LARGE_FONT);
      Rectangle2D lblRect = IvanhoeUIConstants.LARGE_FONT.getStringBounds(
         this.label, g2d.getFontRenderContext());
      
      // init the tab dimensions
      int length, top, left;
      if (this.tabRect == null)
      {
         top = Drawer.lastLeftTabBottom+1;
         length = (int)lblRect.getWidth()+40;
         left = 10;
         this.tabRect = new Rectangle(left,top,TAB_THICKNESS,length);
         
         // adjust largest tab bottom
         if (top + length > Drawer.lastLeftTabBottom)
         {
            Drawer.lastLeftTabBottom = top+length;
         }
      }
      else
      {
         left = this.tabRect.x;
         top = this.tabRect.y;
         length = this.tabRect.height;
      }
      
      // draw the drawer tab
      g2d.setColor(IvanhoeUIConstants.tabColor);
      g2d.fillRoundRect(left-10,top, TAB_THICKNESS-1+10,length-2,20,20);
      g2d.setColor(IvanhoeUIConstants.tabColor.darker().darker());
      g2d.drawRoundRect(left-10, top, TAB_THICKNESS-1+10,length-2,17,17);
      g2d.drawRoundRect(left-10, top, TAB_THICKNESS-1+10,length-3,17,17);
      g2d.setColor(Color.DARK_GRAY);
      g2d.drawRoundRect(left-10, top, TAB_THICKNESS-1+10,length-1,20,20);
      
      // draw the label
      g2d.setColor(Color.BLACK);
      AffineTransform old = g2d.getTransform();
      g2d.transform(AffineTransform.getTranslateInstance(left+7, top+20));
      g2d.transform(AffineTransform.getRotateInstance(Math.PI/2.0));
      g2d.drawString(this.label, 0,0);
      g2d.setTransform(old);
   }

   private void drawRightTab(Graphics2D g2d)
   {      
      // set front for label drawing and calculate the tab size
      g2d.setFont(IvanhoeUIConstants.LARGE_FONT);
      Rectangle2D lblRect = IvanhoeUIConstants.LARGE_FONT.getStringBounds(
         this.label, g2d.getFontRenderContext());
      
      // init tab dimensions
      int length, top, left;
      if (this.tabRect == null)
      {
         length = (int)lblRect.getWidth()+this.labelPadding;
         top = Drawer.lastRightTabBottom;
         left = getWidth() - TAB_THICKNESS;
         this.tabRect = new Rectangle(left,top,TAB_THICKNESS,length);
         
         // adjust largest tab bottom
         if (top + length > Drawer.lastRightTabBottom)
         {
            Drawer.lastRightTabBottom = top+length;
         }
      }
      else
      {
         left = this.tabRect.x;
         top = this.tabRect.y;
         length = this.tabRect.height;
      }
      
      // draw the drawer tab (-10 & +10 to offset the rect so part is
      // under the content
      g2d.setColor(IvanhoeUIConstants.tabColor);
      g2d.fillRoundRect(left-10, top, TAB_THICKNESS-1+10,length-2,20,20);
      g2d.setColor(IvanhoeUIConstants.tabColor.darker().darker());
      g2d.drawRoundRect(left-10, top, TAB_THICKNESS-1+10,length-2,17,17);
      g2d.drawRoundRect(left-10, top, TAB_THICKNESS-1+10,length-3,17,17);
      g2d.setColor(Color.DARK_GRAY);
      g2d.drawRoundRect(left-10, top, TAB_THICKNESS-1+10,length-1,20,20);
      
      // draw the label
      g2d.setColor(Color.BLACK);
      AffineTransform old = g2d.getTransform();
      g2d.transform(AffineTransform.getTranslateInstance(
         left+5, top+this.labelOffset));
      g2d.transform(AffineTransform.getRotateInstance(Math.PI/2.0));
      g2d.drawString(this.label, 0,0);
      g2d.setTransform(old);
   }
}
