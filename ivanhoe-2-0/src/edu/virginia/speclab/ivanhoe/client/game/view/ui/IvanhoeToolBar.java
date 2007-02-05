/*
 * Created on Sep 27, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package edu.virginia.speclab.ivanhoe.client.game.view.ui;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JToolBar;

/**
 * @author Nick
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class IvanhoeToolBar extends JToolBar
{
    private Color background;
    
    public void setBackground( Color backColor )
    {
        background = backColor;
    }

    public void paint( Graphics g )
    {
        g.setColor(background);
        g.fillRect(0,0,getWidth(),getHeight());
        super.paintChildren(g);
        super.paintBorder(g);
    }
    
}
