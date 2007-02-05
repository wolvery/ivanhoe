/*
 * Created on Jul 20, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package edu.virginia.speclab.ivanhoe.client.game.view.navigator.customcolors;

import java.awt.Color;
import java.awt.Component;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.Arc2D;

import javax.swing.Icon;

import edu.virginia.speclab.ivanhoe.client.Ivanhoe;


public class ColorArc implements Icon
{
    public Color fillColor, strokeColor;
    
    public ColorArc( Color fill, Color stroke )
    {
        fillColor = fill;            
        strokeColor = stroke;
    }        

    public void paintIcon(Component arg0, Graphics g, int iconX, int iconY)
    {
		Graphics2D g2 = Ivanhoe.getGraphics2D(g);
		
		double x = iconX-CustomColorsWindow.ARC_SIZE/2+5;
		double y = iconY+5;
		double w = CustomColorsWindow.ARC_SIZE-2;
		double h = w;
		
		Shape arc = new Arc2D.Double(x,y,w,h,0,90,Arc2D.PIE);
		
		Paint arcPaint = new GradientPaint(
				(int) x,
				(int) y, 
				fillColor,
				(int) (x+w),
				(int) (y+h), 
				Color.black,
				true);
		
		g2.setStroke(CustomColorsWindow.arcStroke);
		g2.setPaint(arcPaint);
		g2.fill(arc);
		g2.setPaint(strokeColor);
		g2.draw(arc);		
    }
    
    public int getIconHeight() { return (CustomColorsWindow.ARC_SIZE/2)+10; }
    public int getIconWidth() { return (CustomColorsWindow.ARC_SIZE/2)+10; }
}