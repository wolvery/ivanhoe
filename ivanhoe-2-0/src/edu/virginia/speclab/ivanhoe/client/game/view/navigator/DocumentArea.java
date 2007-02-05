/*
 * Created on Aug 24, 2004
 */
package edu.virginia.speclab.ivanhoe.client.game.view.navigator;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;

import edu.virginia.speclab.ivanhoe.client.Ivanhoe;
import edu.virginia.speclab.ivanhoe.client.IvanhoeUIConstants;
import edu.virginia.speclab.ivanhoe.client.game.model.discourse.MoveEvent;
import edu.virginia.speclab.ivanhoe.client.game.view.Workspace;
import edu.virginia.speclab.ivanhoe.client.game.view.navigator.IDiscourseFieldNavigatorListener;
import edu.virginia.speclab.ivanhoe.client.util.ResourceHelper;

/**
 * @author Nick
 */
public class DocumentArea implements IDiscourseFieldNavigatorListener
{
    public boolean highlighted;
    public final double weight;
    private String documentName;
    private final Date createdTime, publishedTime;
    private double startAngle, arcLength;
    private final boolean inProgress;
    private static final Image editableImage = ResourceHelper.getImage("res/icons/little-pencil.gif");

    private Area documentArea;
    private Area documentLabelArea;
    
    private Collection participants; // a list of the players who have acted in this
                                     // text up to this point in time  
        
    private DiscourseFieldNavigator navigator;

    public static final int MINIMUM_ARC_LENGTH = 3;
    
    private static final int MIN_FONT_SIZE = 12;
    private static final int MAX_FONT_SIZE = 20;
    private static final Color areaColor = IvanhoeUIConstants.GRAY;
    private Color labelColor = Color.WHITE;
    private static final double DOCNAME_DISTANCE = DiscourseFieldCircle.OPTIMAL_RADIUS + 10;
    private static final double RADIAL_TEXT_MARGIN = 20;
    private static final int MAX_LINES_TO_WRAP = 2;    
    
    public class RadialTextLabel
    {
        private Point.Double origin;
        private double distance, angle;
        private LinkedList textLines;
        private Color textColor;
        
        public RadialTextLabel( Graphics g, String text, Font font, Color color, Point2D.Double center, Dimension windowSize, double radius, double angle )
        {
            // size to wrap the label text at
            RadialTextLabelLength textLength = new RadialTextLabelLength( windowSize, angle, radius );
            int width = textLength.getLabelLength();
            
            // check for empty doc name
        	if (documentName.length() == 0)
        	{
        	    documentName = "<untitled work>";
        	}

    		this.origin = center;
    		this.distance = radius;
			this.angle = angle;
			this.textColor = color;
			this.textLines = constructLineList(g,text,font,width);		
		}
        
        private LinkedList constructLineList( Graphics g, String text, Font font, int width )
        {            
            Graphics2D g2 = Ivanhoe.getGraphics2D(g);
            
		    LinkedList lineList = wrapText(g2,text,font,width);
		    
		    // if there is more than one line and not enough arc space, or if the number
		    // of lines exceeds the max allowed, cut the line with an '...'
		    if( ( lineList.size() > 1 && (DocumentArea.MINIMUM_ARC_LENGTH * 2) > DocumentArea.this.getArcLength() ) ||
		        ( lineList.size() > MAX_LINES_TO_WRAP ) )
		    {		        
		        TextLayout singleLine = cutText(g2,text,font,width);
		        
		        if( singleLine != null )
		        {
		            lineList = new LinkedList();
		            lineList.add(singleLine);
		        }
		        else lineList = null;
		    }
		    
		    return lineList;
        }

        // how long is an elipse "..." at this point size and context in pixels?
        private int calculateWidthOfElipse( FontRenderContext frc, Font font )
        {
			AttributedString elipse = new AttributedString("...");			
		    elipse.addAttribute( TextAttribute.FONT, font );
		    AttributedCharacterIterator aciElipse = elipse.getIterator();
		    LineBreakMeasurer lbmElipse = new LineBreakMeasurer(aciElipse,frc);
		    
		    TextLayout elipseTextLayout = lbmElipse.nextLayout(Integer.MAX_VALUE);
		    Rectangle2D rect = elipseTextLayout.getBounds();
		    
		    return (int) rect.getWidth();            
        }
        
