/*
 * Created on Oct 7, 2004
 * 
 * This is a BlackBox class that works within graphics contexts where the box
 * is not at the origin, such as boxes within the navigator
 */
package edu.virginia.speclab.ivanhoe.client.game.view.ui;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.Rectangle;

import edu.virginia.speclab.ivanhoe.client.Ivanhoe;

/**
 * @author benc
 */
public class FloatingBlackBox extends BlackBox {

	/**
	 * @param layout Orientation e.g. BoxLayout.X_AXIS
	 * @param drawTop Draw the top border
	 * @param drawLeft Draw the left border
	 * @param drawRight Draw the right border
	 * @param drawBottom Draw the bottom border
	 */
	public FloatingBlackBox(int layout) 
	{
		super(layout);
	}
	
    public void paint( Graphics g )
    {
    	Graphics2D g2 = Ivanhoe.getGraphics2D(g);
        AffineTransform standardTransform = g2.getTransform();
        AffineTransform floatingTransform = new AffineTransform();
        floatingTransform.translate(getX(), getY());
        g2.transform(floatingTransform);
    	
        g2.setStroke(new BasicStroke(1.0f));
        super.paint(g);
        
        g2.setTransform(standardTransform);
    }
    
    public boolean contains(Point p)
    {
    	Rectangle r = new Rectangle(getX(), getY(), getWidth(), getHeight()); 
    	return r.contains(p);
    }
}
