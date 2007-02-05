/*
 * split from DocumentGutter on Jun 23, 2004
 * 
 */

package edu.virginia.speclab.ivanhoe.client.game.view.document;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import edu.virginia.speclab.ivanhoe.client.Ivanhoe;
import edu.virginia.speclab.ivanhoe.client.IvanhoeUIConstants;
import edu.virginia.speclab.ivanhoe.client.game.view.Workspace;

public class InfoPanel extends JPanel
{
   private static final int MAX_WIDTH = 150;
   private JLabel lbl;
   
   public InfoPanel(String content)
   {
      super();
      this.setOpaque(false);
      
      setLayout(new BorderLayout());
      lbl = new JLabel(content, SwingConstants.CENTER);
      lbl.setFont(IvanhoeUIConstants.SMALL_FONT);
      add(lbl, BorderLayout.CENTER);
      Dimension dim = lbl.getPreferredSize();
      setSize(dim.width+10,dim.height+10);
      lbl.setBorder(new EmptyBorder(5, 8, 5, 5));
   }

   public void setMsg(String content)
   {
      lbl.setText(content);
      Dimension dim = lbl.getPreferredSize();
      dim.width += 10;
      dim.height += 10;
      if (dim.width > MAX_WIDTH) {
         dim.width = MAX_WIDTH;
      }
      setSize(dim.width,dim.height);
   }
   
   public void paint(Graphics g)
   {
      Graphics2D g2d = Ivanhoe.getGraphics2D(g);
      g2d.setColor(IvanhoeUIConstants.WHITE );
      g2d.fillRect(0,0,getWidth()-1,getHeight()-1);
      g2d.setColor(IvanhoeUIConstants.tipColor.darker().darker());
      g2d.drawRect(2,2,getWidth()-5,getHeight()-5);
      g2d.setColor(Color.WHITE);
      g2d.drawRect(1,1,getWidth()-3,getHeight()-3);
      
      super.paint(g);
   }

   public void setLocation(Component c, int x, int y)
   {
       Point p = SwingUtilities.convertPoint(c,x,y,Workspace.instance.getNavigator());
       setLocation(p.x,p.y);
   }
   
   
   public void setLocation(int x, int y)
   {
   	  int xSpace = Workspace.instance.getNavigator().getWidth() - (x + getWidth());
      if ( xSpace < 0 )
      {
         x += xSpace;
      }
      
      int ySpace = Workspace.instance.getNavigator().getHeight() - (y + getHeight());
      if ( ySpace < 0 )
      {
         y += ySpace;
      }
      
      super.setLocation(x,y);
   }
}