        private TextLayout performCut( String text, Font font, FontRenderContext frc, int wrapIndex, boolean ellipse )
        {
            String cutText;
            
	        if( ellipse )
	        {
	            cutText = text.substring(0,wrapIndex) + "...";
	        }
	        else
	        {
	            cutText = text.substring(0,wrapIndex);
	        }
	        
	        AttributedString as = new AttributedString(cutText);			
		    as.addAttribute( TextAttribute.FONT, font );
		    AttributedCharacterIterator aci = as.getIterator();
		    LineBreakMeasurer lbm = new LineBreakMeasurer(aci,frc);
		    return lbm.nextLayout(Integer.MAX_VALUE);
        }
        
        private TextLayout cutText(  Graphics2D g2, String text, Font font, int width )
        {
			AttributedString as = new AttributedString(text);			
		    as.addAttribute( TextAttribute.FONT, font );

            FontRenderContext frc = g2.getFontRenderContext();

            int elipseWidth = calculateWidthOfElipse(frc,font);
            
            AttributedCharacterIterator aci = as.getIterator();
		    LineBreakMeasurer lbm = new LineBreakMeasurer(aci,frc);

		    TextLayout textLayout = null;
             
            if( width > elipseWidth )
            {
			    // get the first line of text
                textLayout = lbm.nextLayout(width-elipseWidth);
			    int wrapIndex = lbm.getPosition();
			    	    
			    // if there is more text, cut the string and insert "..." 
			    if( wrapIndex < aci.getEndIndex() )
			    {
			        textLayout = performCut( text, font, frc, wrapIndex, true );
			    }
            }
            else
            {
                // first line of text without room for ellipses
                textLayout = lbm.nextLayout(width);
			    int wrapIndex = lbm.getPosition();
			    
                textLayout = performCut( text, font, frc, wrapIndex, false );
            }

		    return textLayout;
        }
        
        private LinkedList wrapText( Graphics2D g2, String text, Font font, int wrapWidth )
        {
            LinkedList lines = new LinkedList();

			AttributedString as = new AttributedString(text);			
		    as.addAttribute( TextAttribute.FONT, font );
            AttributedCharacterIterator aci = as.getIterator();
		    
		    FontRenderContext frc = g2.getFontRenderContext();
		    LineBreakMeasurer lbm = new LineBreakMeasurer(aci,frc);
		    
		    // break the text apart into lines
		    while( lbm.getPosition() < aci.getEndIndex() )
		    {
		        // break out next line
		    	TextLayout textLayout = lbm.nextLayout(wrapWidth);
		    	
		        // add line to the list
		        lines.add(textLayout);
		    }            
         
            return lines;
        }
        
