/*
 * Created on Jul 8, 2004
 */
package edu.virginia.speclab.ivanhoe.client.game.view.navigator;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.Area;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;

import edu.virginia.speclab.ivanhoe.client.Ivanhoe;
import edu.virginia.speclab.ivanhoe.client.game.view.Workspace;

/**
 * The LabelManager takes requests for labels through the <code>addLabel</code>
 * method, arranges them so that they don't overlap (hopefully) and draws them.
 * 
 * @author benc
 */

public class LabelManager 
{
	public static final short LEFT_ORIGIN = 0;
    public static final short RIGHT_ORIGIN = 1;
    public static final short CENTER_ORIGIN = 2;
    
	private static final Color LABEL_COLOR = Color.white;
	private static final int MAX_ITERATIONS = 300;
	
	private Vector labels;
	private Graphics graphics;
	private DiscourseFieldNavigator navigator;
	private boolean newGraphicsNeeded;
	
	public LabelManager(DiscourseFieldNavigator nav)
	{ 
		nav.addComponentListener( new ComponentAdapter()
		           {  
		              public void componentResized(ComponentEvent arg0)
		              {
		                 handleWindowResize();
		              }
		           });
		
		graphics = nav.getGraphics();
		navigator = nav;
		labels = new Vector();	
	}
	
	/**
	 *  Add a label to be painted on the visualization. 
	 * @param labelText The text of the label.
	 * @param x The label's X coordinate
	 * @param y The label's Y coordinate
	 * @param wrapWidth The threshold width at which to wrap the text 
	 * @param font The font to render the text 
	 * @param color The color to render the text
	 * @param origin Can be LEFT_ORIGIN, RIGHT_ORIGIN, or CENTER_ORIGIN
	 * @return The assembled text label object
	 */
	public Object addLabel(String labelText, int x, int y, int wrapWidth, Font font, Color color, short origin )
	{
		Point location = new Point(x, y);
		if (newGraphicsNeeded)
		{
			graphics = navigator.getGraphics();
		}
		
		TextLabel lbl = new TextLabel(graphics, labelText, location, wrapWidth, font, color, origin );
		labels.add(lbl);
		return lbl;
	}
	
	public int determineLabelWidth( String text, Font font, int wrapWidth )
	{
		Graphics2D g2 = (Graphics2D) navigator.getGraphics();
		
		if( text.length() == 0 )
		{
			return 0;
		}
		
		AttributedString as = new AttributedString(text);
		
		as.addAttribute(TextAttribute.FONT, font );
		AttributedCharacterIterator aci = as.getIterator();
		
		FontRenderContext frc = g2.getFontRenderContext();
		LineBreakMeasurer lbm = new LineBreakMeasurer(aci,frc);

		float width = 0;
		
		// break the text apart into lines 
		while( lbm.getPosition() < aci.getEndIndex() )
		{
			TextLayout textLayout;
			if (wrapWidth == 0)
			{
				textLayout = lbm.nextLayout(Integer.MAX_VALUE);	
			} else 
			{
				textLayout = lbm.nextLayout(wrapWidth);
			}
		    
		    // calculate total label width and height
		    float advance = textLayout.getAdvance();
		    if( advance > width ) width = advance;
		}
		
		width += 2 * TextLabel.X_MARGIN;
		
		return (int)width;
	}
	
	public static Area getAreaOfLabel(Object label)
	{
		if ( !(label instanceof TextLabel) )
		{
			throw new ClassCastException("label is not of type TextLabel from LabelManager");
		}
		
		return ((TextLabel)label).getArea();
	}
	
	public boolean removeLabel(Object label)
	{
		return labels.remove(label);
	}
	
	public void paint(Graphics g, double aspectW, double aspectH)
	{
		updateLayout();
		
		Graphics2D g2 = Ivanhoe.getGraphics2D(g);
		g2.setColor(LABEL_COLOR);
		for (Iterator i = labels.iterator(); i.hasNext(); )
		{
			TextLabel lbl = (TextLabel)i.next();
			lbl.paint(g, aspectW, aspectH);
		}
	}
	
	public void clear()
	{
		labels.clear();
	}
	
	protected void updateLayout()
	{
		// the whole iteration thing is so that we never actually hang
		int i=0;
		while ( iterateUpdate(i) && i < MAX_ITERATIONS) ++i; 
	}
	  
	/**
	 * 
	 * @return whether or not there was a collision
	 */
	protected boolean iterateUpdate(int iteration) 
	{
		int numLabels = labels.size();
		int wsWidth = Workspace.instance.getNavigator().getWidth();
		int wsHeight = Workspace.instance.getNavigator().getHeight();
		
		for (int i=0; i<numLabels; ++i)
		{
			TextLabel label1 = (TextLabel)labels.get(i);
			Rectangle r1 = label1.getRectangle();
			
			for (int j=i+1; j<numLabels; ++j)
			{
				TextLabel label2 = (TextLabel)labels.get(j);
				Rectangle r2 = label2.getRectangle();
				
				if ( r1.intersects(r2) )
				{
					double halfXOverlap = r1.intersection(r2).width / 2;
					
					/* Add more space the longer we iterate for.
					 *   In a densely packed screen, this will just cause more
					 *   collisions, but we'll assume for the time being that
					 *   collision resolution happens in areas that are only
					 *   locally crowded, and so pushing labels outwards more
					 *   will spread all the labels out into the less densely
					 *   populated screen space. 
					 */
					halfXOverlap += Math.pow(1.01, iteration);
					
					if( (r2.getCenterX() - r1.getCenterX()) < 0 ) {
						halfXOverlap = -halfXOverlap;
					}
					
					label1.location.x -= halfXOverlap;
					label2.location.x += halfXOverlap;
					
					label1.constrainTo(wsWidth, wsHeight);
					label2.constrainTo(wsWidth, wsHeight);
								
					return true;
				}
			}
		}
		return false;
	}
	
