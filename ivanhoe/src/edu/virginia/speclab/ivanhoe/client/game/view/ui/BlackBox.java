/*
 * Created on Sep 28, 2004
 */
package edu.virginia.speclab.ivanhoe.client.game.view.ui;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.Box;
import javax.swing.border.LineBorder;

/**
 * @author Nick
 */
public class BlackBox extends Box
{	
	protected void setColor(Graphics g)
	{
        // For debugging purposes, set this to blue so we can tell them apart
        // from the background
        //g.setColor(Color.BLUE);
		g.setColor(Color.BLACK);
	}
	
	public BlackBox(int layout)
    {
        super(layout);
        this.setBorder(LineBorder.createGrayLineBorder());
        this.setOpaque(true);
    }
    
    public void paint( Graphics g )
    {
        // draw black background
    	setColor(g);
        
        int x = 0;
        int y = 0;
        int w = getWidth();
        int h = getHeight();
        g.fillRect(x,y,w,h);

        super.paint(g);
    }
}