    	public void paint(Graphics g, double constrainingAspect)
		{
    	    if( textLines == null ) return;
    	    
			Graphics2D g2 = Ivanhoe.getGraphics2D(g);
			AffineTransform originalTransform = g2.getTransform();
			if (highlighted)
			{
				g2.setPaint(Color.WHITE);
			}
			else
			{
				g2.setPaint(textColor);
			}
			
			float iconOffset = 0;
			if (inProgress)
			{
				iconOffset = editableImage.getHeight(navigator) * 1.5f;
				
				AffineTransform iconTransform = new AffineTransform();
				iconTransform.rotate(angle, origin.x, origin.y);
				iconTransform.translate(
						origin.x + distance,
						origin.y - iconOffset / 2);
				
				g2.drawImage(editableImage, iconTransform, navigator);
			}
			
			//double dfRadius = DiscourseFieldCircle.OPTIMAL_RADIUS * constrainingAspect;
			//double radPerPixel = Math.toRadians(180.0 / (dfRadius * Math.PI));
			documentLabelArea = new Area();
			
			double y = 0;
			for( Iterator i = textLines.iterator(); i.hasNext(); )
	        {			    		    
	            TextLayout textLayout = (TextLayout) i.next();
	            
	            AffineTransform textTransform = new AffineTransform();
	            
	            
	            
	            textTransform.rotate(angle,origin.x,origin.y);
	            
	            // if we are on the left side of the circle, flip the text so it isn't
	            // upside down            
	            if( angle > Math.PI*0.5 && angle < Math.PI*1.5 )
	            {
	                y -= textLayout.getAscent() / 2.0;
	                textTransform.translate(
	                		origin.x+distance+textLayout.getAdvance()+iconOffset,
							origin.y+y);
	                textTransform.rotate(Math.PI);
	                y -= textLayout.getDescent() + textLayout.getLeading() + textLayout.getAscent() / 2.0;
	            }
	            else
	            {
	                y += textLayout.getAscent() / 2.0; // center it
	                textTransform.translate(
	                		origin.x+distance+iconOffset,
							origin.y+y);
	                y += textLayout.getDescent() + textLayout.getLeading() + textLayout.getAscent() / 2.0;
	            }
	            
	            Rectangle2D r = textLayout.getBounds();
	            Area lineArea = new Area(new Rectangle(
	            		(int)r.getX(), (int)r.getY(), 
						(int)r.getWidth(), (int)r.getHeight()));
	            lineArea.transform(textTransform);
	            documentLabelArea.add(lineArea);
	            
	            g2.setTransform(textTransform);	            
	            textLayout.draw(g2,0,0);
	        }	
			
			g2.setTransform(originalTransform);
		}

        private class RadialTextLabelLength
        {
            private double labelLength;
            
            public RadialTextLabelLength( Dimension windowSize, double centerAngle, double radius )
            {
                labelLength = determineTextWidth( windowSize, centerAngle, radius );                
            }
            
            // if it intersects on a corner, try 2 lines and pick the closer intercept
            private Point2D.Double determineIntercept( int outcode, Rectangle2D.Double box, Point2D.Double point )
            {                               
                Point2D.Double interceptPoint = new Point2D.Double(0,0);
                
                switch(outcode)
                {
                	case Rectangle2D.OUT_TOP:        	    
                	    interceptPoint.x = point.x;
                		interceptPoint.y = box.y;
                	    break;  
                	    
                	case Rectangle2D.OUT_BOTTOM:        	    
                	    interceptPoint.x = point.x;
                		interceptPoint.y = box.y + box.height;
                	    break;
                	    
                	case Rectangle2D.OUT_LEFT:
                	    interceptPoint.x = box.x;
                		interceptPoint.y = point.y;
                	    break;
                	    
                	case Rectangle2D.OUT_RIGHT:
                	    interceptPoint.x = box.x + box.width;
                		interceptPoint.y = point.y;
                	    break;
                	
                	case Rectangle2D.OUT_LEFT | Rectangle2D.OUT_TOP :
                	{
                	    Point2D.Double point1 = determineIntercept( Rectangle2D.OUT_LEFT, box, point );
                    	Point2D.Double point2 = determineIntercept( Rectangle2D.OUT_TOP, box, point );
                    	interceptPoint = comparePoints(point1,point2);
                    	break;
                	}
                	    
                	case Rectangle2D.OUT_LEFT | Rectangle2D.OUT_BOTTOM :
                	{
                	    Point2D.Double point1 = determineIntercept( Rectangle2D.OUT_LEFT, box, point );
                		Point2D.Double point2 = determineIntercept( Rectangle2D.OUT_BOTTOM, box, point );
                		interceptPoint = comparePoints(point1,point2);
                		break;
                	}
                		
                	case Rectangle2D.OUT_RIGHT | Rectangle2D.OUT_TOP :
                	{
                	    Point2D.Double point1 = determineIntercept( Rectangle2D.OUT_RIGHT, box, point );
                		Point2D.Double point2 = determineIntercept( Rectangle2D.OUT_TOP, box, point );
                		interceptPoint = comparePoints(point1,point2);
                		break;
                	}
                	
                	case Rectangle2D.OUT_RIGHT | Rectangle2D.OUT_BOTTOM :
                	{
                	    Point2D.Double point1 = determineIntercept( Rectangle2D.OUT_RIGHT, box, point );
                		Point2D.Double point2 = determineIntercept( Rectangle2D.OUT_BOTTOM, box, point );
                		interceptPoint = comparePoints(point1,point2);
                		break;
                	}
                		
                }
                
                return interceptPoint;        
            }
        