	private void handleWindowResize()
    {
		// put this off 'til we know that the navigator has updated
        newGraphicsNeeded = true;
    }
	
	private class TextLabel
	{
		public Point location;
		
		private double width;
		private double height;
		private Color textColor;
		private LinkedList lineList;
		private static final int X_MARGIN = 3;
		private static final int Y_MARGIN = 2;
		
		/** prepares a multi-line text label at <code>loc</code> in the specified font
	     * 
	     * @param g graphics context
	     * @param text text to render
	     * @param loc position on screen
	     * @param wrapWidth width at which to wrap (0 for no wrap)
	     * @param font font in which to render
	     * @param origin enumerant to specify how label is alligned with respect to x and y values
	     */
		public TextLabel(Graphics g, String text, Point loc, int wrapWidth, Font font, Color color, short origin )
		{
			Graphics2D g2 = Ivanhoe.getGraphics2D(g);
		    		
    		lineList = new LinkedList();
			location = loc;
			textColor = color;

	    	if( text.length() == 0 )
	    	{
	    		return;
	    	}
	    	
	    	AttributedString as = new AttributedString(text);
	    	
	        as.addAttribute(TextAttribute.FONT, font );
	        AttributedCharacterIterator aci = as.getIterator();
	        
	        FontRenderContext frc = g2.getFontRenderContext();
	        LineBreakMeasurer lbm = new LineBreakMeasurer(aci,frc);
	        
	        // break the text apart into lines 
	        while( lbm.getPosition() < aci.getEndIndex() )
	        {
	        	TextLayout textLayout;
	        	if (wrapWidth == 0)
	        	{
	        		textLayout = lbm.nextLayout(Integer.MAX_VALUE);	
	        	} else {
	        		textLayout = lbm.nextLayout(wrapWidth);
	        	}
	            
	            // calculate total label width and height
	            float advance = textLayout.getAdvance();
	            if( advance > width ) width = advance;
	            height += textLayout.getAscent() + textLayout.getDescent() +
						textLayout.getLeading();

	            // add line to the list
	            lineList.add(textLayout);
	        }
	        
	        // offset start of text from the center of the label
	        switch(origin)
			{
	        case LEFT_ORIGIN:
	        	break;
	    	case RIGHT_ORIGIN:
	    		location.x += width;
	    		location.y += height;
	    		break;
			case CENTER_ORIGIN:
				location.x -= width / 2;
		        location.y -= height / 2;
		        break;
		    default:
		    	throw new RuntimeException("Invalid constant passed for origin");
			}
	        
	        location.x -= X_MARGIN;
	        location.y -= Y_MARGIN;
	        width += 2 * X_MARGIN;
	        height += 2 * Y_MARGIN;
	        
//	        int wsWidth = Workspace.instance.getNavigator().getWidth();
//			int wsHeight = Workspace.instance.getNavigator().getHeight();
//			constrainTo(wsWidth, wsHeight);
		}
		
		public void paint(Graphics g, double aspectW, double aspectH)
		{
			Graphics2D g2 = Ivanhoe.getGraphics2D(g);
			
			// locations (x,y) of each text line
			float x = location.x;
			float y = location.y;
			
			g2.setPaint(textColor);
			
			for( Iterator i = lineList.iterator(); i.hasNext(); )
	        {			    
	        	// TODO: actually make the alignment align every line of text			    
	            TextLayout textLayout = (TextLayout) i.next();	            
	            y += textLayout.getAscent();
	            textLayout.draw(g2,x+X_MARGIN,y+Y_MARGIN);	            
	            y += textLayout.getDescent() + textLayout.getLeading();
	        }	
		}
		
		public void constrainTo(int wsWidth, int wsHeight)
		{
			if (location.x < 0)
			{
				location.x = 0;
			}
			else if (location.x + width > wsWidth)
			{
				location.x = (int)(wsWidth - width);
			}

			if (location.y < 0)
			{
				location.y = 0;
			}
			else if (location.y + height > wsHeight)
			{
				location.y = (int)(wsHeight - height);
			}
		}

		public Area getArea()
		{
	        return new Area(getRectangle());
		}
		public Rectangle getRectangle()
		{
			return new Rectangle(location.x, location.y, (int)width, (int)height);
		}
		public double getWidth() {
			return width;
		}
		public double getHeight() {
			return height;
		}
	}
}