            // determine the point at which a ray from the center of box to point intercepts box
            private Point2D.Double determineIntercept( Rectangle2D.Double box, Point2D.Double point )
            {
                int outcode = box.outcode(point.x,point.y);
                return determineIntercept( outcode, box, point );
            }
        
            private Point2D.Double comparePoints( Point2D.Double point1, Point2D.Double point2 )
            {
            	double distanceFromPoint1 = Point2D.distance(point1.x, point1.y,0,0);
            	double distanceFromPoint2 = Point2D.distance(point2.x, point2.y,0,0);
            	if( distanceFromPoint1 < distanceFromPoint2 )
            	{
            	    return point1;
            	}
            	else
            	{
            	    return point2;
            	}        
            }
        
            /**
             * Calculate the optimal length for the text that will fit in the window at this particular angle.
             * 
             * @param windowSize
             * @param centerAngle
             * @param radius
             * @return
             */
            private double determineTextWidth( Dimension windowSize, double centerAngle, double radius )
            {                        
                // determine min radius to enclose window
                double outsideRadius = Point2D.distance(windowSize.width/2,windowSize.height/2,0,0);                            

                // create a point this circle at the specificied angle
                Point2D.Double outsidePoint = new Point2D.Double( Math.cos(centerAngle) * outsideRadius,
                        										  Math.sin(centerAngle) * outsideRadius  );
                        
                // define the window rectangle     
                Rectangle2D.Double box = new Rectangle2D.Double( -windowSize.width/2.0, -windowSize.height/2.0,
                        										  windowSize.width, windowSize.height );
                        
                // determine point at edge of window 
                Point2D.Double interceptPoint = determineIntercept( box, outsidePoint );
                        
                // calculate distance from edge of window to center         
                double distanceFromIntercept = Point2D.distance(interceptPoint.x,interceptPoint.y,0,0);
                
                // subtract radius of discourse field circle
                double textWidth = distanceFromIntercept - radius - RADIAL_TEXT_MARGIN;
                
                // if less than zero, just make it zero
                if( textWidth < 0 ) textWidth = 0;
                
                return textWidth;
            }
            
            /**
             * @return Returns the labelLength.
             */
            public int getLabelLength()
            {
                return (int)labelLength;
            }
        }
    }
    
    public DocumentArea( DiscourseFieldNavigator navigator, String docName,
    		Date createdTime, Date publishedTime,
    		Collection participants, double initialWeight, boolean inProgress)
    {
        this.navigator = navigator; 
        highlighted = false;
        weight = initialWeight;
        documentName = docName;
        startAngle = 0.0;
        arcLength = 0.0;
        documentArea = new Area();
        documentLabelArea = new Area();
        this.participants = participants;
        this.inProgress = inProgress;
        this.createdTime = createdTime;
        this.publishedTime = publishedTime;
        
        
        navigator.addListener(this);
        navigator.getDiscourseFieldCircle().addListener(this);
        navigator.getDiscourseFieldCircle().dirtySelectedPlayer();
    }
    
    private Area constructDocumentArea( double constrainingAspect )
    {
        Dimension windowSize = navigator.getSize();

        // create a rectangle the size of the navigator area.
        Rectangle2D.Double rect = new Rectangle2D.Double(0,0,windowSize.width,windowSize.height);
                
        // create an arc that encompasses the rectangle
        double centerX = windowSize.width/2.0;
        double centerY = windowSize.height/2.0;
        double radius = Math.sqrt( (centerX*centerX) + (centerY*centerY) ) + 5;
        
        double x = centerX - radius;
        double y = centerY - radius;
        double w = radius * 2.0;
        double h = w;

        Shape arc = new Arc2D.Double(x,y,w,h,startAngle,arcLength,Arc2D.PIE);
       
        // create the discourse field circle
        double dfRadius = DiscourseFieldCircle.OPTIMAL_RADIUS * constrainingAspect;
        
        double dfX = centerX - dfRadius;
        double dfY = centerY - dfRadius;
        double dfW = dfRadius * 2.0;
        double dfH = dfW;
        
        Shape circle = new Ellipse2D.Double(dfX,dfY,dfW,dfH);
                    
        // The document area is the intersection of these two shapes
        Area arcArea = new Area(arc);
        Area docArea = new Area(rect);
        Area circleArea = new Area(circle);
        docArea.intersect(arcArea);
        docArea.subtract(circleArea);
                    
        return docArea;
    }
    
    public boolean hitTest( Point p )
    {
        return documentArea.contains(p);
    }        
    
    public DocumentArc getDocumentArc()
    {
        return new DocumentArc( startAngle, arcLength );
    }
    
    private RadialTextLabel constructDocumentNameLabel( Color docLabelColor, 
    		Graphics g, double constrainingAspect )
    {
        // calculate the appropriate font size
        int pointSize;
        if( weight+MIN_FONT_SIZE < MAX_FONT_SIZE )
            pointSize = (int)weight+MIN_FONT_SIZE;
        else
            pointSize = MAX_FONT_SIZE;

        // create the font
        Font font = new Font("Verdana", Font.PLAIN, pointSize);

        // get the size of the window
        Dimension windowSize = navigator.getSize();

        // find center of window
        Point2D.Double centerPoint = new Point2D.Double( windowSize.width/2, windowSize.height/2 );
        
        // angle from the center
        double midAngle = Math.toRadians(startAngle + (arcLength/2.0));        
    	 
        // distance to start of text from center of circle
        double radius = DOCNAME_DISTANCE * constrainingAspect;
        
        // center angle in radians, adjusted for text orientation
        double centerAngleRadians = -midAngle;
            	    	    	    	
    	// create the label
    	RadialTextLabel label = new RadialTextLabel( g, documentName, font, docLabelColor, centerPoint, windowSize, radius, centerAngleRadians );
        
        return label;
    }
    
    public void paintDocumentArea(Graphics g, double constrainingAspect )
    {
        Graphics2D g2 = Ivanhoe.getGraphics2D(g);

        documentArea = constructDocumentArea(constrainingAspect);    

        // if highlighted, fill the area with a gradient
        if( highlighted == true )
        {
        	Rectangle box = documentArea.getBounds();
            GradientPaint areaPaint = new GradientPaint(box.x,box.y, areaColor, 
                                         (box.x+box.width),(box.y+box.height), Color.black, true);
            g2.setPaint(areaPaint);
            g2.fill(documentArea);
        }            
    }
        
    public void paintDocumentLabel(Graphics g, double constrainingAspect )
    {
        RadialTextLabel label = constructDocumentNameLabel(labelColor, g, constrainingAspect);
        label.paint(g, constrainingAspect);
    }

    public void selectedRoleChanged( String roleName, MoveEvent currentEvent )
    {
    	if (participants.contains(roleName))
    	{
    		labelColor = this.navigator.getRoleManager().getRole(roleName).getFillPaint();
    	}
    	else
    	{
    		labelColor = Color.LIGHT_GRAY;
    	}
    }
    
    /**
     * @return the area corresponding to the area under the document label
     */
    public Area getDocumentLabelArea()
    {
    	return documentLabelArea;
    }
    /**
     * @return Returns the arcLength.
     */
    public double getArcLength()
    {
        return arcLength;
    }
    /**
     * @return Returns the startAngle.
     */
    public double getAngle()
    {
        return startAngle;
    }
    /**
     * @param arcLength
     *            The arcLength to set.
     */
    public void setArcLength(double arcLength)
    {
        this.arcLength = arcLength;
    }
    /**
     * @param startAngle
     *            The startAngle to set.
     */
    public void setAngle(double startAngle)
    {
        this.startAngle = startAngle;
    }
	
    public Date getPublishedTime()
	{
		return publishedTime;
	}
    
	public Date getCreatedTime() 
	{
		return createdTime;
	}
	
    
    public void roleAdded(String playerName) {}
    public void roleRemoved(String playerName) {}
    /**
     * @return Returns the documentName.
     */
    public String getDocumentName()
    {
        return documentName;
    }
}